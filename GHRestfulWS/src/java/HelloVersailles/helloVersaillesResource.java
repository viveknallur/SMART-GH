/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HelloVersailles;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;

/**
 * REST Web Service
 *
 * @author amola
 */
@Path("helloVersailles")
public class helloVersaillesResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of helloVersaillesResource
     */
    public helloVersaillesResource() {
    }

    /**
     * Retrieves representation of an instance of HelloVersailles.helloVersaillesResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/plain")
    public String getText() {
        //TODO return proper representation object
        //throw new UnsupportedOperationException();
        
        return "Hello Versailles...I am happy to be here!";
    }


}
