package org.opennms.features.topology.plugins.topo.graphml;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.plugins.topo.graphml.model.*;

public class GraphMLTopologyProviderTest {

    private static final String NAMESPACE = "my-namespace";

    @Test
    public void testLoad() throws InvalidGraphException {
        GraphML graphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph.xml"));
        Assert.assertEquals(NAMESPACE, graphML.getProperty(GraphMLProperties.NAMESPACE));
        Assert.assertEquals(1, graphML.getGraphs().size());

        GraphMLGraph graph = graphML.getGraphs().get(0);
        Assert.assertEquals(NAMESPACE, graph.getProperty(GraphMLProperties.NAMESPACE));

        Assert.assertEquals(20, graph.getEdges().size());
        Assert.assertEquals(25, graph.getNodes().size());

        for (org.opennms.features.topology.plugins.topo.graphml.model.GraphMLEdge eachEdge : graph.getEdges()) {
            Assert.assertEquals(NAMESPACE, eachEdge.getProperty(GraphMLProperties.NAMESPACE));
        }
        for (GraphMLNode eachNode : graph.getNodes()) {
            Assert.assertEquals(NAMESPACE, eachNode.getProperty(GraphMLProperties.NAMESPACE));
        }
    }

}