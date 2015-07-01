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

import com.graphhopper.util.EdgeIteratorState;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//import java.io.PrintWriter;

/**
 * Calculates the least air polluted route- independent of a vehicle as the calculation is based on
 * the air quality data linked is made up
 * <p>
 * @author Nicolas Cardozo
 */
public class PollutionSensorWeighting implements Weighting {
    
	/**
	 * MAX and MIN values for the range of acceptable pollution measurements
	 */
	private final static int MAX_VALUE = 50;
	private final static int MIN_VALUE = 0;
	
	String currentCity;
    String sensorReading = "air";
    private double DEFAULT_SMOKE_VALUE;

    public PollutionSensorWeighting() {

    }

    /**
     * Stub Constructor to come up with the pollution data
     * @param city
     */
    public PollutionSensorWeighting( String city ) {
        DEFAULT_SMOKE_VALUE = getAvgSensorValue();
        System.out.println(" DEFAULT_SMOKE_VALUE = " + DEFAULT_SMOKE_VALUE);
    }

    /**
     * Stub method to return the average data from the sensor
     * @return double - average pollution data
     */
    double getAvgSensorValue() {
        return Math.random()*MAX_VALUE + MIN_VALUE;
    }

    /**
     * Stub method to generate the min value randomly based on parameter value
     * @param smokeValue
     * @return
     */
    @Override
    public double getMinWeight( double smokeValue ) {
        return Math.random()*smokeValue;
    } 

    /**
     * Stub method to generate a random value for street weight in the range (MIN, MAX]
     * @see com.graphhopper.routing.util.Weighting#calcWeight(com.graphhopper.util.EdgeIteratorState, boolean)
     */
    @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse ) {
        return Math.random()*MAX_VALUE + MIN_VALUE;
    }

    @Override
    public String toString() {
        return "LEAST_SMOKEY";
    }
}
