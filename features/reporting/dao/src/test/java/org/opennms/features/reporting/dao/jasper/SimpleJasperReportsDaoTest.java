package org.opennms.features.reporting.dao.jasper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportDefinition;

public class SimpleJasperReportsDaoTest {

    private SimpleJasperReportsDao m_dao;

    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home")
                + File.separator + "etc" + File.separator
                + "simple-jasper-reports.xml").canRead());
    }

    @Test
    public void getValuesForSampleReportTest() {
        this.m_dao = new SimpleJasperReportsDao();
        assertEquals(2, m_dao.getReports().size());
        
        SimpleJasperReportDefinition report = (SimpleJasperReportDefinition) m_dao.getReports().get(0);
        
        assertEquals("423", report.getId());
        assertEquals("sample display-name", report.getDisplayName());
        assertEquals("file:///tmp/resource-uri-test.jrxml", report.getTemplate());

        report = (SimpleJasperReportDefinition) m_dao.getReports().get(1);
        assertEquals("23", report.getId());
        assertEquals("more sample display-name", report.getDisplayName());
        assertEquals("file:///tmp/resource-uri-test.jrxml", report.getTemplate());
 
    }
}
