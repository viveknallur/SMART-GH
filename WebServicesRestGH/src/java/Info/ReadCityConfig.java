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
package Info;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.ini4j.Ini;

/**
 *
 * @author elgammaa
 */
public class ReadCityConfig
{

    public static void main( String[] args ) throws IOException
    {
        // TODO code application logic here
        ReadCityConfig R = new ReadCityConfig();
        String result = R.getAvailableSensors("dublin-m50.osm").toString();
        System.out.println("result = " + result);

    }

    public ReadCityConfig()
    {
    }


    
     public ArrayList getAvailableSensors(String osmFile) throws IOException {
     //we assume that names of the osm files should be in this format <city><optional '-'><any optional string><.*>
     String cityName = getCity(osmFile);

     //sensors configuration files are named as cityname.config
     //Web services folder should be placed on the same level as sensors-config-files
     String fileName = "../sensors-config-files/" + cityName + ".config";
        
     //String fileName = "C:/Users/elgammaa/SMART-GH/sensors-config-files/dublin.config";
     ArrayList sensorsTxt = new ArrayList();

     Ini ini = new Ini(new FileReader(fileName));
     Set<String> m = ini.get("SensorsAvailable").keySet();
     System.out.println("m = "+ m);
     for (String key : m) {
            
     String sensorName = ini.get("SensorsAvailable").fetch(key);
     String text = ini.get(sensorName).fetch("text");
     sensorsTxt.add(text);
     }

     return sensorsTxt;
     }
     
      String getCity( String osmFile )
    {
        int num = osmFile.split("/").length;
        String cityName = osmFile.split("/")[num - 1];

        cityName = cityName.split("\\.")[0];
        if (cityName.contains("-"))
        {
            cityName = cityName.split("-")[0];
        }
        return cityName;
    }
}
