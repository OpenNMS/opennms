package org.opennms.web.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/restServiceTest.xml"
})

public class RequisitionRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        String url = "/requisitions";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Management interface"));

        url = "/requisitions/test";

        sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);
    }

    @Test
    public void testNodes() throws Exception {
        createRequisition();

        String url = "/requisitions/test/nodes";

        // create a node
        sendPost(url, "<node xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" node-label=\"shoe\" parent-node-label=\"david\" foreign-id=\"1111\" />");
        
        // get list of nodes
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<node "));
        assertTrue(xml.contains("node-label="));
        assertTrue(xml.contains("count=\"2\""));
        
        // get individual node
        url = "/requisitions/test/nodes/4243";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("parent-node-label=\"apknd\""));
        
        // set attributes
        sendPut(url, "node-label=homo+sapien");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("node-label=\"homo sapien\""));

        // delete node
        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);
    }

    @Test
    public void testNodeInterfaces() throws Exception {
        createRequisition();
        
        String base = "/requisitions/test/nodes/4243/interfaces";
        String xml;
        
        // create an interface
        sendPost(base, "<interface xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" status=\"1\" snmp-primary=\"S\" ip-addr=\"172.20.1.254\" descr=\"Monkey\"><monitored-service service-name=\"ICMP\"/></interface>");
        sendPost(base, "<interface xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" status=\"1\" snmp-primary=\"S\" ip-addr=\"172.20.1.254\" descr=\"Blah\"><monitored-service service-name=\"ICMP\"/></interface>");
        
        // get list of interfaces
        xml = sendRequest(GET, base, 200);
        assertTrue(xml.contains("count=\"3\""));
        assertTrue(xml.contains("<interface "));
        assertTrue(xml.contains("Blah"));
        assertFalse(xml.contains("Monkey"));

        // get individual interface
        String url = base + "/172.20.1.204";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<interface "));
        assertTrue(xml.contains("VPN interface"));
        assertFalse(xml.contains("172.20.1.201"));

        // set attributes
        sendPut(url, "descr=Total+Crap&snmp-primary=P");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("descr=\"Total Crap\""));
        assertTrue(xml.contains("snmp-primary=\"P\""));

        // delete interface
        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);

        // confirm there is one less interface
        xml = sendRequest(GET, base, 200);
        assertTrue(xml.contains("count=\"2\""));
    }

    @Test
    public void testNodeInterfaceServices() throws Exception {
        createRequisition();
        
        String base = "/requisitions/test/nodes/4243/interfaces/172.20.1.204/services";
        
        // create a service
        sendPost(base, "<monitored-service xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" service-name=\"MONKEY\" />");

        // get list of services
        String xml = sendRequest(GET, base, 200);
        assertTrue(xml.contains("count=\"3\""));
        assertTrue(xml.contains("ICMP"));

        // get individual service
        String url = base + "/ICMP";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("service-name=\"ICMP\""));

        // delete interface
        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);

        // confirm there is one less interface
        xml = sendRequest(GET, base, 200);
        assertTrue(xml.contains("count=\"2\""));
    }
    
    @Test
    public void testNodeCategories() throws Exception {
        createRequisition();

        String base = "/requisitions/test/nodes/4243/categories";

        // create a category
        sendPost(base, "<category xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" name=\"Dead Servers\" />");

        // get list of categories
        String url = base;
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("count=\"4\""));
        assertTrue(xml.contains("name=\"low\""));
        
        // get individual category
        url = "/requisitions/test/nodes/4243/categories/low";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("name=\"low\""));
        
        // delete category
        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);
        
        // confirm there are less categories
        xml = sendRequest(GET, "/requisitions/test/nodes/4243/categories", 200);
        assertTrue(xml.contains("count=\"3\""));
    }
    
    @Test
    public void testNodeAssets() throws Exception {
        createRequisition();
        
        String base = "/requisitions/test/nodes/4243/assets";

        // create an asset
        sendPost(base, "<asset xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" name=\"manufacturer\" value=\"Dead Servers, Inc.\" />");

        // get list of asset parameters
        String url = base;
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("count=\"3\""));
        assertTrue(xml.contains("Windows Pi"));
        
        // get individual asset parameter
        url = "/requisitions/test/nodes/4243/assets/operatingSystem";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("value=\"Windows Pi\""));
        
        // delete asset parameter
        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);
        
        // confirm there are less assets
        xml = sendRequest(GET, "/requisitions/test/nodes/4243/assets", 200);
        assertTrue(xml.contains("count=\"2\""));
    }

    private void createRequisition() throws Exception {
        String req =
            "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"test\">" +
                "<node node-label=\"david\" parent-node-label=\"apknd\" foreign-id=\"4243\">" +
                    "<interface ip-addr=\"172.20.1.204\" status=\"1\" snmp-primary=\"S\" descr=\"VPN interface\">" +
                        "<monitored-service service-name=\"ICMP\"/>" +
                        "<monitored-service service-name=\"HTTP\"/>" +
                    "</interface>" +
                    "<interface ip-addr=\"172.20.1.201\" status=\"1\" snmp-primary=\"P\" descr=\"Management interface\">" +
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

        
        sendPost("/requisitions", req);
    }

}
