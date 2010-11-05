package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ReportdRestServiceTest extends AbstractSpringJerseyRestTestCase {

    
    @Test
    public void testReports() throws Exception {
        String url = "/reports";
        // Testing GET Collection
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<reportName>sample-report</reportName>"));
        assertTrue(xml.contains("totalCount=\"1\""));
        
        //Test create a report
        createReport();
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<reportName>sample-report-2</reportName>"));
        
        url += "/sample-report";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<reportName>sample-report</reportName>"));
        // Testing DELETE
        sendRequest(DELETE, url, 200);
        sendRequest(GET, url, 204);
    }

    private void createReport() throws Exception {
        String report = "<report>" +
        		"<cronSchedule>0 0 0 * * ? *</cronSchedule>" +
        		"<reportEngine>jdbc</reportEngine>" +
        		"<reportFormat>pdf</reportFormat>" +
        		"<reportName>sample-report-2</reportName>" +
        		"<reportTemplate>sample-report2.jrxml</reportTemplate>" +
        		"</report>";
        sendPost("/reports", report);
    }
}
