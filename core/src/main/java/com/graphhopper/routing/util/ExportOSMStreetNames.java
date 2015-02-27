/*
 * Copyright 2015 elgammaa.
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author elgammaa
 */
public class ExportOSMStreetNames
{
    public static void main( String[] strs ) throws Exception
    {
        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXParser parser = parserFactor.newSAXParser();
        SAXHandler1 handler = new SAXHandler1();
        parser.parse("../maps/output17.xml", handler);
        System.out.println("Number of ways = " + handler.waysIDs.size());

        //ArrayList<String> waysNames = new ArrayList<String>();
        HashSet waysNames = new HashSet();

        for (int i = 0; i < handler.waysIDs.size(); i++)
        {
            String wayName = handler.waysIDs.get(i);
            System.out.println("wayName = " + wayName);
            waysNames.add(wayName);

        }

        System.out.println("Number of unique street names = " + waysNames.size());

        BufferedWriter out = new BufferedWriter(new FileWriter("../maps/dublin-unique-streetnames.txt"));
        Iterator it = waysNames.iterator();
        while (it.hasNext())
        {
            out.write(it.next() + "\n");
        }
        out.close();

    }

}

class SAXHandler1 extends DefaultHandler
{
    ArrayList<String> waysIDs = new ArrayList<String>();
    String wayId = "";
    String wayName = "";
    String wayRef = "";
    int refFlag = 0;

    public SAXHandler1()
    {
        super();
    }

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {

        if (qName.equalsIgnoreCase("way"))
        {
            wayId = attributes.getValue("id");
        } else if (qName.equalsIgnoreCase("tag"))
        {
            if (attributes.getValue("k").equalsIgnoreCase("name"))
            {
                wayName = attributes.getValue("v");

            } else if (attributes.getValue("k").equalsIgnoreCase("ref"))
            {
                wayRef = attributes.getValue("v");
                refFlag = 1;
            }
        }

    }

    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        //exclude wayid and could be added (TODO) it as a set inside the hash; e.g. key: dublin_noise_Parnell_R111, fields:(noise, timestamp, wayIDs:Set)

        if (qName.equalsIgnoreCase("way"))
        {
            if (wayName.length() > 0)
            {
                String firstToken = wayName.split(" ")[0];

                if (firstToken.matches(".*\\d.*"))
                {
                    if (wayName.length() > firstToken.length())
                        wayName = wayName.substring(firstToken.length() + 1);
                }

                // if (refFlag == 1)
                //    waysIDs.add(wayName + "_" + wayRef);
                //else
                waysIDs.add(wayName);
            }
            refFlag = 0;

        }

    }

}
