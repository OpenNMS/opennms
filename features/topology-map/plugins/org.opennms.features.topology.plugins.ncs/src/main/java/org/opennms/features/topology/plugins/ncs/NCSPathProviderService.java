package org.opennms.features.topology.plugins.ncs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
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
    
    public NCSPathProviderService(NCSComponentRepository componentRepository, NodeDao nodeDao) {
        m_dao = componentRepository;
        m_nodeDao = nodeDao;
        
        try {
            SimpleRegistry simpleRegistry = new SimpleRegistry();
            simpleRegistry.put("pathservice", this);
            m_camelContext = new DefaultCamelContext();

            m_camelContext.addRoutes(new RouteBuilder() {

                @Override
                public void configure() throws Exception {
                    from("direct:start").setHeader(Exchange.HTTP_URI, simple("http://localhost:10346/ncs-provider/app-name?appName=${header.provisionid}")).to("http://dummyhost").bean("pathservice?method=getServiceName")
                    .setHeader(Exchange.HTTP_URI, simple("http://localhost:10346/ncs-provider/${header.serviceType}/service-path?deviceA=${header.deviceA}&deviceZ=${header.deviceZ}")).to("http://dummyhost").bean("pathservice?method=createPath");
                }

            });
            Map<String, Endpoint> endpointMap = m_camelContext.getEndpointMap();
            m_camelContext.start();
            
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
    
    
    public NCSServicePath getPath(String foreignSource, String deviceAForeignId, String deviceZForeignId) throws Exception {
        Map<String, Object> headers = new HashMap<String,Object>();
        headers.put("provisionid", foreignSource);
        headers.put("deviceA", deviceAForeignId);
        headers.put("deviceZ", deviceZForeignId);
        
        m_template = m_camelContext.createProducerTemplate();
        m_template.start();
        return m_template.requestBodyAndHeaders("direct:start", null, headers, NCSServicePath.class);
    }
    
    public void getServiceName(@JuniperXPath(value="//juniper:ServiceType") String data, Exchange exchange) throws ParserConfigurationException, SAXException, IOException {
        Message in = exchange.getIn();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("serviceType", data);
        header.put("deviceA", in.getHeader("deviceA"));
        header.put("deviceZ", in.getHeader("deviceZ"));
        exchange.getOut().setHeaders(header);
    }
    
    public NCSServicePath createPath(@JuniperXPath("//juniper:ServicePath") NodeList pathList, Exchange exchange) {
        Message in = exchange.getIn();
        String serviceType = (String) in.getHeader("serviceType");
        Node servicePath = pathList.item(0);
        
        return new NCSServicePath(servicePath, m_dao, m_nodeDao, serviceType);
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public NCSComponentRepository getDao() {
        return m_dao;
    }

    public void setDao(NCSComponentRepository dao) {
        m_dao = dao;
    }
    
    

}
