#!/bin/bash

GH_CLASS=com.graphhopper.GraphHopper
GH_HOME=$(dirname "$0")
JAVA=$JAVA_HOME/bin/java
if [ "x$JAVA_HOME" = "x" ]; then
 JAVA=java
fi

vers=$($JAVA -version 2>&1 | grep "java version" | awk '{print $3}' | tr -d \")
bit64=$($JAVA -version 2>&1 | grep "64-Bit")
if [ "x$bit64" != "x" ]; then
  vers="$vers (64bit)"
fi
echo "## using java $vers from $JAVA_HOME"

CONFIG=config.properties
if [ ! -f "config.properties" ]; then
  cp config-example.properties $CONFIG
fi

if [ "x$WS_CONFIG" = "x" ]; then
   echo "\$WS_CONFIG not set!"
   echo "Need to set WS_CONFIG to location of Smart-GH WebServices"
   echo "Typically set as: export WS_CONFIG=\"http://host:port\" "
   exit 1
fi

ACTION=$1

USAGE="./smartghweb.sh clean|web "
if [ "x$ACTION" = "x" ]; then
 echo -e "## action $ACTION not found. try \n$USAGE"
 exit 1
fi


function ensureMaven {
  # maven home existent?
  if [ "x$MAVEN_HOME" = "x" ]; then
    # not existent but probably is maven in the path?
    MAVEN_HOME=$(mvn -v | grep "Maven home" | cut -d' ' -f3)
    if [ "x$MAVEN_HOME" = "x" ]; then
      # try to detect previous downloaded version
      MAVEN_HOME="$GH_HOME/maven"
      if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
        echo "No Maven found in the PATH. Now downloading+installing it to $MAVEN_HOME"
        cd "$GH_HOME"
        MVN_PACKAGE=apache-maven-3.2.1
        wget -O maven.zip http://www.eu.apache.org/dist/maven/maven-3/3.2.1/binaries/$MVN_PACKAGE-bin.zip
        unzip maven.zip
        mv $MVN_PACKAGE maven
        rm maven.zip
      fi
    fi
  fi
}

#function packageCoreJar {
#  if [ ! -d "./target" ]; then
#    echo "## building parent"
#    "$MAVEN_HOME/bin/mvn" --non-recursive install > /tmp/graphhopper-compile.log
#     returncode=$?
#     if [[ $returncode != 0 ]] ; then
#       echo "## compilation of parent failed"
#       cat /tmp/graphhopper-compile.log
#       exit $returncode
#     fi                                     
#  fi
#  
#  if [ ! -f "$JAR" ]; then
#    echo "## now building graphhopper jar: $JAR"
#    echo "## using maven at $MAVEN_HOME"
#    #mvn clean
#    "$MAVEN_HOME/bin/mvn" --projects core -DskipTests=true install assembly:single > /tmp/graphhopper-compile.log
#    returncode=$?
#    if [[ $returncode != 0 ]] ; then
#        echo "## compilation of core failed"
#        cat /tmp/graphhopper-compile.log
#        exit $returncode
#    fi      
#  else
#    echo "## existing jar found $JAR"
#  fi
#}


## now handle actions which do not take an OSM file
if [ "x$ACTION" = "xclean" ]; then
 echo "## Cleaning up..."
 rm -rf ./*/target
 exit
fi


VERSION=$(grep  "<name>" -A 1 pom.xml | grep version | cut -d'>' -f2 | cut -d'<' -f1)
JAR=core/target/graphhopper-$VERSION-jar-with-dependencies.jar

if [ "x$JAVA_OPTS" = "x" ]; then
  JAVA_OPTS="-XX:PermSize=60m -XX:MaxPermSize=60m -Xmx1000m -Xms1000m -server"
fi


ensureMaven
#packageCoreJar

echo "## now building $ACTION. JAVA_OPTS=$JAVA_OPTS"

if [ "x$ACTION" = "xweb" ]; then
  export MAVEN_OPTS="$MAVEN_OPTS $JAVA_OPTS"
  if [ "x$JETTY_PORT" = "x" ]; then  
    JETTY_PORT=8989
  fi
  WEB_JAR="$GH_HOME/web/target/graphhopper-web-$VERSION-with-dep.jar"
  if [ ! -s "$WEB_JAR" ]; then         
    "$MAVEN_HOME/bin/mvn" --projects web -DskipTests=true install assembly:single > /tmp/graphhopper-web-compile.log
    returncode=$?
    if [[ $returncode != 0 ]] ; then
      echo "## compilation of web failed"
      cat /tmp/graphhopper-web-compile.log
      exit $returncode
    fi
  fi

  RC_BASE=./web/src/main/webapp

  if [ "x$GH_FOREGROUND" = "x" ]; then
    exec "$JAVA" $JAVA_OPTS -jar "$WEB_JAR" jetty.resourcebase=$RC_BASE \
        jetty.port=$JETTY_PORT jetty.host=$JETTY_HOST 
    # foreground => we never reach this here
  else
    exec "$JAVA" $JAVA_OPTS -jar "$WEB_JAR" jetty.resourcebase=$RC_BASE \
        jetty.port=$JETTY_PORT jetty.host=$JETTY_HOST <&- &
    if [ "x$GH_PID_FILE" != "x" ]; then
       echo $! > $GH_PID_FILE
    else
       GH_PID_FILE="smartgh_web.pid"
       echo $! > $GH_PID_FILE
       echo "Smart-GH webserver goes to the background."
       echo "Pid in file: $GH_PID_FILE"
    fi
    exit $?                    
  fi

fi
