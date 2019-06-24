/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

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
public class RequisitionRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    MockEventIpcManager m_eventProxy;

    @Test
    public void testRequisition() throws Exception {
        cleanUpImports();

        createRequisition();
        String url = "/requisitions";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Management interface"));

        url = "/requisitions/test";

        sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
    }

    @Test
    public void testDuplicateNodes() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        String req =
            "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"test\">" +
                "<node node-label=\"a\" foreign-id=\"a\" />" +
                "<node node-label=\"b\" foreign-id=\"c\" />" +
                "<node node-label=\"c\" foreign-id=\"c\" />" +
            "</model-import>";

        final MockHttpServletResponse response = sendPost("/requisitions", req, 400, null);
        assertTrue("response should say 'c' has duplicates",  response.getContentAsString().contains("Duplicate nodes found on foreign source test: c (2 found)"));
    }

    @Test
    public void testNodes() throws Exception {
        createRequisition();

        String url = "/requisitions/test/nodes";

        // create a node
        sendPost(url, "<node xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" node-label=\"shoe\" parent-node-label=\"david\" foreign-id=\"1111\" />", 202, "/test/nodes/1111");

        // get list of nodes
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<node "));
        assertTrue(xml.contains("node-label="));
        assertTrue(xml.contains("count=\"2\""));
        
        // get individual node
        url = "/requisitions/test/nodes/4243";
        xml = sendRequest(GET, url, 200);
        System.err.println(xml);
        assertTrue(xml.contains(" parent-node-label=\"apknd\""));
        assertTrue(xml.contains(" node-label=\"david\"")); // The white space is required to avoid match parent-node-label
        
        // set attributes
        sendPut(url, "node-label=homo+sapien", 202, "/nodes/4243");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("node-label=\"homo sapien\""));

        // delete node
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
    }

    @Test
    public void testAddExistingNode() throws Exception {
        createRequisition();

        String url = "/requisitions/test/nodes";

        // attempt to add existing node
        sendPost(url, "<node xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" node-label=\"shoe\" parent-node-label=\"david\" foreign-id=\"4243\" />", 202, "/requisitions/test/nodes/4243");

        // get list of nodes
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<node "));
        assertTrue(xml.contains("node-label=\"shoe\"")); // verify that the node was modified instead of created.
        assertTrue("Expected only 1 node", xml.contains("count=\"1\""));
        
    }

    @Test
    public void testNodeInterfaces() throws Exception {
        createRequisition();
        
        String base = "/requisitions/test/nodes/4243/interfaces";
        String xml;
        
        // create an interface
        sendPost(base, "<interface xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" status=\"1\" snmp-primary=\"S\" ip-addr=\"192.0.2.254\" descr=\"Monkey\"><monitored-service service-name=\"ICMP\"/></interface>", 202, "/nodes/4243/interfaces/192.0.2.254");
        sendPost(base, "<interface xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" status=\"1\" snmp-primary=\"S\" ip-addr=\"192.0.2.254\" descr=\"Blah\"><monitored-service service-name=\"ICMP\"/></interface>", 202, "/nodes/4243/interfaces/192.0.2.254");

        // get list of interfaces
        xml = sendRequest(GET, base, 200);
        assertTrue(xml, xml.contains("count=\"3\""));
        assertTrue(xml, xml.contains("<interface "));
        assertTrue(xml, xml.contains("Blah"));
        assertFalse(xml, xml.contains("Monkey"));

        // get individual interface
        String url = base + "/192.0.2.204";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("<interface "));
        assertTrue(xml, xml.contains("VPN interface"));
        assertFalse(xml, xml.contains("192.0.2.201"));

        // set attributes
        sendPut(url, "status=3&descr=Total+Crap&snmp-primary=P", 202, "/nodes/4243/interfaces/192.0.2.204");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("descr=\"Total Crap\""));
        assertTrue(xml, xml.contains("snmp-primary=\"P\""));
        assertTrue(xml, xml.contains("status=\"3\""));
 
        // delete interface
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);

        // confirm there is one less interface
        xml = sendRequest(GET, base, 200);
        assertTrue(xml, xml.contains("count=\"2\""));
    }

    @Test
    public void testNodeInterfaceServices() throws Exception {
        createRequisition();
        
        String base = "/requisitions/test/nodes/4243/interfaces/192.0.2.204/services";
        
        // create a service
        sendPost(base, "<monitored-service xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" service-name=\"MONKEY\" />", 202, "/interfaces/192.0.2.204/services/MONKEY");

        // get list of services
        String xml = sendRequest(GET, base, 200);
        assertTrue(xml.contains("count=\"3\""));
        assertTrue(xml.contains("ICMP"));

        // get individual service
        String url = base + "/ICMP";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("service-name=\"ICMP\""));

        // delete interface
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);

        // confirm there is one less interface
        xml = sendRequest(GET, base, 200);
        assertTrue(xml.contains("count=\"2\""));
    }
    
    @Test
    public void testNodeCategories() throws Exception {
        createRequisition();

        String base = "/requisitions/test/nodes/4243/categories";

        // create a category
        sendPost(base, "<category xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" name=\"Dead Servers\" />", 202, "/nodes/4243/categories/Dead%20Servers");

        // get list of categories
        String url = base;
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("count=\"4\""));
        assertTrue(xml.contains("name=\"low\""));
        assertTrue(xml.contains("name=\"Dead Servers\""));
        
        // get individual category
        url = "/requisitions/test/nodes/4243/categories/low";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("name=\"low\""));
        
        // delete category
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
        
        // confirm there are less categories
        xml = sendRequest(GET, "/requisitions/test/nodes/4243/categories", 200);
        assertTrue(xml.contains("count=\"3\""));

        // create a category on a node that is not in the requisition
        base = "/requisitions/test/nodes/4244/categories";
        sendPost(base, "<category xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" name=\"New Category\" />", 202, "/nodes/4244/categories/New%20Category");
        xml = sendRequest(GET, base + "/New%20Category", 404);
    }
    
    @Test
    public void testNodeAssets() throws Exception {
        createRequisition();
        
        String base = "/requisitions/test/nodes/4243/assets";

        // create an asset
        sendPost(base, "<asset xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" name=\"manufacturer\" value=\"Dead Servers, Inc.\" />", 202, "/nodes/4243/assets/manufacturer");

        // get list of asset parameters
        String url = base;
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("count=\"3\""));
        assertTrue(xml, xml.contains("Windows Pi"));
        
        // get individual asset parameter
        url = "/requisitions/test/nodes/4243/assets/operatingSystem";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("value=\"Windows Pi\""));
        
        // delete asset parameter
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
        
        // confirm there are less assets
        xml = sendRequest(GET, "/requisitions/test/nodes/4243/assets", 200);
        assertTrue(xml, xml.contains("count=\"2\""));
    }

    @Test
    public void testCreateRequisitionNoNamespace() throws Exception {
        String req =
            "<model-import date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"test\">" +
                "<node node-label=\"david\" parent-node-label=\"apknd\" foreign-id=\"4243\">" +
                    "<interface ip-addr=\"192.0.2.204\" status=\"1\" snmp-primary=\"S\" descr=\"VPN interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"HTTP\"/>" +
                    "</interface>" +
                    "<interface ip-addr=\"192.0.2.201\" status=\"1\" snmp-primary=\"P\" descr=\"Management interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"SNMP\"/>" +
                    "</interface>" +
                    "<category name=\"AC\"/>" +
                    "<category name=\"UK\"/>" +
                    "<category name=\"low\"/>" +
                    "<asset name=\"manufacturer\" value=\"Dell\" />" +
                    "<asset name=\"operatingSystem\" value=\"Windows Pi\" />" +
                    "<asset name=\"description\" value=\"Large and/or In Charge\" />" +
                "</node>" +
            "</model-import>";

    	sendPost("/requisitions", req, 202, "/requisitions/test");
    }

    @Test
    public void testBadRequisition() throws Exception {
        String req =
            "<model-import date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"test\">" +
                "asdfjklasdfjioasdf" +
                "<node node-label=\"david\" parent-node-label=\"apknd\" foreign-id=\"4243\">" +
                    "<interface ip-addr=\"192.0.2.204\" status=\"1\" snmp-primary=\"S\" descr=\"VPN interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"HTTP\"/>" +
                    "</interface>" +
                    "<interface ip-addr=\"192.0.2.201\" status=\"1\" snmp-primary=\"P\" descr=\"Management interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"SNMP\"/>" +
                    "</interface>" +
                    "<category name=\"AC\"/>" +
                    "<category name=\"UK\"/>" +
                    "<category name=\"low\"/>" +
                    "<asset name=\"manufacturer\" value=\"Dell\" />" +
                    "<asset name=\"operatingSystem\" value=\"Windows Pi\" />" +
                    "<asset name=\"description\" value=\"Large and/or In Charge\" />" +
                "</node>" +
            "</model-import>";

        final MockHttpServletResponse response = sendPost("/requisitions", req, 500, null);
        final String responseText = response.getContentAsString();
        assertThat(responseText, containsString("Failed to marshal/unmarshal XML file while unmarshalling an object (Requisition)"));
    }

    @Test
    public void testBadNodeRequest() throws Exception {
        createRequisition();

        final String req = "<node label=\"bad-node\" foreignSource=\"test\" building=\"Office\" type=\"A\" foreignId=\"bad-node\" />\n";
        sendPost("/requisitions/test/nodes", req, 400, null);
    }

    @Test
    public void testImport() throws Exception {
        createRequisition();
        
        EventAnticipator anticipator = m_eventProxy.getEventAnticipator();

        sendRequest(PUT, "/requisitions/test/import", 202);

        assertEquals(1, anticipator.getUnanticipatedEvents().size());
    }

    @Test
    public void testImportNoRescan() throws Exception {
        createRequisition();
        
        EventAnticipator anticipator = m_eventProxy.getEventAnticipator();

        sendRequest(PUT, "/requisitions/test/import", parseParamData("rescanExisting=false"), 202);

        assertEquals(1, anticipator.getUnanticipatedEvents().size());
        final Event event = anticipator.getUnanticipatedEvents().iterator().next();
        final List<Parm> parms = event.getParmCollection();
        assertEquals(2, parms.size());
        assertEquals("false", parms.get(1).getValue().getContent());
    }

    @Test
    public void testDeployedStats() throws Exception {
        createRequisition();

        String xml = sendRequest(GET, "/requisitions/deployed/stats", 200);
        assertTrue(xml.contains("deployed-stats"));
    }

    private void createRequisition() throws Exception {
        String req =
            "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"test\">" +
                "<node node-label=\"david\" parent-node-label=\"apknd\" foreign-id=\"4243\">" +
                    "<interface ip-addr=\"192.0.2.204\" status=\"1\" snmp-primary=\"S\" descr=\"VPN interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"HTTP\"/>" +
                    "</interface>" +
                    "<interface ip-addr=\"192.0.2.201\" status=\"1\" snmp-primary=\"P\" descr=\"Management interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"SNMP\"/>" +
                    "</interface>" +
                    "<category name=\"AC\"/>" +
                    "<category name=\"UK\"/>" +
                    "<category name=\"low\"/>" +
                    "<asset name=\"manufacturer\" value=\"Dell\" />" +
                    "<asset name=\"operatingSystem\" value=\"Windows Pi\" />" +
                    "<asset name=\"description\" value=\"Large and/or In Charge\" />" +
                "</node>" +
            "</model-import>";

        
        sendPost("/requisitions", req, 202, "/requisitions/test");
    }
}
