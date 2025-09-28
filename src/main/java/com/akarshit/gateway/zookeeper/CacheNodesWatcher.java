package com.akarshit.gateway.zookeeper;


import com.akarshit.gateway.router.ConsistentHashRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheNodesWatcher implements Watcher {

    private final ZooKeeper zk;
    private final ConsistentHashRouter router;
    private static final String NODE_PATH = "/cache/nodes";
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public void startWatching() throws Exception {
        executor.submit(() -> {
            try {
                watchNodes();
            } catch (Exception exp) {
                log.error("Exception while watching node ", exp);
            }
        });
    }

    public void watchNodes() throws InterruptedException, KeeperException {
        // Ensure parent exists or handle the NoNodeException gracefully
        if (zk.exists(NODE_PATH, false) == null) {
            log.warn("Parent path {} does not exist yet. Nothing to watch.", NODE_PATH);
            // Try to create it (optional) or return
            try {
                zk.create(NODE_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("Created parent {}", NODE_PATH);
            } catch (KeeperException.NodeExistsException ignore) {
                // race: someone else created it
            } catch (KeeperException | InterruptedException ex) {
                log.error("Failed ensuring parent exists", ex);
                throw ex;
            }
        }

        // getChildren installs a watch on NODE_PATH for NodeChildrenChanged (one-shot)
        List<String> children = zk.getChildren(NODE_PATH, this);
        List<String> nodes = new ArrayList<>(children.size());

        for (String child : children) {
            String full = NODE_PATH + "/" + child;
            try {
                byte[] data = zk.getData(full, false, null);
                if (data != null) {
                    nodes.add(new String(data, StandardCharsets.UTF_8));
                } else {
                    log.warn("Child {} has empty data", full);
                }
            } catch (KeeperException.NoNodeException nne) {
                log.debug("Child {} disappeared before we could read it", full);
            } catch (KeeperException | InterruptedException e) {
                log.error("Error reading data for child " + full, e);
            }
        }

        try {
            router.updateNodes(nodes);
            log.info("Router updated with {} nodes", nodes.size());
        } catch (Exception e) {
            log.error("Failed to update router", e);
        }
    }


    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                startWatching(); // re-register watch
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

