package com.akarshit.gateway.filter;

import com.akarshit.gateway.router.ConsistentHashRouter;
import com.akarshit.gateway.zookeeper.CacheNodesWatcher;
import lombok.SneakyThrows;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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