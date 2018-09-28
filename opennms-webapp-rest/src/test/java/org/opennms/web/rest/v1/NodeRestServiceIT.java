/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.test.xml.XmlTest.assertXpathMatches;

import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NodeRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(NodeRestServiceIT.class);

    private static int m_nodeCounter = 0;

    @Autowired
    private ServletContext m_context;

    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_nodeCounter = 0;
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testNode() throws Exception {
        // Testing POST
        createNode();
        String url = "/nodes";

        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));
        OnmsNodeList list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(1, list.size());
        assertEquals(xml, "TestMachine0", list.get(0).getLabel());

        // Testing orderBy
        xml = sendRequest(GET, url, parseParamData("orderBy=sysObjectId"), 200);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(1, list.size());
        assertEquals("TestMachine0", list.get(0).getLabel());

        // Add 4 more nodes
        for (m_nodeCounter = 1; m_nodeCounter < 5; m_nodeCounter++) {
            createNode();
        }

        // Testing limit/offset
        xml = sendRequest(GET, url, parseParamData("limit=3&offset=0&orderBy=label"), 200);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(3), list.getCount());
        assertEquals(Integer.valueOf(5), list.getTotalCount());
        assertEquals("TestMachine0", list.get(0).getLabel());
        assertEquals("TestMachine1", list.get(1).getLabel());
        assertEquals("TestMachine2", list.get(2).getLabel());

        // This filter should match
        xml = sendRequest(GET, url, parseParamData("comparator=like&label=%25Test%25"), 200);
        LOG.info(xml);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(Integer.valueOf(5), list.getCount());
        assertEquals(Integer.valueOf(5), list.getTotalCount());

        // This filter should fail (return 0 results)
        xml = sendRequest(GET, url, parseParamData("comparator=like&label=%25DOES_NOT_MATCH%25"), 200);
        LOG.info(xml);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(null, list.getCount());
        assertEquals(Integer.valueOf(0), list.getTotalCount());

        // Testing PUT
        url += "/1";
        sendPut(url, "sysContact=OpenNMS&assetRecord.manufacturer=Apple&assetRecord.operatingSystem=MacOSX Leopard", 204);

        // Testing GET Single Object
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<sysContact>OpenNMS</sysContact>"));
        assertTrue(xml.contains("<operatingSystem>MacOSX Leopard</operatingSystem>"));

        // Testing DELETE
        m_mockEventIpcManager.getEventAnticipator().reset();
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.DELETE_NODE_EVENT_UEI, "Test")
                                                                            .setNodeid(1)
                                                                            .getEvent());

        sendRequest(DELETE, url, 202);

        m_mockEventIpcManager.getEventAnticipator().waitForAnticipated(10000);
        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    
    @Test
    @JUnitTemporaryDatabase
    public void canGetNodesWithFilterRule() throws Exception {
        // Create a node which doesn't have any categories assigned
        createNode();
        // The FilterDao requires requires the node to have at least one IP interface
        createIpInterface();
        String url = "/nodes";

        // Retrieve all nodes with the 'Routers' category, there should be none
        String xml = sendRequest(GET, url, parseParamData("filterRule=catincRouters"), 200);
        OnmsNodeList list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(0, list.size());
        assertEquals(Integer.valueOf(0), list.getTotalCount());

        // Assign the category to the node
        sendRequest(POST, "/nodes/1/categories/Routers", 201);
        xml = sendRequest(GET, "/nodes/1/categories", 200);
        assertTrue(xml.contains("name=\"Routers\""));

        // Now apply try searching with the filter again, we should get a match
        xml = sendRequest(GET, url, parseParamData("filterRule=catincRouters"), 200);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(xml, 1, list.size());
        assertEquals(xml, Integer.valueOf(1), list.getTotalCount());
        assertEquals(xml, "TestMachine0", list.get(0).getLabel());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testPutCoordinates() throws Exception {
        createNode();
        String url = "/nodes/1/assetRecord";
        sendPut(url, "longitude=-1.2345&latitude=6.7890", 204);

        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<longitude>-1.2345"));
        assertTrue(xml.contains("<latitude>6.789"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeJson() throws Exception {
        createSnmpInterface();

        final MockHttpServletRequest req = createRequest(m_context, GET, "/nodes");
        req.addHeader("Accept", MediaType.APPLICATION_JSON);
        req.addParameter("limit", "0");
        String json = sendRequest(req, 200);
        JSONObject jo = new JSONObject(json);
        final JSONArray ja = jo.getJSONArray("node");
        assertEquals(1, ja.length());
        jo = ja.getJSONObject(0);
        assertEquals("A", jo.getString("type"));
        assertEquals("TestMachine0", jo.getString("label"));
        assertEquals("Default", jo.getString("location"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAnotherNodeJson() throws Exception {
        createSnmpInterface();

        final MockHttpServletRequest req = createRequest(m_context, GET, "/nodes");
        req.addHeader("Accept", MediaType.APPLICATION_JSON);
        req.addParameter("limit", "0");
        String json = sendRequest(req, 200);
        String expectedJson = IOUtils.toString(new FileInputStream("src/test/resources/v1/nodes.json"));
        JSONAssert.assertEquals(expectedJson, json,
            new CustomComparator(JSONCompareMode.STRICT,
                // we need to exclude lastModifiedDate since it was just updated in the PUT method:
                new Customization("node[0].assetRecord.lastModifiedDate", (o1, o2) -> true)));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testPutNode() throws Exception {
        JAXBContext context = JAXBContext.newInstance(OnmsNodeList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Testing POST
        createNode();
        String url = "/nodes";

        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));
        OnmsNodeList list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(1, list.size());
        assertEquals("TestMachine0", list.get(0).getLabel());

        // Testing PUT
        url += "/1";
        sendPut(url, "sysContact=OpenNMS&assetRecord.manufacturer=Apple&assetRecord.operatingSystem=MacOSX Leopard", 204);

        // Testing GET Single Object to make sure that the parameters changed
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<sysContact>OpenNMS</sysContact>"));        
        assertTrue(xml.contains("<operatingSystem>MacOSX Leopard</operatingSystem>"));        

        // Testing DELETE
        m_mockEventIpcManager.getEventAnticipator().reset();
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.DELETE_NODE_EVENT_UEI, "Test")
                                                                            .setNodeid(1)
                                                                            .getEvent());

        sendRequest(DELETE, url, 202);

        m_mockEventIpcManager.getEventAnticipator().waitForAnticipated(10000);
        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    @Test
    @JUnitTemporaryDatabase
    public void testPutNodeAsset() throws Exception {
        createNode();
        sendPut("/nodes/1/assetRecord", "description=Right here, Right now", 204);
        String xml = sendRequest(GET, "/nodes/1/assetRecord", 200);
	    assertTrue(xml.contains("<description>Right here, Right now</description>"));
	    assertTrue(xml.matches(".*<id>\\d+</id>.*"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testLimits() throws Exception {
        JAXBContext context = JAXBContext.newInstance(OnmsNodeList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Testing POST
        for (m_nodeCounter = 0; m_nodeCounter < 20; m_nodeCounter++) {
            createNode();
        }
        String url = "/nodes";
        // Testing GET Collection
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("limit", "10");
        parameters.put("orderBy", "id");
        String xml = sendRequest(GET, url, parameters, 200);
        assertTrue(xml, xml.contains("Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0"));
        Pattern p = Pattern.compile("<node [^>]*\\s*id=", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = p.matcher(xml);
        int count = 0;
        while (m.find()) {
            count++;
        }
        assertEquals("should get 10 nodes back", 10, count);

        // Validate object by unmarshalling
        OnmsNodeList list = (OnmsNodeList)unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(Integer.valueOf(10), list.getCount());
        assertEquals(10, list.size());
        assertEquals(Integer.valueOf(20), list.getTotalCount());
        int i = 0;
        Set<OnmsNode> sortedNodes = new TreeSet<OnmsNode>(new Comparator<OnmsNode>() {
            @Override
            public int compare(OnmsNode o1, OnmsNode o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                } else {
                    if (o1.getId() == null) {
                        throw new IllegalStateException("Null ID on node: " + o1.toString());
                    }
                    return o1.getId().compareTo(o2.getId());
                }
            }
        });
        // Sort the nodes by ID
        sortedNodes.addAll(list.getObjects());
        for (OnmsNode node : sortedNodes) {
            assertEquals(node.toString(), "TestMachine" + i++, node.getLabel());
        }
    }

    @Test
    @JUnitTemporaryDatabase
    public void testIpInterface() throws Exception {
        createIpInterface();
        String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ipAddress>10.10.10.10</ipAddress>"));
        url += "/10.10.10.10";
        sendPut(url, "isManaged=U", 204);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("isManaged=\"U\""));

        m_mockEventIpcManager.getEventAnticipator().reset();
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.DELETE_INTERFACE_EVENT_UEI, "Test")
                                                                            .setNodeid(1)
                                                                            .setInterface(InetAddressUtils.addr("10.10.10.10"))
                                                                            .getEvent());

        sendRequest(DELETE, url, 202);

        m_mockEventIpcManager.getEventAnticipator().waitForAnticipated(10000);
        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated();
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testIpInterfaceJson() throws Exception {
        createIpInterface();
        String url = "/nodes/1/ipinterfaces";

        final MockHttpServletRequest req = createRequest(m_context, GET, url);
        req.addHeader("Accept", MediaType.APPLICATION_JSON);
        req.addParameter("limit", "0");
        final String json = sendRequest(req, 200);
        assertNotNull(json);
        assertFalse(json.contains("The Owner"));
        JSONObject jo = new JSONObject(json);
        JSONArray ja = jo.getJSONArray("ipInterface");
        assertEquals(1, ja.length());
        jo = ja.getJSONObject(0);
        assertTrue(jo.isNull("ifIndex"));
        assertEquals("10.10.10.10", jo.getString("ipAddress"));
        assertEquals(1, jo.getInt("nodeId"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testIpInterfaceLimit() throws Exception{
        createTwoIpInterface();
        final String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, parseParamData("limit=1"), 200);
        assertTrue(xml.contains("count=\"1\""));
        
        sendPut(url + "/10.10.10.10", "isManaged=U", 204);
        xml = sendRequest(GET, url + "/10.10.10.10", 200);
        assertTrue(xml.contains("isManaged=\"U\""));

        m_mockEventIpcManager.getEventAnticipator().reset();
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.DELETE_INTERFACE_EVENT_UEI, "Test")
                                                                            .setNodeid(1)
                                                                            .setInterface(InetAddressUtils.addr("10.10.10.10"))
                                                                            .getEvent());

        sendRequest(DELETE, url + "/10.10.10.10", 202);

        m_mockEventIpcManager.getEventAnticipator().waitForAnticipated(10000);
        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated();
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testIpInterfaceByIpAddress() throws Exception{
        createTwoIpInterface();
        String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, parseParamData("ipAddress=11&comparator=contains"), 200);
        assertTrue(xml.contains("count=\"1\""));
        
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testIpInterfaceIpLikeFilter() throws Exception{
        createTwoIpInterface();
        String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, parseParamData("ipAddr=*.*.*.11&comparator=iplike"), 200);
        assertTrue(xml.contains("count=\"1\""));
        
    }

    @Test
    @JUnitTemporaryDatabase
    public void testSnmpInterface() throws Exception {
        createSnmpInterface();
        String url = "/nodes/1/snmpinterfaces";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("ifIndex=\"6\""));
        url += "/6";
        sendPut(url, "ifName=eth0", 204);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<ifName>eth0</ifName>"));
        sendRequest(DELETE, url, 204);
        sendRequest(GET, url, 404);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testSnmpInterfaceJson() throws Exception {
        createSnmpInterface();
        String url = "/nodes/1/snmpinterfaces";

        final MockHttpServletRequest req = createRequest(m_context, GET, url);
        req.addHeader("Accept", MediaType.APPLICATION_JSON);
        req.addParameter("limit", "0");
        final String json = sendRequest(req, 200);
        assertNotNull(json);
        assertFalse(json.contains("The Owner"));

        JSONObject jo = new JSONObject(json);
        final JSONArray ja = jo.getJSONArray("snmpInterface");
        assertEquals(1, ja.length());
        jo = ja.getJSONObject(0);
        assertEquals(6, jo.getInt("ifIndex"));
        assertEquals(1, jo.getInt("ifOperStatus"));
        assertEquals("en1", jo.getString("ifDescr"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testMonitoredService() throws Exception {
        createService();
        String url = "/nodes/1/ipinterfaces/10.10.10.10/services";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<name>ICMP</name>"));
        url += "/ICMP";
        sendPut(url, "status=A", 204);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("status=\"A\""));

        m_mockEventIpcManager.getEventAnticipator().reset();
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.DELETE_SERVICE_EVENT_UEI, "Test")
                                                                            .setNodeid(1)
                                                                            .setInterface(InetAddressUtils.addr("10.10.10.10"))
                                                                            .setService("ICMP")
                                                                            .getEvent());

        sendRequest(DELETE, url, 202);

        m_mockEventIpcManager.getEventAnticipator().waitForAnticipated(10000);
        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testCategory() throws Exception {
        createNode();

        String xml = sendRequest(GET, "/nodes/1/categories", 200);
        assertFalse(xml.contains("name=\"Routers\""));

        // add category to node 
        sendRequest(POST, "/nodes/1/categories/Routers", 201);
        xml = sendRequest(GET, "/nodes/1/categories", 200);
        assertTrue(xml.contains("name=\"Routers\""));
        
        // add category to node (again)
        sendRequest(POST, "/nodes/1/categories/Routers", 400); // should fail
        
        // change category description via PUT to categories path
        sendPut("/categories/Routers", "description=My Equipment", 204);
        xml = sendRequest(GET, "/nodes/1/categories/Routers", 200);
        assertTrue(xml.contains("<description>My Equipment</description>"));

        // Change category description via PUT to node path
        sendPut("/nodes/1/categories/Routers", "description=My Equipment UPDATED", 204);
        xml = sendRequest(GET, "/nodes/1/categories/Routers", 200);
        assertTrue(xml.contains("<description>My Equipment UPDATED</description>"));

        // cleanup up...
        sendRequest(DELETE, "/nodes/1/categories/Routers", 204);
        sendRequest(GET, "/nodes/1/categories/Routers", 404); // verify...
        
        // ... ensure that category is not deleted, only association is removed
        xml = sendRequest(GET, "/categories/Routers", 200);
        assertNotNull(xml);
        assertTrue(xml.contains("<description>My Equipment UPDATED</description>"));
        assertTrue(xml.contains("name=\"Routers\""));
        
        // try backwards compatibility
        sendPost("/nodes/1/categories/", JaxbUtils.marshal(new OnmsCategory("Routers")), 201, "/nodes/1/categories/Routers");
        xml = sendRequest(GET, "/nodes/1/categories/Routers", 200);
        assertTrue(xml.contains("<description>My Equipment UPDATED</description>"));
        
        // and clean up again
        sendRequest(DELETE, "/nodes/1/categories/Routers", 204);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeComboQuery() throws Exception {
        String url = "/nodes";
        MockHttpServletRequest request = createRequest(m_context, GET, url);
        request.addParameter("_dc", "1235761409572");
        request.addParameter("start", "0");
        request.addParameter("limit", "10");
        request.addParameter("query", "hell");
        sendRequest(request, 200);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testIPhoneNodeSearch() throws Exception {
        createIpInterface();
        String url = "/nodes";
        String xml = sendRequest(GET, url, parseParamData("comparator=ilike&match=any&label=1%25&ipInterface.ipAddress=1%25&ipInterface.ipHostName=1%25"), 200);
        System.err.println("XXXX> " + xml);
        assertXpathMatches(xml, "//node[@type='A' and @id='1' and @label='TestMachine0']");
        assertTrue(xml, xml.contains("count=\"1\""));
        assertTrue(xml, xml.contains("totalCount=\"1\""));

        xml = sendRequest(GET, url, parseParamData("comparator=ilike&match=any&label=8%25&ipInterface.ipAddress=8%25&ipInterface.ipHostName=8%25"), 200);
        // Make sure that there were no matches
        assertTrue(xml, xml.contains("totalCount=\"0\""));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeWithoutHardwareInventory() throws Exception {
        createIpInterface();
        sendRequest(GET, "/nodes/1/hardwareInventory", 404); // node doesn't have a root entity
    }

    @Test
    @JUnitTemporaryDatabase
    public void testHardwareInventory() throws Exception {
        // Try to fetch inventory on a non-existent node
        sendRequest(GET, "/nodes/1000000/hardwareInventory", 400);

        createIpInterface();
        byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/hardware-inventory.xml"));
        String entity = new String(encoded, StandardCharsets.UTF_8);
        sendPost("/nodes/1/hardwareInventory", entity, 204, null);
        String xml = sendRequest(GET, "/nodes/1/hardwareInventory", 200);
        assertTrue(xml, xml.contains("Cisco 7206VXR, 6-slot chassis"));

        xml = sendRequest(GET, "/nodes/1/hardwareInventory/42", 200);
        assertTrue(xml, xml.contains("Cisco 7200 AC Power Supply"));

        Map<String, String> params = new HashMap<String,String>();
        params.put("entPhysicalSerialNum", "123456789");
        params.put("ceExtProcessorRam", "256MB");
        sendRequest(PUT, "/nodes/1/hardwareInventory/9", params, 204);
        xml = sendRequest(GET, "/nodes/1/hardwareInventory/9", 200);
        assertTrue(xml, xml.contains("<entPhysicalSerialNum>123456789</entPhysicalSerialNum>"));
        assertTrue(xml, xml.contains("value=\"256MB\""));

        sendPost("/nodes/1/hardwareInventory/9", "<hwEntity entPhysicalIndex=\"200\"><entPhysicalName>Sample1</entPhysicalName></hwEntity>", 204, null);
        sendPost("/nodes/1/hardwareInventory/9", "<hwEntity entPhysicalIndex=\"17\"><entPhysicalName>Sample2</entPhysicalName></hwEntity>", 204, null);
        xml = sendRequest(GET, "/nodes/1/hardwareInventory/9", 200);
        assertTrue(xml, xml.contains("Sample1"));
        assertTrue(xml, xml.contains("Sample2"));

        sendRequest(DELETE, "/nodes/1/hardwareInventory/9", 204);
        sendRequest(GET, "/nodes/1/hardwareInventory/9", 404);
    }

    @Test
    @JUnitTemporaryDatabase
    @Ignore
    public void testMetricsResource() throws Exception {
        createIpInterface();
        System.err.println("testMetricsResource(): createIpInterface()");
        String url = "/nodes/1/metrics";
        System.err.println("sendRequest('GET', '"+url+"', 200);");
        String xml = sendRequest(GET, url, 200);
        System.err.println(xml);
        assertTrue(xml.contains("<name>ICMP</name>"));
        url += "/ICMP";
        sendPut(url, "status=A", 204);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("status=\"A\""));
        sendRequest(DELETE, url, 204);
        sendRequest(GET, url, 404);
    }

    @Override
    protected void createNode() throws Exception {
        String node = "<node type=\"A\" label=\"TestMachine" + m_nodeCounter + "\">" +
        "<labelSource>H</labelSource>" +
        "<sysContact>The Owner</sysContact>" +
        "<sysDescription>" +
        "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
        "</sysDescription>" +
        "<sysLocation>DevJam</sysLocation>" +
        "<sysName>TestMachine" + m_nodeCounter + "</sysName>" +
        "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
        // Add some constant values for these timestamps
        "<createTime>2011-09-24T07:12:46.421-04:00</createTime>" +
        "<lastCapsdPoll>2011-09-24T07:12:46.421-04:00</lastCapsdPoll>" +
        "</node>";

        HttpServletResponse response = sendPost("/nodes", node, 201, null);

        // Set the asset record's lastModifiedDate to a constant value as well
        String newNodeLocation = response.getHeader("Location");
        String nodeId = newNodeLocation.substring(newNodeLocation.lastIndexOf("/"));
        sendPut("/nodes" + nodeId + "/assetRecord", "lastModifiedDate=2011-09-24T07:12:46.421-04:00", 204);
    }

    @Override
    protected void createIpInterface() throws Exception {
        createNode();
        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
        "<ipAddress>10.10.10.10</ipAddress>" +
        "<hostName>TestMachine" + m_nodeCounter + "</hostName>" +
        "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface, 201, "/nodes/1/ipinterfaces/10.10.10.10");
    }
    
    
    protected void createTwoIpInterface() throws Exception {
        createNode();
        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
        "<ipAddress>10.10.10.10</ipAddress>" +
        "<hostName>TestMachine" + m_nodeCounter + "</hostName>" +
        "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface, 201, "/nodes/1/ipinterfaces/10.10.10.10");
        
        String ipInterface2 = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
        "<ipAddress>10.10.10.11</ipAddress>" +
        "<hostName>TestMachine" + (m_nodeCounter + 1) + "</hostName>" +
        "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface2, 201, "/nodes/1/ipinterfaces/10.10.10.11");
        
    }

}
