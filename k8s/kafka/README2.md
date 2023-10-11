helm -n kafka upgrade --install velokafka1 azure-marketplace/kafka


- Create kafka ns (velkafka)
  - Each kafka unique ip's 6092,7092,8092,9092 (6094, 7094, 8094, 9094) 
  - On ingress for all kafka
  - Put senders in same namesapce as kafka (sendplanes100k1, sendplanes50k2; etc.)


ingress only does port 80 and 443

Possibly run Kafka on Nodeport (30094, 30095, 30096, 30097);

Then use a proxy VM to provide external access to the nodes: 9094, 9095, 9096, 9097
nginx with certmanager / letsencrypt 


### Install

helm -n kafka upgrade --install --values k8s/kafka/nodeport-values.yaml velokafka1 azure-marketplace/kafka


### Test with rttest plaintext (no authentication no encryption) 

```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:9092 -t quickstart-events -f planes.csv -r 10 -n -1
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:9092 -n 1 -t quickstart-events -r 10
```


## kafka tls

values file nodeport-tls-values.yaml


```
helm -n kafka upgrade --install --values k8s/kafka/nodeport-tls-values.yaml velokafka2 azure-marketplace/kafka
```


Setup a Load balancer Rule

Allow ports via NSG from specified IP.  Still no authentication.



./bin/kafka-topics.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:8092 --list --command-config config-tls.properties




kubectl -n kafka  get secret kafka-tls -o jsonpath="{.data.tls\.crt}" | base64 --decode > velokafka.pem
kubectl -n kafka  get secret kafka-tls -o jsonpath="{.data.tls\.key}" | base64 --decode > velokafka-tls.key

velokafka.pem
1st cert in velokafaka.pem -> velokafka-tls.cert
Rest f certs in velokafka.pem -> velokafka-ca.crt

Let's encrypt combines the ca and cert pem into tls.crt (site first; followed by chain)

kubectl -n kafka create secret generic velokafka --from-file=ca.crt=./velokafka-tls.crt --from-file=tls.crt=./velokafka-ca.crt --from-file=tls.key=./velokafka-tls.key

kubectl create secret generic SECRET_NAME_0 --from-file=ca.crt=./kafka.ca.crt --from-file=tls.crt=./kafka-0.tls.crt --from-file=tls.key=./kafka-0.tls.key


cat config-tls.properties

security.protocol=SSL
ssl.truststore.type=PEM
ssl.truststore.location=velokafka.pem
ssl.endpoint.identification.algorithm=


### Test with Kafka CLI


./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server=velokafka.westus2.cloudapp.azure.com:8092 --command-config config-tls.properties 


./bin/kafka-topics.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:8092 --list --command-config config-tls.properties


### Test Velocity 

curl 'https://a4iot-dj0916a.westus2.cloudapp.azure.com/iot/feed/testConnection' -X POST -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/118.0' -H 'Accept: application/json' -H 'Accept-Language: en-US,en;q=0.5' -H 'Accept-Encoding: gzip, deflate, br' -H 'Referer: https://a4iot-dj0916a.westus2.cloudapp.azure.com/feeds?searchTerm=&sortField=modified&sortOrder=desc&page=1&view=table&status=started%2Cstopped%2Cinitializing%2Cpending%2Cfailed' -H 'Authorization: token=4ul65YVP_Q85JOqk4C_mxFAW0THTO6Ypx8sfIPSG2tTjEfQ7jHWHS19ubl9TmvuxHXEGw14l9axLOuiVPKAyfY-s06R4sUrTGW8pK-S4gDebcVZCEvmYS7YvTaWtmHwpcch7CJOQ6f12-PX1kti2AQQ2AJvfy-bDNGxTq6SKXdFxwO1XF5y5vMdkqTC1yBJ33BQMMts1OYgo1cweFC6Mkb7PcjKOckM2K4Cm0W_ox20.' -H 'Product-version: 4.3.0' -H 'Content-Type: application/json' -H 'Origin: https://a4iot-dj0916a.westus2.cloudapp.azure.com' -H 'Connection: keep-alive' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: same-origin' -H 'TE: trailers' --data-raw '{"name":"kafka","properties":{"kafka.topics":"abc","kafka.brokers":"velokafka.westus2.cloudapp.azure.com:8092","kafka.authenticationType":"none"}}'

