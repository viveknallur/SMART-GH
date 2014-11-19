package com.graphhopper.daemonservice;

import javax.ws.rs.core.UriInfo;

/* std-lib imports */
import java.io.*;
import java.util.*;
import javax.ws.rs.core.Context;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/* third-party imports*/
import org.ini4j.*;

/* our imports */
import com.graphhopper.GraphHopper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("")
public class RouteHandler
{
    private static GraphHopper hopper;
    private static String osmFilePath;
    private static CmdArgs args;
    private static TranslationMap map;
    private static String configFile;

    static
    {

        osmFilePath = new Configuration().getOSMPath() + "dublin-m50.osm";
        hopper = new GraphHopper();
        hopper.setOSMFile(osmFilePath);
        hopper.setInMemory(true);
        hopper.setDoPrepare(false);
        hopper.forServer();
        map = hopper.getTranslationMap();
        try
        {

            configFile = new Configuration().getRealPath() + "/config.properties";
            args = CmdArgs.readFromConfig(configFile, "graphhopper.config");
            System.out.println("args= " + args);
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        hopper.init(args);
        hopper.importOrLoad();

    }

    @Context
    private UriInfo context;


    @GET
    @Path("/sayHello")
    @Produces("text/plain")
    public String sayHello( @QueryParam("name") String name )
    {
        StringBuilder stringBuilder = new StringBuilder("Hello, " + name);
        stringBuilder.append("!").append("There are 10 thousand routes from Tallaght to Dundrum");
        stringBuilder.append(". And graphy says 'hi!' from: ");
        stringBuilder.append(RouteHandler.hopper.getOSMFile());

        return stringBuilder.toString();
    }

    @GET
    @Path("/hello")
    @Produces("text/plain")
    public String HelloHello()
    {
        StringBuilder stringBuilder = new StringBuilder("Hello, ");
        stringBuilder.append("!").append("There are 10 thousand routes from Tallaght to Dundrum");
        stringBuilder.append(". And graphy says 'hi!' from: ");
        stringBuilder.append(RouteHandler.hopper.getOSMFile());

        return stringBuilder.toString();
    }

    /**
     * **************************START OF
     * ROUTE********************************************************
     */
    @GET
    @Path("/route")
    @Produces("application/json")
    public String returnRoute( @QueryParam("lat1") double lat1,
            @QueryParam("lon1") double lon1,
            @QueryParam("lat2") double lat2,
            @QueryParam("lon2") double lon2,
            @QueryParam("vehicle") String vehicleStr,
            @QueryParam("weighting") String weighting,
            @QueryParam("algoStr") String algoStr,
            @QueryParam("elevation") boolean elevationValue,
            @QueryParam("locale") String localeStr,
            @QueryParam("minPathPrecision") double minPathPrecision,
            @QueryParam("enableInstructions") boolean enableInstructions,
            @QueryParam("calcPoints") boolean calcPoints ) throws JSONException, IOException
    {

        GHPoint source = new GHPoint(lat1, lon1);
        GHPoint destination = new GHPoint(lat2, lon2);
        
        //setting default values
        if (!calcPoints)
            calcPoints = true;
        if (!enableInstructions)
            enableInstructions = true;
         if (localeStr.equals(""))
            localeStr = "en";

        if (!(vehicleStr.equals("")))
        {
            vehicleStr = vehicleStr.toUpperCase();
        } else
        {
            vehicleStr = "CAR";
        }
        if (weighting.equals(""))
            weighting = "fastest";
        
        
        //Set the defaults of non-relevant parameters
        minPathPrecision = 1d;
        boolean writeGPX = false;
        boolean elevation = elevationValue;
        boolean pointsEncoded = true;

        List<GHPoint> infoPoints = new ArrayList<GHPoint>();
        infoPoints.add(source);
        infoPoints.add(destination);
        hopper.setElevation(elevation);

        StopWatch sw = new StopWatch().start();

        //GHResponse response = hopper.route(request);
        GHResponse rsp;

        FlagEncoder algoVehicle = hopper.getEncodingManager().getEncoder(vehicleStr);

        if (!hopper.getEncodingManager().supports(vehicleStr))
        {
            rsp = new GHResponse().addError(new IllegalArgumentException("Vehicle not supported: " + vehicleStr));
        } else if (elevation && !hopper.hasElevation())
        {
            rsp = new GHResponse().addError(new IllegalArgumentException("Elevation not supported!"));
        } else
        {

            rsp = hopper.route(new GHRequest(infoPoints).
                    setVehicle(algoVehicle.toString()).
                    setWeighting(weighting).
                    setAlgorithm(algoStr).
                    setLocale(localeStr).
                    putHint("calcPoints", calcPoints).
                    putHint("instructions", enableInstructions).
                    putHint("douglas.minprecision", minPathPrecision));
        }

        float took = sw.stop().getSeconds();
        JSONObject json = writeJson(rsp, elevation, calcPoints, pointsEncoded, enableInstructions, took);
        return json.toString();

    }

    private JSONObject writeJson( GHResponse rsp, boolean elevation, boolean calcPoints, boolean pointsEncoded, boolean enableInstructions, float took ) throws JSONException, IOException
    {
        JSONObject json = new JSONObject();
        JSONObject jsonInfo = new JSONObject();
        json.put("info", jsonInfo);
        json.put("distance", rsp.getDistance());
        json.put("time", rsp.getMillis());
        json.put("debugInfo", rsp.getDebugInfo());
        
        
        if (rsp.hasErrors())
        {
            json.put("hasErrors", "true");   
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            for (Throwable t : rsp.getErrors())
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("message", t.getMessage());
                map.put("details", t.getClass().getName());
                list.add(map);
            }
            jsonInfo.put("errors", list);
        } else if (!rsp.isFound())
        {
            json.put("hasErrors", "true");  
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", "Not found");
            map.put("details", "");
            jsonInfo.put("errors", Collections.singletonList(map));
        } else
        {
            json.put("hasErrors", "false");   
            jsonInfo.put("took", Math.round(took * 1000));
            JSONObject jsonPath = new JSONObject();
            jsonPath.put("distance", Helper.round(rsp.getDistance(), 3));
            jsonPath.put("time", rsp.getMillis());

            if (calcPoints)
            {
                jsonPath.put("points_encoded", pointsEncoded);

                PointList points = rsp.getPoints();
                if (points.getSize() >= 2)
                    jsonPath.put("bbox", rsp.calcRouteBBox(hopper.getGraph().getBounds()).toGeoJson());

                jsonPath.put("points", createPoints(points, pointsEncoded, elevation));

                if (enableInstructions)
                {
                    InstructionList instructions = rsp.getInstructions();
                    jsonPath.put("instructions", instructions.createJson());
                }
            }
            json.put("paths", Collections.singletonList(jsonPath));
        }

        return json;

    }

