#!/usr/bin/env bash

#source veltest.env
echo "Running Tests on"
echo "CLUSTER_URL=${CLUSTER_URL}"
echo "SERVICE=${SERVICE}"
echo "USERNAME=${USERNAME}"
echo "HASHES=${HASHES}"
echo "SIZES=${SIZES}"

SERVER=$(echo ${CLUSTER_URL} | cut -d'/' -f 1)
PREFIX=$(echo ${CLUSTER_URL} | cut -d'/' -f 2-)

SECS_BETWEEN_TESTS=10

./getToken.py -c ${CLUSTER_URL} -u ${USERNAME} -p ${PASSWORD}
TOKEN=$(cat ~/.itemctl/velocity_shared.token )

./createBboxes.py

for SIZE in ${SIZES}
do
    for HASH in ${HASHES}
    do
        rm -f ${HASH}_results${SIZE}.csv
        echo Testing ${HASH} using ${SIZE} degree bboxes
        sed -e "s/REPLACE_SERVICE/${SERVICE}/g" \
            -e "s/REPLACE_SERVER/${SERVER}/g" \
            -e "s/REPLACE_TOKEN/${TOKEN}/g" \
            -e "s/REPLACE_HASH_STYLE/${HASH}/g" \
            -e "s/SummaryReport/${HASH}_${SIZE}/g" \
            -e "s|REPLACE_PREFIX|${PREFIX}|g" \
            -e "s/REPLACE_BBOX_SIZE/${SIZE}/g" template.jmx > test.jmx
        ~/apache-jmeter/bin/jmeter -n -t test.jmx
        echo "*****"
        cat ${HASH}_${SIZE}.csv
        echo "*****"
        ./calcStats.py ${HASH}_${SIZE}.csv ${CLUSTER_URL} ${SERVICE} ${HASH} ${SIZE} | tee  ${HASH}_${SIZE}.json
        sleep ${SECS_BETWEEN_TESTS}
    done
done
