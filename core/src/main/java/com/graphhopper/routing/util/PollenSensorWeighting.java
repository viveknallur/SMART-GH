/**
 *
 * graphhopper-core - PollenSensorWeighting.java
 * @date 1 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;

/**
 * Stub class to gather pollen data from the system
 * @author Nicolas Cardozo
 */
public class PollenSensorWeighting implements Weighting {

	/**
	 * MAX and MIN values for the range of acceptable pollution measurements
	 */
	private final static int MAX_VALUE = 80; 
	private final static int MIN_VALUE = 2;
	
	private double DEFAULT_POLLEN_VALUE;
	
	/**
	 * Constructor
	 */
	public PollenSensorWeighting() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Stub Constructor to get the pollen data
	 * @param city
	 */
	public PollenSensorWeighting(String city) {
		DEFAULT_POLLEN_VALUE = getSensorValue(); 
        System.out.println(" DEFAULT_POLLEN_VALUE = " + DEFAULT_POLLEN_VALUE);
	}
	
	/**
	 * Stub method to return a random value for the pollen sensor
	 * @return double - pollen value in the range (MIN, MAX]
	 */
	public double getSensorValue() {
		return Math.random()*MAX_VALUE + MIN_VALUE;
	}
	
	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#getMinWeight(double)
	 */
	@Override
	public double getMinWeight(double pollenValue) {
		// TODO Auto-generated method stub
		return Math.random()*pollenValue;
	}

	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#calcWeight(com.graphhopper.util.EdgeIteratorState, boolean)
	 */
	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse) {
		return Math.random()*MAX_VALUE + MIN_VALUE;
	}

}
