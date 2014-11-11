/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Info;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.json.JSONObject;
import org.ini4j.Ini;
import java.io.FileReader;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.Constants;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import javax.ws.rs.QueryParam;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author Amal Elgammal
 */
@Path("info")
public class InfoResource {

    @Context
    private UriInfo context;
    
    protected Logger logger = LoggerFactory.getLogger(getClass());

    //@Inject
    private GraphHopper hopper;

    JSONObject json = new JSONObject();

    /**
     * Creates a new instance of InfoResource
     */
    public InfoResource() {
    }

    /**
     * Retrieves representation of an instance of InfoWS.InfoResource
     * @return an instance of java.lang.String
     */
    
    //TODO: Check if we really need to pass any parameters!
    
    @GET
    @Produces("application/json")
    public String getJson() throws Exception {
       
        return getInfos().toString();
        
        //throw new UnsupportedOperationException();
    }

    JSONObject getInfos() throws JSONException, IOException{
        //TODO
        BBox bb = hopper.getGraph().getBounds();
        List<Double> list = new ArrayList<Double>(4);
        list.add(bb.minLon);
        list.add(bb.minLat);
        list.add(bb.maxLon);
        list.add(bb.maxLat);
        
         json.put("bbox", list);

        String[] vehicles = hopper.getGraph().getEncodingManager().toString().split(",");
        json.put("supported_vehicles", vehicles);
        
         JSONObject features = new JSONObject();
        for (String v : vehicles)
        {
            JSONObject perVehicleJson = new JSONObject();
            perVehicleJson.put("elevation", hopper.hasElevation());
            features.put(v, perVehicleJson);
        }
        json.put("features", features);

        json.put("version", Constants.VERSION);
        json.put("build_date", Constants.BUILD_DATE);

        StorableProperties props = hopper.getGraph().getProperties();
        json.put("import_date", props.get("osmreader.import.date"));
        
         if (!Helper.isEmpty(props.get("prepare.date")))
            json.put("prepare_date", props.get("prepare.date"));
         
        /*String osmFile = hopper.getOSMFile();
        
        ArrayList sensorsTxt = new ArrayList();
        
        sensorsTxt = getAvailableSensors(osmFile);
        //logger.info("These weighting are also available for "+ osmFile +":" + sensorsTxt.toString());
        json.put("osmFile", osmFile);
        json.put("city", getCity(osmFile));
        json.put("sensors",sensorsTxt);*/

        return json;
    }

    
   /* ArrayList getAvailableSensors(String osmFile) throws IOException
    {
        //we assume that names of the osm files should be in this format <city><optional '-'><any optional string><.*>
        String cityName = getCity(osmFile);
        //sensors configuration files are named as cityname.config
        
        //TODO check the root of this to pass the correct config file path
        String fileName = "./sensors-config-files/"+cityName + ".config";
        ArrayList sensorsTxt = new ArrayList();
        try
        {
            Ini ini = new Ini(new FileReader(fileName));
          
            for(String key: ini.get("SensorsAvailable").keySet())
            {
                String sensorName = ini.get("SensorsAvailable").fetch(key);
                String text = ini.get(sensorName).fetch("text");
                sensorsTxt.add(text);
            }
           
        } catch (IOException e)
        {
            logger.error(e.getMessage());
        }
           return sensorsTxt;
    }
    
    String getCity(String osmFile)
    {
        int num = osmFile.split("/").length;
        String cityName = osmFile.split("/")[num-1];
        
        cityName = cityName.split("\\.")[0];
        if (cityName.contains("-"))
             cityName = cityName.split("-")[0];
        
        return cityName;
    }*/
}
