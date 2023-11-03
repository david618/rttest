# nginx

Installing nginx in the kafka namespace.  We'll configure nginx to automatically retrieve a valid SSL Certificate using CertManager/LetsEncrypt.

The cert will used by kafka for TLS on some of the Kafka variants. 


https://bitnami.com/stack/nginx/helm


https://github.com/bitnami/charts/tree/main/bitnami/nginx/#installing-the-chart


```
kubectl create ns kafka
helm -n kafka upgrade --install --values k8s/nginx/values.yaml web oci://registry-1.docker.io/bitnamicharts/nginx
```

## nginx ingress controller 

```
helm -n kafka install --set controller.ingressClass.name=kafka nginx-kafka oci://ghcr.io/nginxinc/charts/nginx-ingress
```

## Set DNS Name

Find Public IP in AWS Managed Cluster and set name (e.g. velokafka).  The rest of the domain will be set by the region you created the K8S cluster in.

```
velokafka.westus2.cloudapp.azure.com
```

## cert-manager 

```
helm repo add jetstack https://charts.jetstack.io
helm repo update
```

```
helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.13.1 \
  --set installCRDs=true
```

## Create issuer and ingress.

```
kubectl apply -f k8s/nginx/issuer.yaml 
kubectl apply -f k8s/nginx/ingress.yaml
```

## Custom index.html page

### Single index.html

```
kubectl -n kafka create cm index-html --from-file=index.html
```

### Index Page and other docs

Created web files in folder k8s/nginx/html

```
kubectl -n kafka delete cm index-html 
kubectl -n kafka create cm index-html --from-file=k8s/nginx/html
```

These are very basic index pages; but you get the idea.



