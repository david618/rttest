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
