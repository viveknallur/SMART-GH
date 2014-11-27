cd /opt/gh
bash set-redis-url.sh --url $redisurl
cd /opt/gh/
mvn clean 
mvn -DskipTests install
cd daemon-wservice/
mvn package
cd target/
cp restful-graphhopper-1.0.war /maven/
/opt/tomcat/bin/deploy-and-run.sh

while [ 1 ]
do
  sleep 1000
done
