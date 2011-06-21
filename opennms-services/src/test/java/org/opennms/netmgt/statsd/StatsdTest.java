package org.opennms.netmgt.statsd;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-statisticsDaemon.xml"
//        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
//        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
})
@JUnitConfigurationEnvironment
public class StatsdTest {
    @Autowired
    Statsd m_statsd;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void testStartup() throws Exception {
        assertNotNull(m_statsd);
        m_statsd.start();
        m_statsd.unscheduleReports();
    }
}
