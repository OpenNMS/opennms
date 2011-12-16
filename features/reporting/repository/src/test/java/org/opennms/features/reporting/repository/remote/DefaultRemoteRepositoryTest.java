package org.opennms.features.reporting.repository.remote;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDAO;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO tak: test needs a full working remote repository server with configuration
@Ignore
public class DefaultRemoteRepositoryTest {
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryTest.class.getSimpleName());
    private DefaultRemoteRepository m_defaultRemoteRepository;
    private static final String OPENNMS_HOME = "src/test/resources";
    
    @BeforeClass
     public static void setup(){
        System.setProperty("opennms.home", OPENNMS_HOME);
        assertEquals(OPENNMS_HOME, System.getProperty("opennms.home"));
    }
    
    @Before
    public void initialize() {
        //TODO Tak: GANZ BÖSER CODE ZU GEFÄHRLICH FÜR ALLE OHNE UMLAUTE!
        m_defaultRemoteRepository = new DefaultRemoteRepository(new DefaultRemoteRepositoryConfigDAO().getActiveRepositories().get(0), "423");
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

    @Test
    public void catchUniformExceptionTest() {
        String report = m_defaultRemoteRepository.getReportService("null");
        logger.debug("ReportService for null-report '{}'", report);
    }
}
