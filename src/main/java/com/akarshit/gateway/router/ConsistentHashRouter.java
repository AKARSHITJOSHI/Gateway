package com.akarshit.gateway.router;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@Log4j2
public class ConsistentHashRouter {
    private final TreeMap<Integer, String> ring = new TreeMap<>();
    private final int VIRTUAL_NODES = 100;

    public synchronized void updateNodes(List<String> nodes) {
        ring.clear();
        log.info("Building virtual nodes with size {}",nodes.size());
        for (String node : nodes) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                int hash = hash(node + "#" + i);
                ring.put(hash, node);
            }
        }
    }

    public synchronized String getNode(String key) {
        if (ring.isEmpty()) return null;
        int hash = hash(key);
        Map.Entry<Integer, String> entry = ring.ceilingEntry(hash);
        return entry != null ? entry.getValue() : ring.firstEntry().getValue();
    }

    private int hash(String key) {
        return key.hashCode() & 0x7fffffff;
    }
}
