package org.opennms.feature.reporting.dao.jasper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class DefaultLocalJasperReportsDaoTest {

    private DefaultLocalJasperReportsDao m_defaultLocalJasperReportsDao;
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "local-jasper-reports.xml").canRead());
    }
    
    @Test
    public void getValuesForSampleReportTest () {
        this.m_defaultLocalJasperReportsDao = new DefaultLocalJasperReportsDao();
        assertEquals("sample-report.jrxml", m_defaultLocalJasperReportsDao.getTemplateLocation("sample-report"));
        assertEquals("jdbc", m_defaultLocalJasperReportsDao.getEngine("sample-report"));
    }
    
    @Test
    public void getValuesForTrivialReportTest () {
        this.m_defaultLocalJasperReportsDao = new DefaultLocalJasperReportsDao();
        assertEquals("trivial-report.jrxml", m_defaultLocalJasperReportsDao.getTemplateLocation("trivial-report"));
        assertEquals("null", m_defaultLocalJasperReportsDao.getEngine("trivial-report"));
    }
}
