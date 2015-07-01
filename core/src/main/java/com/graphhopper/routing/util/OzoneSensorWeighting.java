/**
 *
 * graphhopper-core - OzoneSensorWeighting.java
 * @date 1 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;

/**
 * Stub class to generate the data from ozone sensors
 * @author Nicolas Cardozo
 */
public class OzoneSensorWeighting implements Weighting {

	/**
	 * MAX and MIN values for the range of acceptable pollution measurements
	 */
	private final static int MAX_VALUE = 1; 
	private final static int MIN_VALUE = 0;
	
	private double DEFAULT_OZONE_VALUE;
	
	/**
	 * Constructor
	 */
	public OzoneSensorWeighting() {
		// TODO Auto-generated constructor stub
	}
	
	public OzoneSensorWeighting(String city) {
		DEFAULT_OZONE_VALUE = getAvgSensorValue();
		System.out.println("DEFAULT_OZONE_VALUE "+ DEFAULT_OZONE_VALUE);
	}
	
	/**
	 * Stub method to get the average value for the ozone sensor
	 * @return double - random value in the range (MIN, MAX]
	 */
	private double getAvgSensorValue() {
		return Math.random()*MAX_VALUE + MIN_VALUE;
	}
	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#getMinWeight(double)
	 */
	@Override
	public double getMinWeight(double ozoneValue) {
		return Math.random()*ozoneValue;
	}

	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#calcWeight(com.graphhopper.util.EdgeIteratorState, boolean)
	 */
	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse) {
		return Math.random()*MAX_VALUE + MIN_VALUE;
	}

}
