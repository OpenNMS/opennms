/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.systemreport.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-systemReport.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public abstract class ReportPluginITCase {
    @Resource(name="serviceRegistry")
    private ServiceRegistry m_defaultServiceRegistry;

    List<SystemReportPlugin> m_plugins;

    @Before
    public void setUp() {
        m_plugins = new ArrayList<>(m_defaultServiceRegistry.findProviders(SystemReportPlugin.class));
        Collections.sort(m_plugins);
    }

    protected boolean listContains(Class<? extends SystemReportPlugin> clazz) {
        for (final SystemReportPlugin p : m_plugins) {
            if (p.getClass().isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

}
