package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/*
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */
public class NodeRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    
    @Test
    public void testNode() throws Exception {
        // Testing POST
        createNode();
        String url = "/nodes";
        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));
        url += "/1";
        // Testing PUT
        sendPut(url, "sysContact=OpenNMS&assetRecord.manufacturer=Apple&assetRecord.operatingSystem=MacOSX Leopard");
        // Testing GET Single Object
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<sysContact>OpenNMS</sysContact>"));        
        assertTrue(xml.contains("<operatingSystem>MacOSX Leopard</operatingSystem>"));        
        // Testing DELETE
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }

    @Test
    public void testIpInterface() throws Exception {
        createIpInterface();
        String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ipAddress>10.10.10.10</ipAddress>"));
        url += "/10.10.10.10";
        sendPut(url, "isManaged=U");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("isManaged=\"U\""));
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }

    @Test
    public void testSnmpInterface() throws Exception {
        createSnmpInterface();
        String url = "/nodes/1/snmpinterfaces";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ifIndex>6</ifIndex>"));
        url += "/6";
        sendPut(url, "ifName=eth0");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ifName>eth0</ifName>"));
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }

    @Test
    public void testMonitoredService() throws Exception {
        createService();
        String url = "/nodes/1/ipinterfaces/10.10.10.10/services";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<name>ICMP</name>"));
        url += "/ICMP";
        sendPut(url, "status=A");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("status=\"A\""));
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }
    
    @Test
    public void testCategory() throws Exception {
        createCategory();
        String url = "/nodes/1/categories";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("name=\"Routers\""));
        url += "/Routers";
        sendPut(url, "description=My Equipment");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<description>My Equipment</description>"));
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }

    @Test
    public void testNodeComboQuery() throws Exception {
        String url = "/nodes";
        MockHttpServletRequest request = createRequest(GET, url);
        request.addParameter("_dc", "1235761409572");
        request.addParameter("start", "0");
        request.addParameter("limit", "10");
        request.addParameter("query", "hell");
        sendRequest(request, 200);
    }

    private void createNode() throws Exception {
        String node = "<node>" +            
        "<label>TestMachine</label>" +
        "<labelSource>H</labelSource>" +
        "<sysContact>The Owner</sysContact>" +
        "<sysDescription>" +
        "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
        "</sysDescription>" +
        "<sysLocation>DevJam</sysLocation>" +
        "<sysName>TestMachine</sysName>" +
        "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
        "<type>A</type>" +
        "</node>";
        sendPost("/nodes", node);
    }
    
    private void createIpInterface() throws Exception {
        createNode();
        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
        "<ipAddress>10.10.10.10</ipAddress>" +
        "<hostName>TestMachine</hostName>" +
        "<ipStatus>1</ipStatus>" +
        "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface);
    }

    private void createSnmpInterface() throws Exception {
        createIpInterface();
        String snmpInterface = "<snmpInterface>" +
        "<ifAdminStatus>1</ifAdminStatus>" +
        "<ifDescr>en1</ifDescr>" +
        "<ifIndex>6</ifIndex>" +
        "<ifName>en1</ifName>" +
        "<ifOperStatus>1</ifOperStatus>" +
        "<ifSpeed>10000000</ifSpeed>" +
        "<ifType>6</ifType>" +
        "<ipAddress>10.10.10.10</ipAddress>" +
        "<netMask>255.255.255.0</netMask>" +
        "<physAddr>001e5271136d</physAddr>" +
        "</snmpInterface>";
        sendPost("/nodes/1/snmpinterfaces", snmpInterface);
    }
    
    private void createService() throws Exception {
        createIpInterface();
        String service = "<service>" +
        "<notify>Y</notify>" +
        "<serviceType>" +
        "<name>ICMP</name>" +
        "</serviceType>" +
        "<source>P</source>" +
        "<status>N</status>" +
        "</service>";
        sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", service);
    }

    private void createCategory() throws Exception {
        createNode();
        String service = "<category name=\"Routers\">" +
            "<description>Core Routers</description>" +
            "</category>";
        sendPost("/nodes/1/categories", service);
    }

}
