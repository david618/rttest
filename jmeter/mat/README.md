# Cron Testing Map Service


## Manual Calls

```
curl https://us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/dj1028a_cqvg_planes1x500_lyr/MapServer?token=
```

```
TOKEN=J71T1BYJEdv3Ej5TT2jLPDfL8D4qrs6rs3xxkebpQTyy9Bh0JJDsXtXZ6IoLJzEMOeDqm4NRn_sWwbwFP1lMsl7Kais2f4GsBWYuzOAJZMBmc38MLPkDxW2SagRah8y3jjpDyws92XB2EBGaeVah4lGWPySFHliOkk7qozrSLak6hr0oGBLgjkrjSUM6qaqGPPsmPLmOpygNSxdmijZoQnDl8VKB7LlyE6DmXfv_Tas.
```

```
curl https://us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/dj1028a_cqvg_planes1x500_lyr/MapServer?token=${TOKEN}
```


```
BBOX=-179.99997007660568%2C-87.99934000242501%2C179.99995993450284%2C87.99992996267974
TOKEN=J71T1BYJEdv3Ej5TT2jLPDfL8D4qrs6rs3xxkebpQTyy9Bh0JJDsXtXZ6IoLJzEMOeDqm4NRn_sWwbwFP1lMsl7Kais2f4GsBWYuzOAJZMBmc38MLPkDxW2SagRah8y3jjpDyws92XB2EBGaeVah4lGWPySFHliOkk7qozrSLak6hr0oGBLgjkrjSUM6qaqGPPsmPLmOpygNSxdmijZoQnDl8VKB7LlyE6DmXfv_Tas. 
```

```
curl -s "https://us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/dj1028a_cqvg_planes1x500_lyr/MapServer/export?bbox=${BBOX}&f=image&token=${TOKEN}" > img.png
```

```
source test/set-env-cluster-tenant.sh dj1028a cqvgkj9zrnkn9bcu 
CLUSTER_ID: dj1028a
ORGID: cqvgkj9zrnkn9bcu
USERNAME: velocity_shared
PREFIX: dj1028a_cqvg
AGOL: us-iotdev.arcgis.com
CLUSTER_URL: us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu
```

## App IPO Input-Processing-Output 

Inputs:
- Username (e.g. velocity_shared)
- Password (Secret named velocity_shared with data.password)
- ClusterUrl (e.g. us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu) 
- service (e.g. dj1028a_cqvg_planes1x500_lyr)
- bboxSize (Space delimited values from 10,20,30,40,50,60,70,80,90,100,110,120,130,140)
- hashStyle (Space delimited values from flatHex, geohash, pointyHex, square)
- createBboxes.py
- jmeter (apache-jmeter-5.6.2.tgz)
  - service_name (REPLACE_SERVICE) dj1028a_cqvg_planes1x500_lyr
  - server (REPLACE_SERVER) us-iotdev.arcgis.com
  - prefix (REPLACE_PREFIX) dj1028a/cqvgkj9zrnkn9bcu
  - hashStyle (REPLACE_HASH_STYLE)
  - bboxSize (REPLACE_BBOX_SIZE)
- Template (template.jmx) do replacments to create test.jmx (mount template.jmx as configmap)
- ./apache-jmeter-5.6.2/bin/jmeter -n -t temp.jmx


Processing:
- Create Token 
- Create test.jmx from Template
- Run jmeter test.jmx


Outputs:
- Print results to stdout 
- Send results to Prometheus, or Opensearch, or Database


**NOTE:** Loading planes100m_aws often fails. I rarely can get 100M planes to load. 

planes10m_aws worked and loaded 10 million features. 

Might need to resize pvc prior to loading 100 million (10x 10m).  The default 32G may not be enough. Resized to 128G each.


```
kubectl -n monitoring  port-forward svc/prometheus-server 9090:80
```

```
./admin/get-pvc-info.py dj1028a available
```

## Output to Opensearch

https://opensearch.org/docs/latest/install-and-configure/install-opensearch/helm/

https://github.com/opensearch-project/helm-charts/blob/main/charts/README.md


```
helm repo add opensearch https://opensearch-project.github.io/helm-charts/
helm -n test upgrade --install  --create-namespace --set replicas=1 --set sysctl.enabled=true opensearch opensearch/opensearch
```

```
helm -n test upgrade --install opensearch-dashboards opensearch/opensearch-dashboards
```

Error: "max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]"

Tried a couple of tweaks; this is not going to works well for these simple needs.


## Postgres 

Easy to install.

