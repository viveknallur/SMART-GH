#!/bin/bash

#A quick way to set up the redis url for a partical city 
#bash ./set-redis-url --city dublin --url 127.0.0.1:1234
#will be used by cloudml to link the GH/sensors to a redis which is not in 
#the same docker host

city_flag=false
url_flag=false
city='dublin'
url=''
for a;
do 
  echo $a
  if $city_flag; then 
	city=$a; 
  fi
  if $url_flag; then 
	url=$a; 
  fi
  city_flag=false
  url_flag=false
  if [ "$a" = "--city" ]; then 
	city_flag=true 
  fi
  if [ "$a" = "--url" ]; 
	then url_flag=true 
  fi
done

sed -i "s/REDIS_URL=.*/REDIS_URL=$url/g" sensors-config-files/$city.config
#sed -i "s/REDIS_URL= localhost/REDIS_URL=$url/g" sensors-config-files/$city.config
