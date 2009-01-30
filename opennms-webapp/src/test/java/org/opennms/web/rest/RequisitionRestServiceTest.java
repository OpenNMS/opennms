package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class RequisitionRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        String url = "/requisitions";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Management interface"));
        
        url = "/requisitions/test";
        sendPut(url, "dateStamp=2009-01-01T00:00:00");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("date-stamp=\"2009-01-01T00:00:00\""));
        
        url = "/requisitions/test";
        sendPut(url, "dateStamp=2009-01-01T00:00:00");
        sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("date-stamp=\"2006-03-09T00:03:09\""));
    }

    /*
    @Test
    public void testDetectors() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/detectors";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<detectors>"));
        assertTrue(xml.contains("<detector "));
        assertTrue(xml.contains("name=\"DHCP\""));
        
        url = "/foreignSources/test/detectors/HTTP";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("org.opennms.netmgt.provision.detector.simple.HttpDetector"));

        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);
    }

    @Test
    public void testPolicies() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/policies";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<policies>"));
        assertTrue(xml.contains("<policy "));
        assertTrue(xml.contains("name=\"lower-case-node\""));
        
        url = "/foreignSources/test/policies/all-ipinterfaces";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        
        xml = sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 204);
    }
    */

    private void createRequisition() throws Exception {
        String req =
            "<model-import date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"test\">" +
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
