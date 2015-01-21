# std libs
import logging
import random

# third-party libs

# our libs

logger = logging.getLogger('D42Experiment')

def randomTrip(od_points):
    """
    Picks a random origin and destination from the hash of od_points, and 
    returns it
    """
    logger.info("Generating one random trip")
    journey = random.sample(od_points.keys(), 2)
    logger.info("Trip going from %s to %s"%(journey[0], journey[1]))
    triplist = []
    origin = od_points[journey[0]]
    triplist.append(origin)
    logger.debug("Origin coordinates: %s"%(str(origin)))
    destination = od_points[journey[1]]
    triplist.append(destination)
    logger.debug("Destination coordinates: %s"%(str(destination)))
    logger.debug("Length of triplist: %d"%(len(triplist)))
    logger.debug(triplist)
    yield triplist



