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


### Consistent Hashing impl with Virtual Nodes

🌀 Consistent Hashing with Virtual Nodes

This class implements consistent hashing using a TreeMap as a hash ring, with virtual nodes to balance key distribution across physical servers.

private final TreeMap<Integer, String> ring = new TreeMap<>();
private final int VIRTUAL_NODES = 100;

### 🔧 How it works

#### Build the ring

``` java
public void updateNodes(List<String> nodes) {
    ring.clear();
    for (String node : nodes) {
    for (int i = 0; i < VIRTUAL_NODES; i++) {
    int hash = hash(node + "#" + i);
    ring.put(hash, node);
    }
  }
}
```

- Each physical node is represented by 100 virtual nodes.

- Each virtual node (node#i) is hashed to a position on the ring.

#### Find node for a key
``` java
public String getNode(String key) {
int hash = hash(key);
Map.Entry<Integer, String> entry = ring.ceilingEntry(hash);
return entry != null ? entry.getValue() : ring.firstEntry().getValue();
}
```

1. Hash the key.

    1.1 Pick the next node clockwise (ceilingEntry).

    1.2 Wrap to the first entry if we reach the ring’s end.

#### Hash function

``` java
private int hash(String key) {
return key.hashCode() & 0x7fffffff; // ensure positive hash
}
```

⚙️ Example

For nodes [A, B, C] and VIRTUAL_NODES = 5:

0 ------------------ 2^31
|  A#1  B#1  C#1  A#2  B#2  C#2 ... |


When key "user42" hashes to a position between B#2 and C#2,
the responsible node is C (the next clockwise entry).

✅ Benefits

- Even load distribution across nodes.

- Minimal key movement when nodes join or leave.

- Simple implementation using TreeMap and virtual nodes.
- Using virtual nodes we increase the randomness of hash matching with key and improve distribution.