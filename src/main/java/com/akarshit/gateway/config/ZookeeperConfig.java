package com.akarshit.gateway.config;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
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
    @SneakyThrows
    public ZooKeeper zooKeeper() throws IOException {
        ZooKeeper zk= new ZooKeeper(connectString, 3000, event -> {
            try {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info(" Gateway connected to ZooKeeper at " + connectString);
                }
            } catch (Exception exp) {
                log.error("Unable to connect to zookeeper : {}", connectString);
            }

        });
        return zk;
    }
}

