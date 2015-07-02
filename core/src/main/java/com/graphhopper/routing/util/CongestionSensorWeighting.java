/**
 *
 * graphhopper-core - CongestionSensorWeighting.java
 * @date 2 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;

/**
 * Stub class to gather sensor information for congestion
 * @author Nicolas Cardozo
 */
public class CongestionSensorWeighting implements Weighting {

	/**
	 * MAX and MIN values for the range of acceptable pollution measurements
	 */
	private final static int MAX_VALUE = 80; 
	private final static int MIN_VALUE = 2;
	
	private String currentCity;
    private String sensorReading = "traffic";
    private double DEFAULT_CONGESTION_VALUE;
    
    /**
     * Constructor
     */
    public CongestionSensorWeighting() {
    }
    
    /**
     * Stub constructor to return the average weighting from the congestion information 
     * @param cityName
     */
    public CongestionSensorWeighting(String cityName) {
    	currentCity = cityName;
    	DEFAULT_CONGESTION_VALUE = getAvgSensorValue();
    	System.out.println("DEFAULT_CONGESTION_VALUE " + DEFAULT_CONGESTION_VALUE);
    }
    
    /**
     * Stub method to get the info from the sensors
     * @return
     */
    private double getAvgSensorValue() {
    	return Math.random()*MAX_VALUE + MIN_VALUE;
    }
    
	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#getMinWeight(double)
	 */
	@Override
	public double getMinWeight(double congestionValue) {
		return Math.random()*congestionValue;
	}

	/* (non-Javadoc)
	 * @see com.graphhopper.routing.util.Weighting#calcWeight(com.graphhopper.util.EdgeIteratorState, boolean)
	 */
	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse) {
		return Math.random()*MAX_VALUE + MIN_VALUE;
	}

}
