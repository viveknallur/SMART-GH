package com.graphhopper.daemonservice;

public class CacheTest {
 
    public static void main(String[] args) throws InterruptedException {
 
        CacheTest Cache = new CacheTest();
 
        System.out.println("\n\n==========Test1: TestAddRemoveObjects ==========");
        Cache.TestAddRemoveObjects();
        System.out.println("\n\n==========Test2: TestExpiredCacheObjects ==========");
        Cache.TestExpiredCacheObjects();
        System.out.println("\n\n==========Test3: TestObjectsCleanupTime ==========");
        Cache.TestObjectsCleanupTime();
    }
 	
	

    private void TestAddRemoveObjects() {
 
        // Test with timeToLiveInSeconds = 200 seconds
        // timerIntervalInSeconds = 500 seconds
        // maxItems = 6
        SmartHopperCache<String, String> cache = new SmartHopperCache<String, String>(200, 500, 6);
 
        cache.put("eBay", "eBay");
        cache.put("Paypal", "Paypal");
        cache.put("Google", "Google");
        cache.put("Microsoft", "Microsoft");
        cache.put("IBM", "IBM");
        cache.put("Facebook", "Facebook");
 
        System.out.println("6 Cache Object Added.. cache.size(): " + cache.size());
        cache.remove("IBM");
        System.out.println("One object removed.. cache.size(): " + cache.size());
 
        cache.put("Twitter", "Twitter");
        cache.put("SAP", "SAP");
        System.out.println("Two objects Added but reached maxItems.. cache.size(): " + cache.size());
 
    }
 
