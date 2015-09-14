/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.http;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

//import javax.naming.directory.BasicAttributes;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ini4j.Ini;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

//import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

/**
 * @author Amal Elgammal
 */
public class SensorDataServlet extends GHBaseServlet {
    /**
	 * SensorDataServlet.java:long. Represents 
	 */
	private static final long serialVersionUID = -5487299279396130802L;
	
	//File management constants
/*	private static final String FILE_PATH = "./sensor_processing/";
	private static final String EXTENSION = "_heatmap.dat";
	private static final String GEOCODE_URL = "http://maps.googleapis.com/maps/api/geocode/json";
	private static final long FILE_LIVENESS_TIME = 900000; //15min
*/	
	@Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
        try {
            writeSensorData(req, res);
        } catch (IllegalArgumentException ex) {
            writeError(res, SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }

    void writeSensorData( HttpServletRequest req, HttpServletResponse res ) throws Exception {
        //String jsonTxt = {"noise": [53.345561050008236, -6.266153144973648, 0.36875],[53.34485697622874, -6.276308793517197, -0.0140625],[53.343238988281236, -6.250881488397291, 0.3234375]}";

        JSONObject json = new JSONObject();
        //--------- Getting all sensors data, should connect directly to redis?   
        try {
        	Ini ini = new Ini(new FileReader("./sensors-config-files/dublin.config"));
        	String redisURL = ini.get("ConnectionSettings").fetch("REDIS_URL");
        	int redisPort = Integer.parseInt(ini.get("ConnectionSettings").fetch("REDIS_PORT"));
        	String city = ini.get("ConnectionSettings").fetch("CITY_PREFIX");
        	
        	Jedis jedis = new Jedis(redisURL, redisPort);
        	
        	Set<String> allSensors = ini.get("SensorsAvailable").keySet();
        	GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyA_KjJQteitfgzMpNqgb_Ew6wYeMMqfLH0");
        	
        	for (String key : allSensors) {
            	String sensorName = ini.get("SensorsAvailable").fetch(key);
            	String type = ini.get(sensorName).fetch("type");      	
            	
            	Set<String> keys = jedis.keys(city+"_"+type+"*");
            	StringBuffer values = new StringBuffer("[");
            	for(String k : keys) {
            		values.append("[");
            		GeocodingResult[] results = GeocodingApi.geocode(context, k.split("_")[2] +", "+ city).await();
            		values.append(Double.toString(results[0].geometry.location.lat) + ", ");
            		values.append(Double.toString(results[0].geometry.location.lng)  + ", ");
            		values.append(jedis.get(k));
            		values.append("],");
        		}
            	values.delete(values.length()-1, values.length());
            	if(values.length() > 0)
            		values.append("]");
            	json.put(type, values.toString());
            }
        	//TODO remove pre-calculated data
/*            Set<String> allSensors = ini.get("SensorsAvailable").keySet();
            for (String key : allSensors) {
                String sensorName = ini.get("SensorsAvailable").fetch(key);
                String type = ini.get(sensorName).fetch("type");
				String dirname = ini.get(sensorName).fetch("dirname");
                String dataFile = FILE_PATH + dirname + type + EXTENSION;
                String sensorContent = new Scanner(new File(dataFile)).useDelimiter("\\Z").next();
                json.put(type, sensorContent);
            }
*/      } catch (IOException e) {
            e.printStackTrace();
        }
        writeJson(req, res, json);
    }
}
