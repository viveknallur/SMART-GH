# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4 

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

def allTripsFromX(od_points, source = None):
    """
    Yields all trips from source, to all destinations in od_points. If source is 
    not given, picks a random source from od_points and returns all trips from 
    that source.
    """
    if not source:
        source = random.sample(od_points.keys(),1)[0]
    logger.info("Generating all trips from %s"%(source))

    for place in od_points.keys():
        if source == place:
            logger.debug("Cannot have trip from %s to %s"%(source, place))
            continue
        else:
            origin = od_points[source]
            destination = od_points[place]
            triplist = [origin, destination]
            logger.info("Trip generated: %s --> %s"%(source, place))
            logger.debug(triplist)
            yield triplist


def allTripsToX(od_points, destination = None):
    """
    Yields all trips to a given destination. If destination is None, then picks 
    a random destination from od_points, and generates trips to that destination
    """
    if not destination:
        destination = random.sample(od_points.keys(),1)[0]
    logger.info("Generating all trips from %s"%(destination))

    for place in od_points.keys():
        if destination == place:
            logger.debug("Cannot have trip from %s to %s"%(place, destination))
            continue
        else:
            dest_coords = od_points[destination]
            src_coords = od_points[place]
            triplist = [src_coords, dest_coords]
            logger.info("Trip generated: %s --> %s"%(destination, place))
            logger.debug(triplist)
            yield triplist

def allPossibleTrips(od_points):
    """
    Yields every 'X' --> 'Y' trip, for points in od_points. Note: 'X' --> 'Y' is 
    distinct from 'Y'--> 'X' due to possible one-ways. Hence, the total number 
    of trips generated is: (sizeof od_points) * (sizeof od_points - 1)
    """
    for place in od_points.keys():
        logger.debug("Generating trips from %s"%(place))
        for trip in allTripsFromX(od_points, place):
            logger.debug("Trip from %s to %s"%(trip[0], trip[1]))
            yield trip
    
