package org.opennms.features.reporting.repository.global;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class GlobalReportRepositoryTest {
    Logger logger = LoggerFactory.getLogger(GlobalReportRepositoryTest.class.getSimpleName());
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        logger.debug("opennms.home '{}'", System.getProperty("opennms.home"));
    }
    
    @Test
    public void getRepositoryForReportIdTest() {
        GlobalReportRepository repo = new DefaultGlobalReportRepository();
        assertNotNull(repo);
        logger.debug(repo.getDisplayName("local_sample-report"));
        logger.debug("local repository : '{}'", repo.getRepositoryById("cioreporting"));
        ReportRepository localRepo = repo.getRepositoryById("local");
        logger.debug(localRepo.toString());
        BasicReportDefinition report = localRepo.getOnlineReports().get(0);
        logger.debug(report.toString());
        logger.debug(report.getId());
        report.setId("LOCAL_" + report.getId());
        assertEquals("LOCAL_local_sample-report", report.getId());
        assertFalse(("local_LOCAL_local_sample-report".equals(localRepo.getOnlineReports().get(0).getId())));
        assertEquals("local_sample-report", localRepo.getOnlineReports().get(0).getId());
    }
}