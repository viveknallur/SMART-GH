# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4

# std libs
import logging
import random

# third-party libs

# our libs

logger = logging.getLogger('D42Experiment')
ALGO_POOL = ['astar', 'astarbi', 'dijkstra', 'dijkstrabi', 'dijkstraNativebi']
BI_POOL = ['astarbi', 'dijkstrabi', 'dijkstraNativebi']
NONBI_POOL = ['astar', 'dijkstra']
DIJSKTRA_POOL = ['dijkstra', 'dijkstrabi', 'dijkstraNativebi']
ASTAR_POOL = ['astar', 'astarbi']
ASTAR_MIX = ['astar', 'astarbi', 'dijkstraNativebi']
DIJKSTRA_MIX = ['dijkstra', 'dijkstrabi', 'dijkstraNativebi', 'astar']
TWO_MIX = [['astar', 'dijkstrabi'], ['astar', 'dijkstraNativebi'],['astar', \
'dijkstra']]
THREE_MIX = [['astarbi', 'dijkstra', 'dijkstrabi'], ['astarbi', 'dijkstra', \
    'dijkstraNativebi'], ['astarbi','dijkstrabi', 'dijkstraNativebi']]

WEIGHTINGS = ['fastest', 'shortest']

def pickFirst():
    first = ALGO_POOL[0]
    logger.info("Picking algorithm: %s"%(first))
    return first

def pickDijkstra():
    return ALGO_POOL[2]

def pickFromTwoMix():
    two_mix_pool = random.sample(TWO_MIX, 1)[0]
    logger.info("Picked two mix pool: %s"%(str(two_mix_pool)))
    while True:
        randAlgo = random.sample(two_mix_pool, 1)[0]
        yield randAlgo

def pickFromThreeMix():
    three_mix_pool = random.sample(THREE_MIX, 1)[0]
    logger.info("Picked three mix pool: %s"%(str(three_mix_pool)))
    while True:
        randAlgo = random.sample(three_mix_pool, 1)[0]
        yield randAlgo

def pickLast():
    last = ALGO_POOL[-1]
    logger.info("Picking algorithm: %s"%(last))
    return last

def pickRandom():
    randAlgo = random.sample(ALGO_POOL, 1)[0]
    logger.info("Picking random algorithm: %s"%(randAlgo))
    return randAlgo

def pickRandomBi():
    randAlgo = random.sample(BI_POOL, 1)[0]
    logger.info("Picking random bidirectional algorithm: %s"%(randAlgo))
    return randAlgo

def pickRandomNonBi():
    randAlgo = random.sample(NONBI_POOL, 1)[0]
    logger.info("Picking random non-bidirectional algorithm: %s"%(randAlgo))
    return randAlgo

def pickFastestWeighting():
    weighting = WEIGHTINGS[0]
    logger.debug("Picking weighting to be: %s"%(weighting))
    return weighting


def pickShortestWeighting():
    weighting = WEIGHTINGS[1]
    logger.debug("Picking weighting to be: %s"%(weighting))
    return weighting

def pickRandomWeighting():
    weighting = random.sample(WEIGHTINGS, 1)[0]
    logger.debug("Picking weighting to be: %s"%(weighting))
    return weighting
