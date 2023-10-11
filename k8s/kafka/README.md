## Bitnami Kafka

https://bitnami.com/stack/kafka/helm


```
helm repo add azure-marketplace https://marketplace.azurecr.io/helm/v1/repo
```

```
helm pull azure-marketplace/kafka
```

```
helm install velokafka1 azure-marketplace/kafka
```

Mac Download Kafka tgz and unzip

```
./bin/kafka-topics.sh --bootstrap-server=20.99.165.55:9094 --list
```

```
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server 20.99.165.55:9094 
```

Kafka has no authentication and is not accessible outside of the cluster

- Creates in default namespace
- 1 zookeeper and 1 kafka pod
- Each has an 8G default PVC


## Expose Externally





- Kafka is accessible on the External IP created by Load Balancer 
- No Authentication 
- Accessible from any IP 

## Making Kafka Accessible outside Cluster

Create a values file "values.yaml"; with a few changes for external access. 

```
export NAMESPACE=velocikafka1
kubectl create namespace ${NAMESPACE}
helm --namespace ${NAMESPACE} install --values k8s/kafka/values.yaml kafka azure-marketplace/kafka
```

```
kubectl -n velocikafka1 get svc
NAME                       TYPE           CLUSTER-IP     EXTERNAL-IP      PORT(S)                      AGE
kafka                      ClusterIP      10.0.128.18    <none>           9092/TCP                     28s
kafka-0-external           LoadBalancer   10.0.166.176   20.120.185.252   9094:31478/TCP               28s
kafka-headless             ClusterIP      None           <none>           9092/TCP,9093/TCP            28s
kafka-zookeeper            ClusterIP      10.0.135.16    <none>           2181/TCP,2888/TCP,3888/TCP   28s
kafka-zookeeper-headless   ClusterIP      None           <none>           2181/TCP,2888/TCP,3888/TCP   28s
```

```
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server 20.120.185.252:9094 
```

```
./bin/kafka-topics.sh --bootstrap-server=20.120.185.252:9094  --list 
```

By default the kafka endpoint is available to anyone on Internet.   

### NSG

Modify the NSG to only allow specified IP's.  This was the approach I used for Kafka on a4iot-resources-kafka.

From Azure Portal I removed the rule that allows Internet Access to port 9094.

```
NAME=david-home
SID="844b25fe-7752-4bbd-ba37-ad545aa62be0"

NSG=aks-agentpool-18563280-nsg
NSG_RG=MC_simulators_vel2023_westus2
```

```
IP=$(curl -s ifconfig.me)
```

```
HIGHEST_NSG_PRIORITY=$(az network nsg rule list --subscription ${SID} --nsg-name ${NSG} --resource-group ${NSG_RG} --query [].priority -o tsv | sort -n | tail -n 1)
NEXT_NSG_PRIORITY=$((${HIGHEST_NSG_PRIORITY}+1))
```


```
az network nsg rule create --name ${NAME}  \
                           --subscription ${SID} \
                           --nsg-name ${NSG} \
                           --priority ${NEXT_NSG_PRIORITY} \
                           --resource-group ${NSG_RG} \
                           --access Allow \
                           --description "Allow ${NAME} to 9094" \
                           --destination-address-prefixes '*' \
                           --destination-port-ranges 9094 \
                           --protocol Tcp \
                           --source-address-prefixes ${IP} \
                           --source-port-ranges '*'
```


After this I can still access Kafka from my Computer with my IP; however, I cannot access from computer with different IP. 

Set the name to: velocikafka1.westus2.cloudapp.azure.com (This is done via Public IP in MC Resource Group)


## Using SASL for Authentication

Created values_sasl.yaml.  You can specify username and password in the values file. 

```
export NAMESPACE=velocikafka2
kubectl create namespace ${NAMESPACE}
helm --namespace ${NAMESPACE} install --values k8s/kafka/values_sasl.yaml kafkasasl azure-marketplace/kafka
```

```
kubectl -n ${NAMESPACE} get svc
NAME                           TYPE           CLUSTER-IP     EXTERNAL-IP     PORT(S)                      AGE
kafkasasl                      ClusterIP      10.0.234.225   <none>          9092/TCP                     2m33s
kafkasasl-0-external           LoadBalancer   10.0.228.93    20.69.141.225   9094:32767/TCP               2m33s
kafkasasl-headless             ClusterIP      None           <none>          9092/TCP,9093/TCP            2m33s
kafkasasl-zookeeper            ClusterIP      10.0.230.199   <none>          2181/TCP,2888/TCP,3888/TCP   2m33s
kafkasasl-zookeeper-headless   ClusterIP      None           <none>          2181/TCP,2888/TCP,3888/TCP   2m33s
```

helm --namespace ${NAMESPACE} upgrade --install --values k8s/kafka/values_sasl.yaml kafkasasl azure-marketplace/kafka

cat config.properties
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="user" password="password1";
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN


./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server 20.69.141.225:9094 --command-config config.properties

./bin/kafka-topics.sh --bootstrap-server=20.69.141.225:9094 --list --command-config config.properties
quickstart-events


./bin/kafka-console-consumer.sh --bootstrap-server=20.69.141.225:9094 --topic planes --consumer.config config.properties


The kafkaSend code does not support SASL; etc.  A small change allowed me to send using sendKafka.

david62243/rttest-send:230928
Extra parameters:  -u username -p password 
Defaults to SASL Plaintext. 

