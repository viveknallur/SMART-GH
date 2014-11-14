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
    private static CmdArgs args;

    static
    {

        hopper = new GraphHopper();
        hopper.setOSMFile(osmFilePath);
        hopper.setInMemory(true);
        hopper.setDoPrepare(false);
        hopper.forServer();
        try
        {
            args = CmdArgs.readFromConfig("../config.properties", "graphhopper.config");
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
    @Path("/info")
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
    
    
    
     /****************************START OF ROUTE*********************************************************/

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
	    @QueryParam("calcPoints") boolean calcPoints) throws JSONException, IOException
    {
       
        //setting default values
        calcPoints = true;
        enableInstructions = true;
        boolean pointsEncoded = true;

        //Set the defaults of non-relevant parameters
        minPathPrecision = 1d;
        boolean writeGPX = false;
        
        boolean elevation = elevationValue;

        GHPoint source = new GHPoint(lat1, lon1);
        GHPoint destination = new GHPoint(lat2, lon2);
        
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

        List<GHPoint> infoPoints = new ArrayList<GHPoint>();
        infoPoints.add(source);
        infoPoints.add(destination);
        //GHRequest request = new GHRequest(infoPoints);
        //request.setVehicle(vehicleStr);
        //request.setWeighting(weighting);
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
    /****************************END OF ROUTE*******************************************************************/

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
