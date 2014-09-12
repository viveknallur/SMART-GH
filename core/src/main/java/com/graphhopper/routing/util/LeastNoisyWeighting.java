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

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

/**
 * Calculates the least noisy route- independent of a vehicle as the calculation is based on the
 * noise data linked to edges stores in Redis
 * <p>
 * @author Amal Elgammal
 */
public class LeastNoisyWeighting implements Weighting
{
    String currentCity;
    String sensorReading = "noise";

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

         System.out.println("edge.getEdge() = " + edge.getEdge());
         System.out.println("edge.getFlags() = " + edge.getFlags());
         System.out.println("edge.getName() = " + edge.getName());

         return returnedRandom;*/

        double noiseValue = getNoiseFromRedis(edge);
        return noiseValue;
    }

    double getNoiseFromRedis( EdgeIteratorState edge ) throws JedisConnectionException, JedisDataException
    {
        double noiseValue = 30;
        String ntime;
        
        //Matching keys based on patterns is very very slow
        try
        {

            Jedis jedis = new Jedis("localhost");
            String edgeName = edge.getName();
            //Set<String> matchedEdges = new HashSet();

            if (edgeName.length() > 0)
            {

                if (edgeName.contains(","))
                    edgeName = edgeName.replace(", ", "_");

                //edgeName = "dublin_noise_" + edgeName;
                edgeName =  this.currentCity+"_"+this.sensorReading+"_"+ edgeName;
                System.out.println("edgeName = " + edgeName);
                if (jedis.exists(edgeName))
                {
                    noiseValue = Double.parseDouble(jedis.hget(edgeName, "noise"));
                    System.out.println("noiseValue = " + noiseValue);
                    ntime = jedis.hget(edgeName, "timestamp");
                }
            }

            /*if (edgeName.length() >0)
             {
             matchedEdges = jedis.keys("dublin_noise_"+edgeName+"*");
             }*/
            //could loop on all returned edges, and get the average of returned noise readings, however, it would make
            //the process slower
            //if(matchedEdges.size()>0)
            //{
            //Iterator iter = matchedEdges.iterator();
            //String firstKey = (String)iter.next();
            //}
            //check what to do with time and how to amend the instruction list with noise readings and timestamp 
        } catch (JedisConnectionException e)
        {
            System.out.println("JedisConnectionException: " + e.getMessage());
        } catch (JedisDataException e)
        {
            System.out.println("JedisDataException: " + e.getMessage());

        } catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());

        }

        return noiseValue;
    }

    
    @Override
    public void setCurrentCity( String city )
    {
        this.currentCity = city;
    }

    //TODO: Start here: should be set in the route method (graphhopper) when selecting and init the appropriate weighting
    @Override
    public String toString()
    {
        //TODO: check if we need to register it with the encodering manger or elsewhere
        return "LEAST_NOISY";
    }

}
