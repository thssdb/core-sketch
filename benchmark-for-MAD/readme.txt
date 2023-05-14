compile:
	mvn clean package -Dmaven.test.skip=true
run:	replace target/..../config.propoerties with iot/Influx Data/Query.properties
	run ./benchmark.sh or ./benchmark.bat
