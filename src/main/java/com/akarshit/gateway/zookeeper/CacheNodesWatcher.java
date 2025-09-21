package com.akarshit.gateway.zookeeper;


import com.akarshit.gateway.router.ConsistentHashRouter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheNodesWatcher implements Watcher {

    private final ZooKeeper zk;
    private final ConsistentHashRouter router;

    public void watchNodes() throws Exception {
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

