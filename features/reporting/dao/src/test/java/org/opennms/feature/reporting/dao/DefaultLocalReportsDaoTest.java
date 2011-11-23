package org.opennms.feature.reporting.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class DefaultLocalReportsDaoTest {

    private LegacyLocalReportsDao m_defaultLocalReportsDao;
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "local-reports.xml").canRead());
    }
    
    @Test
    public void getReportsCountTest () {
        this.m_defaultLocalReportsDao = new LegacyLocalReportsDao();
        assertEquals(18, this.m_defaultLocalReportsDao.getReports().size());
    }
}
