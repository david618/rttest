# rttest

Real Time Test (rttest) provides tools for sending lines (producers), receiving data (sinks), and monitoring data stores (monitors). 


## Installation

### Software Prerequsites

You must have java, maven, and git installed. 

#### Linux (Red Hat or CentOS) 
```
sudo yum -y install epel-release
sudo yum -y install git
sudo yum -y install java-1.8.0-openjdk
sudo yum -y install maven
```

#### Linux (Ubuntu)



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

```
git clone https://github.com/david618/rttest
cd rttest
mvn install 
```

After Build; the target folder will contain:
- lib folder: all of the jar depdencies
- rttest.jar: small executable jar without dependencies.
- rttest-full.jar: full executable jar with dependencies.


## Data

Small sample `planes.csv` data is included.  This file has 100,000 lines.

The data file was created using [planes](https://github.com/david618/planes). 


Fields
- id : Plane ID : integer
- ts : Epoch Time (ms) : long
- speed : Speed (km/s) : double
- dist : Distance to next dest (km) : double
- bearing : Bearing (degrees) measured from North : double
- rtid : Route ID (Multiple Planes can be on same route : integer
- orig : Origin airport name for this segment of the route : text
- dest : Destination airport name for this segment of the route : text
- secsToDep : Number of seconds before the plane with leave orig : integer
- lon : Current WGS84 longitude of plane
- lat : Current WGS84 latitude of plane


## Running the Tools

You'll need to have Java installed.  You can use [OpenJDK](https://openjdk.java.net/install/).

Other Java's should work too.  Development was done on OpenJDK.

```
java -jar target/rttest.jar
```

Outputs a list of the tools and description of each tool.

The individual tools are ran using.

```
java -cp target/rttest.jar com.esri.rttest.mon.Elasticsearch
```

For example:

```
java -cp target/rttest.jar com.esri.rttest.mon.ElasticsearchMon
```

If a tool is ran without arguments it will output a list of arguments.

For example:

```
java -cp target/rttest.jar com.esri.rttest.mon.ElasticIndexMon
Usage: ElasticIndexMon [ElasticsearchUrl] (sampleRateSec=10) (numSampleEqualBeforeExit=1) (username) (password)
Example: java -cp target/rttest.jar com.esri.rttest.mon.ElasticIndexMon http://coordinator.sats-ds01.l4lb.thisdcos.directory:9200/planes/planes 20 elasic changeme
```


The tools are in two major groups Monitor (mon) and Senders (send). 

For more details

## Monintor (mon)
These tools montior counts or listen for messages and report rates. 
- [ElasticIndexMon](docs/mon/ElasticIndexMon.md) : Monitor count and rate for Elasticsearch Index.
- [FeatureLayerMon](docs/mon/FeatureLayerMon.md) : Monitor count and rate for Feature Layer.
- [KafkaTopicMon](docs/mon/KafkaTopicMon.md) : Monitor count and rate for Kafka Topic.
- [SolrIndexMon](docs/mon/SolrIndexMon.md) : Monitor count and rate for Solr Index.
- [TcpSink](docs/mon/TcpSink.md) : Receive lines on a TCP port; report count and rate.
- [TimescaleSqlMon](docs/mon/TimescaleSqlMon.md) : Monitor count and rate for Timescale DB Hypertable.
- [WebSocketSink](docs/mon/WebSocketSink.md) : Consume lines from WebSocket; report count and rate.

## Senders (send)
These tools send lines from a file.
- [Elasticsearch](docs/send/Elasticsearch.md) : Send lines to Elasticsearch using HTTP API.
- [Http](docs/send/Http.md) : Send lines to server using HTTP POST.
- [Kafka](docs/send/Kafka.md) : Send lines to Kafka topic.
- [Tcp](docs/send/Tcp.md) : Send lines to Server to TCP port.




## Docker

The code is also available as a Docker image.

### Using Docker Image

```
docker run -it david62243/rttest-mon:20190718 tmux
```

The image includes tmux (Terminal Multiplexer).  You could also use bash if you prefer.

Once you are in the docker image ``cd /opt/rttest``

From there you can run the rttest tools.



The images are available on Docker Hub.
- [rttest-mon](https://cloud.docker.com/u/david62243/repository/docker/david62243/rttest-mon)
- [rttest-send](https://cloud.docker.com/u/david62243/repository/docker/david62243/rttest-send)

rttest-send includes addition larger planes datasets. 
- planes00000 - 5 million lines; base planes schema with additional hash fields 
  - geohash 
  - square Hash 
  - Pointy Triangle Hash
  - Flat Triangle Hash  
- planes00001 - 5 million lines; planes schema 

**NOTE:** rttest-send is much larger (~1GB) compared to rttest-mon (~200MB).

### How Docker Image was Created

For example, from project root run docker build.

```
docker build -t david62243/rttest-mon:20190718 -f docker/Dockerfile-rttest-mon .
``` 

This will build image with tag: ``david62243/rttest-mon:20190718``.  The tage includes a version number. In this case 20190718 (Date Tag).


Then pushed the image to Docker Hub

```$xslt
docker login
docker push david62243/rttest-mon:20190718
```


## License

http://www.apache.org/licenses/LICENSE-2.0 




