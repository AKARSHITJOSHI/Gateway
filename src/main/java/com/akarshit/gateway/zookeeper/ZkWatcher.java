package com.akarshit.gateway.zookeeper;


import com.akarshit.gateway.router.ConsistentHashRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
@Component
@RequiredArgsConstructor
public class ZkWatcher implements Watcher {

    private static final String NODE_PATH = "/cache/nodes";
    private final ZooKeeper zk;
    private final ConsistentHashRouter router;

    private final List<String> lastKnownNodes = new CopyOnWriteArrayList<>();


    public void startWatching() {
        try {
            ensureParent(zk, NODE_PATH);
            zk.register(this); // receive session state events
            refreshRouterFromZk(); // initial load
            log.info("Started watching ZooKeeper path {}", NODE_PATH);
        } catch (Exception e) {
            log.error("Failed to start watching ZooKeeper", e);
        }
    }


    @Override
    public void process(WatchedEvent event) {
        try {
            log.info("ZkWatcher.process()....");
            Event.EventType type = event.getType();
            Watcher.Event.KeeperState state = event.getState();
            String path = event.getPath();

            if (type == Event.EventType.None) {
                log.info("ZooKeeper connection event: {}", state);
                if (state == Watcher.Event.KeeperState.Expired) {
                    log.warn("Session expired -> reloading router after reconnect");
                    refreshRouterFromZk();
                } else if (state == Watcher.Event.KeeperState.SyncConnected) {
                    refreshRouterFromZk();
                }
                return;
            }

            if (type == Event.EventType.NodeChildrenChanged && NODE_PATH.equals(path)) {
                log.info("Children changed under {}", NODE_PATH);
                refreshRouterFromZk();
                return;
            }

            if (type == Event.EventType.NodeCreated || type == Event.EventType.NodeDeleted) {
                log.info("{} event for {}", type, path);
                refreshRouterFromZk();
            }

            if (type == Event.EventType.NodeDataChanged) {
                log.info("Data changed for {}", path);
                refreshRouterFromZk();
            }
        } catch (Exception e) {
            log.error("Error handling ZooKeeper event", e);
        }
    }

    private void refreshRouterFromZk() {
        try {
            ensureParent(zk, NODE_PATH);
            List<String> children = zk.getChildren(NODE_PATH, this); // re-arm watcher
            List<String> nodeUrls = new ArrayList<>();

            for (String child : children) {
                String childPath = NODE_PATH + "/" + child;
                try {
                    byte[] data = zk.getData(childPath, this, new Stat());
                    if (data == null || data.length == 0) continue;
                    String url = new String(data, StandardCharsets.UTF_8).trim();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    nodeUrls.add(url);
                } catch (KeeperException.NoNodeException ignored) {
                    log.debug("Child {} disappeared before reading data", childPath);
                } catch (Exception e) {
                    log.warn("Failed to read data for {}: {}", childPath, e.toString());
                }
            }

            // Only update router if node set changed
            if (!new HashSet<>(nodeUrls).equals(new HashSet<>(lastKnownNodes))) {
                lastKnownNodes.clear();
                lastKnownNodes.addAll(nodeUrls);
                router.updateNodes(nodeUrls);
                log.info("Router updated with {} nodes -> {}", nodeUrls.size(), nodeUrls);
            } else {
                log.debug("No change in nodes; router unchanged");
            }
        } catch (KeeperException.NoNodeException e) {
            log.warn("ZK parent path {} missing; creating and retrying", NODE_PATH);
            ensureParent(zk, NODE_PATH);
        } catch (Exception e) {
            log.error("Failed to refresh router from ZooKeeper", e);
        }
    }


    private void ensureParent(ZooKeeper zk, String path) {
        try {
            if (zk.exists(path, false) == null) {
                zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException.NodeExistsException ignored) {
        } catch (Exception e) {
            log.error("Error ensuring parent path {} exists", path, e);
        }
    }

    public void close() {
        try {
            if (zk != null) zk.close();
        } catch (InterruptedException ignored) {
        }
    }
}