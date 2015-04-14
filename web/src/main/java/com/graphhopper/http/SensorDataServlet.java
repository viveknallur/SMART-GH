/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.http;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import org.json.JSONObject;

/**
 *
 * @author Amal Elgammal
 */
public class SensorDataServlet extends GHBaseServlet
{

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            writeSensorData(req, res);
        } catch (IllegalArgumentException ex)
        {
            writeError(res, SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }

    void writeSensorData( HttpServletRequest req, HttpServletResponse res ) throws Exception
    {
        //String jsonTxt = {"noise": [53.345561050008236, -6.266153144973648, 0.36875],[53.34485697622874, -6.276308793517197, -0.0140625],[53.343238988281236, -6.250881488397291, 0.3234375],[53.34479073622439, -6.252396596516416, 0.71875],[53.34417327074869, -6.264826040752744, 0.575],[53.34597551, -6.26792379, 0.125],[53.342325056669004, -6.255005766717831, 0.11396484375],[53.345372448674674, -6.267221951911133, 0.7265625], "air": [53.345561050008236, -6.266153144973648, 0.36875],[53.34485697622874, -6.276308793517197, -0.0140625],[53.343238988281236, -6.250881488397291, 0.3234375],[53.34479073622439, -6.252396596516416, 0.71875],[53.34417327074869, -6.264826040752744, 0.575],[53.34597551, -6.26792379, 0.125],[53.342325056669004, -6.255005766717831, 0.11396484375],[53.345372448674674, -6.267221951911133, 0.7265625]}";

        JSONObject json = new JSONObject();
       
        String noiseDataFile = "./sensor_processing/sensor_readings/noise/noise_heatmap.dat";
		String noiseFakeDataFile = "./sensor_processing/sensor_readings/noise/noise_heatmap2.dat";
        String airDataFile = "./sensor_processing/sensor_readings/noise/air_heatmap.dat";
        
        String noiseContent = new Scanner(new File(noiseDataFile)).useDelimiter("\\Z").next();
		String moreNoiseContent = new Scanner(new File(noiseFakeDataFile)).useDelimiter("\\Z").next();
/*		noiseContent.substring(0, noiseContent.length()-1);
		noiseContent += ", ";
		noiseContent +=  moreNoiseContent.substring(1, moreNoiseContent.length()); 
//		noiseContent += new Scanner(new File(noiseFakeDataFile)).useDelimiter("\\Z").next();
      */
  		String airContent = new Scanner(new File(airDataFile)).useDelimiter("\\Z").next();
        json.put("noise",noiseContent);
        json.put("air",airContent);
		json.put("backgroundNoise", moreNoiseContent);
        writeJson(req, res, json);

    }

}
