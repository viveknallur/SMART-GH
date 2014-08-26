/*
 * Copyright 2014 Amal Elgammal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;
import java.util.Random;

import java.io.IOException;                                                    
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;                                                      
import java.util.Map;                                                          
import java.util.List;                                                          
                                                                               
import redis.clients.jedis.BinaryJedis;                                        
import redis.clients.jedis.Jedis;                                              
import redis.clients.jedis.Protocol;                                           
import redis.clients.jedis.exceptions.JedisConnectionException;                
import redis.clients.jedis.exceptions.JedisDataException;                      
import redis.clients.util.SafeEncoder;  

/**
 * Calculates the least noisy route- independent of a vehicle as the calculation is based on 
 * the noise data linked to edges stores in Redis
 * @author Amal Elgammal
 */
public class LeastNoisyWeighting implements Weighting
{
    String currentCity;
    
    public LeastNoisyWeighting()
    {
        System.out.println("LeastNoiseWeighting instantiated!");
        
    }
    
     @Override
    public double getMinWeight( double noiseValue )
    {
        //TODO: Check if this needs to be updated
        return noiseValue;
    }
    
     @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse )
    {
        //Experimenting with returning a random radom between 0-80...Worked!
        /*Random nw = new Random();
        double returnedRandom = nw.nextInt(100);
        System.out.println("Random noise value for edge = " +edge+ " = "+ returnedRandom );
        return returnedRandom;*/
        
        double noiseValue = getNoiseFromRedis(edge);
        return noiseValue;
    
    }
    
      double getNoiseFromRedis(EdgeIteratorState edge)
    {
        double noiseValue=0;
        String city = getCurrentCity();
        //TODO: connect to redis
        //based on the value of city, the appropriate database is selected, and query with the edge to return the noise value
        //check if we can increment the time of the noise reading to instructions in the response??
        
        
        return noiseValue;
    }
      
      //TODO: Start here: should be set in the route method (graphhopper) when selecting and init the appropriate weighting
      void setCurrentCity(String city)
      {
          this.currentCity = city;
      }
  
      String getCurrentCity()
      {
          return currentCity;
          
      }
    
    
    @Override
    public String toString()
    {
        //TODO: check if we need to register it with the encodering manger or elsewhere
        return "LEAST_NOISY";
    }
    
  
    
}
