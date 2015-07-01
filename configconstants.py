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
	'OzoneDetect' : 'air',
	'PollenWatch' : 'air'
}

def getSensorTypeFromName(constant):
	return SENSOR_NAME.get(constant, False)

SENSOR_UI_TEXT = {
	'air' : 'Least Polluted',
	'noise' : 'Least Noisy',
	'traffic' : 'Least Congested'
}

def getUITextFromType(constant):
	return SENSOR_UI_TEXT.get(constant, False)