```
helm install my-release oci://registry-1.docker.io/bitnamicharts/postgresql
helm -n test upgrade --install --create-namespace pg oci://registry-1.docker.io/bitnamicharts/postgresql
export POSTGRES_PASSWORD=$(kubectl get secret --namespace test pg-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)
echo ${POSTGRES_PASSWORD} 
```

```
DgFJceKAJU
```

```
brew install pgadmin4
```

```
kubectl -n test port-forward svc/pg-postgresql 5432
```

```
Connected with pgadmin4
```

```
Create user tester
```


CreatedDB testresults with owner tester
```
pg_host = "127.0.0.1"
pg_db = "testresults"
pg_username = "tester"
pg_password = "**REDACTED**"
pg_port = "5432"
pg_table = "maprequest"
```

```
conn = psycopg2.connect(
   database=pg_db, user=pg_username, password=pg_password, host=pg_host, port=pg_port
)
```

```
cursor = conn.cursor()
```

```
[{"epoch_time": 1700146989, "cluster_url": "us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu", "service": "dj1028a_cqvg_planes1x500_lyr", "hashStyle": "pointyHex", "bboxSize": "80", "name": "MapRequest", "averageResponseTime": 4634.14, "numberOfSuccess": 100, "totalCalls": 100, "averageSizeBytes": 79775.48}]
```

```
CREATE TABLE maprequest(
  epoch_time integer,
  cluster_url varchar(512),
  service varchar(256),
  hashStyle varchar(100),
  bboxSize varchar(5),
  name varchar(100),
  averageResponseTime numeric,
  numberOfSuccess integer,
  totalCalls integer,
  averageSizeBytes numeric 
);
```

```
update maprequest set bboxsizestr = bboxsize::text
update maprequest set time = dt
```

```
cursor = conn.cursor()
```

```
sql = f"INSERT INTO {pg_table}(epoch_time, cluster_url, service, hashStyle, bboxSize,name, averageResponseTime, numberOfSuccess, totalCalls, averageSizeBytes) \
      VALUES(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
```

```
cursor.execute(sql, (1700146989,'us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu','dj1028a_cqvg_planes1x500_lyr','pointyHex',80,'MapRequest',4634.14,100,100,79775.4))
```


```
kubectl -n test create secret generic testuser-password --from-literal=password="**REDACTED**"
```

When connecting to Grafana for visualization; some challenges.  Found that the best approach was to load tables much like we do for Prometheus.  

Create table for each metric.

Table names like service_geohash_10 or service_pointyhex_20.  Added timestamp for the time the metric was gathered. 

```
CREATE TABLE {table} (time timestamp, {suffix} numeric)
```

Insert one metric per tuple. 

```
INSERT INTO {table}(time, {suffix}) VALUES(%s, %s)
```

This approach worked really well. 

## Solr

```
helm install my-release oci://registry-1.docker.io/bitnamicharts/solr
helm -n test upgrade --install --create-namespace solr oci://registry-1.docker.io/bitnamicharts/solr
8.3.1
```


```
helm -n test upgrade --install --create-namespace --set replicaCount=1 --set collectionReplicas=0 --set zookeeper.replicaCount=1 solr oci://registry-1.docker.io/bitnamicharts/solr
```

```
echo Username: admin
echo Password: $(kubectl get secret --namespace test solr -o jsonpath="{.data.solr-password}" | base64 -d)
```

```
Username: admin
Password: **REDACTED**
```

Installation was easier than Opensearch.  

Did tweak for 1 replica and it also install zookeeper.

It was easy to insert data; however, while Grafana had a plugin for Solr; for some reason I couldn't get it to work.


## Grafana

```
helm install my-release oci://registry-1.docker.io/bitnamicharts/grafana
helm -n test upgrade --install --create-namespace --set grafana.plugins[0]=pue-solr-datasource  grafana oci://registry-1.docker.io/bitnamicharts/grafana
```

```
Password: **REDACTED**
```

We are not reselling Grafana so using it for purposes would be no different thatn if we were using Prometheus Service with Grafana from Azure or AWS.  

## mongo

```
https://github.com/bitnami/charts/tree/main/bitnami/mongodb
```

```
helm -n test upgrade --install mongo oci://registry-1.docker.io/bitnamicharts/mongodb
```

```
export MONGODB_ROOT_PASSWORD=$(kubectl get secret --namespace test mongo-mongodb -o jsonpath="{.data.mongodb-root-password}" | base64 -d)
echo $MONGODB_ROOT_PASSWORD
```

```
t5vUvrh9Xn
```

```
pip3 install pymongo
```

```
import pymongo
from pymongo import MongoClient
```

```
kubectl -n test port-forward svc/mongo-mongodb 27017
```

```
username = root
password = **REDACTED**
server = 127.0.0.1
myclient = MongoClient('mongodb://%s:%s@%s' % (username, password, server))
```

