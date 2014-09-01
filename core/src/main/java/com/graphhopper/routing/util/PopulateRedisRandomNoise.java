/*
 * Copyright 2014 elgammaa.
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

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 *
 * @author elgammaa
 */
public class PopulateRedisRandomNoise
{

    public static void main( String[] strs ) throws Exception, JedisConnectionException, JedisDataException
    {

        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXParser parser = parserFactor.newSAXParser();
        SAXHandler handler = new SAXHandler();
        parser.parse("output.xml", handler);
        System.out.println("Number of ways = " + handler.waysIDs.size());
        System.out.println("1st way = " + handler.waysIDs.get(0));
        try
        {
            Jedis jedis = new Jedis("localhost");
            jedis.flushAll();

            for (int i = 0; i < handler.waysIDs.size(); i++)
            {
                String hashkey = handler.waysIDs.get(i);
                System.out.println("hashkey = "+hashkey);
                hashkey = "dublin-noise-" + hashkey;

                Random noiseValue = new Random();
                double returnedNoise = noiseValue.nextInt(80);
                String noise = String.valueOf(returnedNoise);
                Random noiseTime = new Random();
                double returnedTime = noiseTime.nextInt(23);
                String ntime = String.valueOf(returnedTime) + "time";
                jedis.hset(hashkey, "Noisetube_value", noise);
                jedis.hset(hashkey, "Noisetube_timestamp", ntime);
            }

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

    }
}

class SAXHandler extends DefaultHandler
{
    ArrayList<String> waysIDs = new ArrayList<String>();
    String wayId, wayName, wayRef;

    public SAXHandler()
    {
        super();
    }

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {

        if (qName.equalsIgnoreCase("way"))
        {
            wayId = attributes.getValue("id");
            System.out.println("WayID = " + wayId);
        }
        else if (qName.equalsIgnoreCase("tag"))
        {
            if (attributes.getValue("k").equalsIgnoreCase("name"))
            {
                wayName = attributes.getValue("v");
                System.out.println("wayName = " + wayName);
            }
            else if(attributes.getValue("k").equalsIgnoreCase("ref"))
            {
                wayRef = attributes.getValue("v");
                System.out.println("wayRef = " + wayRef);
            }
        }
       
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException 
    {
        if (qName.equalsIgnoreCase("way"))
        {
           if(wayId!=null && wayName!=null && wayRef!=null)
           {
               waysIDs.add(wayName+"-"+wayRef+"-"+wayId);
           }
            
        }
        
    }

}


