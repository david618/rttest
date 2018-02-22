#!/bin/bash

if [ "$#" -ne 5 ];then
        echo "Usage: $0 <server:port> <file> <ratePerThread> <numToSendPerThread> <numThreads>"
        echo "Example: $0 a91:9092 planes.csv 250000 5000000 8"
        exit 99
fi


SERVER=$1
FILE=$2
RATE=$3
NUMSEND=$4
NUMTHRDS=$5


for i in $(seq 1 $NUMTHRDS); do
  java -cp target/rttest.jar com.esri.rttest.send.Tcp2 $SERVER $FILE $RATE $NUMSEND > ~/runTcpTest_${i}.dat 2>&1 &
done
