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

import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

/**
 * Calculates the least noisy route- independent of a vehicle as the calculation is based on the
 * noise data randomly from a range of data
 * <p>
 * @author Nicolás Cardozo
 */
public class NoiseSensorWeighting implements Weighting {
	/**
	 * MAX and MIN values for the range of acceptable pollution measurements
	 */
	private final static int MAX_VALUE = 80; 
	private final static int MIN_VALUE = 2;
	
	String currentCity;
    String sensorReading = "noise";
    private double DEFAULT_NOISE_VALUE;
    
    public NoiseSensorWeighting() {
    }
    
    /**
     * Stub constructor
     * @param city
     */
    public NoiseSensorWeighting(String city) {
    	DEFAULT_NOISE_VALUE = getSensorValue(); 
        System.out.println(" DEFAULT_NOISE_VALUE = " + DEFAULT_NOISE_VALUE);
    }
    
    
    /**
     * Stub method to return the noise measurment in a city. This returns a random value
     * in the range (MIN_VAL, MAX_VAL] 
     * @return
     */
    double getSensorValue() {
    	return Math.random()*MAX_VALUE + MIN_VALUE;
    }
    
    @Override
    /**
     * Stub method to get the minimum weight of a street
     */
    public double getMinWeight( double noiseValue )  {
        //TODO: Check if this needs to be updated with other routing algorithms
        return Math.random()*noiseValue;
    }

    @Override
    /**
     * Stub method to make up the weight of each street 
     * @param edge
     * @return double
     */
    public double calcWeight( EdgeIteratorState edge, boolean reverse ) {
    	return Math.random()*MAX_VALUE;
    }

    @Override
    public String toString() {
        //TODO: check if we need to register it with the encodering manger or elsewhere
        return "LEAST_NOISY";
    }

}
