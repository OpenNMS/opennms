package org.opennms.netmgt.dao.castor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.config.reportd.Report;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


public class DefaultReportdDaoTest {

    private static DefaultReportdConfigurationDao m_reportdDao;
    
    @BeforeClass
    public static void beforeClass() {
        Resource resource = new ClassPathResource("reportd-configuration-testdata.xml");
        m_reportdDao = new DefaultReportdConfigurationDao();
        m_reportdDao.setConfigResource(resource);
        m_reportdDao.afterPropertiesSet();
    }
    
    @Test
    public void testGetReports() {
        assertNotNull(m_reportdDao);
        
        List<Report> reports = m_reportdDao.getReports();
        assertEquals(1, reports.size());
    }
    
    @Test
    public void testGetReportByName() {
        
        Report report = m_reportdDao.getReport("sample-report");
        assertNotNull(report);
        assertEquals("sample-report", report.getReportName());
    }
    
    @Ignore
    @Test
    public void testDeleteReport() {
        
        List<Report> reports = m_reportdDao.getReports();
        assertEquals(1, reports.size());
        
        m_reportdDao.deleteReport("sample-report");
        List<Report> reports2 = m_reportdDao.getReports();
        assertEquals(0, reports2.size());
    }
    
    @Test
    public void testSaveReport() {
        List<Report> reports = m_reportdDao.getReports();
        assertEquals(1, reports.size());
        
        Report report = new Report();
        report.setReportName("test-report-1");
        report.setReportEngine("jdbc");
        report.setReportTemplate("test-report-1.jrxml");
        report.setCronSchedule("0 0 0 * * ? *");
        m_reportdDao.saveReport(report);
        
        List<Report> reports2 = m_reportdDao.getReports();
        assertEquals(2, reports2.size());
        
        Report testReport = reports2.get(0);
        assertEquals("test-report-1", testReport.getReportName());
    }
    
}
