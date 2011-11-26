package org.opennms.features.reporting.dao.jasper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;

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
        for (BasicReportDefinition report : m_dao.getReports()) {
            assertEquals("423", report.getId());
            assertEquals("your display name here", report.getDisplayName());
        }

        assertEquals("file:///tmp/foo.txt", m_dao.getTemplateLocation("423"));
        assertEquals(1, m_dao.getReports().size());
    }
}
