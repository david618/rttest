# rttest

Real Time Test (rttest) provides tools for sending lines (producers), receiving data (sinks), and monitoring data stores (monitors). 

## Installation

### Prerequsites
You must have java, maven, and git installed. 

#### Linux 
<pre>
# yum -y install epel-release
# yum -y install git
# yum -y install java-1.8.0-openjdk
# yum -y install maven
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



## License

http://www.apache.org/licenses/LICENSE-2.0 




