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
import com.graphhopper.util.shapes.GHPoint;

@Path("")
public class RouteHandler
{

    private static GraphHopper hopper;
    private static String osmFilePath = "../maps/dublin-m50.osm";

    static
    {
        hopper = new GraphHopper();
        hopper.setInMemory(true);
        hopper.setDoPrepare(false);
        hopper.forServer();
        hopper.setOSMFile(osmFilePath);
        //TODO replace with graphhopper folder
        hopper.load("C:/Users/elgammaa/SMART-GH");
    }

    @Context
    private UriInfo context;

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
            @QueryParam("elevation") boolean elevationValue )
    {

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
        boolean calcPoints = true;
        boolean enableInstructions = true;
        boolean elevation = elevationValue;
        String localeStr = "en";
        hopper.setElevation(elevation);

        GHResponse rsp;

        if (!hopper.getEncodingManager().supports(vehicleStr))
        {
            rsp = new GHResponse().addError(new IllegalArgumentException("Vehicle not supported: " + vehicleStr));
        } else if (elevation && !hopper.hasElevation())
        {
            rsp = new GHResponse().addError(new IllegalArgumentException("Elevation not supported!"));
        } else
        {
            FlagEncoder algoVehicle = hopper.getEncodingManager().getEncoder(vehicleStr);
            //Amal
            System.out.println("algoVehicle=" + algoVehicle);
            rsp = hopper.route(new GHRequest(lat1, lon1, lat2, lon2).
                    setVehicle(algoVehicle.toString()).
                    setWeighting(weighting).
                    setAlgorithm(algoStr).
                    setLocale(localeStr).
                    putHint("calcPoints", calcPoints).
                    putHint("instructions", enableInstructions).
                    putHint("douglas.minprecision", minPathPrecision));
        }

     
        System.out.println(rsp);

        return rsp.toString();

        //return lat1 + "and" + lon1;
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
