package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.mock.web.MockHttpServletRequest;

/*
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */
public class NodeRestServiceTest extends AbstractSpringJerseyRestTestCase {

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        MockLogAppender.setupLogging();
    }

    @Test
    public void testNode() throws Exception {
        JAXBContext context = JAXBContext.newInstance(OnmsNodeList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Testing POST
        createNode();
        String url = "/nodes";

        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));
        OnmsNodeList list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(1, list.getNodes().size());

        // Testing orderBy
        xml = sendRequest(GET, url, parseParamData("orderBy=sysObjectId"), 200);
        list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(1, list.getNodes().size());

        // Testing limit/offset
        xml = sendRequest(GET, url, parseParamData("limit=0&offset=3&orderBy=label"), 200);
        list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(1, list.getNodes().size());

        // This filter should match
        xml = sendRequest(GET, url, parseParamData("comparator=like&label=%25Test%25"), 200);
        LogUtils.infof(this, xml);
        list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(1, list.getCount());
        assertEquals(1, list.getTotalCount());

        // This filter should fail (return 0 results)
        // TODO: Make this test work properly
        xml = sendRequest(GET, url, parseParamData("comparator=like&label=%25DOES_NOT_MATCH%25"), 200);
        LogUtils.infof(this, xml);
        list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        //assertEquals(0, list.getCount());
        //assertEquals(0, list.getTotalCount());

        // Testing PUT
        url += "/1";
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
    public void testLimits() throws Exception {
        JAXBContext context = JAXBContext.newInstance(OnmsNodeList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Testing POST
        for (int i = 0; i < 20; i++) {
            createNode();
        }
        String url = "/nodes";
        // Testing GET Collection
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("limit", "10");
        String xml = sendRequest(GET, url, parameters, 200);
        assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));
        Pattern p = Pattern.compile("<node>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = p.matcher(xml);
        int count = 0;
        while (m.find()) {
            count++;
        }
        assertEquals("should get 10 nodes back", 10, count);
        
        // Validate object by unmarshalling
        OnmsNodeList list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(10, list.getCount());
        assertEquals(10, list.getNodes().size());
        assertEquals(20, list.getTotalCount());
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
        assertTrue(xml.contains("ifIndex=\"6\""));
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
        String snmpInterface = "<snmpInterface ifIndex=\"6\">" +
        "<ifAdminStatus>1</ifAdminStatus>" +
        "<ifDescr>en1</ifDescr>" +
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
