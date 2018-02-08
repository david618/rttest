# rttest

Real Time Test (rttest) provides tools for sending lines (producers), receiving data (sinks), and monitoring data stores (monitors). 

## Installation

### Prerequsites
You must have java, maven, and git installed. 

For example in CentOS:
<pre>
# sudo yum install epel-release 
# sudo yum install git maven
</pre>

First line installs the "Extra Packages for Enterprise Linux" repository to yum.

Second line install git and maven.  This install will install java also if it's not installed.

### Build rttest

<pre>
$ git clone https://github.com/david618/rttest
$ cd rttest
$ mvn install 
</pre>

After Build; the target folder will contain:
- lib folder: all of the jar depdencies
- rttest.jar: small executable jar without dependencies.
- rttest-big.jar: big executable jar with dependencies.

### Producers
These tools send lines from a file.
- [Elasticsearch](./docs/Elasticsearch.md)
- [Http](./docs/Http.md)
- [Kafka](./docs/Kafka.md)
- [Tcp](./docs/Tcp.md)

### Sinks
These tools consume lines.
- [TcpSink](./docs/TcpSink.md)
- [WebSocketSink](.docs/WebSocketSink.md)

## Monitors
These tools montior counts and report changes.
- [ElasticIndexMon](./docs/ElasticIndexMon.md)
- [FeatureLayerMon](./docs/FeatureLayerMon.md)
- [KafkaTopicMon](./docs/KafkaTopicMon.md)


### Data

Small sample `planes.csv` data is included.  This file has 100,000 lines.

The data file was created using [planes](https://github.com/david618/planes). 

Larger data files
- 1 Million lines: https://s3.amazonaws.com/esriplanes/planes00001.1M
- 10 Million lines: https://s3.amazonaws.com/esriplanes/planes00001


## License

http://www.apache.org/licenses/LICENSE-2.0 




