package org.opennms.features.topology.app.internal;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class GCFilterableContainerTest {

    private GraphContainer graphContainer;

    @Before
    public void setUp() throws MalformedURLException, JAXBException {
        GraphProvider provider = new AbstractTopologyProvider("test") {
            @Override public void save() { }
            @Override public void refresh() { }
            @Override public void load(String filename) throws MalformedURLException, JAXBException { 
                resetContainer();
                
                String vId1 = getNextVertexId();
                TestVertex v1 = new TestVertex(vId1, 0, 0);
                v1.setLabel("a leaf vertex");
                
                String vId2 = getNextVertexId();
                TestVertex v2 = new TestVertex(vId2, 0, 0);
                v2.setLabel("another leaf");
                
                addVertices(v1, v2);
            }
        };
        provider.load(null);
        graphContainer = new VEProviderGraphContainer(provider, new ProviderManager());
    }
    
    @Test
    public void createTopLevelGroup() {
        // elements to group
        List<Vertex> allVertices = graphContainer.getBaseTopology().getVertices();
        
        // create group
        String groupName = "groupName";
        VertexRef groupId = graphContainer.getBaseTopology().addGroup(groupName,  "group");
        
        // Link all targets to the newly-created group
        for(VertexRef vertexRef : allVertices) {
            graphContainer.getBaseTopology().setParent(vertexRef, groupId);
        }

        // Save the topology
        graphContainer.getBaseTopology().save();
        graphContainer.redoLayout();
        graphContainer.getBaseTopology().refresh();
        
        // all childs must have group as parent
        for (Vertex vertex : allVertices) {
            Assert.assertEquals(groupId, vertex.getParent());
        }
        
        // group must have no parent
        Assert.assertEquals(null, graphContainer.getBaseTopology().getVertex(groupId).getParent());
    }
    
}
