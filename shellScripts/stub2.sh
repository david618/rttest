#!/bin/sh
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi

CLASS=com.esri.rttest.send.Elasticsearch

APP=$(echo "$1" | tr '[:upper:]' '[:lower:]')

case $APP in
				"elasticsearch" | "es")
								CLASS=com.esri.rttest.send.Elasticsearch
								;;
				"kafka":)
								CLASS=com.esri.rttest.send.Kafka
								;;
				"tcp")
								CLASS=com.esri.rttest.send.Tcp
								;;
				"http")
								CLASS=com.esri.rttest.send.Http
								;;
				"elasticindexmon" | "eim")
								CLASS=com.esri.rttest.mon.ElasticIndexMon
								;;
				"featurelayermon" | "flm")
								CLASS=com.esri.rttest.mon.FeatureLayerMon
								;;
				"httpsink")
								CLASS=com.esri.rttest.mon.HttpSink
								;;
				"kafkatopicmon" | "ktm")
								CLASS=com.esri.rttest.mon.KafkaTopicMon
								;;
				"solrcoremon" | "scm")
								CLASS=com.esri.rttest.mon.SolrCoreMon
								;;
				"tcpsink")
								CLASS=com.esri.rttest.mon.TcpSink
								;;
				"timescalesqlmon")
								CLASS=com.esri.rttest.mon.TimescaleSqlMon
								;;
				"websocketsink" | "wss")
								CLASS=com.esri.rttest.mon.WebSocketSink
								;;
	      *)
								echo 
								echo "Send"
								echo "Elasticsearch|es       : Send File to Elasticsearch Index"
								echo "Kafka                  : Send file to Kafka Topic"
								echo "Tcp                    : Send file to Tcp Socket"
								echo "Http                   : Send file to Http Server"
								echo
								echo "Monitor/Sink"
								echo "ElasticIndexMon or eim : Monitor Elasticsearch Index"
								echo "FeatureLayerMon or flm : Monitor Esri Feature Layer"
								echo "HttpSink               : Listen on port for Http Posts"
								echo "KafkaTopicMon or ktm   : Monitor Kafka Topic"
								echo "SolrCoreMon or scm     : Monitor Solr Core"
								echo "TcpSink                : Listen on port for Tcp Messages"
								echo "TimescaleSqlMon or tsm : Monitor TimescaleDB Table"
								echo "WebSocketSink or wss   : Consumme Web Socket"

								exit 1
								;;
esac

shift;

exec "$java" $java_args -cp $MYSELF ${CLASS} "$@"
#echo '''exec "$java" $java_args -cp $MYSELF ${CLASS} "$@"'''

#exit 1 
