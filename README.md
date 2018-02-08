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

### Sources
[Elasticsearch](./docs/Elasticsearch.md)

### Notes

Details on how the simFile data (simFile*) in this project are covered in [Create Flight Simulation Data Blog Post](http://davidssysadminnotes.blogspot.com/2016/07/create-flight-simulation-data.html).

Several Python scripts were created to support gathering test results in DCOS.  Details on how these scripts were used are in the [Performance Testing Kafka on DCOS Blog Post](http://davidssysadminnotes.blogspot.com/2016/08/performance-testing-kafka-on-dcos.html)

## License

http://www.apache.org/licenses/LICENSE-2.0 




