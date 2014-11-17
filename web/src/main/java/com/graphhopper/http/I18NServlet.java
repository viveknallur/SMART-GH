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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONObject;

/**
 * @author Peter Karich
 */
public class I18NServlet extends GHBaseServlet
{

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            String path = req.getPathInfo();
            String acceptLang = req.getHeader("Accept-Language");

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/restful-daemon/i18n");

            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            queryParams.add("path", path);
            queryParams.add("acceptLang", acceptLang);

            String wsResponse = webResource.queryParams(queryParams).get(String.class);
            JSONObject json = new JSONObject(wsResponse);

            writeJson(req, res, json);
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }
}
