"""
This module contains the parsers for sensor data. When a new sensor is added, a 
parser must be added to read through the data dumped by the web-service, and 
create a hash, that can be returned.
"""
# std libs
import collections
import ConfigParser
import json
import logging
import os
import time
import urllib2
import xml.etree.ElementTree as ET

# third-party libs

# our libs
import constants
import summerlogger
import utils

def create_noise_sensor_hash(sensor_name, sensor_file, sensor_propagation):
    """
    This function reads an XML file created by NoiseTube, finds the appropriate
    streets that the readings pertain to, and creates a hash that can be
    inserted inside Redis. It ignores measurements that have no location 
    associated with them. If the same lat/long combination have multiple 
    measurements, the last one is taken

    :returns: Hash containing Way-id and sensor value and timestamp
    """
    logger = logging.getLogger('summer.reverse_geocode.create_noise_sensor_hash')
    logger.info("Parsing sensor data for: %s"%(sensor_name,))
    sensor_file = os.path.abspath(sensor_file)
    logger.info("Sensor data being grabbed from:%s"%(sensor_file))
    sensor_hash = collections.defaultdict(dict)
    prev_latitude = 0
    prev_longitude = 0
    try:
            for event, elem in ET.iterparse(sensor_file):
                if elem.tag == "measurement":
                    loudness = elem.get('loudness')
                    timestamp = elem.get('timeStamp')
                    geoloc = elem.get('location')
                    if geoloc is None:
                        continue
                    lat,long = geoloc.lstrip("geo:").split(",")
                    if not relevant_noise_measurement(prev_latitude, prev_longitude, \
                                                        lat, long, sensor_propagation):
                        logger.debug("Prev: %s,%s | curr: %s,%s"%(prev_latitude, \
                                prev_longitude, lat, long))
                        logger.debug("Skipping this measurement")
                        continue
                    else:
                        prev_latitude, prev_longitude = lat, long
                        relevant_streets = utils.get_relevant_streets(lat, long,\
                                sensor_propagation)
                        # sleep for a little while, just to be polite
                        time.sleep(3)
                        for street in relevant_streets:
                            sensor_hash[street].update({sensor_name +"_value":loudness, \
                            sensor_name + "_timestamp": timestamp})
    except ET.ParseError as pe:
        logger.warn("Malformed XML. Skipping rest of the file")
    logger.info("Finished processing %s"%(sensor_file))
    logger.info("Number of streets affected by sensor update: %d"%(len(sensor_hash)))
    return sensor_hash

def relevant_noise_measurement(prev_lat, prev_long, cur_lat, cur_long, \
        sensor_propagation):
    """
    This helper function checks if the new measurement is sufficiently distant 
    from the previous measurement, to warrant a reverse geo-coding. If the new 
    measurement is less than sensor_propagation-distance away from the previous 
    measurement, then return False. Else, return True

    """
    logger = \
        logging.getLogger('summer.reverse_geocode.relevant_noise_measurement')
    prev_lat = float(prev_lat)
    prev_long = float(prev_long)
    cur_lat = float(cur_lat)
    cur_long = float(cur_long)
    sensor_propagation = float(sensor_propagation)

    gps_distance = utils.calc_gps_distance(prev_lat, prev_long, cur_lat, \
            cur_long)
    logger.debug("GPS distance between prev & current measurment: \
            %f"%(gps_distance))

    return gps_distance >= sensor_propagation


