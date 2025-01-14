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
package org.opennms.systemreport.opennms;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.opennms.core.resource.Vault;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.core.utils.TimeSeries;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

public class OpenNMSReportPlugin extends AbstractSystemReportPlugin implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSReportPlugin.class);
    @Autowired
    public NodeDao m_nodeDao;

    @Autowired
    public IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    public SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    public EventDao m_eventDao;
    
    @Autowired
    public AlarmDao m_alarmDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Override
    public String getName() {
        return "OpenNMS";
    }

    @Override
    public String getDescription() {
        return "OpenNMS core information, version, and basic configuration";
    }


    @Override
    public boolean isVisible() { return true; }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Map<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        map.put("OpenNMS Home",getResourceFromProperty("opennms.home"));
        map.put("Version", getResource(Vault.getProperty("version.display")));

        if (m_nodeDao != null) {
            map.put("Number of Nodes", getResource(Integer.toString(m_nodeDao.countAll())));
        }
        if (m_ipInterfaceDao != null) {
            map.put("Number of IP Interfaces", getResource(Integer.toString(m_ipInterfaceDao.countAll())));
        }
        if (m_snmpInterfaceDao != null) {
            map.put("Number of SNMP Interfaces", getResource(Integer.toString(m_snmpInterfaceDao.countAll())));
        }
        if (m_eventDao != null) {
            map.put("Number of Events", getResource(Integer.toString(m_eventDao.countAll())));
        }
        if (m_alarmDao != null) {
            map.put("Number of Alarms", getResource(Integer.toString(m_alarmDao.countAll())));
        }

        RuntimeMXBean runtimeBean = getBean(ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        if (runtimeBean == null) {
            LOG.info("falling back to local VM RuntimeMXBean");
            runtimeBean = ManagementFactory.getRuntimeMXBean();
        }
        addGetters(runtimeBean, map);

        map.put("OpenNMS Up Time",getResource( getOnmsUptimeAsString(runtimeBean) ));
        map.put("Time-Series Strategy",getResource(TimeSeries.getTimeseriesStrategy().getName()));

        return map;
    }

    private String getOnmsUptimeAsString(RuntimeMXBean runtimeBean){

        long startTimeMillis = runtimeBean.getStartTime();

        // Get the current time (in milliseconds since epoch)
        long currentTimeMillis = System.currentTimeMillis();

        // Calculate uptime
        long uptimeMillis = currentTimeMillis - startTimeMillis;

        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (uptimeMillis % (1000 * 60)) / 1000;

        return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
    }

}
