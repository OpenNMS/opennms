package org.opennms.features.topology.plugins.ncs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.opennms.features.topology.plugins.ncs.xpath.JuniperXPath;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NCSPathProviderService {

    private NodeDao m_nodeDao;
    private NCSComponentRepository m_dao;
    private CamelContext m_camelContext;
    private ProducerTemplate m_template;
    private String m_baseUrl;
    
    public NCSPathProviderService(NCSComponentRepository componentRepository, NodeDao nodeDao, String baseUrl) {
        m_dao = componentRepository;
        m_nodeDao = nodeDao;
        m_baseUrl = baseUrl;
        
        try {
            SimpleRegistry simpleRegistry = new SimpleRegistry();
            simpleRegistry.put("pathservice", this);
            m_camelContext = new DefaultCamelContext(simpleRegistry);

            m_camelContext.addRoutes(new RouteBuilder() {

                @Override
                public void configure() throws Exception {
                    from("direct:start").setHeader(Exchange.HTTP_URI, simple(m_baseUrl + "/api/space/nsas/service-management/services/${header.foreignId}?appName=${header.foreignSource}")).to("http://dummyhost")
                    .beanRef("pathservice", "getServiceName")
                    .setHeader(Exchange.HTTP_URI, simple(m_baseUrl + "/api/space/nsas/${headers.serviceType}/service-management/services/${header.foreignId}/servicepath?deviceA=${header.deviceA}&deviceZ=${header.deviceZ}")).to("http://dummyhost")
                    .beanRef("pathservice", "createPath");
                }

            });
            m_camelContext.start();
            
            m_template = m_camelContext.createProducerTemplate();
            m_template.start();
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
    
    
    public NCSServicePath getPath(String foreignId, String foreignSource, String deviceAForeignId, String deviceZForeignId) throws Exception {
        Map<String, Object> headers = new HashMap<String,Object>();
        headers.put("foreignId", foreignId);
        headers.put("foreignSource", foreignSource);
        headers.put("deviceA", deviceAForeignId);
        headers.put("deviceZ", deviceZForeignId);
        
        
        return m_template.requestBodyAndHeaders("direct:start", null, headers, NCSServicePath.class);
    }
    
    public void getServiceName(@JuniperXPath(value="//juniper:ServiceType") String data, Exchange exchange) throws ParserConfigurationException, SAXException, IOException {
        Message in = exchange.getIn();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("serviceType", data);
        header.put("deviceA", in.getHeader("deviceA"));
        header.put("deviceZ", in.getHeader("deviceZ"));
        header.put("foreignId", in.getHeader("foreignId"));
        exchange.getOut().setHeaders(header);
    }
    
    public NCSServicePath createPath(@JuniperXPath("//juniper:ServicePath") NodeList pathList, Exchange exchange) {
        Message in = exchange.getIn();
        String serviceType = (String) in.getHeader("serviceType");
        Node servicePath = pathList.item(0);
        
        return new NCSServicePath(servicePath, m_dao, m_nodeDao, serviceType);
    }


}
