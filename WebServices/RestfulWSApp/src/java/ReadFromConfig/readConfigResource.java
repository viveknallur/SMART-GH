/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReadFromConfig;

import com.graphhopper.util.CmdArgs;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;

/**
 * REST Web Service
 *
 * @author Amal Elgammal
 */
@Path("readConfig")
public class readConfigResource {

    private CmdArgs args;

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of readConfigResource
     */
    public readConfigResource() {
    }

    /**
     * Retrieves representation of an instance of
     * ReadFromConfig.readConfigResource
     *       

     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("systemProperty") String systemProperty){
        //TODO return proper representation object
        try{
            
            args = CmdArgs.readFromConfig("C:/Users/elgammaa/SMART-GH/config.properties", systemProperty);
            //args = CmdArgs.readFromConfig("/../../../../config.properties", systemProperty);
        }catch(IOException e)
        { 
            System.out.println(e.getMessage());
        }
        return  args.toString();
        //throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of readConfigResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }
}