    Object createPoints( PointList points, boolean pointsEncoded, boolean includeElevation ) throws JSONException
    {
        if (pointsEncoded)
            return WebHelper.encodePolyline(points, includeElevation);

        JSONObject jsonPoints = new JSONObject();
        jsonPoints.put("type", "LineString");
        jsonPoints.put("coordinates", points.toGeoJson(includeElevation));
        return jsonPoints;
    }

    /**
     * **************************END OF
     * ROUTE******************************************************************
     */
    /**
     * **************************START OF
     * INFO********************************************************
     */
    @GET
    @Path("/info")
    @Produces("application/json")
    public String returnInfo() throws JSONException, IOException
    {
        JSONObject json = new JSONObject();
        BBox bb = hopper.getGraph().getBounds();
        List<Double> list = new ArrayList<Double>(4);
        list.add(bb.minLon);
        list.add(bb.minLat);
        list.add(bb.maxLon);
        list.add(bb.maxLat);

        json.put("bbox", list);

        String[] vehicles = hopper.getGraph().getEncodingManager().toString().split(",");
        json.put("supported_vehicles", vehicles);

        JSONObject features = new JSONObject();
        for (String v : vehicles)
        {
            JSONObject perVehicleJson = new JSONObject();
            perVehicleJson.put("elevation", hopper.hasElevation());
            features.put(v, perVehicleJson);
        }
        json.put("features", features);

        json.put("version", Constants.VERSION);
        json.put("build_date", Constants.BUILD_DATE);

        StorableProperties props = hopper.getGraph().getProperties();
        json.put("import_date", props.get("osmreader.import.date"));

        if (!Helper.isEmpty(props.get("prepare.date")))
        {
            json.put("prepare_date", props.get("prepare.date"));
        }

        String osmFile = hopper.getOSMFile();
        ArrayList sensorsTxt = new ArrayList();

        sensorsTxt = getAvailableSensors(osmFile);
        json.put("osmFile", osmFile);
        json.put("city", getCity(osmFile));
        json.put("sensors", sensorsTxt);

        return json.toString();
    }

