package com.graphhopper.daemonservice;

/* std-lib imports */
import java.io.*;
import java.util.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;


/* third-party imports*/
import org.ini4j.*;


/* our imports */
import com.graphhopper.GraphHopper;

@Path("")
@Produces("text/plain")
public class RouteHandler {
   
   private static GraphHopper hopper;
   private static String osmFilePath = "../maps/dublin-m50.osm";

   static{
      hopper = new GraphHopper();
      hopper.setInMemory(true);
      hopper.setDoPrepare(false);
      hopper.forServer();
      hopper.setOSMFile(osmFilePath);
   }
   @GET @Path("/info/{user}")
   public String sayHello(@PathParam("user") String username){
      StringBuilder stringBuilder = new StringBuilder("Hello, " + username);
      stringBuilder.append("!").append("There are 10 thousand routes from Tallaght to Dundrum");
      stringBuilder.append(". And graphy says 'hi!' from: ");
      stringBuilder.append(RouteHandler.hopper.getOSMFile());

      return stringBuilder.toString();
   }
   @GET @Path("/route/{user}")
   public String returnRoute(@PathParam("user") String username){
      StringBuilder stringBuilder = new StringBuilder("Hello, " + username);
      stringBuilder.append("!").append("We try and try to reach our destination");
      stringBuilder.append(", but the road never ends ");

      return stringBuilder.toString();
   }
   @GET @Path("/config")
   public String returnRoute(){
      String city = "dublin";
      String ext = ".config";
      String config_file = city + ext;
      String realPath = getClass().getResource("/").getPath();

      StringBuilder stringBuilder = new StringBuilder("Hello! ");
      stringBuilder.append("Sensor text options to be added: ");
      
      try{
	      Ini iniReader = new Ini(new FileReader(realPath + "/" + config_file));
	      Set<String> allSensors = iniReader.get("SensorsAvailable").keySet();
	      for (String key: allSensors){
	              String sensorName = iniReader.get("SensorsAvailable").fetch(key);
	              String sensorText = iniReader.get(sensorName).fetch("text");
	              stringBuilder.append(sensorText);
	      }
      }catch(Exception fe){
              fe.printStackTrace();
      }


      return stringBuilder.toString();
   }



}
