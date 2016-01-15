package com.graphhopper.daemonservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
 
/**
 * @author Vivek
 * 
 */
 
public class SmartHopperCache<K, T> {
    
    private long timeToLive;
    private Map<K, T> cacheMap;
    
    protected class SmartHopperCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public String value;
        
        protected SmartHopperCacheObject(String value) {
            this.value = value;
        }
    }
    
    public SmartHopperCache(long timeToLive, final long timeInterval, int max) {
        this.timeToLive = timeToLive * 20000000; //The time to live is equal to 333 minutes (when passed a 1 as parameter)
        
        cacheMap = new HashMap<K, T>();
        
        if (timeToLive > 0 && timeInterval > 0) {
            
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(timeInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        
                    }
                }
            });
            
            t.setDaemon(true);
            t.start();
        }
    }
    
    // PUT method
    public void put(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, value);
        }
    }
    
    // GET method
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (cacheMap) {
          /*  SmartHopperCacheObject c = (SmartHopperCacheObject) cacheMap.get(key);
            
            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return (T) c.value;
            }
*/
return cacheMap.get(key);
        }
    }
    
    // REMOVE method
    public void remove(String key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }
    
    // Get Cache Objects Size()
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
    
    // CLEANUP method
    public void cleanup() {
       synchronized(cacheMap){
           cacheMap.clear();
       } 
    }
}
