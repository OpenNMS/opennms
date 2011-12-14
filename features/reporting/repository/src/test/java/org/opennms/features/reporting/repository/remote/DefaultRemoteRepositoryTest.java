package org.opennms.features.reporting.repository.remote;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO tak: test needs a full working remote repository server with configuration
@Ignore
public class DefaultRemoteRepositoryTest {
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryTest.class.getSimpleName());
    private DefaultRemoteRepository m_defaultRemoteRepository;

    @BeforeClass
     public static void setup(){
        System.setProperty("opennms.home", "src/test/resources");
    }
    
    @Before
    public void initialize() {
        m_defaultRemoteRepository = new DefaultRemoteRepository();
        assertNotNull(System.getProperty("opennms.home"));
    }
    
    @Test
    public void getOnlineReports() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getOnlineReports();
        logger.debug("getOnlineReports");
        for(BasicReportDefinition report : reports) {
            logger.debug(report.toString());
        }
    }

    @Test
    public void getReports() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getReports();
        logger.debug("getReports");
        for(BasicReportDefinition report : reports) {
            logger.debug(report.toString());
        }
    }
    
    @Test public void reportIdsStartWithRepositoryIdTest() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getReports();
        logger.debug("reportIdsStartWithRepositoryIdTest");
        for(BasicReportDefinition report : reports) {
            assertTrue(report.getId().startsWith(m_defaultRemoteRepository.getRepositoryId()));
            logger.debug(report.getId());
            logger.debug("'{}'", report.getRepositoryId());
        }
    }
    
    @Test
    public void reportIdsWithRepoIdgetMappedToRemoteReportTest() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getReports();
        for(BasicReportDefinition report : reports) {
            assertTrue(m_defaultRemoteRepository.getDisplayName(report.getId()).length() > 0);
            logger.debug("'{}' \t '{}'", report.getId(), m_defaultRemoteRepository.getDisplayName(report.getId()));
        }
    }
}
