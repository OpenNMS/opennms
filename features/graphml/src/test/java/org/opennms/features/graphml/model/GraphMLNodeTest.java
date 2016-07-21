package org.opennms.features.graphml.model;

import org.junit.Assert;
import org.junit.Test;

public class GraphMLNodeTest {

    @Test
    public void verifyEqualsAndHashCode() {
        GraphMLNode node = new GraphMLNode();
        node.setProperty("id", "some-id");
        node.setProperty("label", "some-label");

        GraphMLNode copy = new GraphMLNode();
        copy.setProperty("id", "some-id");
        copy.setProperty("label", "some-label");

        Assert.assertEquals(node.hashCode(), node.hashCode());
        Assert.assertEquals(node, node);
        Assert.assertEquals(node.hashCode(), copy.hashCode());
        Assert.assertEquals(node, copy);

    }

}