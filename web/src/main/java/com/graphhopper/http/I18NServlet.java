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
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Karich
 */
public class I18NServlet extends GHBaseServlet {
	/**
	 * I18NServlet.java:long. Represents 
	 */
	private static final long serialVersionUID = 4963915170576350144L;
	private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            String path = req.getPathInfo();
            String acceptLang = req.getHeader("Accept-Language");

            Client client = Client.create();
          

            String webSvcHost = System.getenv("WS_CONFIG");
            String svcName = "/restful-graphhopper-1.0/i18n";
            System.out.println(webSvcHost + svcName);
            WebResource webResource = client.resource(webSvcHost + svcName);
            //WebResource webResource = client.resource("http://localhost:8080/restful-graphhopper-1.0/i18n");
           
            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            queryParams.add("path", path);
            queryParams.add("acceptLang", acceptLang);
            String wsResponse = webResource.queryParams(queryParams).get(String.class);
            
            ClientResponse response = webResource.get(ClientResponse.class);
            int status = response.getStatus();
            logger.info("Invoking i18n WS, HTTP Status: " + status);
            
               JSONObject json = new JSONObject(wsResponse);
            //String myResource = wsResponse.getEntity(String.class);
            //logger.info("Sending a request to i18n WS, HTTP status: " + wsResponse.);

            writeJson(req, res, json);
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }
}
