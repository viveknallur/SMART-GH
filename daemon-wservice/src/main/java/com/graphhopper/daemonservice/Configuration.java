package com.graphhopper.daemonservice;

public class Configuration{
        public Configuration(){}
        public String getRealPath(){
               //return getClass().getResource("/").getPath();
            return "/tmp";
        }
        public String getOSMPath(){
                return getRealPath() + "/maps/";
        }
}
