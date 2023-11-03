# Kafka Server on Kubernetes

Documents how Kafka was deployed on Kubernetes to support testing. 

Benefits of Running on Kubernetes
- Several Kafka's with different configurations 
- Senders running as pods on the cluster  (see rttest-send folder)

## Create Cluster

https://learn.microsoft.com/en-us/cli/azure/aks?view=azure-cli-latest#az-aks-create()

```
SID="84..."
RG=simulators
CLUSTER=vel2023
VMSIZE=Standard_D2s_v3
LOCATION=westus2
```

```
az group create --subscription ${SID} --name ${RG} --location ${LOCATION}
```

```
az aks create \
    --subscription ${SID} \
    --resource-group ${RG} \
    --name ${CLUSTER} \
    --node-vm-size ${VMSIZE} \
    --min-count 2 \
    --max-count 5 \
    --enable-cluster-autoscaler
```

```
az aks get-credentials \
  --subscription ${SID} \
  --resource-group ${RG} \
  --name ${CLUSTER} \
  -f ${HOME}/${RG}-${CLUSTER}.kubeconfig
```

```
export KUBECONFIG=${HOME}/${RG}-${CLUSTER}.kubeconfig
```

```
kubectl get nodes
NAME                                STATUS   ROLES   AGE     VERSION
aks-nodepool1-34630476-vmss000000   Ready    agent   6m52s   v1.26.6
aks-nodepool1-34630476-vmss000001   Ready    agent   6m34s   v1.26.6
aks-nodepool1-34630476-vmss000002   Ready    agent   6m40s   v1.26.6
```

## Install Kafka

Bitnami maintains a helm chart for installing Kafka on Kubernetes. 
https://bitnami.com/stack/kafka/helm


### Tale of Two Versions of Kafka

Initially I installed using 

```
helm repo add azure-marketplace https://marketplace.azurecr.io/helm/v1/repo
```

A couple of weeks after setup/install the Azure Marketplace helm repo stopped working. 

When I looked at the Bitnami repo. 

```
helm install my-release oci://registry-1.docker.io/bitnamicharts/kafka
```

I realized the Azure helm chart "latest" version was 19.1.4 with Kafka 3.3.1.   

The latest from Bitnami helm chart version 26.2.0 installing Kafka 3.6.0 

The values had also changed so what I created for version 3.3 did not work with 3.6.

Additionally Version 3.6 no longer includes zookeeper. 


### Exposing Kafka to Internet with SSL Certificates 

**NOTE:** Tried using LoadBalancer Service type. Each kafka would get it's own public IP and would require some additional integration (perhaps LetsEncrypt) to create a verified SSL Certificate.

Using nginx and nginx-ingress controller (seed nginx folder) a valid SSL Cert is created and maintained.

The Kafka's are configured using Kubernetes NodePort which allows access via the Nodes on ports from 30000-32767.  

Once a kafka is deployed we can use the "kubernetes" Load Balancer to route external ports to the nodes.

Additionally we'll add ingress rules to the Network Security Group (NSG) to allow access from select IP's to the external IP with DNS https://velokafka.westus2.cloudapp.azure.com/

### Downloading Helm Charts

To pull old helm chart I originally got from Azure Marketplace.

```
helm pull oci://registry-1.docker.io/bitnamicharts/kafka --version 19.1.4
```

This creates a tar zip file kafka-19.1.4.tgz.  Extracted in helm folder and renamed folder to kafka33.

```
helm pull oci://registry-1.docker.io/bitnamicharts/kafka 
```

This created a tar zip file kafka-26.2.0.tgz.  Extracted in helm folder and renamed folder to kafka36. 


### Install Kafka

After building the values file each Kafka was installed using a command like. 

```
helm -n kafka upgrade --install --values k8s/kafka/values/33/nodeport-values.yaml velokafka1 k8s/kafka/helm/kafka
```

- Customized to use Kubernetes Nodeport of specific value (e.g. 30009)
- Set Listeners to point client to extra Kafka DNS name (e.g. velokafka.westus2.cloudapp.azure.com name set during nginx install )
- Added a "Load Balancing Rule" to "kubernetes" Load Balancer in Manage Cluster Resource Group. 
  - velokafka1 
  - Connected the velokafka IP to kubernetes backend 
  - Connected port 30009 on load balancer to 30009 (node port)
  - Used health probe with same name as Frontend IP address
  - Enabled TCP Reset 
- Added an "Inbound Security Rule" to NSG for aks-nodegroup 
  - Allow access to 30009 to specific IP's 
  - For Services secured with authentication you can add a rule to open to any IP if desired 
  - For test clusters you can used the script in the devops repo devops/scripts/test/add-nsg.sh (e.g. ./test/add-nsg.sh clustername simulators-cluster).  This script will add an NSG rule to allow access to Kafka ports

## Repeated Kafka install for various configurations.

- PLAINTEXT : no authentication , no encryption 
- SASL: authentication, no encryption
- TLS or SSL: no authentication, encryption 
- SASL_TLS or SALS_SSL: authentication, encryption

**NOTE:** The helm chart changed the label TLS to SSL from 3.3 to 3.6.  

## Ports 