curl 'https://a4iot-dj0916a.westus2.cloudapp.azure.com/iot/feed/testConnection' -X POST -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/118.0' -H 'Accept: application/json' -H 'Accept-Language: en-US,en;q=0.5' -H 'Accept-Encoding: gzip, deflate, br' -H 'Referer: https://a4iot-dj0916a.westus2.cloudapp.azure.com/feeds?searchTerm=&sortField=modified&sortOrder=desc&page=1&view=table&status=started%2Cstopped%2Cinitializing%2Cpending%2Cfailed' -H 'Authorization: token=4ul65YVP_Q85JOqk4C_mxFAW0THTO6Ypx8sfIPSG2tTjEfQ7jHWHS19ubl9TmvuxHXEGw14l9axLOuiVPKAyfY-s06R4sUrTGW8pK-S4gDebcVZCEvmYS7YvTaWtmHwpcch7CJOQ6f12-PX1kti2AQQ2AJvfy-bDNGxTq6SKXdFxwO1XF5y5vMdkqTC1yBJ33BQMMts1OYgo1cweFC6Mkb7PcjKOckM2K4Cm0W_ox20.' -H 'Product-version: 4.3.0' -H 'Content-Type: application/json' -H 'Origin: https://a4iot-dj0916a.westus2.cloudapp.azure.com' -H 'Connection: keep-alive' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: same-origin' -H 'TE: trailers' --data-raw '{"name":"kafka","properties":{"kafka.topics":"abc","kafka.brokers":"velokafka.westus2.cloudapp.azure.com:8092","kafka.authenticationType":"none","kafka.useSSL":true}}'

curl 'https://a4iot-dj0916a.westus2.cloudapp.azure.com/iot/feed/testConnection' -X POST -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/118.0' -H 'Accept: application/json' -H 'Accept-Language: en-US,en;q=0.5' -H 'Accept-Encoding: gzip, deflate, br' -H 'Referer: https://a4iot-dj0916a.westus2.cloudapp.azure.com/feeds?searchTerm=&sortField=modified&sortOrder=desc&page=1&view=table&status=started%2Cstopped%2Cinitializing%2Cpending%2Cfailed' -H 'Authorization: token=4ul65YVP_Q85JOqk4C_mxFAW0THTO6Ypx8sfIPSG2tTjEfQ7jHWHS19ubl9TmvuxHXEGw14l9axLOuiVPKAyfY-s06R4sUrTGW8pK-S4gDebcVZCEvmYS7YvTaWtmHwpcch7CJOQ6f12-PX1kti2AQQ2AJvfy-bDNGxTq6SKXdFxwO1XF5y5vMdkqTC1yBJ33BQMMts1OYgo1cweFC6Mkb7PcjKOckM2K4Cm0W_ox20.' -H 'Product-version: 4.3.0' -H 'Content-Type: application/json' -H 'Origin: https://a4iot-dj0916a.westus2.cloudapp.azure.com' -H 'Connection: keep-alive' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: same-origin' -H 'TE: trailers' --data-raw '{"name":"kafka","properties":{"kafka.topics":"test","kafka.brokers":"velokafka.westus2.cloudapp.azure.com:8092","kafka.authenticationType":"saslPlain","kafka.useSSL":true,"kafka.username":"a","kafka.password":"b"}}'

Velocity failed

### List keys

keytool -list -v -keystore /path/to/cacerts

keytool -list -v -keystore /usr/local/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home/lib/security/cacerts
changeit

Tried using this in client didn't work. 


### Covert pem to jks


keytool -importcert -keystore velokafka.p12 -storepass "password123" -file velokafka.pem -alias "cert"
keytool -importkeystore -srckeystore velokafka.p12 -srcstoretype pkcs12 -destkeystore velokafka.jks -deststoretype jks


keytool -importkeystore -srckeystore velokafka.p12 -srcstoretype pkcs12 -destkeystore velokafka.jks -deststoretype jks
Importing keystore velokafka.p12 to velokafka.jks...
Enter destination keystore password:
Re-enter new password:
Enter source keystore password:
Entry for alias cert successfully imported.
Import command completed:  1 entries successfully imported, 0 entries failed or cancelled



### Test with rttest tls (no authentication with encryption)

```
./sendKafka ./monKafka -b velokafka.westus2.cloudapp.azure.com:8092 -t quickstart-events -f planes.csv -r 10 -n -1 -s /Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:8092 -n 1 -t quickstart-events -r 10 -s /Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```



## kafka sasl

This method enables username/password but no encryption of traffic to/from server. 


### Install Kafka with Sasl
```
helm -n kafka upgrade --install --values k8s/kafka/nodeport-sasl-values.yaml velokafka3 azure-marketplace/kafka
```

### Update NSG

Look up Nodeport; we could set this manually.  I'm just letting the deployment pick one. 

