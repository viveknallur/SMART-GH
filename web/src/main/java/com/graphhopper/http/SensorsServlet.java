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
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
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
        
        
        
        helloVersaillesResource_JerseyClient jerseyclient = new helloVersaillesResource_JerseyClient();
        String wsReturnedValue = jerseyclient.getText();
        //JSONObject json = new JSONObject(wsReturnedValue);
        
        System.out.println("LwsReturnedValue=" + wsReturnedValue);


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

    static class helloVersaillesResource_JerseyClient
    {
        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8080/GHRestfulWS/webresources";

        public helloVersaillesResource_JerseyClient()
        {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("helloVersailles");
        }

        public String getText() throws ClientErrorException
        {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.TEXT_PLAIN).get(String.class);
        }

        public void close()
        {
            client.close();
        }
    }

    
    

}
