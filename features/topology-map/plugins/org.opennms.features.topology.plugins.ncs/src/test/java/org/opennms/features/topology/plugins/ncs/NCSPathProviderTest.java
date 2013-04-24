package org.opennms.features.topology.plugins.ncs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
public class NCSPathProviderTest extends CamelTestSupport{
    
    protected NCSPathProviderService m_ncsPathService;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        m_ncsPathService = new NCSPathProviderService(new MockNCSComponentRepository(), new MockNodeDao());
    }
    
    @Test
    @JUnitHttpServer(port=10346, webapps=@Webapp(context="/ncs-provider", path="src/test/resources/ncsPathProviderWar"))
    public void testSendMatchingMessage() throws Exception {
        Collection<Endpoint> endpoints = context.getEndpoints();
        Map<String, Object> headers = new HashMap<String,Object>();
        headers.put("provisionid", "space_ServiceProvisioning");
        headers.put("deviceA", "131103");
        headers.put("deviceZ", "688141");
        
        NCSServicePath path = m_ncsPathService.getPath("space_ServiceProvisioning", "131103", "688141");
        assertNotNull(path);
        assertNotNull(path.getVertices());
        assertEquals(2, path.getVertices().size());
        
    }
    
//    @Override
//    protected RouteBuilder createRouteBuilder() {
//        return new RouteBuilder() {
//
//            @Override
//            public void configure() throws Exception {
//                from("direct:start").setHeader(Exchange.HTTP_URI, simple("http://localhost:10346/ncs-provider/app-name?appName=${header.provisionid}")).to("http://dummyhost")
//                .bean(m_ncsPathService.getClass(), "getServiceName")
//                .setHeader(Exchange.HTTP_URI, simple("http://localhost:10346/ncs-provider/${header.serviceType}/service-path?deviceA=${header.deviceA}&deviceZ=${header.deviceZ}")).to("http://dummyhost")
//                .bean(m_ncsPathService.getClass(), "createPath");
//            }
//            
//        };
//    }
//    
}
