package com.akarshit.gateway.router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConsistentHashRouterTest {
    private ConsistentHashRouter hashRouter;

    @BeforeEach
    public void setup() {
        hashRouter = new ConsistentHashRouter();
    }

    @Test
    public void testGetNodeEmptyRing() {
        assertNull(hashRouter.getNode("anyKey"), "Expected null when ring is empty");
    }

    @Test
    public void testUpdateNodesAndGetNode() {
        hashRouter.updateNodes(Arrays.asList("Node1", "Node2"));

        // For keys, the returned node should be one of the updated nodes
        String nodeForKey1 = hashRouter.getNode("myKey1");
        assertTrue(Arrays.asList("Node1", "Node2").contains(nodeForKey1), "Returned node should be from the list");

        String nodeForKey2 = hashRouter.getNode("anotherKey");
        assertTrue(Arrays.asList("Node1", "Node2").contains(nodeForKey2), "Returned node should be from the list");
    }

    @Test
    public void testUpdateNodesClearsPreviousEntries() {
        hashRouter.updateNodes(Collections.singletonList("Node1"));
        String assignedNodeBefore = hashRouter.getNode("key");

        hashRouter.updateNodes(Collections.singletonList("Node2"));
        String assignedNodeAfter = hashRouter.getNode("key");

        assertNotEquals(assignedNodeBefore, assignedNodeAfter, "After update, assigned node should change");
    }

    @Test
    public void testGetNodeReturnsFirstEntryIfCeilingEntryIsNull() {
        // Add a single node expected to be the only ring entry
        hashRouter.updateNodes(Collections.singletonList("Node1"));

        // To simulate ceilingEntry returning null, provide a key with very high hash
        // Since ceilingEntry in the code checks ring.ceilingEntry(hash)
        String highHashKey = String.valueOf(Integer.MAX_VALUE) + "key";

        String node = hashRouter.getNode(highHashKey);
        assertEquals("Node1", node, "Should return first entry node if ceilingEntry is null");
    }
}