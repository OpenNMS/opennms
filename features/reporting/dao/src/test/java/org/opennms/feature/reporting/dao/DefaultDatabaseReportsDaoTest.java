package org.opennms.feature.reporting.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class DefaultDatabaseReportsDaoTest {

    private DefaultDatabaseReportsDao m_defaultDatabaseReportsDao;
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "database-reports.xml").canRead());
    }
    
    @Test
    public void getReportsCountTest () {
        this.m_defaultDatabaseReportsDao = new DefaultDatabaseReportsDao();
        assertEquals(18, this.m_defaultDatabaseReportsDao.getReports().size());
    }
}
