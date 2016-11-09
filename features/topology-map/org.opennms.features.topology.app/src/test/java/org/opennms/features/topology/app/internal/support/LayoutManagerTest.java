package org.opennms.features.topology.app.internal.support;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

public class LayoutManagerTest {
    
    @Test
    public void testHash() {
        ArrayList<VertexRef> defaultVertexRefs = Lists.newArrayList(
                new DefaultVertexRef("namespace1", "id1", "Label 1"),
                new DefaultVertexRef("namespace2", "id2", "Label 2"),
                new DefaultVertexRef("namespace3", "id3", "Label 3"));

        String hash1 = LayoutManager.calculateHash(defaultVertexRefs);
        String hash2 = LayoutManager.calculateHash(defaultVertexRefs);
        Assert.assertEquals(hash1, hash2);
    }

}