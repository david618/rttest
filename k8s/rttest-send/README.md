# Send Data to Kafka

Each Deployment uses the rttest-send Container Image to send data to Kafka

## Set Context

```
export KUBECONFIG=/users/davi5017/simulators-vel2023.kubeconfig
```

## Create Namespace


```
NAMESPACE=rttest-send
kubectl create ns ${NAMESPACE}
```

## Create PVC 

Storage Class: azurefile-premium

Created as RWX which will allow the same PVC to be mounted by many pods. 

```
kubectl -n ${NAMESPACE} apply -f k8s/rttest-send/pvc.yaml
```

## Load Data to PVC

```
kubectl -n ${NAMESPACE} apply  -f k8s/rttest-send/deployment.yaml
```


Used "AKI...UNEWN" AWS Key.  Same key we use for test services.  

I have a copy of the secret key in ~/.itemctl/storage_account_keys.txt


Mounts the PVC.

```
kubectl -n simulators exec -it send-planes100-94745c47b-4dnlj  -- bash
```


```
apt update
apt install awscli
aws configure
AKI...UNEWN
**REDCATED**
```


```
aws s3 ls s3://esriplanes/lat88/
```

```
for i in {1..9}; do
  aws s3 cp s3://esriplanes/lat88/planes0000${i} . 
done
```

```
for i in {10..20}; do
  aws s3 cp s3://esriplanes/lat88/planes000${i} . 
done
```

```
mkdir planes
cd planes
for i in {1..9}; do
  split  ../planes0000${i} -l 1000000 -d planes00${i}
done
for i in {10..20}; do
  split  ../planes000${i} -l 1000000 -d planes0${i}
done
```

Other datasets could be loaded; as needed.  The PVC has 128G of space; but can be expanded if needed. 

Each of the files after split have 1,000,000 rows and there are 200 files.

At 1,000/s this would last 2,000 seconds before repeating or about 30 minutes. 



```
java -cp target/rttest.jar com.esri.rttest.send.Kafka a4iot-resources-kafka.westus2.cloudapp.azure.com:9092 planes1x50 /opt/kafka/azureuser/lat88_csv/planes 50 -1
java -cp target/rttest.jar com.esri.rttest.send.Kafka a4iot-resources-kafka.westus2.cloudapp.azure.com:9092 planes1x100 /opt/kafka/azureuser/lat88_csv/planes 100 -1
java -cp target/rttest.jar com.esri.rttest.send.Kafka a4iot-resources-kafka.westus2.cloudapp.azure.com:9092 planes1x500 /opt/kafka/azureuser/lat88_csv/planes 500 -1
```
## rttest-send

```
mvn install 
```

```
docker build -t david62243/rttest-send:230923 -f docker/Dockerfile-rttest-send  .
```

```
docker push david62243/rttest-send:230923 
```

```
java -cp target/rttest.jar com.esri.rttest.send.Kafka a4iot-resources-kafka.westus2.cloudapp.azure.com:9092 planes1x500 /opt/kafka/azureuser/lat88_csv/planes 500 -1
```

```
java -cp target/rttest.jar com.esri.rttest.send.Kafka velokafka1.default:9092 planes planes.csv 10 -1
```

```
./sendKafka  -b velokafka1.default:9092 -t planes -f planes.csv -r 10 -n -1 
```

Consume Topic

```
./bin/kafka-console-consumer.sh --bootstrap-server=20.99.165.55:9094 --topic planes
```

velokafka1.westus2.cloudapp.azure.com

## MQTT



PASSWORD=**REDACTED**

```
./sendMqtt -f planes.json -h tcp://velokafka.westus2.cloudapp.azure.com:31883 -r 10 -t planes -n -1 -u david -p ${PASSWORD}
```

```
./monMqtt  -h tcp://velokafka.westus2.cloudapp.azure.com:31883 -t planes -u david -p ${PASSWORD} -r 10 -n 1
```
