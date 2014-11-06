/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Route;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.*;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.GHPoint;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import javax.inject.Inject;
import javax.ws.rs.QueryParam;

/**
 * REST Web Service
 *
 * @author Amal Elgammal
 */
@Path("route")
public class RouteResource {

    @Context
    private UriInfo context;

    @Inject
    private GraphHopper hopper;

    /**
     * Creates a new instance of RouteResource
     */
    public RouteResource() {
    }

    /**
     * Retrieves representation of an instance of Route.RouteResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("infoPoints") List<GHPoint> infoPoints,
            @QueryParam("minPathPrecision") double minPathPrecision,
            @QueryParam("writeGPX") boolean writeGPX,
            @QueryParam("enableInstructions") boolean enableInstructions,
            @QueryParam("calcPoints") boolean calcPoints,
            @QueryParam("elevation") boolean elevation,
            @QueryParam("vehicleStr") String vehicleStr,
            @QueryParam("weighting") String weighting,
            @QueryParam("algoStr") String algoStr,
            @QueryParam("localeStr") String localeStr
    ) throws JSONException {

        //TODO: In the servlet, it should send this parameters, otherwise send the defaults
        vehicleStr = vehicleStr.toUpperCase();
        hopper.setElevation(elevation);
        StopWatch sw = new StopWatch().start();

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

        float took = sw.stop().getSeconds();
        //PointList points = rsp.getPoints();
        //if (rsp.hasErrors())
        //    return rsp.getErrors().toString();

        //write JSON from rsp
        JSONObject rspJson = formRspInJson(rsp, took, enableInstructions);

        return rspJson.toString();
    }

    JSONObject formRspInJson(GHResponse rsp, float took, boolean enableInstructions) throws JSONException {

        //TODO: should be extracted from the sent request!
        boolean pointsEncoded = true;
        boolean calcPoints = true;
        boolean includeElevation = false;

        JSONObject json = new JSONObject();
        JSONObject jsonInfo = new JSONObject();
        json.put("info", jsonInfo);

        if (rsp.hasErrors()) {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            for (Throwable t : rsp.getErrors()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("message", t.getMessage());
                map.put("details", t.getClass().getName());
                list.add(map);
            }
            jsonInfo.put("errors", list);
        } else if (!rsp.isFound()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", "Not found");
            map.put("details", "");
            jsonInfo.put("errors", Collections.singletonList(map));
        } else {
            jsonInfo.put("took", Math.round(took * 1000));
            JSONObject jsonPath = new JSONObject();
            jsonPath.put("distance", Helper.round(rsp.getDistance(), 3));
            jsonPath.put("time", rsp.getMillis());

            if (calcPoints) {
                jsonPath.put("points_encoded", pointsEncoded);

                PointList points = rsp.getPoints();
                if (points.getSize() >= 2) {
                    jsonPath.put("bbox", rsp.calcRouteBBox(hopper.getGraph().getBounds()).toGeoJson());
                }

                jsonPath.put("points", createPoints(points, pointsEncoded, includeElevation));

                if (enableInstructions) {
                    InstructionList instructions = rsp.getInstructions();
                    jsonPath.put("instructions", instructions.createJson());
                }
            }
            json.put("paths", Collections.singletonList(jsonPath));
        }
        
        return json;

    }

    Object createPoints(PointList points, boolean pointsEncoded, boolean includeElevation) throws JSONException {
        
        
        if (pointsEncoded) {
            return WebHelper.encodePolyline(points, includeElevation);
        }

        JSONObject jsonPoints = new JSONObject();
        jsonPoints.put("type", "LineString");
        jsonPoints.put("coordinates", points.toGeoJson(includeElevation));
        return jsonPoints;
    }

}
