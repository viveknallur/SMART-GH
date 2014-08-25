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

/**
 * Calculates the least noisy route- independent of a vehicle as the calculation is based on 
 * the noise data linked to edges stores in Redis
 * @author Amal Elgammal
 */
public class LeastNoisyWeighting implements Weighting
{
    public LeastNoisyWeighting()
    {
        
    }
    
     @Override
    public double getMinWeight( double noiseValue )
    {
        //TODO: Check if this needs to be updated
        return noiseValue;
    }
    
     @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse )
    {
        //TODO : First test when you pass least_noise from the web app
        //if this invokes here--Code to capture noise value of edge from Redis and returns it!
        return edge.getDistance();
    }
    
    @Override
    public String toString()
    {
        //TODO: check if we need to define it with the encoder manger or elsewhere
        return "LEAST_NOISY";
    }
    
}