```
mydb = myclient["dj1028a"]
mycol = mydb["planes1x500"]
```

```
a = json.loads('{"cluster_url": "us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu", "service": "dj1028a_cqvg_planes1x500_lyr", "hashStyle": "pointyHex", "bboxSize": "80", "name": "MapRequest", "averageResponseTime": 1459.97, "numberOfSuccess": 100, "totalCalls": 100, "averageSizeBytes": 81611.75}')
```


```
x = mycol.insert_one(a)
```

The Grafana plugin for Mongo requires Enterprise Grafana License.

## Run Script

```
run.sh
```

This is what we run in Cron Job.  



```
jmeter from my desktop sometimes hangs.   No errors just stops.
```

Tried increase HEAP

```
export JVM_ARGS="-Xms4g -Xmx4g -XX:MaxMetaspaceSize=1g"
```


Tried adding timeout; but it still sticks.   Looking at jmeter.log.  Looks like the job is done and the Threads all close.   Ctrl-C seems to wake it back up.  I saw threads close; but I didn't verify all threads closed.  Perhaps one or two are stuck.  

Have not seen this problem when running as Cronjob. 



## Summary

Goal: On a scheduled interval run a JMeter test(s) against a Velocity Map service.  This will allow us to identify when changes happen that impact the map performance.  

- Created JMeter Test Plan
- Parameterized Plan
  - CLUSTER_URL
  - SERVICE
  - USERNAME
  - PASSWORD
  - HASHES (e.g. geohash, square, pointyHex)
  - SIZES (e.g. 10 20 40 80)
- Used getToken.py to get a Token Using CLUSTER_URL, USERNAME, PASSWORD
- Created Random Bounding Boxes (createBboxes.py)
- Loop Through Each size
  - Loop Through Each Hash 
    - Run JMeter Test
    - Calculate Stats and Write them to Postgres (calcStats.py)

For output 
- Opensearch (Heavy Requiment on Cluster; requires  vm.max_map_count)
- Solr (Easy to write output; Could not access using Grafana plugin)
- MongoDB (Easy to write output; Plugin requires Enterprise Grafana)
- Postgres (Little more challenging to write; Plugin worked. After some trial/error created a nice Grafana Chart)

## Stop Cronjob

```
kubectl -n test patch cronjobs.batch dj1028a-cqvg-planes10m-aws -p '{"spec" : {"suspend" : true}}'
kubectl -n test patch cronjobs.batch mat-cronjob-dj1028a -p '{"spec" : {"suspend" : true}}'
```

## FlatHex Not Working 

```
[{"id":0,"name":"planes","source":{"type":"mapLayer","mapLayerId":0},"drawingInfo":{"renderer":{"type":"aggregation","style":"Grid","featureThreshold":100,"lodOffset":0,"minBinSizeInPixels":25,"fullLodGrid":false,"labels":{"color":[0,0,0,255],"font":"Arial","size":12,"style":"PLAIN","format":"###.#KMB"},"fieldStatistic":null,"binRenderer":{"type":"Continuous","minColor":[255,0,0,0],"maxColor":[255,0,0,255],"minOutlineColor":[0,0,0,100],"maxOutlineColor":[0,0,0,100],"minOutlineWidth":0.5,"maxOutlineWidth":0.5,"minValue":null,"maxValue":null,"minSize":100,"maxSize":100,"normalizeByBinArea":false},"geoHashStyle":{"style":"flatHexagon","sr":"102100"},"featureRenderer":{"type":"simple","symbol":{"type":"esriSMS","style":"esriSMScircle","color":[158,202,225,150],"size":12,"angle":0,"xoffset":0,"yoffset":0,"outline":{"color":[0,0,0,255],"width":1}},"label":"","description":"","rotationType":"","rotationExpression":""}}},"minScale":0,"maxScale":0}]
```

Get or Post return Nothing.