    ArrayList getAvailableSensors( String osmFile ) throws IOException
    {
        //we assume that names of the osm files should be in this format <city><optional '-'><any optional string><.*>
        String cityName = getCity(osmFile);
        String ext = ".config";

        //sensors configuration files are named as cityname.config
        String config_file = cityName + ext;
        String realPath = getClass().getResource("/").getPath();

        //String fileName = "./sensors-config-files/" + cityName + ".config";
        ArrayList sensorsTxt = new ArrayList();
        try
        {
            Ini ini = new Ini(new FileReader(realPath + "/" + config_file));
            Set<String> allSensors = ini.get("SensorsAvailable").keySet();
            for (String key : allSensors)
            {
                String sensorName = ini.get("SensorsAvailable").fetch(key);
                String text = ini.get(sensorName).fetch("text");
                sensorsTxt.add(text);
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return sensorsTxt;
    }

    String getCity( String osmFile )
    {
        int num = osmFile.split("/").length;
        String cityName = osmFile.split("/")[num - 1];

        cityName = cityName.split("\\.")[0];
        if (cityName.contains("-"))
            cityName = cityName.split("-")[0];

        return cityName;
    }

    /**
     * **************************END OF
     * INFO******************************************************************
     */
    /**
     * *************************START OF
     * I18N*****************************************************************
     */
    @GET
    @Path("/i18n")
    @Produces("application/json")
    public String returnI18N( @QueryParam("path") String path,
            @QueryParam("acceptLang") String acceptLang ) throws JSONException
    {
        String locale = "";

        if (!Helper.isEmpty(path) && path.startsWith("/"))
            locale = path.substring(1);

        if (Helper.isEmpty(locale))
        {
            // fall back to language specified in header e.g. via browser settings
            if (!Helper.isEmpty(acceptLang))
                locale = acceptLang.split(",")[0];
        }

        Translation tr = map.get(locale);
        JSONObject json = new JSONObject();
        if (tr != null && !Locale.US.equals(tr.getLocale()))
            json.put("default", tr.asMap());

        json.put("locale", locale.toString());
        json.put("en", map.get("en").asMap());

        return json.toString();

    }

    /* **************************END OF I18N******************************************************************/
    /**
     * *************************START OF
     * GHConfig*****************************************************************
     */
    @GET
    @Path("/GHConfig")
    @Produces("application/json")
    public String returnGHConfig() throws JSONException
    {
        return args.toString();

    }

    /* **************************END OF GHConfig******************************************************************/
    @GET
    @Path("/config")
    @Produces("text/plain")
    public String returnConfig()
    {
        String city = "dublin";
        String ext = ".config";
        String config_file = city + ext;
        String realPath = getClass().getResource("/").getPath();

        StringBuilder stringBuilder = new StringBuilder("Hello! ");
        stringBuilder.append("Sensor text options to be added: ");
        try
        {
            FileReader configFile = new FileReader(realPath + "/" + config_file);
            stringBuilder.append("and file ready? : " + configFile.ready());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {

            Ini iniReader = new Ini(new FileReader(realPath + "/" + config_file));
            Set<String> allSensors = iniReader.get("SensorsAvailable").keySet();
            for (String key : allSensors)
            {
                String sensorName = iniReader.get("SensorsAvailable").fetch(key);
                String sensorText = iniReader.get(sensorName).fetch("text");
                stringBuilder.append(sensorText);
            }
        } catch (Exception fe)
        {
            fe.printStackTrace();
        }

        return stringBuilder.toString();
    }

}
