# Gateway Application

### Routing Strategy

- For cache , consisten hashing is used.
- Filter -> ConsistenHashRouter -> pulls/updates from Zookeeper.


### 🚀 Start ZooKeeper
```bash
docker compose -f zookeeper-docker-compose.yml up 
````
### 💻 Connect to zookeepr
```bash

- docker exec -it zookeeper zkCli.sh to connect

```