Set the name to: velocikafka2.westus2.cloudapp.azure.com (This is done via Public IP in MC Resource Group)

## TLS

https://docs.bitnami.com/kubernetes/infrastructure/kafka/administration/enable-tls/

Created a values file for TLS only 

values_tls.yaml

I opted to allow the helm chart to create a self-signed cert. 

```
export NAMESPACE=velocikafka3
kubectl create namespace ${NAMESPACE}
helm --namespace ${NAMESPACE} install --values k8s/kafka/values_tls.yaml kafkatls azure-marketplace/kafka
```


```
kubectl -n velocikafka3  get secret kafkatls-0-tls -o jsonpath="{.data.tls\.crt}" | base64 --decode > cert.pem
kubectl -n velocikafka3  get secret kafkatls-0-tls -o jsonpath="{.data.ca\.crt}" | base64 --decode > ca.pem
kubectl -n velocikafka3  get secret kafkatls-0-tls -o jsonpath="{.data.tls\.key}" | base64 --decode > key.pem
```


```
cat cert.pem ca.pem > truststore.pem
```

```
./bin/kafka-topics.sh --bootstrap-server=20.72.196.147:9094 --list --command-config config-tls.properties
```

At this point the client complained that the cert could not be verified. 

```
openssl s_client -debug -connect 20.72.196.147:9094 -tls1_2 -showcert-tls1_2 -showcerts
```

This showed the CN as kafkatls-0.kafkatls-headless.  I tried adding an /etc/host 


```
./bin/kafka-topics.sh --bootstrap-server=kafkatls-0.kafkatls-headless:9094 --list --command-config config-tls.properties
```
Still didn't work.

Adding identification algorithm and setting it to "" tells the client to not try to validate the cert.

```
cat config-tls.properties
```

```
security.protocol=SSL
ssl.truststore.type=PEM
ssl.truststore.location=truststore.pem
ssl.endpoint.identification.algorithm=
```

```
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server 20.72.196.147:9094 --command-config config-tls.properties
```

Velocity does not support tls without password authentication.   I tried to use SASL and gave it a fake name; no "joy".

# SASL TLS 

For next attempt I did sasl and tls. 

values_sasl_tls.yaml

```
export NAMESPACE=velocikafka4
kubectl create namespace ${NAMESPACE}
helm --namespace ${NAMESPACE} install --values k8s/kafka/values_sasl_tls.yaml kafkasasltls azure-marketplace/kafka
```

52.137.89.46

```
./bin/kafka-topics.sh --bootstrap-server=52.137.89.46:9094 --list --command-config config-sasl-tls.properties
```

```
kubectl -n velocikafka4  get secret kafkasasltls-0-tls -o jsonpath="{.data.tls\.crt}" | base64 --decode > cert-sasltls.pem
kubectl -n velocikafka4  get secret kafkasasltls-0-tls -o jsonpath="{.data.ca\.crt}" | base64 --decode > ca-sasltls.pem
```

```
cat cert-sasltls.pem ca-sasltls.pem > truststore-sasltls.pem
```

```
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server 52.137.89.46:9094 --command-config config-sasl-tls.properties
./bin/kafka-topics.sh --bootstrap-server=52.137.89.46:9094 --list --command-config config-sasl-tls.properties
```

```
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="david" password="password1";
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
ssl.truststore.type=PEM
ssl.truststore.location=truststore-sasltls.pem
ssl.endpoint.identification.algorithm=
```

I was able to create a topic and list the topic. 

However, from velocity still got an error.

Velocity reports an error "Failed to connect to feed."

Looking at a4iot-cluster-service sampler logs.

```
[2023-10-05 02:16:36.473] [ERROR] [org.apache.kafka.clients.NetworkClient] [Consumer clientId=consumer-null-7, groupId=null] Connection to node -1 (52.137.89.46/52.137.89.46:9094) failed authentication due to: SSL handshake failed :::LF:::
[2023-10-05 02:16:36.474] [ERROR] [controllers.SamplerFeedController] [2023-10-05T02:16:36.474267Z] [controllers.SamplerFeedController] [unknown] [] [] [cqvgkj9zrnkn9bcu] [] [velocity_shared] [user] [ITEM_MANAGER__TEST_CONNECTION_FEED_FAILED] [] Failed to connect to feed.:::LF::: :::LF:::
[2023-10-05 02:18:01.591] [ERROR] [org.apache.kafka.clients.NetworkClient] [Consumer clientId=consumer-null-8, groupId=null] Connection to node -1 (52.137.89.46/52.137.89.46:9094) failed authentication due to: SSL handshake failed :::LF:::
[2023-10-05 02:18:01.593] [ERROR] [controllers.SamplerFeedController] [2023-10-05T02:18:01.593202Z] [controllers.SamplerFeedController] [unknown] [] [] [cqvgkj9zrnkn9bcu] [] [velocity_shared] [user] [ITEM_MANAGER__TEST_CONNECTION_FEED_FAILED] [] Failed to connect to feed.:::LF::: :::LF:::
```

It appears Velocity is having the same problem I had; it's failing to identify the ssl cert.  Doesn't trust this self signed cert. 

It appears I need to figure out how to create a use a valid cert for both TLS and SASL_TLS. 

I can give them names. 


velocikafka3.westus2.cloudapp.azure.com
velocikafka4.westus2.cloudapp.azure.com

I just need to get a cert.  Perhaps certmanager / LetsEncrypt. 

istio or nginx-controller to automate creation/update of tls cert from letsencrypt.
