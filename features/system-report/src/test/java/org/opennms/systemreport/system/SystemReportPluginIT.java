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

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.mock.MockAlarmDao;
import org.opennms.netmgt.dao.mock.MockEventDao;
import org.opennms.netmgt.dao.mock.MockIpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSnmpInterfaceDao;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.systemreport.opennms.OpenNMSReportPlugin;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

public class SystemReportPluginIT {
    private SystemReportPlugin m_javaReportPlugin = new JavaReportPlugin();
    private SystemReportPlugin m_osReportPlugin = new OSReportPlugin();
    private SystemReportPlugin m_onmsReportPlugin = new OpenNMSReportPlugin();
    private SystemReportPlugin m_hardDriveReportPlugin=new HardDriveReportPlugin();

    public SystemReportPluginIT() {
        MockLogAppender.setupLogging(false, "ERROR");
    }

    @Test
    public void testJavaReportPlugin() {
        final Map<String, org.springframework.core.io.Resource> entries = m_javaReportPlugin.getEntries();
        assertTrue(entries.containsKey("Class Version"));
        assertTrue(entries.containsKey("Home"));
        assertTrue(entries.containsKey("Initial Heap Size"));
        assertTrue(entries.containsKey("Max Heap Size"));
        assertTrue(entries.containsKey("VM Name"));
        assertTrue(entries.containsKey("VM Version"));
        assertTrue(entries.containsKey("Vendor"));
        assertTrue(entries.containsKey("Version"));

    }

    @Test
    public void testHardDriveReportPlugin() {
        final Map<String, org.springframework.core.io.Resource> entries = m_hardDriveReportPlugin.getEntries();
        assertTrue(entries.containsKey("Hard Drive Capacity"));
        assertTrue(entries.containsKey("Hard Drive Performance"));
    }


    @Test
    public void testOpenNMSReportPlugin() {
        ReflectionTestUtils.setField(m_onmsReportPlugin,"m_nodeDao",new MockNodeDao());
        ReflectionTestUtils.setField(m_onmsReportPlugin,"m_ipInterfaceDao",new MockIpInterfaceDao());
        ReflectionTestUtils.setField(m_onmsReportPlugin,"m_snmpInterfaceDao",new MockSnmpInterfaceDao());
        ReflectionTestUtils.setField(m_onmsReportPlugin,"m_eventDao",new MockEventDao());
        ReflectionTestUtils.setField(m_onmsReportPlugin,"m_alarmDao",new MockAlarmDao());
        final Map<String, org.springframework.core.io.Resource> entries = m_onmsReportPlugin.getEntries();
        assertTrue(entries.containsKey("Number of Alarms"));
        assertTrue(entries.containsKey("Number of Events"));
        assertTrue(entries.containsKey("Number of IP Interfaces"));
        assertTrue(entries.containsKey("Number of Nodes"));
        assertTrue(entries.containsKey("Number of SNMP Interfaces"));
        assertTrue(entries.containsKey("OpenNMS Home Dir"));
        assertTrue(entries.containsKey("OpenNMS Up Time"));
        assertTrue(entries.containsKey("Time-Series Strategy"));
        assertTrue(entries.containsKey("Version"));
    }


    @Test
    public void testOSPlugin() {
        final Map<String, org.springframework.core.io.Resource> entries = m_osReportPlugin.getEntries();
        assertTrue(entries.containsKey("Architecture"));
        assertTrue(entries.containsKey("Name"));
        assertTrue(entries.containsKey("Distribution"));
        assertTrue(entries.containsKey("HTTP(S) ports"));
        assertTrue(entries.containsKey("Total System RAM"));
        assertTrue(entries.containsKey("Used System RAM"));
        assertTrue(entries.containsKey("Version"));
    }
    
    private String getResourceText(final org.springframework.core.io.Resource r) {
        if (r instanceof ByteArrayResource) {
            return new String(((ByteArrayResource) r).getByteArray());
        }
        return "Not a string resource.";
    }
}