    private void TestExpiredCacheObjects() throws InterruptedException {
 
        // Test with timeToLiveInSeconds = 1 second
        // timerIntervalInSeconds = 1 second
        // maxItems = 10
        SmartHopperCache<String, String> cache = new SmartHopperCache<String, String>(1, 1, 10);
 
        cache.put("eBay", "eBay");
		System.out.println(cache.get("eBay"));
        cache.put("Paypal", "Paypal");
		cache.put("a", "{u'info': {u'took': 23}, u'distance': 9553.52851944449, u'hasErrors': u'false', u'paths': [{u'distance': 9553.529, u'points_encoded': True, u'points': u'kwidIzxje@w@I]Ig@YUUW[e@cAYgAO_AGu@AaADeCBi@WGiBEi@Ag@AoGfAqAZsB\\oALmAGmE}@s@a@aBm@kIeCqGkAc@@e@LeAd@YBa@Gg@QmBcAiA}@aBaBeHmG{EwC}Bw@_AKeAGsBPwCjA{AXmBFcASuHgCiJ{AwAEu@m@_BeA{LsJs@_@ICWG}AUqBQgEQuADoBFu@?yFcBu@o@i@o@qAiAuAgA]SyH_Di@Ks@EyBPcBH_Ca@oAk@q@GgARgA@kBEi@?iBCK~@i@lBYp@{@hB]x@_@j@MF?Z?d@In@a@bA]b@Sf@_@n@i@v@[D}@QaBs@a@BMNgArJ[`D_A~ISbDYdGStCEnAA`A]d^YnEQnBMbAIKqAo@C?]x@_@hCGVi@`EEv@G`EIpCAb@IpDArBKJOVSr@kIv[{B|IKZFV?TCLONK?IG}@bDy@jC}Yt_AcYp}@FP@TAXl@h@VFtEeA~GeCvArB@VyC~J', u'bbox': [-6.327808, 53.303102, -6.272254, 53.360029], u'time': 787382, u'instructions': [{u'distance': 284.128, u'interval': [0, 12], u'text': u'Continue onto Dodder View Road, R112', u'time': 22729, u'sign': 0}, {u'distance': 72.291, u'interval': [12, 14], u'text': u'Turn left onto Rathfarnham Road, R114', u'time': 5783, u'sign': -2}, {u'distance': 23.563, u'interval': [14, 15], u'text': u'Continue onto Pearse Bridge, R114', u'time': 1885, u'sign': 0}, {u'distance': 522.854, u'interval': [15, 23], u'text': u'Continue onto Rathfarnham Road, R114', u'time': 41824, u'sign': 0}, {u'distance': 497.827, u'interval': [23, 30], u'text': u'Continue onto Terenure Road North, R137', u'time': 39820, u'sign': 0}, {u'distance': 1822.763, u'interval': [30, 53], u'text': u\"Turn slight right onto Harold's Cross Road, R137\", u'time': 145803, u'sign': 1}, {u'distance': 13.907, u'interval': [53, 54], u'text': u'Continue onto Robert Emmet Bridge, R137', u'time': 1112, u'sign': 0}, {u'distance': 275.655, u'interval': [54, 58], u'text': u'Continue onto Clanbrassil Street Upper, R137', u'time': 22049, u'sign': 0}, {u'distance': 402.559, u'interval': [58, 65], u'text': u'Continue onto Clanbrassil Street Lower, R137', u'time': 32202, u'sign': 0}, {u'distance': 376.64, u'interval': [65, 71], u'text': u'Continue onto New Street South, R137', u'time': 30129, u'sign': 0}, {u'distance': 288.048, u'interval': [71, 77], u'text': u'Turn slight right onto Patrick Street, R137', u'time': 23040, u'sign': 1}, {u'distance': 81.656, u'interval': [77, 79], u'text': u'Continue onto Nicholas Street, R137', u'time': 6531, u'sign': 0}, {u'distance': 192.651, u'interval': [79, 86], u'text': u'Turn left onto Back Lane', u'time': 16234, u'sign': -2}, {u'distance': 22.126, u'interval': [86, 88], u'text': u'Turn left onto High Street, R108', u'time': 1770, u'sign': -2}, {u'distance': 16.882, u'interval': [88, 89], u'text': u'Turn slight right onto Cornmarket', u'time': 1350, u'sign': 1}, {u'distance': 121.378, u'interval': [89, 94], u'text': u'Turn slight right onto Bridge Street Upper, R108', u'time': 9709, u'sign': 1}, {u'distance': 135.434, u'interval': [94, 99], u'text': u'Turn slight right onto Bridge Street Lower, R108', u'time': 10833, u'sign': 1}, {u'distance': 308.006, u'interval': [99, 102], u'text': u\"Turn slight left onto Usher's Quay, R148\", u'time': 24638, u'sign': -1}, {u'distance': 243.513, u'interval': [102, 107], u'text': u\"Continue onto Usher's Island, R148\", u'time': 19480, u'sign': 0}, {u'distance': 440.233, u'interval': [107, 110], u'text': u'Continue onto Victoria Quay, R148', u'time': 35217, u'sign': 0}, {u'distance': 24.107, u'interval': [110, 111], u'text': u'Continue', u'time': 1928, u'sign': 0}, {u'distance': 57.131, u'interval': [111, 114], u'text': u'Turn sharp right onto Frank Sherwin Bridge, R148', u'time': 4569, u'sign': 3}, {u'distance': 84.156, u'interval': [114, 117], u'text': u'Turn left onto Wolfe Tone Quay, R109', u'time': 6731, u'sign': -2}, {u'distance': 212.809, u'interval': [117, 122], u'text': u'Continue onto Parkgate Street, R109', u'time': 17022, u'sign': 0}, {u'distance': 97.545, u'interval': [122, 124], u'text': u'Continue onto Conyngham Road, R109', u'time': 7803, u'sign': 0}, {u'distance': 543.135, u'interval': [124, 130], u'text': u'Turn slight right onto Chesterfield Avenue', u'time': 43448, u'sign': 1}, {u'distance': 45.209, u'interval': [130, 136], u'text': u'Turn left', u'time': 3616, u'sign': -2}, {u'distance': 1769.388, u'interval': [136, 140], u'text': u'Turn sharp left onto Chesterfield Avenue', u'time': 141550, u'sign': -3}, {u'distance': 577.935, u'interval': [140, 150], u'text': u'Turn left', u'time': 68577, u'sign': -2}, {u'distance': 0, u'interval': [150, 150], u'text': u'Finish!', u'time': 0, u'sign': 4}]}], u'debugInfo': u'idLookup[0]:0.001686537s, [1] idLookup:3.32232E-4s, algoInit:3.4255E-5s, dijkstra-routing:0.020378169s, extract time:2.4595E-5, simplify (258->151)', u'time': 787382}");
		System.out.println(cache.get("a"));
        // Adding 3 seconds sleep.. Both above objects will be removed from
        // Cache because of timeToLiveInSeconds value
        Thread.sleep(3000);
 
        System.out.println("Two objects are added but reached timeToLive. cache.size(): " + cache.size());
 
    }
 
    private void TestObjectsCleanupTime() throws InterruptedException {
        int size = 500000;
 
        // Test with timeToLiveInSeconds = 100 seconds
        // timerIntervalInSeconds = 100 seconds
        // maxItems = 500000
 
        SmartHopperCache<String, String> cache = new SmartHopperCache<String, String>(100, 100, 500000);
 
        for (int i = 0; i < size; i++) {
            String value = Integer.toString(i);
            cache.put(value, value);
        }
 
        Thread.sleep(200);
 
        long start = System.currentTimeMillis();
        cache.cleanup();
        double finish = (double) (System.currentTimeMillis() - start) / 1000.0;
 
        System.out.println("Cleanup times for " + size + " objects are " + finish + " s");
 
    }
}
