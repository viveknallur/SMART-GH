package com.graphhopper.daemonservice;

public class Configuration{
        public Configuration(){}
        public String getRealPath(){
               return getClass().getResource("/").getPath(); 
        }
        public String getOSMPath(){
                return getRealPath() + "maps/";
        }
}
