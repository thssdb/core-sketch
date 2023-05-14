USE test;

DROP PROCEDURE IF EXISTS `exists_view`;
DROP PROCEDURE IF EXISTS `creatView`;

DELIMITER $$

CREATE PROCEDURE exists_view(IN viewName VARCHAR(500), OUT re BOOLEAN)
BEGIN
   DECLARE num INT;
   SELECT COUNT(information_schema.VIEWS.TABLE_SCHEMA) INTO num
	FROM information_schema.VIEWS
	WHERE (information_schema.VIEWS.TABLE_SCHEMA=viewName);

   IF(num != 0 ) THEN
        SET re=TRUE;
   ELSE
        SET re=FALSE;
   END IF;

END $$


CREATE PROCEDURE creatView()
BEGIN
DECLARE isExits BOOLEAN;

CALL exists_view('insertResult',isExits);
SELECT isExits;
IF(isExits = FALSE) THEN
CREATE
    /*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
    VIEW `insertResult` (projectID,createSchemaTime,totalPoints,totalInsertionTime,totalErrorPoint,totalElapseTime,avg,midAvg,min,max,p1,p5,p50,p90,p95,p99,p999,p9999)
    AS
(SELECT a.projectID,a.result_value,b.result_value,c.result_value,d.result_value,e.result_value,
        f1.result_value,f2.result_value,f3.result_value,f4.result_value,f5.result_value,f6.result_value,
        f7.result_value,f8.result_value,f9.result_value,f10.result_value,f11.result_value,f12.result_value FROM
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'createSchemaTime(s)') a  JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalPoints') b ON a.projectID = b.projectID JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalInsertionTime(s)') c ON a.projectID = c.projectID JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalErrorPoint') d ON a.projectID = d.projectID JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalElapseTime(s)') e ON a.projectID = e.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'avg') f1 ON a.projectID = f1.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'middleAvg') f2 ON a.projectID = f2.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'min') f3 ON a.projectID = f3.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'max') f4 ON a.projectID = f4.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p1') f5 ON a.projectID = f5.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p5') f6 ON a.projectID = f6.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p50') f7 ON a.projectID = f7.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p90') f8 ON a.projectID = f8.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p95') f9 ON a.projectID = f9.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p99') f10 ON a.projectID = f10.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p999') f11 ON a.projectID = f11.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p9999') f12 ON a.projectID = f12.projectID
);
END IF;

CALL exists_view('queryResult',isExits);
SELECT isExits;
IF(isExits = FALSE) THEN
CREATE
    /*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
    VIEW `queryResult`(projectID,queryNumber,totalPoints,totalTimes,totalErrorPoint,resultPointPerSecond,avg,midAvg,min,max,p1,p5,p50,p90,p95,p99,p999,p9999)
    AS
(SELECT a.projectID,a.result_value,b.result_value,c.result_value,d.result_value,e.result_value,
        f1.result_value,f2.result_value,f3.result_value,f4.result_value,f5.result_value,f6.result_value,
        f7.result_value,f8.result_value,f9.result_value,f10.result_value,f11.result_value,f12.result_value
 FROM
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'queryNumber') a  JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalPoint') b ON a.projectID = b.projectID JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalTime(s)') c ON a.projectID = c.projectID JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'totalErrorQuery') d ON a.projectID = d.projectID JOIN
(SELECT projectID, result_value FROM RESULT WHERE result_key = 'resultPointPerSecond(points/s)') e ON a.projectID = e.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'avgQueryLatency') f1 ON a.projectID = f1.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'middleAvgQueryLatency') f2 ON a.projectID = f2.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'minQueryLatency') f3 ON a.projectID = f3.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'maxQueryLatency') f4 ON a.projectID = f4.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p1QueryLatency') f5 ON a.projectID = f5.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p5QueryLatency') f6 ON a.projectID = f6.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p50QueryLatency') f7 ON a.projectID = f7.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p90QueryLatency') f8 ON a.projectID = f8.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p95QueryLatency') f9 ON a.projectID = f9.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p99QueryLatency') f10 ON a.projectID = f10.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p999QueryLatency') f11 ON a.projectID = f11.projectID JOIN
    (SELECT projectID, result_value FROM RESULT WHERE result_key = 'p9999QueryLatency') f12 ON a.projectID = f12.projectID
);
END IF;

CALL exists_view('configCommonInfo',isExits);
SELECT isExits;
IF(isExits = FALSE) THEN
CREATE
    /*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
    VIEW `configCommonInfo`(projectID,`mode`,dbSwitch,`version`,clientNumber,`loop`,serverIP,clientName)
    AS
(SELECT a.projectID,a.configuration_value,b.configuration_value,c.configuration_value,d.configuration_value,e.configuration_value ,f.configuration_value,g.configuration_value FROM
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'MODE') a  JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'DB_SWITCH') b ON a.projectID = b.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'VERSION') c ON a.projectID = c.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'CLIENT_NUMBER') d ON a.projectID = d.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'LOOP') e ON a.projectID = e.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'ServerIP') f ON a.projectID = f.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'CLIENT') g ON a.projectID = g.projectID
);
END IF;

CALL exists_view('configInsertInfo',isExits);
SELECT isExits;
IF(isExits = FALSE) THEN
CREATE
    /*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
    VIEW `configInsertInfo`(projectID,`mode`,dbSwitch,`version`,clientNumber,`loop`,serverIP,clientName,
    IS_OVERFLOW,MUL_DEV_BATCH,GROUP_NUMBER,DEVICE_NUMBER,SENSOR_NUMBER,CACHE_NUM,POINT_STEP,ENCODING)
    AS
(SELECT  DISTINCT (a.projectID),a.mode,a.dbSwitch,a.version,a.clientNumber,a.loop,a.serverIP,a.clientName,aa.configuration_value,
b.configuration_value,d.configuration_value,e.configuration_value ,f.configuration_value,g.configuration_value ,h.configuration_value,i.configuration_value FROM
(SELECT * FROM configCommonInfo ) a  JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'IS_OVERFLOW') aa  ON a.projectID = aa.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'MUL_DEV_BATCH') b ON a.projectID = b.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'GROUP_NUMBER') d ON a.projectID = d.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'DEVICE_NUMBER') e ON a.projectID = e.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'SENSOR_NUMBER') f ON a.projectID = f.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'BATCH_SIZE') g ON a.projectID = g.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'POINT_STEP') h ON a.projectID = h.projectID LEFT JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'ENCODING') i ON a.projectID = i.projectID
);
END IF;

CALL exists_view('configQueryBasicInfo',isExits);
SELECT isExits;
IF(isExits = FALSE) THEN
CREATE
    /*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
    VIEW `configQueryBasicInfo`(projectID,QUERY_CHOICE,QUERY_DEVICE_NUM,QUERY_SENSOR_NUM,查询数据集存储组数,查询数据集设备数,查询数据集传感器数,IOTDB编码方式)
    AS
(SELECT a.projectID,a.configuration_value,b.configuration_value,c.configuration_value,d.configuration_value,e.configuration_value ,f.configuration_value,g.configuration_value  FROM
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'QUERY_CHOICE') a  JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'QUERY_DEVICE_NUM') b ON a.projectID = b.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'QUERY_SENSOR_NUM') c ON a.projectID = c.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = '查询数据集存储组数') d ON a.projectID = d.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = '查询数据集设备数') e ON a.projectID = e.projectID JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = '查询数据集传感器数') f ON a.projectID = f.projectID LEFT JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'IOTDB编码方式') g ON a.projectID = g.projectID
);

END IF;

CALL exists_view('configQueryInfo',isExits);
SELECT isExits;
IF(isExits = FALSE) THEN
CREATE
    /*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
    VIEW `configQueryInfo`(projectID,QUERY_CHOICE,`LOOP`,CLIENT_NUMBER,QUERY_DEVICE_NUM,QUERY_SENSOR_NUM,IS_RESULTSET_NULL,QUERY_AGGREGATE_FUN,
    TIME_INTERVAL,FILTRATION_CONDITION,TIME_UNIT)
    AS
(SELECT  DISTINCT (a.projectID),a.QUERY_CHOICE, aa.loop, aa.clientNumber,a.QUERY_DEVICE_NUM,a.QUERY_SENSOR_NUM,
b.configuration_value,d.configuration_value,e.configuration_value ,f.configuration_value,g.configuration_value FROM
(SELECT projectID,QUERY_CHOICE,QUERY_DEVICE_NUM,QUERY_SENSOR_NUM FROM configQueryBasicInfo) a  JOIN
(SELECT projectID, `loop`,clientNumber FROM configCommonInfo) aa  ON a.projectID = aa.projectID LEFT JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'IS_RESULTSET_NULL') b ON a.projectID = b.projectID LEFT JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'QUERY_AGGREGATE_FUN') d ON a.projectID = d.projectID LEFT  JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'TIME_INTERVAL') e ON a.projectID = e.projectID LEFT  JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'FILTRATION_CONDITION') f ON a.projectID = f.projectID LEFT  JOIN
(SELECT projectID, configuration_value FROM CONFIG WHERE configuration_item = 'TIME_UNIT') g ON a.projectID = g.projectID
);
END IF;

END $$

DELIMITER ;

CALL creatView();