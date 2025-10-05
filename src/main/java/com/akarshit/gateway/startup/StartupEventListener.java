package com.akarshit.gateway.startup;

import com.akarshit.gateway.zookeeper.ZkWatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
        name = "startup.listener.enabled",
        havingValue = "true"
)
public class StartupEventListener {

    private final ZkWatcher watcher;

    // Connect to zookeeper only when application is ready
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            log.info("Start watching nodes");
            watcher.startWatching();
        } catch (Exception exp) {
            log.error("Unable to watch zookeeper", exp);
        }
    }
}
