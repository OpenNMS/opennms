package org.opennms.features.topology.plugins.ncs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.features.topology.plugins.ncs.support.NCSPathRouteUtil;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
public class NCSPathProviderTest{
    
    protected NCSPathProviderService m_ncsPathService;
    
    @Before
    public void setUp() throws Exception {
        NCSPathRouteUtil pathUtil = new NCSPathRouteUtil(new MockNCSComponentRepository(), new MockNodeDao());
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("pathProviderUtil", pathUtil);
        CamelContext camel = new DefaultCamelContext(registry);
        camel.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:start").setHeader(Exchange.HTTP_URI, simple("http://localhost:10346/ncs-provider/api/space/nsas/service-management/services/884779")).to("http://dummyhost")
                    .beanRef("pathProviderUtil", "getServiceName")
                    .setHeader(Exchange.HTTP_URI, simple("http://localhost:10346/ncs-provider/api/space/nsas/eline-ptp/service-management/services/884779/servicepath?deviceA=${header.deviceA}&deviceZ=${header.deviceZ}")).to("http://dummyhost")
                    .beanRef("pathProviderUtil", "createPath");
            }
            
        });
        
        camel.start();
        m_ncsPathService = new NCSPathProviderService(camel);
    }
    
    @Test
    @JUnitHttpServer(port=10346, webapps=@Webapp(context="/ncs-provider", path="src/test/resources/ncsPathProviderWar"))
    public void testSendMatchingMessage() throws Exception {
        
        NCSServicePath path = m_ncsPathService.getPath("884779", "space_ServiceProvisioning", "131103", "688141", "foSource", null);
        assertNotNull(path);
        assertEquals(200, path.getStatusCode());
        assertNotNull(path.getVertices());
        assertEquals(2, path.getVertices().size());
        assertNotNull(path.getEdges());
        assertEquals(1, path.getEdges().size());
        
        NCSServicePath path2 = m_ncsPathService.getPath("884779", "space_ServiceProvisioning", "688141", "131103", "foSource", null);
        assertNotNull(path2);
        assertEquals(200, path.getStatusCode());
        assertNotNull(path2.getVertices());
        assertEquals(3, path2.getVertices().size());
        assertNotNull(path2.getEdges());
        assertEquals(2, path2.getEdges().size());
        
    }
    
    @Test
    @JUnitHttpServer(port=10346, webapps=@Webapp(context="/ncs-provider", path="src/test/resources/ncsPathProviderWar"))
    public void testErrorCode() throws Exception {
        NCSServicePath path = m_ncsPathService.getPath("884779", "space_ServiceProvisioning", "131103", "error", "foSource", null);
        assertNotNull(path);
        assertEquals(500, path.getStatusCode());
        assertNotNull(path.getVertices());
        assertEquals(2, path.getVertices().size());
        assertNotNull(path.getEdges());
        assertEquals(1, path.getEdges().size());
        
    }
    
    
}
