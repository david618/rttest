# rttest

Real Time Test (rttest) provides tools for sending lines (producers), receiving data (sinks), and monitoring data stores (monitors). 


## Installation

### Software Prerequsites

You must have java, maven, and git installed. 

#### Linux 
<pre>
sudo yum -y install epel-release
sudo yum -y install git
sudo yum -y install java-1.8.0-openjdk
sudo yum -y install maven
</pre>

#### Windows 
- Install [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
  -- Java SE Development Kit Windows x64.  
  -- Accept Defaults for installs.
  -- Open Windows Advanced System Settings; Environemnt Variables
  -- Create a new System Variable; Variable name: JAVA_HOME, Variable value: C:\Program Files\Java\jdk1.8.0_121.  
  -- NOTE: The last number should match the version of Java you installed. You can use Explorer and navigate to the folder. then cut and paste the value.
- Install [Git](https://git-scm.com/download/win) Download 64-bit Git for Windows Setup. Defaults for all installation dialogs. 
  -- You can use defaults during install
- Install [Maven](https://maven.apache.org/download.cgi)
  -- Download the Binary zip archive and unzip (I usually put it in C:\) 
  -- Open Windows Advanced System Settings; Environemnt Variables
  -- Edit the Path variable and append Maven to the path. Append a semicolon followed by path to Maven bin folder (e.g. ;C:\apache-maven-3.5.0\bin).  Cut and paste is helpful.

### Build rttest

<pre>
git clone https://github.com/david618/rttest
cd rttest
mvn install 
</pre>

After Build; the target folder will contain:
- lib folder: all of the jar depdencies
- rttest.jar: small executable jar without dependencies.
- rttest-big.jar: big executable jar with dependencies.

### Senders (send)
These tools send lines from a file.
- [ElasticsearchHttp](./docs/ElasticsearchHttp.md) : Send lines to Elasticsearch using HTTP API.
- [ElasticsearchTrans](./docs/ElasticsearchTrans.md) : Send lines to Elasticsearch using Transport API.
- [Http](./docs/Http.md) : Send lines to server using HTTP POST.
- [Kafka](./docs/Kafka.md) : Send lines to Kafka topic.
- [Tcp](./docs/Tcp.md) : Send lines to Server to TCP port.

### Sinks (sinks)
These tools consume lines.
- [TcpSink](./docs/TcpSink.md) : Receive lines on a TCP port; report count and rate.
- [WebSocketSink](./docs/WebSocketSink.md) : Consume lines from WebSocket; report count and rate.

### Monintor (mon)
These tools montior counts and report changes.
- [ElasticIndexMon](./docs/ElasticIndexMon.md) : Monitor count and rate for Elasticsearch Index.
- [FeatureLayerMon](./docs/FeatureLayerMon.md) : Monitor count and rate for Feature Layer.
- [KafkaTopicMon](./docs/KafkaTopicMon.md) : Monitor count and rate for Kafka Topic.
- [SolrIndexMon](./docs/SolrIndexMon.md) : Monitor count and rate for Solr Index.



### Data

Small sample `planes.csv` data is included.  This file has 100,000 lines.

The data file was created using [planes](https://github.com/david618/planes). 



## License

http://www.apache.org/licenses/LICENSE-2.0 




