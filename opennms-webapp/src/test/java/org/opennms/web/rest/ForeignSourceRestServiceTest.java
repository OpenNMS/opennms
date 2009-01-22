package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ForeignSourceRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    @Test
    public void testForeignSource() throws Exception {
        // Testing POST
        createForeignSource();
        String url = "/foreignSources";
        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("DHCP"));
        url += "/test";
        // Testing PUT
        // /opennms/rest/foreignSources/test/?scan-interval=1h
        sendPut(url, "scanInterval=1h");
        // Testing GET Single Object
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1h</scan-interval>"));        
        // Testing DELETE
        sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1d</scan-interval>"));
    }

    /*
    @Test
    public void testIpInterface() throws Exception {
        createIpInterface();
        String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ipAddress>10.10.10.10</ipAddress>"));
        url += "/10.10.10.10";
        sendPut(url, "ipStatus=0");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ipStatus>0</ipStatus>"));
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
        assertTrue(xml.contains("<status>A</status>"));
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }
    
    @Test
    public void testCategory() throws Exception {
        createCategory();
        String url = "/nodes/1/categories";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<name>Routers</name>"));
        url += "/Routers";
        sendPut(url, "description=My Equipment");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<description>My Equipment</description>"));
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }
    */

    private void createForeignSource() throws Exception {
        String fs =
            "<foreign-source name=\"test\">" +
                "<scan-interval>1d</scan-interval>" +
                "<detectors>" + 
                    "<detector class=\"org.opennms.netmgt.provision.detector.dhcp.DhcpDetector\" name=\"DHCP\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.datagram.DnsDetector\" name=\"DNS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpDetector\" name=\"HTTP\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpsDetector\" name=\"HTTPS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" name=\"ICMP\"/>" +
                "</detectors>" +
                "<policies/>" +
            "</foreign-source>";
        sendPost("/foreignSources", fs);
    }
    
    /*
    private void createIpInterface() throws Exception {
        createNode();
        String ipInterface = "<ipInterface>" +
        "<ipAddress>10.10.10.10</ipAddress>" +
        "<ipHostName>TestMachine</ipHostName>" +
        "<ipStatus>1</ipStatus>" +
        "<isManaged>M</isManaged>" +
        "<isSnmpPrimary>" +
        "<charCode>80</charCode>" +
        "</isSnmpPrimary>" +
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
        String service = "<category>" +
        "<name>Routers</name>" +
        "<description>Core Routers</description>" +
        "</category>";
        sendPost("/nodes/1/categories", service);
    }
    */
}
