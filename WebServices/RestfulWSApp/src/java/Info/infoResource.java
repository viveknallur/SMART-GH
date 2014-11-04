/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Info;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;

import org.json.JSONObject;

/**
 * REST Web Service
 *
 * @author Amal Elgammal
 */
@Path("info")
public class infoResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of infoResource
     */
    public infoResource() {
    }

    /**
     * Retrieves representation of an instance of Info.infoResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("osmPath") String osmPath) throws  Exception{
        //TODO return proper representation object
        JSONObject json = new JSONObject();
     
        String [] vehicles = {"car", "bike", "foot" };
        json.put("vehicles", vehicles);
        json.put("city", "Dublin");
        String[] sensors = {"Least_Noisy", "Least_Air_Pollution"};
        json.put("sensors", sensors);
        
        String osm = osmPath;
        json.put("osmFile", osm);
        System.out.println(json.toString());
         return json.toString();
        //throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of infoResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }
}
