package org.opennms.systemreport.system;

import static org.junit.Assert.assertTrue;

import java.util.TreeMap;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-systemReport.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
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