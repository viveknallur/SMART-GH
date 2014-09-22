# SMART-GH Route Planner

SMART-GH extends graphhopper (https://graphhopper.com/) ,which is a fast and memory efficient Java road routing engine released under Apache License 2.0, with a smart and personalized dimension. 
SMART-GH integrates real time data captured from sensors, utilizing citizens as sensors, and enables the user to submit smart and personalized routing requests:

	- Transport mode: car, foot, bike, mtb and racing bike.
	- Quality of route: fastest, shortest, least noisy, least air polluted, least polluted (combination of sensors).

GraphHopper is tuned towards road networks with OpenStreetMap data but can be useful for public transport problems as well.

SMART-GH Pre-requisites:
-----------------------
 
 * SMART-GH is built using Maven, so you need to have Maven and Java installed and configured on your computer (http://maven.apache.org/download.cgi)
 * SMART-GH uses Redis to store and maintain sensors data, so you need to have Redis running:
	- Linux: (http://redis.io/download)
	- Windows: One click Redis install as a Windows service(https://github.com/rgl/redis/downloads)
 * Windows user must install Cygwin: https://www.cygwin.com/
 * Sensor parsing: (TODO: vivek describes what needs to be installed and how; Python...etc. )
		
Get Started with SMART-GH
-------------------------

 1. Clone SMART-GH: https://github.com/DIVERSIFY-project/SMART-GH.git
 2. Copy dublin-m50.osm map (https://www.dropbox.com/s/ozo6nowib7dcfbj/dublin-m50.osm?dl=0) to SMART-GH/maps
	- For sensor-based routing to work correctly, all maps should be placed in maps folder, and should be named following this naming convention: city-*.osm; e.g.,paris-centre.osm
 3. Make sure that 'config.properties' file exists inside SMART-GH folder, and that it satisfies these settings: https://www.dropbox.com/s/bsnkxc3vtgax0tf/config.properties?dl=0
 4. Run Sensor parsing Daemon:
	- Go to SMART-GH/sensor_processing
	- run "python process_sensor_data_daemon.py"
 5. Run "mvn -DskipTests package" (use Cygwin if you are using Windows)
 6. Run "./graphhopper.sh web ./maps/dublin-m50.osm" (use Cygwin if you are using Windows)
 
Features
---------

 * Written in Java
 * Open Source
 * Memory efficient and fast
 * Highly customizable
 * Works on the desktop, as a web service and offline on Android
 * Well tested
 * Supports smart and personalized routing requests.
 * ...
 
 
Graphhopper links:
-----------------
Read through our [docs](https://github.com/graphhopper/graphhopper/blob/master/docs/index.md), 
[how to contribute](https://github.com/graphhopper/graphhopper/blob/master/CONTRIBUTING.md) and 
ask questions on [Stackoverflow](http://stackoverflow.com/questions/tagged/graphhopper)
or sign up to the [mailing list](http://graphhopper.com/#developers).

Graphhoper [![Build Status](https://secure.travis-ci.org/graphhopper/graphhopper.png?branch=master)](http://travis-ci.org/graphhopper/graphhopper)

There are subprojects to make GraphHopper working on [Android](https://github.com/graphhopper/graphhopper/blob/master/docs/android/index.md) or 
as a [web application](https://github.com/graphhopper/graphhopper/tree/master/web).