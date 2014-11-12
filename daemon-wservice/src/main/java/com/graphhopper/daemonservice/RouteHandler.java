package com.graphhopper.daemonservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

import com.graphhopper.GraphHopper;

@Path("/graphhopper")
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

}
