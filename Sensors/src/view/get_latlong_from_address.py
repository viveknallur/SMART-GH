# std libs
import json
import requests
import time
import sys

# third party libs


# our libs
import utils

# constants
SERVER = 'https://maps.googleapis.com/maps/api/geocode/json'
API_KEY = 'AIzaSyA_KjJQteitfgzMpNqgb_Ew6wYeMMqfLH0'
CITY_COUNTRY = ', Dublin, Republic of Ireland'
POLITENESS_INTERVAL = 3

streets = ['Abbey Street Lower', 'Railway Street', 'Mayor Street Lower', 'Park Lane', 'Horse Fair', 'Mark\'s Lane', 'Lime Street']


google_request = requests.get(SERVER, params = {'address': ''.join([sys.argv[1], CITY_COUNTRY]), 'key': API_KEY})
	
print("Full url being requested: %s"%(google_request.url))
try:
        address_json = google_request.json()
        full_address = utils.byteify(address_json)
        #print(full_address)
        ne_lat = full_address['results'][0]['geometry']['bounds']['northeast']['lat']
        ne_lng = full_address['results'][0]['geometry']['bounds']['northeast']['lng']
        sw_lat = full_address['results'][0]['geometry']['bounds']['southwest']['lat']
        sw_lng = full_address['results'][0]['geometry']['bounds']['southwest']['lng']
        print ("Lat/Lng coordinates for NE of %s"%(sys.argv[1]))
        print ("Lat: %s | Lng: %s"%(ne_lat, ne_lng))
        print ("Lat/Lng coordinates for SW of %s"%(sys.argv[1]))
        print ("Lat: %s | Lng: %s"%(sw_lat, sw_lng))
except Exception as e:
        print("Exception occurred: %s"%(e))
finally:
	time.sleep(POLITENESS_INTERVAL)
	



