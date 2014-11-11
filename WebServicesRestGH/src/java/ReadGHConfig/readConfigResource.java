/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReadGHConfig;

import com.graphhopper.util.CmdArgs;
import java.io.IOException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * REST Web Service
 *
 * @author elgammaa
 */
@Path("readConfig")
public class readConfigResource {

    @Context
    private UriInfo context;
    
     private CmdArgs args;

    /**
     * Creates a new instance of readConfigResource
     */
    public readConfigResource() {
    }

    /**
     * Retrieves representation of an instance of ReadGHConfig.readConfigResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("systemProperty") String systemProperty) {
       //TODO return proper representation object
        
        if(systemProperty.equals(""))
            systemProperty = "graphhopper.config";
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

}
