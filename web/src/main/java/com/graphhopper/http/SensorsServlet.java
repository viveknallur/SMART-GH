/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.http;

/**
 *
 * @author elgammaa
 */
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import org.ini4j.Ini;
import java.io.FileReader;
import javax.ws.rs.client.WebTarget;
import org.json.JSONObject;


public class SensorsServlet extends GHBaseServlet
{
    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException
    {
        System.out.println("Entered SensorsServlet");
        try
        {
            getSensors(req, res);
        } catch (IllegalArgumentException ex)
        {
            writeError(res, SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }

    }

    void getSensors( HttpServletRequest req, HttpServletResponse res ) throws IOException
    {
        String fileName = "./sensors-config-files/dublin.config";
        ArrayList sensorsTxt = new ArrayList();
        
        
        
        infoResource_JerseyClient jerseyclient = new infoResource_JerseyClient();
        String wsReturnedValue = jerseyclient.getJson("./maps/dublin-m50.osm");
        JSONObject json = new JSONObject(wsReturnedValue);
        
        System.out.println("Length of returned string=" + json.length());


        try
        {
            Ini ini = new Ini(new FileReader(fileName));
            System.out.println("Sensors available = " + ini.get("SensorsAvailable").toString());

            for (String key : ini.get("SensorsAvailable").keySet())
            {
                String sensorName = ini.get("SensorsAvailable").fetch(key);
                String text = ini.get(sensorName).fetch("text");
                System.out.println("Text of Sensor " + sensorName + " is " + text);
                sensorsTxt.add(text);
            }

            System.out.println("Size of reurned array = " + sensorsTxt.size());

        } catch (IOException e)
        {
            logger.error(e.getMessage());
        }

        res.setContentType("text/plain");
        res.setHeader("Cache-Control", "no-cache");
        res.getWriter().write(sensorsTxt.toString());

    }

    
    
    static class infoResource_JerseyClient
    {
        private javax.ws.rs.client.WebTarget webTarget;
        private javax.ws.rs.client.Client client;
        private static final String BASE_URI = "http://localhost:8080/RestfulWSApp/webresources";

        public infoResource_JerseyClient()
        {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("info");
        }

        public void putJson( Object requestEntity ) throws javax.ws.rs.ClientErrorException
        {
            webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON));
        }

        public String getJson( String osmPath ) throws javax.ws.rs.ClientErrorException
        {
            WebTarget resource = webTarget;
            if (osmPath != null)
            {
                resource = resource.queryParam("osmPath", osmPath);
            }
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public void close()
        {
            client.close();
        }
    }
}
