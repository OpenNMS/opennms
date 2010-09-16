package org.opennms.systemreport.system;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ByteArrayResource;
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
public class JavaReportPluginTest {
    @Resource(name="serviceRegistry")
    private ServiceRegistry m_defaultServiceRegistry;

    @Resource(name="javaReportPlugin")
    private SystemReportPlugin m_javaReportPlugin;

    @Resource(name="osReportPlugin")
    private SystemReportPlugin m_osReportPlugin;

    private List<SystemReportPlugin> m_plugins;

    public JavaReportPluginTest() {
        MockLogAppender.setupLogging(false, "ERROR");
    }

    @Before
    public void setUp() {
        System.err.println("---");
        m_plugins = new ArrayList<SystemReportPlugin>(m_defaultServiceRegistry.findProviders(SystemReportPlugin.class));
        Collections.sort(m_plugins);
        for (final SystemReportPlugin plugin : m_plugins) {
            System.err.println(plugin.getName() + ":");
            for (final Entry<String, org.springframework.core.io.Resource> entry : plugin.getEntries().entrySet()) {
                System.err.println("  " + entry.getKey() + ": " + getResourceText(entry.getValue()));
            }
        }
    }

    @Test
    public void testJavaReportPlugin() {
        assertTrue(listContains(JavaReportPlugin.class));
        final TreeMap<String, org.springframework.core.io.Resource> entries = m_javaReportPlugin.getEntries();
        final float classVer = Float.valueOf(getResourceText(entries.get("Class Version")));
        assertTrue(classVer >= 49.0);
    }

    @Test
    public void testOSPlugin() {
        assertTrue(listContains(OSReportPlugin.class));
        final TreeMap<String, org.springframework.core.io.Resource> entries = m_osReportPlugin.getEntries();
        assertTrue(entries.containsKey("Architecture"));
        assertTrue(entries.containsKey("Name"));
        assertTrue(entries.containsKey("Distribution"));
    }
    
    private boolean listContains(Class<? extends SystemReportPlugin> clazz) {
        for (final SystemReportPlugin p : m_plugins) {
            if (p.getClass().isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
    
    private String getResourceText(final org.springframework.core.io.Resource r) {
        if (r instanceof ByteArrayResource) {
            return new String(((ByteArrayResource) r).getByteArray());
        }
        return null;
    }
}
