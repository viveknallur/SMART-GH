# import std libraries
import logging


# import third-party libs
import requests
from plumbum import cli

# import our modules
import constants
import TripGenerator
import AlgorithmSelector

logger = logging.getLogger('D42Experiment')

class MyRouteFinder(cli.Application):
    _od_points = {}
    _app_config = {}
    _preconfig = False
    _batchmode = False
    _is_logging = False

    @cli.autoswitch(str)
    def log_to_file(self, filename):
        """
            Creates a logger, and sets logging level to INFO
        """
        logger.addHandler(logging.FileHandler(filename))
        logger.setLevel(logging.INFO)
        self._is_logging = True

    @cli.switch(["-v", "--verbose"], excludes = ["--terse"])
    def set_debug(self):
        """
        Enable verbose mode in logging
        """
        logger.setLevel(logging.DEBUG)

    @cli.switch(["--terse"], excludes = ["--verbose"])
    def set_terse(self):
        """
        Enable terse mode in logging. Sets logging to WARN levels
        """
        logger.setLevel(logging.WARN)

    @cli.autoswitch(str)
    def lat1(self, lat1):
        """
            The latitude of the origin
        """
        self._lat1 = lat1

    @cli.autoswitch(str)
    def lat2(self, lat2):
        """
            The latitude of the destination
        """
        self._lat2 = lat2

    @cli.autoswitch(str)
    def lon1(self, lon1):
        """
            The longitude of the origin
        """
        self._lon1 = lon1

    @cli.autoswitch(str)
    def lon2(self, lon2):
        """
            The longitude of the destination
        """
        self._lon2 = lon2

    @cli.autoswitch(str)
    def host_and_port(self, host_and_port):
        """
            The host and port where the webservice resides, including the 
            protocol and slashes (http://host:port/)
        """
        self._host_and_port = host_and_port

    @cli.autoswitch(str)
    def service_name(self, service_name):
        """
            The name of the webservice, including trailing slash
            (e.g., restful-graphhopper-1.0/)
        """
        self._service_name = service_name

    @cli.autoswitch(str)
    def endpoint(self, endpoint):
        """
            The name of the endpoint in the webservice that we are querying
            (e.g., route)
        """
        self._endpoint = endpoint

    @cli.switch(["--batch"])
    def batchmode(self):
        """
            If given, run in batch mode using source and destination from
            GPS_LOCATIONS given in constants file. Any origin/destination points 
            given on the commandline are ignored
        """
        self._batchmode = True
        self._od_points = constants.getAllLocations()

    @cli.switch(["--preconfig"])
    def use_config_from_constants(self):
        self._preconfig = True
        self._app_config = constants.getAppConfig()

    def create_url(self, trip, algo):
        """
            Uses _app_config to create the url for the webservice to be queried.
            Must be called before the webservice is actually called
        """
        if self._preconfig:
            logger.debug("Using host, service and endpoint values from \
                    constants file")
            self._host_and_port = self._app_config['host_and_port']
            self._service_name = self._app_config['service']
            self._endpoint = self._app_config['endpoint']

        wsurl = ''.join([self._host_and_port, \
                                self._service_name, \
                                self._endpoint])
        logger.info("Will connect to service at: %s"%(wsurl))
        origin_lat = trip[0][0]
        origin_lng = trip[0][1]
        dest_lat = trip[1][0]
        dest_lng = trip[1][1]
        locale = constants.getAppParams('locale')
        trip = {'lat1': origin_lat, 'lon1': origin_lng,
                'lat2': dest_lat, 'lon2': dest_lng,
                'algoStr': algo, 'locale': locale}
        return [wsurl, trip]


    def ask_service(self, trip):
        """
            Makes a request to the webservice, and returns the result
        """
        logger.debug("Trip going from %s To %s"%(str(trip[0]),str(trip[1]))) 
        chosenAlgo = AlgorithmSelector.pickFirst()
        full_url = self.create_url(trip, chosenAlgo)
        req = requests.get(full_url[0], params=full_url[1])
        logger.info("Full url used for connection is: ")
        logger.info(req.url)
        graphy_route = req.text
        logger.info("The route returned by the webservice is")
        logger.info(graphy_route)


    def main(self):
        if not self._is_logging:
            ch = logging.StreamHandler()
            logger.addHandler(ch)
            logger.setLevel(logging.DEBUG)
            logger.info("No logger defined. Logging to console...")
            
        if self._preconfig:
            logger.debug("Configuration slurped from constants file")

        if self._batchmode:
            logger.debug("Running in batch mode")

        for trip in TripGenerator.randomTrip(self._od_points):
            data = self.ask_service(trip)
            logger.info(data)



if __name__ == "__main__":
    MyRouteFinder.run()