kubectl -n kafka get svc 

velokafka3                             NodePort       10.0.209.152   <none>          7092:32060/TCP               3m4s

Firewall (NSG) needed both 7092 and 32060.  

### Add LB rule

From Azure added LB Rule to point 7092 to 32060.
- Name: velokafka3 (whatever you want)
- Pick the Frontend IP for velokafka (e.g. 20.54.192.186)
- Backend (kubernetes)
- TPC
- Port: 7092
- Backend Port: 32060 (This is the NodePort)
- Health Probe you healthz that already exists
- Enable TCP Reset (Not sure this is needed; but it worked)




### Test with Kafka CLI

Create Properties file


config-david.properties
```
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="david" password="password1234";
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
```


```
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server=velokafka.westus2.cloudapp.azure.com:7092 --command-config config-david.properties 
./bin/kafka-topics.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:7092 --list --command-config config-david.properties
```

### Test with rttest sasl (Authentication no Encryption)

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:7092 -n 1 -t quickstart-events -r 10 -u david -p password1234
```

```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:7092 -t quickstart-events -f planes.csv -r 10 -n -1 -u david -p password1234
```

## Kafka sasl_tls

Values: k8s/kafka/nodeport-sasl-tls-values.yaml

### Install Kafka with Sasl
```
helm -n kafka upgrade --install --values k8s/kafka/nodeport-sasl-tls-values.yaml velokafka4 azure-marketplace/kafka
```

### Update NSG

Look up Nodeport; we could set this manually.  I'm just letting the deployment pick one. 

kubectl -n kafka get svc 

velokafka4                             NodePort       10.0.40.68     <none>          6092:32707/TCP               75s
Firewall (NSG) needed both 6092 and 32707.  

### Add LB rule

From Azure added LB Rule to point 6092 to 32707.
- Name: velokafka4 (whatever you want)
- Pick the Frontend IP for velokafka (e.g. 20.54.192.186)
- Backend (kubernetes)
- TPC
- Port: 6092
- Backend Port: 32060 (This is the NodePort)
- Health Probe you healthz that already exists
- Enable TCP Reset (Not sure this is needed; but it worked)


### Test with kafka cli

Create Properties file


config-david-tls.properties
```
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="david" password="password1";
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
ssl.truststore.type=PEM
ssl.truststore.location=velokafka.pem
ssl.endpoint.identification.algorithm=
```


```
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server=velokafka.westus2.cloudapp.azure.com:6092 --command-config config-david-tls.properties
./bin/kafka-topics.sh --bootstrap-server=velokafka.westus2.cloudapp.azure.com:6092 --list --command-config config-david-tls.properties
```

### Test with rttest sasl_tls (Authentication and Encryption)

```
./monKafka -b velokafka.westus2.cloudapp.azure.com:6092 -n 1 -t quickstart-events -r 10 -u david -p password1 -s /
Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```

```
./sendKafka -b velokafka.westus2.cloudapp.azure.com:6092 -t quickstart-events -f planes.csv -r 10 -n -1 -u david -p password1 -s /Users/davi5017/kafka_2.13-3.3.1/velokafka.pem
```



9092,30864,8092,30678,7092,32060,6092,32707


Perhaps 

    pemChainIncluded: false 

    Create secret with three parts. 

    The ca on pod 

    /opt/bitnami/kafka/config/certs/kafka.truststore.pem

    Was in reverse order than what was in the pem created by LetsEncrypt

    Also when I exported the cert from Chrome the last key was different....

    So strange.


Took velokafka.pem and reversed order of certs after first in tls.key

velokafka-test.pem

kubectl -n kafka delete secret velokafka
kubectl -n kafka create secret generic velokafka --from-file=tls.crt=./velokafka-test.pem --from-file=tls.key=./velokafka-tls.key


kubectl -n kafka create secret generic velokafka --from-file=ca.crt=./velokafka.pem --from-file=tls.crt=./velokafka.pem --from-file=tls.key=./velokafka-tls.key

helm -n kafka upgrade --install --values k8s/kafka/nodeport-tls-values.yaml velokafka2 azure-marketplace/kafka


velokafka.westus2.cloudapp.azure.com:443

openssl s_client -connect velokafka.westus2.cloudapp.azure.com:443 2>/dev/null </dev/null |  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'


openssl s_client -debug -connect  velokafka.westus2.cloudapp.azure.com:443 -tls1_2 -showcerts 2>/dev/null </dev/null |  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > pull-cert.pem

This is identical to what I have on in velokafka.pem