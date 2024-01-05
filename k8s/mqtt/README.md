# Deploy mqtt

## Password Creation

```
brew install mosquitto 
mosquitto_passwd -c test  david
```

The file "test" is created you can copy the hashed password from here and replace it in mosquitto-password.yaml


## Create mqtt Deployment

```
kubectl create ns mqtt
```

From k8s folder.

```
kubectl -n mqtt apply -k mqtt
```

## Access from Internet 

Added NSG rule to allow port 31883 to everyone.

Added Route to kubernetes Load balancer to forward 31883 to 31883 on the Kubernetes nodes.


```
./sendMqtt -f planes.json -h tcp://velokafka.westus2.cloudapp.azure.com:31883 -r 20 -t planes -q 2 -n -1 -u david -p **REDACTED**
```


## Create Secret

USERNAME=david
PASSWORD=**REDACTED**

kubectl -n rttest-send create secret generic mqtt \
    --from-literal=username=${USERNAME} \
    --from-literal=password=${PASSWORD}
