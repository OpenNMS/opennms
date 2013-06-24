package org.opennms.features.topology.plugins.ncs;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.LoggerFactory;

public class NCSPathProviderService {

    private CamelContext m_camelContext;
    private ProducerTemplate m_template;
    
    public NCSPathProviderService(CamelContext camelContext) {
        m_camelContext = camelContext;
        try {
            
            m_template = m_camelContext.createProducerTemplate();
            m_template.start();
            
        } catch (Exception e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception Occurred while creating route: ", e);
        }
        
        
    }
    
    public NCSServicePath getPath(String foreignId, String foreignSource, String deviceAForeignId, String deviceZForeignId, String nodeForeignSource, String serviceName) throws Exception {
        Map<String, Object> headers = new HashMap<String,Object>();
        headers.put("foreignId", foreignId);
        headers.put("foreignSource", foreignSource);
        headers.put("deviceA", deviceAForeignId);
        headers.put("deviceZ", deviceZForeignId);
        headers.put("nodeForeignSource", nodeForeignSource);
        headers.put("serviceName", serviceName);
        
        return m_template.requestBodyAndHeaders("direct:start", null, headers, NCSServicePath.class);
    }
    

}
