package com.akarshit.gateway.zookeeper;


import com.akarshit.gateway.router.ConsistentHashRouter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CacheNodesWatcher implements Watcher {

    private final ZooKeeper zk;
    private final ConsistentHashRouter router;

    @PostConstruct
    public void init() throws Exception {
        watchNodes();
    }

    private void watchNodes() throws Exception {
        List<String> children = zk.getChildren("/cache/nodes", this);
        List<String> nodes = new ArrayList<>();

        for (String child : children) {
            byte[] data = zk.getData("/cache/nodes/" + child, false, null);
            nodes.add(new String(data));
        }

        router.updateNodes(nodes); // rebuild consistent hash ring
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                watchNodes(); // re-register watch
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

