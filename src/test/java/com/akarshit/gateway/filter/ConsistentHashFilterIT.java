package com.akarshit.gateway.filter;

import com.akarshit.gateway.zookeeper.CacheNodesWatcher;
import lombok.SneakyThrows;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConsistentHashFilterIT {

    @Autowired
    ZooKeeper zooKeeper;

    @Autowired
    CacheNodesWatcher watcher;


    @Autowired
    private WebTestClient webTestClient;

    @Test
    @SneakyThrows
    void testZKNodeWatcher(){
        assertNotNull(zooKeeper);
        assertNotNull(watcher);

        assertDoesNotThrow(() -> {
            watcher.process(new WatchedEvent(
                    Watcher.Event.EventType.NodeChildrenChanged,
                    Watcher.Event.KeeperState.SyncConnected,
                    "/some/path"
            ));
        });
    }
}