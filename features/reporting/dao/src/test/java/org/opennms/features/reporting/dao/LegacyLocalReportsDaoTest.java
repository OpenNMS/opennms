package org.opennms.features.reporting.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: indigo
 * Date: 1/3/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:LegacyLocalReportsDaoTest-context.xml"})
public class LegacyLocalReportsDaoTest {

    @Autowired
    private LegacyLocalReportsDao m_legacyLocalReportsDao;
    
    private List<BasicReportDefinition> m_onlineReports;
    
    private List<BasicReportDefinition> m_reports;
    
    private String CONFIG_FILE_NAME = "local-reports.xml";

    @Before
    public void sanityCheck() throws Exception {
        assertNotNull("Inject legacy local report data access.", m_legacyLocalReportsDao);
        assertTrue("Test " + CONFIG_FILE_NAME + " exist.", m_legacyLocalReportsDao.getConfigResource().exists());
        m_legacyLocalReportsDao.afterPropertiesSet();
        
        m_onlineReports = m_legacyLocalReportsDao.getOnlineReports();
        assertNotNull("Test to retrieve 2 online reports from " + CONFIG_FILE_NAME, m_onlineReports);
        assertEquals("Test 2 configured online reports.", 2, m_onlineReports.size());
        assertFalse("Online reports from " + CONFIG_FILE_NAME + " is empty.",m_legacyLocalReportsDao.getReports().isEmpty());
        
        m_reports = m_legacyLocalReportsDao.getReports();
        assertNotNull("Test to retrieve 3 online reports from " + CONFIG_FILE_NAME, m_reports);
        assertFalse("Reports from " + CONFIG_FILE_NAME + " is empty.", m_legacyLocalReportsDao.getReports().isEmpty());
        assertEquals("Test 3 configured online reports.", 3, m_reports.size());        
    }

    @Test
    public void testReportDescription() throws Exception {
        assertEquals("First report description test","sample Jasper report using jdbc datasource", m_reports.get(0).getDescription());
        assertEquals("Second report description test","online sample Jasper report using jdbc datasource", m_reports.get(1).getDescription());
        assertEquals("Third report description test","NOT online sample Jasper report using jdbc datasource", m_reports.get(2).getDescription());
    }

    @Test
    public void testReportIds() throws Exception {
        assertEquals("First report id test","sample-report", m_reports.get(0).getId());
        assertEquals("Second report id test","online-sample-report", m_reports.get(1).getId());
        assertEquals("Third report id test","not-online-sample-report", m_reports.get(2).getId());
    }
    
    @Test
    public void testReportDisplayName() throws Exception {
        assertEquals("First report display name test","sample JasperReport", m_reports.get(0).getDisplayName());
        assertEquals("Second report display name test","online sample JasperReport", m_reports.get(1).getDisplayName());
        assertEquals("Third report display name test","NOT online sample JasperReport", m_reports.get(2).getDisplayName());
    }

    @Test
    public void testReportService() throws Exception {
        assertEquals("First report report-service test","jasperReportService", m_reports.get(0).getReportService());
        assertEquals("Second report report-service test","jasperReportService", m_reports.get(1).getReportService());
        assertEquals("Third report report-service test","jasperReportService", m_reports.get(2).getReportService());
    }

    @Test
    public void testReportRepositoryId() throws Exception {
        assertNull("First report repository id test", m_reports.get(0).getRepositoryId());
        assertNull("Second report repository id test", m_reports.get(1).getRepositoryId());
        assertNull("Third report repository id test", m_reports.get(2).getRepositoryId());
    }

    @Test
    public void testReportAllowAccess() throws Exception {
        assertFalse("First report allow access test", m_reports.get(0).getAllowAccess());
        assertFalse("Second report allow access test", m_reports.get(1).getAllowAccess());
        assertFalse("Third report allow access test", m_reports.get(2).getAllowAccess());
    }

    @Test
    public void testReportIsOnline() throws Exception {
        assertTrue("First report is online test", m_reports.get(0).getOnline());
        assertTrue("Second report is online test", m_reports.get(1).getOnline());
        assertFalse("Third report is online test", m_reports.get(2).getOnline());
    }

    public void setLegacyLocalReportsDao(LegacyLocalReportsDao legacyLocalReportsDao) {
        this.m_legacyLocalReportsDao = legacyLocalReportsDao;
    }

    public LegacyLocalReportsDao getLegacyLocalReportsDao() {
        return this.m_legacyLocalReportsDao;
    }
}
