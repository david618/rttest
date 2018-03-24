#!/bin/bash

if [ "$#" -ne 6 ];then
        echo "Usage: $0 <broker> <topic> <file> <ratePerThread> <numToSendPerThread> <numThreads>"
        echo "Example: $0 a91:9092 planes planes.csv 250000 5000000 8"
        exit 99
fi


SERVER=$1
TOPIC=$2
FILE=$3
RATE=$4
NUMSEND=$5
NUMTHRDS=$6


for i in $(seq 1 $NUMTHRDS); do
  java -cp target/rttest.jar com.esri.rttest.send.Kafka $SERVER $TOPIC $FILE $RATE $NUMSEND > runKafkaTest_${i}.dat 2>&1 &
done
