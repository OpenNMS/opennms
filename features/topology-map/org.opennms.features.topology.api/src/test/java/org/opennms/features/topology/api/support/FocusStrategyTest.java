package org.opennms.features.topology.api.support;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.GraphProvider;

import com.google.common.collect.Lists;

public class FocusStrategyTest {

    @Test
    public void testFocusStrategies() {
        GraphProvider provider = new SimpleGraphBuilder("namespace1")
                .vertex("1")
                .vertex("2")
                .vertex("3")
                .get();
        Assert.assertEquals(Lists.newArrayList(), FocusStrategy.EMPTY.getFocusCriteria(provider));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "1"),
                hopCriteria("namespace1", "2"),
                hopCriteria("namespace1", "3")), FocusStrategy.ALL.getFocusCriteria(provider));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "1")), FocusStrategy.FIRST.getFocusCriteria(provider));
        Assert.assertEquals(Lists.newArrayList(
                hopCriteria("namespace1", "2")), FocusStrategy.SPECIFIC.getFocusCriteria(provider, "2"));
    }

    private VertexHopGraphProvider.DefaultVertexHopCriteria hopCriteria(String namespace, String id) {
        return new VertexHopGraphProvider.DefaultVertexHopCriteria(new AbstractVertex(namespace, id));
    }

}