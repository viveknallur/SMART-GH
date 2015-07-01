# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4

# import std libraries
import logging


# import third-party libs
import requests
from plumbum import cli

# import our modules
import constants
import utils

logger = logging.getLogger('smarthopper.config')

class ConfigGenerator(cli.Application):
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

    @cli.switch(["--city"], str)
    def city_config(self, city):
        """
            The city smart-hopper is going to use
        """
        self._city = city

    @cli.switch(["--sensors"], str)
    def city_sensors(self, sensors):
        """
            The sensors in the city
        """
        self._sensors = sensors

    @cli.switch(["--transport"], str, list=True)
    def modes(self, modes):
        """
            The modes of transport available with this city
        """
        self._modes = modes

	def create_main_config():
	"""
		Creates the main config file called config.properties
	"""
		with open("config-pre.heredoc", r) as pre_file:
			config_pre = pre_file.read()

		with open("config-post.heredoc", r) as post_file:
			config_post = post_file.read()

		

	def create_city_config():
	"""
		Creates the city config file named as <city>.config, where <city> is the value of self._city
	"""
		pass

    def main(self):
        if not self._is_logging:
            ch = logging.StreamHandler()
            logger.addHandler(ch)
            logger.setLevel(logging.DEBUG)
            logger.info("No logger defined. Logging to console...")

	
            



if __name__ == "__main__":
    ConfigGenerator.run()
