# core-sketch

## Introduction

This is the code for our paper "*CORE-Sketch: On Exact Computation of Median Absolute Deviation with Limited Space*". The code is writen in JAVA, in the folder "code\src\main\java".

## Overview
For the subfolders, they all serve as JAVA packages, and the functions are listed as follows:

- benchmark: It contains the exact MAD algorithm No-Sketch and CORE-Sketch in EXACT_MAD.java and CORE_MAD.java respectively. The approximate MAD algorithm based on DD-Sketch is in DD_MAD.java. The TP algorithm based on TP-Sketch is in TP_MAD.java.
- dataset: It gives the methods for generating synthetic datasets in synthetic.java.
- mad: It contains the data structure, CORE-Sketch, TP-Sketch and DD-Sketch used for computing MAD.
- utils: It contains the methods for file reading & writing, parameter setting, etc.
- experiments (Please make sure that the machine has enough memory (16GB for 1E9 data points). The tested data size and number of test repeated can be changed by modifying variables in code.):

  - Experiments varying N: NormExp.java is used for testing synthetic data with varied amount N up to 1E10. ApproxAndDataSizeExp.java is used for testing real-world data with varied amount N up to 1E7.
  - Experiments varying M: ApproxSpaceExp.java is used for testing with varied bucket limit M. ApproxSpaceAnoExp.java also shows the  F1 scores of outlier detection.
  - Experiments varying ε: ApproxBoundExp.java is used for testing with varied epsilon ε. ApproxBoundAnoExp.java also shows the F1 scores.
  - LocalApplication.java shows how to run the experiments and set some parameters.

# IoTDB Benchmark
This is for the concurrent query experiment in Figure 21. Note that this benchmark is used for evaluating CORE-Sketch in databases, unlike the folder "benchmark" in the last section.

How to compile benchmark:
	mvn clean package -Dmaven.test.skip=true
For more details about iot-benchmark, please check https://github.com/thulab/iot-benchmark/blob/master/README.md

Before running benchmark, the IoTDB server with Core-Sketch implementation should be started.
	The deployed system can be cloned from:
		https://github.com/apache/iotdb/tree/research/core-sketch
	and complied with:
		mvn spotless:apply
		mvn clean package -pl cli -am -Dmaven.test.skip=trueD:
	Then we can run server by .\iotdb\server\target\iotdb-server-0.13.0-SNAPSHOT\sbin\start-server.bat or .\iotdb\server\target\iotdb-server-0.13.0-SNAPSHOT\sbin\start-server.sh

Note that the behaviors (query or ingest data) depends on the configuration file iot-benchmark-2023-04-28-e7dad042-java8\iotdb-0.13\target\iot-benchmark-iotdb-0.13\iot-benchmark-iotdb-0.13\conf\config.propoerties
To ingest synthetic data into Apache IoTDB, the configuration file should be modified with the content in "iot Data.properties".
	To ingest different types of data, the code in benchmark should be changed and re-compile. In default it generates Normal data.
To perform concurrent query, it should be modified with the content in "iot Query.properties".
	To perform diferent types of queries (CORE-Sketch or No-Sketch), the parameter "QUERY_AGGREGATE_FUN" should be changed to "mad_core" or "mad_qs".
	To change concurrency, the parameter CLIENT_NUMBER should be varied (from 1 to 32).