```
https://us-iotdev.arcgis.com/dj1028a/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/dj1028a_cqvg_planes10m_aws/MapServer/export?bbox=-179.99992003664374%2C-79.91879001259804%2C179.9999899417162%2C87.99997996073216&bboxSR=&layers=&layerDefs=&size=&imageSR=&format=png&transparent=false&dpi=&time=&layerTimeOptions=&dynamicLayers=%5B%7B%22id%22%3A0%2C%22name%22%3A%22planes%22%2C%22source%22%3A%7B%22type%22%3A%22mapLayer%22%2C%22mapLayerId%22%3A0%7D%2C%22drawingInfo%22%3A%7B%22renderer%22%3A%7B%22type%22%3A%22aggregation%22%2C%22style%22%3A%22Grid%22%2C%22featureThreshold%22%3A100%2C%22lodOffset%22%3A0%2C%22minBinSizeInPixels%22%3A25%2C%22fullLodGrid%22%3Afalse%2C%22labels%22%3A%7B%22color%22%3A%5B0%2C0%2C0%2C255%5D%2C%22font%22%3A%22Arial%22%2C%22size%22%3A12%2C%22style%22%3A%22PLAIN%22%2C%22format%22%3A%22%23%23%23.%23KMB%22%7D%2C%22fieldStatistic%22%3Anull%2C%22binRenderer%22%3A%7B%22type%22%3A%22Continuous%22%2C%22minColor%22%3A%5B255%2C0%2C0%2C0%5D%2C%22maxColor%22%3A%5B255%2C0%2C0%2C255%5D%2C%22minOutlineColor%22%3A%5B0%2C0%2C0%2C100%5D%2C%22maxOutlineColor%22%3A%5B0%2C0%2C0%2C100%5D%2C%22minOutlineWidth%22%3A0.5%2C%22maxOutlineWidth%22%3A0.5%2C%22minValue%22%3Anull%2C%22maxValue%22%3Anull%2C%22minSize%22%3A100%2C%22maxSize%22%3A100%2C%22normalizeByBinArea%22%3Afalse%7D%2C%22geoHashStyle%22%3A%7B%22style%22%3A%22flatHexagon%22%2C%22sr%22%3A%22102100%22%7D%2C%22featureRenderer%22%3A%7B%22type%22%3A%22simple%22%2C%22symbol%22%3A%7B%22type%22%3A%22esriSMS%22%2C%22style%22%3A%22esriSMScircle%22%2C%22color%22%3A%5B158%2C202%2C225%2C150%5D%2C%22size%22%3A12%2C%22angle%22%3A0%2C%22xoffset%22%3A0%2C%22yoffset%22%3A0%2C%22outline%22%3A%7B%22color%22%3A%5B0%2C0%2C0%2C255%5D%2C%22width%22%3A1%7D%7D%2C%22label%22%3A%22%22%2C%22description%22%3A%22%22%2C%22rotationType%22%3A%22%22%2C%22rotationExpression%22%3A%22%22%7D%7D%7D%2C%22minScale%22%3A0%2C%22maxScale%22%3A0%7D%5D&gdbVersion=&mapScale=&f=html&token=RXCPL1Cp2OxFw4PbzwhBIpnvpbid4_2OJ9n5JFtP8HmvR13i-Y8n5DeXpKE9O8I3kth8-YuHLv56fgbpJICT9a9U9kWEqwfH0vunwzta7ZWjQLst6nOE262KlEgx8uOdEtCiOG_NNSBYODZnaghMvFufZcul08pPcO5v-KJcvfZOZHP_qjJYPsfOV_d3OIMIbVno6fVVpALbIzxBPcvseu_WmM4IT1KpQY54Q60jReI.
```

Nothing is working for me from 

## Create planes1x500

### Create config map from environment 

```
kubectl -n test create cm devus3d-planes1x500 --from-env-file test/mat/k8s/devus3d-planes1x500.env
```

### Create Manifest cronjob

```
kubectl apply -f devops/scripts/test/mat/k8s/devus3d-planes1x500.yaml 
```

### Create Grafana Chart 

Create grafana-mat-template.json 
- REPLACE_PROM_METRIC
- REPLACE_DASHBOARD_NAME
- REPLACE_GUID

```
uuidgen | tr 'A-Z' 'a-z'
```

## Create batplanes 

### Create config map from environment 

```
kubectl -n test create cm devus3d-batplanes --from-env-file test/mat/k8s/devus3d-batplanes.env
```

### Create Manifest cronjob

```
kubectl apply -f test/mat/k8s/devus3d-batplanes.yaml 
```

First load of 10M count 11,785,200
Second load of 10M count 21,785,200
Third Load of 10M count 31,785,200
Fourth Load of 10M count 41,785,200
Fifth Load of 10M count 52,008,356
Sixth Load of 10M count 62,231,582
Seventh Load of 10M count 72,231,582


https://us-iotdev.arcgis.com/devusa3default/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/devusa3default_cqvg_planes1x500_lyr/MapServer

### Long Term Test Results

Let the cronjob run every 30 minutes for 10 days. 

Restuls for bat consistently showed average response times of 4 to 7 seconds.  

During one day the response times jumped to 12 to 20 seconds; this coincided with another RAT running on the cluster that was causing the cpu's of elasticsearch to be at 5cpu each the limit for this cluster type.

Once the RAT stopped results droped back to 4 to 7 seconds. 

