# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4

# import std libraries
import logging
import os.path


# import third-party libs
from plumbum import cli

# import our modules
import configconstants


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
        self._sensors = sensors.split(",")

    @cli.switch(["--modes"], str)
    def modes(self, modes):
        """
            The modes of transport available with this city
        """
        self._modes = modes

    def create_main_config(self):
        """
        Creates the main config file called config.properties
        """
        map_extension = ".osm"
        map_file = ''.join([self._city, map_extension])
        logger.debug("Map file: %s"%(map_file))

        osm_map_var = 'osmMap='
        osm_map_var = ''.join([osm_map_var,map_file])
        logger.debug("osm_map_var = %s"%(osm_map_var))

        osm_way_var = 'osmreader.acceptWay='
        osm_way_var = ''.join([osm_way_var,self._modes])
        logger.debug("osm_way_var = %s"%(osm_way_var))

        with open("config-pre.heredoc", "r") as pre_file:
                config_pre = pre_file.read()

        with open("config-post.heredoc", "r") as post_file:
                config_post = post_file.read()

        with open("config.properties", "w") as config_file:
                config_file.write(config_pre)
                config_file.write(osm_map_var)
                config_file.write("\n\n")
                config_file.write(osm_way_var)
                config_file.write("\n\n")
                config_file.write(config_post)

    def create_city_config(self):
        """
        Creates the city config file named as <city>.config, where <city> is the value of self._city
        """
        subdir = "sensors-config-files"
        config_extension = '.config'
        config_file = ''.join([self._city, config_extension])

        city_var = 'CITY_PREFIX = '
        city_var = ''.join([city_var, self._city])

	logger.debug("sensors found: %s"%(self._sensors))
	
	

        
        
        

    def main(self):
        if not self._is_logging:
            ch = logging.StreamHandler()
            logger.addHandler(ch)
            logger.setLevel(logging.DEBUG)
            logger.info("No logger defined. Logging to console...")

        if self._city:
                logger.debug("Found city: %s"%(self._city))
                self.create_main_config()
		self.create_city_config()
        
            



if __name__ == "__main__":
    ConfigGenerator.run()
