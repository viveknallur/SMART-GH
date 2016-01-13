/**
 *
 * graphhopper-core - SceanicSensorWeighting.java
 * @date 2 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;

/**
 * Stub class to gather sensor information about "how pretty" is a street 
 */
public class ScenicSensorWeighting implements Weighting {

	/**
	 * MAX and MIN values for the range of acceptable pollution measurements
	 */
	private final static int MAX_VALUE = 11;
	private final static int MIN_VALUE = 1;
	
	private double DEAFAULT_SCENIC_VALUE;
	private String currentCity;
	
	/**
	 * Constructor
	 */
	public ScenicSensorWeighting() {
		
	}
	
	/**
	 * Stub Constructor to generate the scenic weighting of routes 
	 * @param cityName
	 */
	public ScenicSensorWeighting(String cityName) {
		currentCity = cityName;
		DEAFAULT_SCENIC_VALUE = getAvgSensorValue(); 
        System.out.println(" DEFAULT_NOISE_VALUE = " + DEAFAULT_SCENIC_VALUE);
	}
	
	
	private double getAvgSensorValue() {
		return MIN_VALUE; //Math.floor(Math.random()*MIN_VALUE);
	}
	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#getMinWeight(double)
	 */
	@Override
	public double getMinWeight(double scenicValue) {
		return MIN_VALUE; //Math.floor(Math.random()*MIN_VALUE*scenicValue);
	}

	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#calcWeight(com.graphhopper.util.EdgeIteratorState, boolean)
	 */
	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse) {
		return MIN_VALUE; //Math.floor(Math.random()*MIN_VALUE);
	}
}
