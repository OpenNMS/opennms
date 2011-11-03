/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.test.mock.MockLogAppender;

public class UserRestServiceTest extends AbstractSpringJerseyRestTestCase {

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        MockLogAppender.setupLogging();
    }

    @Test
    public void testUser() throws Exception {
        String url = "/users";

        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("admin"));
        OnmsUserList list = JaxbUtils.unmarshal(OnmsUserList.class, xml);
        assertEquals(1, list.getUsers().size());
        assertEquals(xml, "admin", list.getUsers().get(0).getUsername());

        xml = sendRequest(GET, url + "/admin", 200);
        assertTrue(xml.contains("admin"));

        createUser("test");
        
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<user><user-id>test</user-id>"));
        
        /*
        // Testing orderBy
        xml = sendRequest(GET, url, parseParamData("orderBy=sysObjectId"), 200);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(1, list.getNodes().size());
        assertEquals("TestMachine0", list.getNodes().get(0).getLabel());

        // Add 4 more nodes
        for (m_nodeCounter = 1; m_nodeCounter < 5; m_nodeCounter++) {
            createNode();
        }

        // Testing limit/offset
        xml = sendRequest(GET, url, parseParamData("limit=3&offset=0&orderBy=label"), 200);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(3, list.getNodes().size());
        assertEquals(3, list.getCount());
        assertEquals(5, list.getTotalCount());
        assertEquals("TestMachine0", list.getNodes().get(0).getLabel());
        assertEquals("TestMachine1", list.getNodes().get(1).getLabel());
        assertEquals("TestMachine2", list.getNodes().get(2).getLabel());

        // This filter should match
        xml = sendRequest(GET, url, parseParamData("comparator=like&label=%25Test%25"), 200);
        LogUtils.infof(this, xml);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(5, list.getCount());
        assertEquals(5, list.getTotalCount());

        // This filter should fail (return 0 results)
        xml = sendRequest(GET, url, parseParamData("comparator=like&label=%25DOES_NOT_MATCH%25"), 200);
        LogUtils.infof(this, xml);
        list = JaxbUtils.unmarshal(OnmsNodeList.class, xml);
        assertEquals(0, list.getCount());
        assertEquals(0, list.getTotalCount());

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
        */
    }

    /*
    @Test
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
        assertEquals(1, list.getNodes().size());
        assertEquals("TestMachine0", list.getNodes().get(0).getLabel());

        // Testing PUT
        url += "/1";
        sendPut(url, "sysContact=OpenNMS&assetRecord.manufacturer=Apple&assetRecord.operatingSystem=MacOSX Leopard");

        // Testing GET Single Object to make sure that the parameters changed
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
        assertEquals(10, list.getCount());
        assertEquals(10, list.getNodes().size());
        assertEquals(20, list.getTotalCount());
        int i = 0;
        Set<OnmsNode> sortedNodes = new TreeSet<OnmsNode>(new Comparator<OnmsNode>() {
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
        sortedNodes.addAll(list.getNodes());
        for (OnmsNode node : sortedNodes) {
            assertEquals(node.toString(), "TestMachine" + i++, node.getLabel());
        }
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
    public void testIpInterfaceLimit() throws Exception{
        createTwoIpInterface();
        String url = "/nodes/1/ipinterfaces";
        String xml = sendRequest(GET, url, parseParamData("limit=1"), 200);
        assertTrue(xml.contains("count=\"1\""));
        
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

    @Test
    public void testIPhoneNodeSearch() throws Exception {
        createIpInterface();
        String url = "/nodes";
        String xml = sendRequest(GET, url, parseParamData("comparator=ilike&match=any&label=1%25&ipInterface.ipAddress=1%25&ipInterface.ipHostName=1%25"), 200);
        assertTrue(xml, xml.contains("<node type=\"A\" id=\"1\" label=\"TestMachine0\">"));
        assertTrue(xml, xml.contains("count=\"1\""));
        assertTrue(xml, xml.contains("totalCount=\"1\""));

        xml = sendRequest(GET, url, parseParamData("comparator=ilike&match=any&label=8%25&ipInterface.ipAddress=8%25&ipInterface.ipHostName=8%25"), 200);
        // Make sure that there were no matches
        assertTrue(xml, xml.contains("count=\"0\""));
        assertTrue(xml, xml.contains("totalCount=\"0\""));
    }

    protected void createTwoIpInterface() throws Exception {
        createNode();
        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
        "<ipAddress>10.10.10.10</ipAddress>" +
        "<hostName>TestMachine" + m_nodeCounter + "</hostName>" +
        "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface);
        
        String ipInterface2 = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
        "<ipAddress>10.10.10.11</ipAddress>" +
        "<hostName>TestMachine" + (m_nodeCounter + 1) + "</hostName>" +
        "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface2);
        
    }
    */

    protected void createUser(final String username) throws Exception {
        String user = "<user>" +
                "<user-id>" + username + "</user-id>" +
                "<full-name>" + username + " Full Name</full-name>" +
                "<user-comments>Autogenerated by a unit test...</user-comments>" +
                "<password>21232F297A57A5A743894A0E4A801FC3</password>" +
                "</user>";
        sendPost("/users", user);
    }
    

}
