package org.opennms.features.topology.plugins.ncs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
public class NCSPathProviderTest{
    
    protected NCSPathProviderService m_ncsPathService;
    
    @Before
    public void setUp() throws Exception {
        m_ncsPathService = new NCSPathProviderService(new MockNCSComponentRepository(), new MockNodeDao(), "http://localhost:10346/ncs-provider");
    }
    
    @Test
    @JUnitHttpServer(port=10346, webapps=@Webapp(context="/ncs-provider", path="src/test/resources/ncsPathProviderWar"))
    public void testSendMatchingMessage() throws Exception {
        
        NCSServicePath path = m_ncsPathService.getPath("884779", "space_ServiceProvisioning", "131103", "688141");
        assertNotNull(path);
        assertNotNull(path.getVertices());
        assertEquals(2, path.getVertices().size());
        assertNotNull(path.getEdges());
        assertEquals(1, path.getEdges().size());
        
        NCSServicePath path2 = m_ncsPathService.getPath("884779", "space_ServiceProvisioning", "688141", "131103");
        assertNotNull(path2);
        assertNotNull(path2.getVertices());
        assertEquals(3, path2.getVertices().size());
        assertNotNull(path2.getEdges());
        assertEquals(2, path2.getEdges().size());
        
    }
    
}
