package org.opennms.systemreport.system;

import static org.junit.Assert.assertTrue;

import java.util.TreeMap;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-systemReport.xml"
})
@JUnitTemporaryDatabase()
public class TopReportPluginTest {
    @Resource(name="topReportPlugin")
    private SystemReportPlugin m_topReportPlugin;

    public TopReportPluginTest() {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testTopReportPlugin() {
        final TreeMap<String, org.springframework.core.io.Resource> entries = m_topReportPlugin.getEntries();
        assertTrue(entries.containsKey("Output"));
    }
}