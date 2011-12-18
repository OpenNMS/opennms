package org.opennms.features.reporting.repository.local;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLocalReportRepositoryTest {
    Logger logger = LoggerFactory.getLogger(LegacyLocalReportRepositoryTest.class);
    ReportRepository m_repo = new LegacyLocalReportRepository();
    
    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", "src/test/resources");
    }
    
    @Test
    public void reportIdsWithRepositoryIdsTest() {
        assertEquals("local", m_repo.getRepositoryId());
        BasicReportDefinition report = m_repo.getReports().get(0);
        logger.debug(report.getId());
        logger.debug(m_repo.getDisplayName(report.getId()));
    }
    
    @Test
    public void getReportsTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        for (BasicReportDefinition report : reports) {
	        logger.debug("'{}' \t '{}'", report.getId(), report.getReportService());
	    }
    }  
}