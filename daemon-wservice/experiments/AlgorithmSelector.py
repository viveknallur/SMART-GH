# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4

# std libs
import logging
import random

# third-party libs

# our libs

logger = logging.getLogger('D42Experiment')
ALGO_POOL = ['astar', 'astarbi', 'dijkstra', 'dijkstrabi', 'dijkstraNativebi']

def pickFirst():
    first = ALGO_POOL[4]
    logger.info("Picking algorithm: %s"%(first))
    return first
