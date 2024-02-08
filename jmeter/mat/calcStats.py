#!/usr/bin/env python3

import json
import sys
import os
import urllib.parse
import time
# import pymongo
# from pymongo import MongoClient
import psycopg2
from datetime import datetime, timezone
from prometheus_client import CollectorRegistry, Gauge, push_to_gateway


num_params=len(sys.argv)

fname="SummaryReport.csv"
if num_params > 1:
    fname = sys.argv[1]

cluster_url=""
if num_params > 2:
    cluster_url = sys.argv[2]

service="" 
if num_params > 3:
    service = sys.argv[3]

hash_style=""
if num_params > 4:
    hash_style = sys.argv[4]

bbox_size=""
if num_params > 5:
    bbox_size = sys.argv[5]


fin = open(fname)

results = {}

#TimeStamp,Latency,Label,ResponseCode,ResponseMessage,Thread Name,Type,ElapsedTime,Bytes,Encoding
#1699482415700,2181,MapRequestGet,200,OK,Thread Group 1-4,bin,true,60165,2093,UTF-8

for line in fin:
    parts = line.split(',')
    name = parts[2]
    responseCode = parts[3]
    sizeBytes = int(parts[8])
    responseTime = int(parts[9])

    if name not in results:
        results[name] = {}
        results[name]["count"] = []
        results[name]["responseTime"] = []
        results[name]["sizeBytes"] = []

    if responseCode == "200":
        results[name]["count"].append(1)
        results[name]["responseTime"].append(responseTime)
        results[name]["sizeBytes"].append(sizeBytes)
    else:
        results[name]["count"].append(0)


output = []
for name in results:
    averageResponseTime = float(sum(results[name]["responseTime"]))/float(len(results[name]["responseTime"]))
    numberOfSuccess = sum(results[name]["count"])
    totalCalls = len(results[name]["count"])
    averageSizeBytes = float(sum(results[name]["sizeBytes"]))/float(len(results[name]["sizeBytes"])) 
    #print("{}|{:.0f}|{}|{}|{:.0f}".format(name,averageResponseTime,numberOfSuccess,totalCalls,averageSizeBytes))
    result = {}
    result["time"] = datetime.now(timezone.utc)
    result["cluster_url"] = cluster_url
    result["service"] = service
    result["hashStyle"] = hash_style
    result["bboxSize"] = bbox_size
    result["name"] = name
    result["averageResponseTime"] = averageResponseTime
    result["numberOfSuccess"] = numberOfSuccess
    result["totalCalls"] = totalCalls
    result["averageSizeBytes"] = averageSizeBytes
    output.append(result)
fin.close()

#print(json.dumps(output)) # Fails when using datetime.now
print(output)

prom_gateway = os.getenv("PROM_GATEWAY")
prom_metric = os.getenv("PROM_METRIC")

if prom_gateway is not None:
    for result in output:
        suffix = (result["hashStyle"] + "_" + result["bboxSize"]).lower()
        registry = CollectorRegistry() 
        g = Gauge(prom_metric, 'Average Response Time', registry=registry)
        g.set(result["averageResponseTime"])
        push_to_gateway(prom_gateway, job=suffix, registry=registry)


pg_host = os.getenv("PG_HOST")

if pg_host is not None:

    pg_db = os.getenv("PG_DB")
    pg_username = os.getenv("PG_USERNAME")
    pg_password = os.getenv("PG_PASSWORD")
    pg_port = os.getenv("PG_PORT")
    pg_table = os.getenv("PG_TABLE")

    conn = psycopg2.connect(database=pg_db, user=pg_username, password=pg_password, host=pg_host, port= pg_port)
    conn.autocommit = True

    cursor = conn.cursor()

    for result in output:
        suffix = (result["hashStyle"] + "_" + result["bboxSize"]).lower()
        table = (pg_table + "_" + suffix).lower()
        sql = f"SELECT EXISTS ( SELECT 1 FROM pg_tables WHERE tablename = '{table}' ) AS table_existence"
        print(sql)
        cursor.execute(sql)
        exists = cursor.fetchone()
        if not exists[0]: # Table does not exist
            sql = f"CREATE TABLE {table} (time timestamp, {suffix} numeric)"
            print(sql)
            cursor.execute(sql)
        sql = f"INSERT INTO {table}(time, {suffix}) VALUES(%s, %s)"
        print(sql)
        cursor.execute(sql, (result["time"],result["averageResponseTime"]))
