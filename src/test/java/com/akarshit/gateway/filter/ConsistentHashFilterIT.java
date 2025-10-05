package com.akarshit.gateway.filter;

import com.akarshit.gateway.zookeeper.CacheNodesWatcher;
import lombok.SneakyThrows;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ConsistentHashFilterIT {

    @Autowired
    ZooKeeper zooKeeper;

    @Autowired
    CacheNodesWatcher watcher;


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

//    @Test
//    public void filterTest() {
//        ConsistentHashRouter router = mock(ConsistentHashRouter.class);
//        ConsistentHashFilter filter = new ConsistentHashFilter(router);
//
//        ServerWebExchange exchange = mock(ServerWebExchange.class);
//        GatewayFilterChain chain = mock(GatewayFilterChain.class);
//
//
//        // setup mocks for exchange methods etc.
//
//        when(chain.filter(exchange)).thenReturn(Mono.empty());
//
//        StepVerifier.create(filter.filter(exchange, chain))
//                .expectComplete()
//                .verify();
//
//        verify(chain, times(1)).filter(exchange);
//    }
}