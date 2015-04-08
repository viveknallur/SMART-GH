# stdlib imports
import collections
import ConfigParser
import logging
import sys

#third-party imports
import redis

def get_street_names(redis_connection,city_prefix, sensor_prefix):
    """
    Uses the redis connection to get a list of all the street names that we have
    data for. Since, we may have data for multiple types of sensors, this func takes as argument, the city and sensor prefix used to qualify streets
    """  
    logger = logging.getLogger('smartgh.get_street_names')
    city_way_set = ''.join([city_prefix, '_set'])
    city_way_data = redis_connection.smembers(city_way_set)
    logger.debug("Number of streets in %s: %d"%(city_prefix, len(city_way_data)))
    only_sensor_streets = [street for street in city_way_data if street.find(sensor_prefix) > 0]
    for street in city_way_data:
        logger.debug("Street name: %s"%(street))

    logger.info("Number of streets with %s data: %d"%(sensor_prefix, len(only_sensor_streets)))
    
    for street in only_sensor_streets:
        logger.info("%s"%(street.rsplit('_',1)[1]))


def get_redis_connection(REDIS_URL):
    """
    Tries to connect to redis and returns a connection, if it can
    """
    logger = logging.getLogger('smartgh.get_redis_connection')
    try:
        redis_connection = redis.Redis(REDIS_URL)
        if not redis_connection:
            logger.warn("Could not connect to Redis!!")
            raise IOError
    except IOError as ioe:
        logger.critical("Why Redis no respond, huh?")
    else:
        logger.debug("Connected to redis. Returning connection var")
        return redis_connection


if __name__ == '__main__':
    logger = logging.getLogger('smartgh')
    logger.setLevel(logging.DEBUG)
    
    ch = logging.StreamHandler(sys.stdout)
    ch.setLevel(logging.INFO)
    logger.addHandler(ch)

    REDIS_URL='localhost'
    city = 'dublin'
    sensor = 'air'
    rs = get_redis_connection(REDIS_URL)
    get_street_names(rs, city, sensor)
