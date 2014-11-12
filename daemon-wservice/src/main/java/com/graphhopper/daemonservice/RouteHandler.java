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
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;
import org.json.JSONException;
import org.json.JSONObject;

@Path("")
public class RouteHandler
{
    private static GraphHopper hopper;
    private static String osmFilePath = "../maps/dublin-m50.osm";
    

    

    static
    {
        //hopper = new GraphHopper().init(args);
      
        hopper = new GraphHopper();
        hopper.setInMemory(true);
        hopper.setDoPrepare(false);
        hopper.forServer();
        hopper.setOSMFile(osmFilePath);
        //hopper.setGraph("../maps/dublin-m50.osm");
        
        //TODO replace with graphhopper folder

    }

    @Context
    private UriInfo context;

    //for route: setting the default values
    boolean calcPoints = true;
    boolean enableInstructions = true;
    boolean pointsEncoded = true;

    //

    @GET
    @Path("/info")
    @Produces("text/plain")
    public String sayHello( @QueryParam("name1") String name1,
            @QueryParam("name2") String name2 )
    {

        StringBuilder stringBuilder = new StringBuilder("Hello, " + name1 + " and " + name2);
        stringBuilder.append("!").append("There are 10 thousand routes from Tallaght to Dundrum");
        stringBuilder.append(". And graphy says 'hi!' from: ");
        stringBuilder.append(RouteHandler.hopper.getOSMFile());

        return stringBuilder.toString();
    }

    @GET
    @Path("/route")
    @Produces("application/json")
    public String returnRoute( @QueryParam("lat1") double lat1,
            @QueryParam("lon1") double lon1,
            @QueryParam("lat2") double lat2,
            @QueryParam("lon2") double lon2,
            @QueryParam("vehicleStr") String vehicleStr,
            @QueryParam("weighting") String weighting,
            @QueryParam("algoStr") String algoStr,
            @QueryParam("elevation") boolean elevationValue ) throws JSONException, IOException
    {

        String[] strs ={osmFilePath};
        CmdArgs args = CmdArgs.read(strs);
        hopper.init(args);
        hopper = hopper.importOrLoad();

        GHPoint source = new GHPoint(lat1, lon1);
        GHPoint destination = new GHPoint(lat2, lon2);

        List<GHPoint> infoPoints = new ArrayList<GHPoint>();
        infoPoints.add(source);
        infoPoints.add(destination);

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
        double minPathPrecision = 1d;
        boolean writeGPX = false;
        String localeStr = "en";
        boolean elevation = elevationValue;
        hopper.setElevation(elevation);

        StopWatch sw = new StopWatch().start();
        GHResponse rsp;

        FlagEncoder algoVehicle = hopper.getEncodingManager().getEncoder(vehicleStr);

        rsp = hopper.route(new GHRequest(infoPoints).
                setVehicle(algoVehicle.toString()).
                setWeighting(weighting).
                setAlgorithm(algoStr).
                setLocale(localeStr).
                putHint("calcPoints", calcPoints).
                putHint("instructions", enableInstructions).
                putHint("douglas.minprecision", minPathPrecision));
       // }

        float took = sw.stop().getSeconds();
        System.out.println(rsp);
        //PointList points = rsp.getPoints();

        //return rsp.toString();
        JSONObject json = writeJson(rsp, elevation, took);
        return json.toString();

        //return lat1 + "and" + lon1;
    }

    private JSONObject writeJson( GHResponse rsp, boolean elevation, float took ) throws JSONException, IOException
    {
        JSONObject json = new JSONObject();
        JSONObject jsonInfo = new JSONObject();
        json.put("info", jsonInfo);

        if (rsp.hasErrors())
        {
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
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", "Not found");
            map.put("details", "");
            jsonInfo.put("errors", Collections.singletonList(map));
        } else
        {
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
