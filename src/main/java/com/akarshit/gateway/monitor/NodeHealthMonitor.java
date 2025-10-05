package com.akarshit.gateway.monitor;

import com.akarshit.gateway.router.ConsistentHashRouter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@RequiredArgsConstructor
public class NodeHealthMonitor {

    private static final String NODE_PATH = "/cache/nodes";

    private final ZooKeeper zk;
    private final ConsistentHashRouter router;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final long UNHEALTHY_THRESHOLD_MS = 15_000L; // 15 seconds (≈3 missed heartbeats)
    private static final long POLL_INTERVAL_MS = 5_000L;        // how often to poll ZK

    // remember timestamps per node URL
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private volatile Set<String> lastInstalled = Collections.emptySet();

    /**
     * Periodically polls ZooKeeper for node data and updates the consistent hash ring.
     * Re-arms watches so that data changes also trigger future polls.
     */
    @Scheduled(fixedDelay = POLL_INTERVAL_MS, initialDelay = 2000)
    public void pollNodeHealth() {
        try {
            // Re-arm watch on parent
            List<String> children;
            try {
                children = zk.getChildren(NODE_PATH, true);
            } catch (KeeperException.NoNodeException nne) {
                log.warn("Parent path {} does not exist yet", NODE_PATH);
                return;
            }

            long now = System.currentTimeMillis();
            Set<String> activeUrls = new HashSet<>();

            for (String child : children) {
                String childPath = NODE_PATH + "/" + child;
                try {
                    byte[] data = zk.getData(childPath, true, null); // re-arm watch for child
                    if (data == null || data.length == 0) {
                        log.debug("Empty data for {}", childPath);
                        continue;
                    }

                    JsonNode node = mapper.readTree(new String(data, StandardCharsets.UTF_8));
                    String url = node.path("url").asText(null);
                    long ts = node.path("timestamp").asLong(0L);

                    if (url == null || url.isEmpty()) {
                        log.warn("No URL present in {}", childPath);
                        continue;
                    }

                    activeUrls.add(url);
                    lastSeen.put(url, ts);
                } catch (KeeperException.NoNodeException ignore) {
                    log.debug("Child disappeared before reading data: {}", childPath);
                } catch (Exception ex) {
                    log.warn("Failed to read data for {}: {}", childPath, ex.toString());
                }
            }

            // Mark healthy if heartbeat is recent
            long cutoff = now - UNHEALTHY_THRESHOLD_MS;
            List<String> healthy = new ArrayList<>();
            for (String url : activeUrls) {
                long ts = lastSeen.getOrDefault(url, 0L);
                if (ts >= cutoff) {
                    healthy.add(url);
                }
            }

            // Clean up vanished nodes
            Set<String> vanished = new HashSet<>(lastSeen.keySet());
            vanished.removeAll(activeUrls);
            for (String v : vanished) {
                lastSeen.remove(v);
            }
            log.info("Health data {} at {}",lastSeen,System.currentTimeMillis());
            
            // Update router only if healthy set changed
            Set<String> healthySet = new HashSet<>(healthy);
            if (!healthySet.equals(lastInstalled)) {
                log.info("Updating router ring with {} healthy nodes: {}", healthySet.size(), healthySet);
                router.updateNodes(new ArrayList<>(healthySet)); // consistent hashing ring rebuild
                lastInstalled = healthySet;
            } else {
                log.debug("No changes in healthy nodes; router unchanged ({})", healthySet.size());
            }

        } catch (KeeperException | InterruptedException e) {
            log.error("ZooKeeper error while polling {}", NODE_PATH, e);
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error in pollNodeHealth", e);
        }
    }
}

