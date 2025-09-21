package com.akarshit.gateway.config;

import lombok.extern.log4j.Log4j2;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Log4j2
public class ZookeeperConfig {

    @Value("${zookeeper.connect-string:localhost:2181}")
    private String connectString;

    @Bean(destroyMethod = "close")
    public ZooKeeper zooKeeper() throws IOException {
        return new ZooKeeper(connectString, 3000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                log.info(" Gateway connected to ZooKeeper at " + connectString);
            }
        });
    }
}

