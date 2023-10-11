https://bitnami.com/stack/nginx/helm


https://github.com/bitnami/charts/tree/main/bitnami/nginx/#installing-the-chart


```
kubectl create ns kafka
helm -n kafka upgrade --install --values k8s/nginx/values.yaml web oci://registry-1.docker.io/bitnamicharts/nginx
```

## nginx 

```
helm -n kafka install --set controller.ingressClass.name=kafka nginx-kafka oci://ghcr.io/nginxinc/charts/nginx-ingress
```

### Set DNS 

Find Public IP in AWS set config

velokafka.westus2.cloudapp.azure.com


Create issuer and ingress.

kubectl apply -f k8s/nginx/issuer.yaml 
kubectl apply -f k8s/nginx/ingress.yaml





``` 
NAME: nginx-proxy
LAST DEPLOYED: Fri Oct  6 20:47:15 2023
NAMESPACE: kafka
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: nginx
CHART VERSION: 15.3.1
APP VERSION: 1.25.2

** Please be patient while the chart is being deployed **
NGINX can be accessed through the following DNS name from within your cluster:

    nginx-proxy.kafka.svc.cluster.local (port 80)

To access NGINX from outside the cluster, follow the steps below:

1. Get the NGINX URL by running these commands:

  NOTE: It may take a few minutes for the LoadBalancer IP to be available.
        Watch the status with: 'kubectl get svc --namespace kafka -w nginx-proxy'

    export SERVICE_PORT=$(kubectl get --namespace kafka -o jsonpath="{.spec.ports[0].port}" services nginx-proxy)
    export SERVICE_IP=$(kubectl get svc --namespace kafka nginx-proxy -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    echo "http://${SERVICE_IP}:${SERVICE_PORT}"
```

## Custom index.html page

kubectl -n kafka create cm index-html --from-file=index.html


Created files in folder k8s/nginx/html

kubectl -n kafka delete cm index-html 
kubectl -n kafka create cm index-html --from-file=k8s/nginx/html

