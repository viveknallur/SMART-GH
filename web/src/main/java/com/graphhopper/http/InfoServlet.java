/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import org.json.JSONObject;

/**
 * @author Peter Karich
 */
public class InfoServlet extends GHBaseServlet {
    /**
	 * InfoServlet.java:long. Represents 
	 */
	private static final long serialVersionUID = -2159520190756305397L;

	@Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            writeInfos(req, res);
        } catch (IllegalArgumentException ex)
        {
            writeError(res, SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }

    void writeInfos( HttpServletRequest req, HttpServletResponse res ) throws Exception
    {

        Client client = Client.create();
        String webSvcHost = System.getenv("WS_CONFIG");
        String svcName = "/restful-graphhopper-1.0/info";
        System.out.println(webSvcHost + svcName);
        WebResource webResource = client.resource(webSvcHost + svcName);
        String infoWSResponse = webResource.get(String.class);

        ClientResponse response = webResource.get(ClientResponse.class);
        int status = response.getStatus();
        String entity = response.getEntity(String.class);
        logger.info("Invoking info WS, HTTP Status: " + status);
        
        //TODO: Decide with Vivek what needs to be logged
        //logger.info(entity);
        
        JSONObject json = new JSONObject(infoWSResponse);

        writeJson(req, res, json);

    }

}
