"""
This module contains all the constants that we need for generating the city config
"""

SENSOR_NAME = {
	'NoiseTube' : 'noise',
	'Air_Avg' : 'air',
	'Air_RT' : 'air',
	'GoogleTraffic' : 'traffic',
	'WazeTraffic': 'traffic',
	'TrafficApp': 'traffic',
	'OzoneDetect' : 'atmosphere',
	'PollenWatch' : 'pollen',
	'CrowdSourcedRoutes' : 'scenic'
}

def getSensorTypeFromName(constant):
	return SENSOR_NAME.get(constant, False)

SENSOR_UI_TEXT = {
	'atmosphere' : 'Most_Ozonic',
	'pollen' : 'Least_Pollen',
	'scenic' : 'Most_Scenic',
	'air' : 'Least_Polluted',
	'noise' : 'Least_Noisy',
	'traffic' : 'Least_Congested'
}

def getUITextFromType(constant):
	return SENSOR_UI_TEXT.get(constant, False)

TRAVEL_MODES = {
    'car': 'car',
    'bike': 'bike',
    'walk': 'foot',
    'scooter': 'scooter',
    'motorcycle': 'motorcycle',
    'monocycle': 'monocycle'
}

def getTravelMode(constant):
    return TRAVEL_MODES.get(constant, False)
