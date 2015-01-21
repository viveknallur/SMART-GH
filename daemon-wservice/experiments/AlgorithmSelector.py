# std libs
import logging
import random

# third-party libs

# our libs

logger = logging.getLogger('D42Experiment')
ALGO_POOL = ['astar', 'astarbi', 'dijkstra', 'dijkstrabi', 'dijkstraOneToMany']

def pickFirst():
    first = ALGO_POOL[0]
    logger.info("Picking algorithm: %s"%(first))
    return first
