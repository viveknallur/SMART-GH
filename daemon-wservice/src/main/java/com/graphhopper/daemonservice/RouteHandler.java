package com.graphhopper.daemonservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

import com.graphhopper.GraphHopper;

@Path("/route/{user}")
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
   @GET
   public String sayHello(@PathParam("user") String username){
      StringBuilder stringBuilder = new StringBuilder("Hello, " + username);
      stringBuilder.append("!").append("There are 10 thousand routes from Tallaght to Dundrum");
      stringBuilder.append(". And graphy says 'hi!' from: ");
      stringBuilder.append(RouteHandler.hopper.getOSMFile());

      return stringBuilder.toString();
   }
}
