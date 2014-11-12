/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Info;

import java.io.IOException;
import java.util.ArrayList;
//import com.graphhopper.util.ReadCityConfig;


/**
 *
 * @author elgammaa
 */
public class Tests {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here

        /*PrintWriter writer = new PrintWriter("../sensors-config-files/andYou.txt", "UTF-8");
         writer.println("The first line");
         writer.println("The second line");
         writer.close();*/
        ReadCityConfig R = new ReadCityConfig();
        ArrayList result = R.getAvailableSensors("dublin-m50.osm");
        System.out.println("result = " + result);

        /*File f = new File("../sensors-config-files/" + cityName + ".config");
         if (f.exists() && !f.isDirectory()) 
         { System.out.println("Yaaaahhhh...." + cityName + ".config could be reached!!!"); }*/
    }

    public Tests() {
    }

}
