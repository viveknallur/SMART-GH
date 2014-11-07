#!/bin/bash
city_flag=false
url_flag=false
city=''
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

sed -i "s/REDIS_URL= localhost/REDIS_URL=$url/g" sensors-config-files/$city.config
