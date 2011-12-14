package org.opennms.features.reporting.dao.jasper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLocalJasperReportsDaoTest {

    Logger logger = LoggerFactory.getLogger(DefaultLocalJasperReportsDaoTest.class.getSimpleName());
    
    private LegacyLocalJasperReportsDao m_defaultLocalJasperReportsDao;
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "local-jasper-reports.xml").canRead());
    }
    
    @Test
    public void getValuesForSampleReportTest () {
        this.m_defaultLocalJasperReportsDao = new LegacyLocalJasperReportsDao();
        assertEquals("sample-report.jrxml", m_defaultLocalJasperReportsDao.getTemplateLocation("sample-report"));
        assertEquals("jdbc", m_defaultLocalJasperReportsDao.getEngine("sample-report"));
    }
    
    @Test
    public void getValuesForTrivialReportTest () {
        this.m_defaultLocalJasperReportsDao = new LegacyLocalJasperReportsDao();
        assertEquals("trivial-report.jrxml", m_defaultLocalJasperReportsDao.getTemplateLocation("trivial-report"));
        assertEquals("null", m_defaultLocalJasperReportsDao.getEngine("trivial-report"));
    }
    
    @Test
    public void getTemplateForSampleReportAsStreamTest () throws IOException {
        this.m_defaultLocalJasperReportsDao = new LegacyLocalJasperReportsDao();
        InputStream m_templateStream = m_defaultLocalJasperReportsDao.getTemplateStream("sample-report");
        assertEquals("check filesize by availiable call", 4822, m_templateStream.available());
    }
}
