package com.group17.smart_gh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class requestRoute{
	
	private static String url;
	private static String results;
	
	public requestRoute(String route)
	{
		url = route;
		results = "";
	}
	
	public requestRoute(double fromLong, double fromLat, double toLong, double toLat, String weighting, String vehicle)
	{		
		url = "http://localhost:8989/route/";
		url += "?point=" + fromLong + "%2C" + fromLat; 
		url += "&point=" + toLong + "%2C" + toLat; 
		url += "&vehicle=" + vehicle;
		if(weighting.equals("Least Noise Pollution")) url += "&weighting=least_noise"; 
		else if(weighting.equals("Least Air Pollution")) url += "&weighting=least_air_pollution";
		else url += "&weighting=" + weighting;
		
		url += "&locale=en-US";		
	}
	
	public String sendRoute() throws Exception{
		new Thread(new Runnable(){

			public void run(){		
				try{
					URL obj = new URL(url);
					
					HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
			
					connection.setRequestMethod("GET");
					connection.setRequestProperty("User-Agent", "Mozilla/5.0");
					connection.setRequestProperty("connection", "close");
					
					BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
			
					String inputLine;
					StringBuffer response = new StringBuffer();
			 
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
			 
					results = response.toString();
				}
				catch (Exception E){
					
				}
			}
		}).start();
		
		return this.getResults();
			
	}
	
	public String getResults(){
		while(results.equals(""))
		{
			
		}
		return results;
	}
}
