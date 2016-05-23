package org.opennms.features.minion.dominion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
                "classpath:/META-INF/opennms/applicationContext-soa.xml",
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/component-service.xml",
                "classpath:/META-INF/opennms/applicationContext-daemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class DominionConfigIT {
    
    @Before
    public void setUp() throws Throwable {
    }

    @Test
    public void testConfig() throws Throwable {
        
        // tests loading of component-service.xml
            
    }

}
