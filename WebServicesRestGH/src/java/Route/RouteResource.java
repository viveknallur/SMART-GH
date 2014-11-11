/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Route;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.util.Downloader;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.shapes.GHPoint;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * REST Web Service
 *
 * @author amola
 */
@Path("route")
public class RouteResource implements GraphHopperAPI {

    @Context
    private UriInfo context;
    
    private String serviceUrl;
    private boolean pointsEncoded = true;
    private Downloader downloader = new Downloader("RouteResource");
    private boolean instructions = true;
    private final TranslationMap trMap = new TranslationMap().doImport();

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
    //TODO: Pass the points and all other attributes as query params
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("lat1") double lat1,
            @QueryParam("lon1") double lon1,
            @QueryParam("lat2") double lat2,
            @QueryParam("lon2") double lon2,
            @QueryParam("vehicleStr") String vehicleStr,
            @QueryParam("weighting") String weighting,
            @QueryParam("algoStr") String algoStr) {

        GraphHopperAPI gh = new RouteResource();

        //online service
        gh.load("http://localhost:8989/route");
        //offline
        //gh.load("C:/Users/amola/SMART-GH");

        /*if (lat1.equals(""))
         lat1 = 53.287931; 
         if (lon1.equals(""))
         lon1 = -6.366547;
         if (lat2.equals(""))
         lat2 = 53.289146";
         if (lon2.equals(""))
         lon1 = "-6.243376";*/
        //GHResponse ph = gh.route(new GHRequest(53.287931,-6.366547,53.289146,-6.243376));
        GHRequest req = new GHRequest(lat1, lon1, lat2, lon2);
        req.setVehicle(vehicleStr.toUpperCase());
        req.setWeighting(weighting);

        if (!(algoStr.equals(""))) 
            req.setAlgorithm(algoStr);
   
        GHResponse ph = gh.route(req);
        System.out.println(ph);

        return ph.toString();
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    
    //TODO: Will be removed when connecting to Vivek's daemon
    /**
     * Example url: http://localhost:8989 or http://217.92.216.224:8080
     */
    @Override
    public boolean load(String url) {
        this.serviceUrl = url;
        return true;
    }

    public RouteResource setPointsEncoded(boolean b) {
        pointsEncoded = b;
        return this;
    }

    public RouteResource setInstructions(boolean b) {
        instructions = b;
        return this;
    }

    @Override
    public GHResponse route(GHRequest request) {
        StopWatch sw = new StopWatch().start();
        double took = 0;
        try {
            String places = "";
            for (GHPoint p : request.getPoints()) {
                places += "point=" + p.lat + "," + p.lon + "&";
            }

            boolean withElevation = false;
            
            System.out.println("Value of request.getAlgorithm() = " + request.getAlgorithm());

            String url = serviceUrl
                    + "?"
                    + places
                    + "&type=json"
                    + "&points_encoded=" + pointsEncoded
                    + "&min_path_precision=" + request.getHint("douglas.minprecision", 1)
                    + "&algo=" + request.getAlgorithm()
                    + "&locale=" + request.getLocale().toString()
                    + "&elevation=" + withElevation
                    + "&vehicle=" + request.getVehicle()
                    + "&weighting=" + request.getWeighting();

            
            //TODO: check if we need to change the value of rsp.found when returned to the web app
            String str = downloader.downloadAsString(url);
            JSONObject json = new JSONObject(str);
            took = json.getJSONObject("info").getDouble("took");
            JSONArray paths = json.getJSONArray("paths");
            JSONObject firstPath = paths.getJSONObject(0);
            double distance = firstPath.getDouble("distance");
            int time = firstPath.getInt("time");
            PointList pointList;
            if (pointsEncoded) {
                pointList = WebHelper.decodePolyline(firstPath.getString("points"), 100, withElevation);
            } else {
                JSONArray coords = firstPath.getJSONObject("points").getJSONArray("coordinates");
                pointList = new PointList(coords.length(), withElevation);
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray arr = coords.getJSONArray(i);
                    double lon = arr.getDouble(0);
                    double lat = arr.getDouble(1);
                    if (withElevation) {
                        pointList.add(lat, lon, arr.getDouble(2));
                    } else {
                        pointList.add(lat, lon);
                    }
                }
            }
            GHResponse res = new GHResponse();
            if (instructions) {
                JSONArray instrArr = firstPath.getJSONArray("instructions");

                InstructionList il = new InstructionList(trMap.getWithFallBack(request.getLocale()));
                for (int instrIndex = 0; instrIndex < instrArr.length(); instrIndex++) {
                    JSONObject jsonObj = instrArr.getJSONObject(instrIndex);
                    double instDist = jsonObj.getDouble("distance");
                    String text = jsonObj.getString("text");
                    long instTime = jsonObj.getLong("time");
                    int sign = jsonObj.getInt("sign");
                    JSONArray iv = jsonObj.getJSONArray("interval");
                    int from = iv.getInt(0);
                    int to = iv.getInt(1);
                    PointList instPL = new PointList(to - from, withElevation);
                    for (int j = from; j <= to; j++) {
                        instPL.add(pointList, j);
                    }

                    // TODO way and payment type
                    Instruction instr = new Instruction(sign, text, InstructionAnnotation.EMPTY, instPL).
                            setDistance(instDist).setTime(instTime);
                    il.add(instr);
                }
                res.setInstructions(il);
            }
            return res.setPoints(pointList).setDistance(distance).setMillis(time);
        } catch (Exception ex) {
            throw new RuntimeException("Problem while fetching path " + request.getPoints(), ex);
        } finally {
            //logger.debug("Full request took:" + sw.stop().getSeconds() + ", API took:" + took);
        }
    }

}
