package org.opennms.features.reporting.repository.remote;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRemoteRepositoryTest {
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryTest.class.getSimpleName());
    private DefaultRemoteRepository m_repo;

    @Before
    public void setup(){
        System.setProperty("opennms.home", "/opt/opennms");
        logger.debug(System.getProperty("opennms.home"));
        m_repo = new DefaultRemoteRepository();
    }
    
    @Test
    public void getOnlineReports() {
        List<BasicReportDefinition> reports = m_repo.getOnlineReports();
        logger.debug("getOnlineReports");
        for(BasicReportDefinition report : reports) {
            logger.debug(report.toString());
        }
    }

    @Test
    public void getReports() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        logger.debug("getReports");
        for(BasicReportDefinition report : reports) {
            logger.debug(report.toString());
        }
    }
    
    @Test public void reportIdsStartWithRepositoryIdTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        logger.debug("reportIdsStartWithRepositoryIdTest");
        for(BasicReportDefinition report : reports) {
            assertTrue(report.getId().startsWith(m_repo.getRepositoryId()));
            logger.debug(report.getId());
            logger.debug("'{}'", report.getRepositoryId());
        }
    }
    
    @Test
    public void reportIdsWithRepoIdgetMappedToRemoteReportTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        for(BasicReportDefinition report : reports) {
            assertTrue(m_repo.getDisplayName(report.getId()).length() > 0);
            logger.debug("'{}' \t '{}'", report.getId(), m_repo.getDisplayName(report.getId()));
        }
    }
}