- 30009: Kafka 3.3 PLAINTEXT
- 30008: Kafka 3.3 TLS
- 30007: Kafka 3.3 SASL
- 30006: Kafka 3.3 SASL_TLS

- 30109: Kafka 3.6 PLAINTEXT
- 30108: Kafka 3.6 SSL
- 30107: Kafka 3.6 SASL
- 30106: Kafka 3.6 SASL_SSL

**NOTE:** The certificate returned by Kafka 3.3 (TLS and SASL_TLS) did not include the Certificate chain.  I've added instructions on how to download the certificate so the client can include that in the requests.

The certificate returned by Kafka 3.6 includes the certificate chain you can consume these services without providing the truststore. 

The helm install command for each install is included as a comment in the values files.


## Stop/Start Kafka's

```
kubectl -n kafka get sts
NAME                      READY   AGE
velokafka1                1/1     5d22h
velokafka1-zookeeper      1/1     5d22h
velokafka2                0/0     13d
velokafka2-zookeeper      0/0     13d
velokafka3                0/0     17d
velokafka3-zookeeper      0/0     17d
velokafka361-controller   1/1     2d4h
velokafka362-controller   1/1     2d3h
velokafka363-controller   1/1     28h
velokafka364-controller   1/1     2d1h
velokafka4                0/0     16d
velokafka4-zookeeper      0/0     16d
```

```
kubectl -n kafka scale sts velokafka2 --replicas 1
kubectl -n kafka scale sts velokafka2-zookeeper --replicas 1
```


## Example Client   

### 30009: Kafka 3.3 PLAINTEXT velokafka1

The ip of the client must be allowed via the Azure NSG.

#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30009 -t planes -f planes.csv -r 10 -n -1 
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30009 -t planes -n 1 -r 10 
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30009 --topic planes
```

#### Velocity

You can use add-nsg.sh script in devops repo to add a NSG rule to allow connection from a cluster.

The following adds a rule to allow the nodes on cluster dj0916a to connect to kafka ports on simulators-cluster.

```
./test/add-nsg.sh simulators-cluster dj0916a
```


### 30008: Kafka 3.3 TLS velokafka

#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30008 -t planes -f planes.csv -r 10 -n -1 -s /Users/davi5017/kafka_2.13-3.3.1
/velokafka.pem
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30008 -t planes -n 1 -r 10  -s /Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30008 --topic planes --consumer.config config-tls.properties
```

#### Velocity 

Velocity will not connect. There is no way at this time to supply the pem.

### 30007: Kafka 3.3 SASL velokafka3

#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30007 -t planes -f planes.csv -r 10 -n -1 -u user -p LongPasswordHere
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30007 -t planes -n 1 -r 10 -u user -p LongPasswordHere
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30007 --topic planes --consumer.config config-david.properties
```

#### Velocity 

You can connect with username and password.  An NSG rule must allow connection to port 30007.

Be sure to disable SSL.


### 30006: Kafka 3.3 SASL_TLS velokafka4


#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30006 -t planes -f planes.csv -r 10 -n -1 -u user -p LongPasswordHer -s /Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30006 -t planes -n 1 -r 10 -u user -p LongPasswordHere -s /Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30006 --topic planes --consumer.config config-david-tls.properties
```

#### Velocity

Velocity will not connect.

### 30109: Kafka 3.6 PLAINTEXT velokafka361


#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30109 -t planes -f planes.csv -r 10 -n -1 
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30109 -t planes -n 1 -r 10 
```

#### kafka command line

**NOTE:** The kafka-console tool for some reason when consuming requests --partition 0. 

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30109 --topic planes --partition 0
```

#### Velocity

You can use add-nsg.sh script in devops repo to add a NSG rule to allow connection from a cluster.

The following adds a rule to allow the nodes on cluster dj0916a to connect to kafka ports on simulators-cluster.

```
./test/add-nsg.sh simulators-cluster dj0916a
```

###  30108: Kafka 3.6 SSL velokafka362

Since the cert include the chain; you can send request with nocert.

#### rttest

```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30108 -t planes -f planes.csv -r 10 -n -1 -s nocert
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30108 -t planes -n 1 -r 10  -s nocert
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30108 --topic planes --consumer.config config-tls.properties --partition 0
```

#### Velocity 

Velocity seems to connect but reports the messages as invalid format. Perhaps a bug in Velocity code.


###  30107: Kafka 3.6 SASL velokafka363

#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30107 -t planes -f planes.csv -r 10 -n -1 -u user -p LongPasswordHere
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30107 -t planes -n 1 -r 10 -u user -p LongPasswordHere
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30107 --topic planes --consumer.config config-david.properties --partition 0
```

#### Velocity 

You can connect with username and password.  An NSG rule must allow connection to port 30007.

Be sure to disable SSL.


###  30106: Kafka 3.6 SASL_SSL velokafka364


#### rttest
```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:30106 -t planes -f planes.csv -r 10 -n -1 -u user -p LongPasswordHere -s nocert
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:30106 -t planes -n 1 -r 10 -u user -p LongPasswordHere -s nocert
```

#### kafka command line

```
./bin/kafka-console-consumer.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:30106 --topic planes --consumer.config config-david-tls.properties --partition 0
```

#### Velocity

Velocity seems to connect but reports the messages as invalid format. Perhaps a bug in Velocity code.
