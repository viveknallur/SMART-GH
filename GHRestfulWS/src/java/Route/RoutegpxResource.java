/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Route;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.xml.crypto.dsig.XMLObject;
import org.json.JSONException;

/**
 * REST Web Service
 *
 * @author amola
 */
@Path("routegpx")
public class RoutegpxResource {

    @Context
    private UriInfo context;

    @Inject
    private GraphHopper hopper;
    /**
     * Creates a new instance of RoutegpxResource
     */
    public RoutegpxResource() {
    }

    /**
     * Retrieves representation of an instance of Route.RoutegpxResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml(@QueryParam("infoPoints") List<GHPoint> infoPoints,
            @QueryParam("minPathPrecision") double minPathPrecision,
            @QueryParam("writeGPX") boolean writeGPX,
            @QueryParam("enableInstructions") boolean enableInstructions,
            @QueryParam("calcPoints") boolean calcPoints,
            @QueryParam("elevation") boolean elevation,
            @QueryParam("vehicleStr") String vehicleStr,
            @QueryParam("weighting") String weighting,
            @QueryParam("algoStr") String algoStr,
            @QueryParam("localeStr") String localeStr) throws JSONException {
        
        //TODO: In the servlet, it should send this parameters, otherwise send the defaults
        vehicleStr = vehicleStr.toUpperCase();
        hopper.setElevation(elevation);

        GHResponse rsp;
        if (!hopper.getEncodingManager().supports(vehicleStr)) {
            rsp = new GHResponse().addError(new IllegalArgumentException("Vehicle not supported: " + vehicleStr));
        } else if (elevation && !hopper.hasElevation()) {
            rsp = new GHResponse().addError(new IllegalArgumentException("Elevation not supported!"));
        } else {
            FlagEncoder algoVehicle = hopper.getEncodingManager().getEncoder(vehicleStr);

            rsp = hopper.route(new GHRequest(infoPoints).
                    setVehicle(algoVehicle.toString()).
                    setWeighting(weighting).
                    setAlgorithm(algoStr).
                    setLocale(localeStr).
                    putHint("calcPoints", calcPoints).
                    putHint("instructions", enableInstructions).
                    putHint("douglas.minprecision", minPathPrecision));
        }
      
       String rspGPX = formRspInGPX(rsp); 
        //write GPX from rsp
         return rspGPX;
    }
    
    String formRspInGPX(GHResponse rsp) throws JSONException {
        
        //@Amal Elgammal: Change to be sent as a parameter---refer to the servlet
        //boolean includeElevation = false;
        //String trackName = "GraphHopper Track";
        return rsp.getInstructions().createGPX().toString();   
    
    }


}
