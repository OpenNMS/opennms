/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
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
