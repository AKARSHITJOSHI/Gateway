package com.akarshit.gateway.startup;

import com.akarshit.gateway.zookeeper.CacheNodesWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
public class StartupEventListener {

    CacheNodesWatcher watcher;

    // Connect to zookeeper only when application is ready
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() throws Exception {
        log.info("Start watching nodes");
        watcher.startWatching();
    }
}
