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

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Servlet to use GraphHopper in a remote application (mobile or browser). Attention: If type is
 * json it returns the points in GeoJson format (longitude,latitude) unlike the format "lat,lon"
 * used otherwise.
 * <p/>
 * @author Peter Karich
 */
public class GraphHopperServlet extends GHBaseServlet
{


    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            writePath(req, res);
        } catch (IllegalArgumentException ex)
        {
            writeError(res, SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }

    void writePath( HttpServletRequest req, HttpServletResponse res ) throws Exception
    {

        //TODO: Amal, Replace such that points are sent to our WS as lat1, lon1, lat2, lon2
        List<StringPoint> infoPoints = getPoints(req);

        //Here we assume that only two points are sent, which is supported by the WS, however GH-core supports up to 5 points
        String lat1 = infoPoints.get(0).lat;
        String lon1 = infoPoints.get(0).lon;
        String lat2 = infoPoints.get(1).lat;
        String lon2 = infoPoints.get(1).lon;;

        // we can reduce the path length based on the maximum differences to the original coordinates
        double minPathPrecision = getDoubleParam(req, "min_path_precision", 1d);
        boolean writeGPX = "gpx".equalsIgnoreCase(getParam(req, "type", "json"));
        boolean enableInstructions = writeGPX || getBooleanParam(req, "instructions", true);
        boolean calcPoints = getBooleanParam(req, "calc_points", true);
        boolean elevation = getBooleanParam(req, "elevation", false);
        String vehicleStr = getParam(req, "vehicle", "CAR").toUpperCase();
        String weighting = getParam(req, "weighting", "fastest");
        String algoStr = getParam(req, "algorithm", "");
        String localeStr = getParam(req, "locale", "en");

        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/restful-daemon/route");

         MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
         queryParams.add("lat1", lat1);
         queryParams.add("lon1", lon1);
         queryParams.add("lat2", lat2);
         queryParams.add("lon2", lon2);
         queryParams.add("minPathPrecision", String.valueOf(minPathPrecision));
         queryParams.add("enableInstructions",String.valueOf(enableInstructions));
         queryParams.add("calcPoints",String.valueOf(calcPoints));
         queryParams.add("elevation",String.valueOf(elevation));
         queryParams.add("locale",localeStr);
         queryParams.add("vehicle",vehicleStr);
         queryParams.add("weighting",weighting);
         queryParams.add("algoStr",algoStr);
         
         String wsResponse = webResource.queryParams(queryParams).get(String.class);
         //System.out.println("ghResponse = " + wsResponse);
         JSONObject json = new JSONObject(wsResponse);
         //System.out.println("json = " + json);
         
         String infoStr = req.getRemoteAddr() + " " + req.getLocale() + " " + req.getHeader("User-Agent");
         //PointList points = rsp.getPoints();
         String logStr = req.getQueryString() + " " + infoStr + " " + infoPoints
         + ", distance: " /*+ json.getString("distance")*/ + ", time:" /*+ Math.round(Double.parseDouble(json.getString("time"))/ 60000f)
         + "min, points:" /*+ points.getSize() + ", took:" + json.getString("info")*/
         + ", debug - " /*+ rsp.getDebugInfo()*/ + ", " + algoStr + ", "
         + weighting + ", " + vehicleStr;
         
         logger.info(logStr);
         
        /*if (rsp.hasErrors())
         logger.error(logStr + ", errors:" + rsp.getErrors());
         else
         logger.info(logStr);*/
       

        //if (writeGPX)
        //    writeGPX(req, res, rsp);
        //else
            writeJson(req, res, json);
    }

   /* private void writeGPX( HttpServletRequest req, HttpServletResponse res, GHResponse rsp )
    {
        boolean includeElevation = getBooleanParam(req, "elevation", false);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/xml");
        String trackName = getParam(req, "track", "GraphHopper Track");
        res.setHeader("Content-Disposition", "attachment;filename=" + "GraphHopper.gpx");
        String timeZone = getParam(req, "timezone", "GMT");
        long time = getLongParam(req, "millis", System.currentTimeMillis());
        writeResponse(res, rsp.getInstructions().createGPX(trackName, time, timeZone, includeElevation));
    }*/


    private List<StringPoint> getPoints( HttpServletRequest req ) throws IOException
    {
        String[] pointsAsStr = getParams(req, "point");
        final List<StringPoint> infoPoints = new ArrayList<StringPoint>(pointsAsStr.length);

        //final List<GHPoint> infoPoints = new ArrayList<GHPoint>(pointsAsStr.length);
        for (int pointNo = 0; pointNo < pointsAsStr.length; pointNo++)
        {
            final String str = pointsAsStr[pointNo];
            String[] fromStrs = str.split(",");
            if (fromStrs.length == 2)
            {

                StringPoint place = new StringPoint(fromStrs[0], fromStrs[1]);
                //GHPoint place = GHPoint.parse(str);
                if (place != null)
                    infoPoints.add(place);
            }
        }

        return infoPoints;
    }

    //@Amal Elgammal: created to simulate the data structure of GHPoint
    private class StringPoint
    {
        String lat;
        String lon;

        StringPoint( String lat, String lon )
        {
            this.lat = lat;
            this.lon = lon;

        }

    }

}
