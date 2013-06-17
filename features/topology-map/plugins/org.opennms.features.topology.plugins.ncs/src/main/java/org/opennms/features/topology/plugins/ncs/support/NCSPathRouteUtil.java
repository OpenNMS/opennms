package org.opennms.features.topology.plugins.ncs.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.opennms.features.topology.plugins.ncs.NCSServicePath;
import org.opennms.features.topology.plugins.ncs.xpath.JuniperXPath;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NCSPathRouteUtil {

    
    private NCSComponentRepository m_dao;
    private NodeDao m_nodeDao;

    public NCSPathRouteUtil(NCSComponentRepository dao, NodeDao nodeDao) {
        m_dao = dao;
        m_nodeDao = nodeDao;
    }
    
    public void getServiceName(@JuniperXPath(value="//juniper:ServiceType") String data, Exchange exchange) throws ParserConfigurationException, SAXException, IOException {
        Message in = exchange.getIn();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("serviceType", data);
        header.put("deviceA", in.getHeader("deviceA"));
        header.put("deviceZ", in.getHeader("deviceZ"));
        header.put("foreignId", in.getHeader("foreignId"));
        header.put("foreignSource", in.getHeader("foreignSource"));
        header.put("nodeForeignSource", in.getHeader("nodeForeignSource"));
        exchange.getOut().setHeaders(header);
    }
    
    public NCSServicePath createPath(@JuniperXPath("//juniper:ServicePath") NodeList pathList, Exchange exchange) {
        Message in = exchange.getIn();
        String nodeForeignSource = (String) in.getHeader("nodeForeignSource");
        String serviceForeignSource = (String) in.getHeader("foreignSource");
        Node servicePath = pathList.item(0);
        String deviceA = (String) in.getHeader("deviceA");
        String deviceZ = (String) in.getHeader("deviceZ");
        
        return new NCSServicePath(servicePath, m_dao, m_nodeDao, nodeForeignSource, serviceForeignSource, deviceA, deviceZ);
    }
}
