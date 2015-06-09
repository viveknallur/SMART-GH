package com.group17.smart_gh;
import org.json.*;

/*
 * When JSON String is passed in as it is sent from the GET request,
 * the JSONParser has a shitfit on the points:"..." object due to random
 * characters in the encoded string. Need to fix this
 * 
 * 
 */
 
public class routeParser {
    private JSONObject routeString;
    private JSONArray instructions;
    private int distance;
    private int eta;
    private String points;
    private String[][] route;
   
   
    public routeParser(String route) throws Exception{
            routeString = new JSONObject(route);      
            distance = routeString.getInt("distance");
            eta = routeString.getInt("time");
            JSONObject temp = routeString.getJSONArray("paths").getJSONObject(0);
            instructions = temp.getJSONArray("instructions");
           
            //Points - "points":"giqdI`wce@LBVRvAjAjA~@|BtBpAlApGmW\\wA`DfDlBfBFQrEkIlB{CrCiDT?HM?Cb@m@Ta@pAsCp@sBh@oB|\\`Lz@XK\\"
            //characters within the points string are being read as illegal escape characters            
            points = temp.getString("points");
    }
    public void parseRoute() throws Exception{
                int t, d;
               
            route = new String[instructions.length()][3];
            for (int i = 0; i < instructions.length(); i++)
            {
                route[i][0] = instructions.getJSONObject(i).getString("text");
                t = instructions.getJSONObject(i).getInt("time");
                route[i][1] = "" + t;
                d = instructions.getJSONObject(i).getInt("distance");
                route[i][2] = "" + d;
            }
    }
    public int getDistance(){
            return distance;
    }
    public String getPoints(){
            return points;
    }
    public int getETA(){
            return eta;
    }
    public String[][] getRoute(){
            return route;
    }
}

