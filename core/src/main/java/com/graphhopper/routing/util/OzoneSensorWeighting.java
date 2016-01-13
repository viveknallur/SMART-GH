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
	private final static int MAX_VALUE = 2; 
	private final static int MIN_VALUE = 1;
	
	String currentCity;
    String sensorReading = "ozone";
	private double DEFAULT_OZONE_VALUE;
	
	/**
	 * Constructor
	 */
	public OzoneSensorWeighting() {
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
		return MIN_VALUE; //Math.random()*MIN_VALUE;
	}
	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#getMinWeight(double)
	 */
	@Override
	public double getMinWeight(double ozoneValue) {
		return MIN_VALUE; //Math.random()*MIN_VALUE*ozoneValue;
	}

	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#calcWeight(com.graphhopper.util.EdgeIteratorState, boolean)
	 */
	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse) {
		return MIN_VALUE; //Math.random()*MIN_VALUE;
	}

}
