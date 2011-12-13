package org.opennms.features.reporting.repository.global;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
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
    }
}