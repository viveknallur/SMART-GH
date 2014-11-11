/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package I18N;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;

/**
 * REST Web Service
 *
 * @author elgammaa
 */
@Path("env")
public class EnvResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of EnvResource
     */
    public EnvResource() {
    }

    /**
     * Retrieves representation of an instance of I18N.EnvResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/plain")
    public String getText() {
        //TODO return proper representation object
        //throw new UnsupportedOperationException();
        
          String IP = System.getenv("JAVA_HOME");
       return IP;
    }

    /**
     * PUT method for updating or creating an instance of EnvResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("text/plain")
    public void putText(String content) {
    }
}
