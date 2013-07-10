/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-systemReport.xml",
        "classpath:/applicationContext-test-systemReport.xml"
})
@JUnitConfigurationEnvironment
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
        return "Not a string resource.";
    }
}
