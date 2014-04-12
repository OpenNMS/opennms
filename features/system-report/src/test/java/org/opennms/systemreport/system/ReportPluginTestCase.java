package org.opennms.systemreport.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-systemReport.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class ReportPluginTestCase {
    @Resource(name="serviceRegistry")
    private ServiceRegistry m_defaultServiceRegistry;

    List<SystemReportPlugin> m_plugins;

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

    protected boolean listContains(Class<? extends SystemReportPlugin> clazz) {
        for (final SystemReportPlugin p : m_plugins) {
            if (p.getClass().isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    protected String getResourceText(final org.springframework.core.io.Resource r) {
        if (r instanceof ByteArrayResource) {
            return new String(((ByteArrayResource) r).getByteArray());
        }
        return "Not a string resource.";
    }

}
