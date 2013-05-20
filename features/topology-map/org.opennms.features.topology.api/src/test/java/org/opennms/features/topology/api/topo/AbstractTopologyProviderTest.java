package org.opennms.features.topology.api.topo;

import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

public class AbstractTopologyProviderTest {

    @Test
    public void testIdGenerator() throws MalformedURLException, JAXBException {
        AbstractTopologyProvider provider = new AbstractTopologyProvider("test") {
            
            @Override
            public void save() {
                ; // nothing to do 
            }
            
            @Override
            public void refresh() {
                ; // nothing to do
            }
            
            @Override
            public void load(String filename) throws MalformedURLException, JAXBException {
                for (int i=0; i<10; i++) 
                    addVertex(0, i);
                
                for (int i=0; i<5; i++)
                    addGroup("group"+i, "group");
                
                for (int i=0; i<2; i++)
                    addEdges(new AbstractEdge("test", getNextEdgeId(), getVertices().get(i), getVertices().get(i+1)));
            }
        };
        provider.load(null);
        
        Assert.assertEquals(10, provider.getVerticesWithoutGroups().size());
        Assert.assertEquals(5,  provider.getGroups().size());
        Assert.assertEquals(15, provider.getVertices().size());
        Assert.assertEquals(2,  provider.getEdges().size());
        
        Assert.assertEquals("e2", provider.getNextEdgeId());
        Assert.assertEquals("g5", provider.getNextGroupId());
        Assert.assertEquals("v10", provider.getNextVertexId());
    }
}
