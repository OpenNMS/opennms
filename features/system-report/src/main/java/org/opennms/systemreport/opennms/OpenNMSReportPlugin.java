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
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.opennms.core.spring.BeanUtils;
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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.ReflectionException;
import javax.management.MBeanException;

public class OpenNMSReportPlugin extends AbstractSystemReportPlugin implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSReportPlugin.class);
    private static final String JMX_OBJ_OS = "java.lang:type=OperatingSystem";
    private static final String JMX_ATTR_AVAILABLE_PROCESSORS = "AvailableProcessors";
    private static final MBeanServer M_BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
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
    public int getPriority() {
        return 3;
    }

    @Override
    public Map<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        final InputStream is = this.getClass().getResourceAsStream("/version.properties");
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                map.put("Version", getResource(p.getProperty("version.display")));
            } catch (final IOException e) {
                LOG.warn("Unable to load from version.properties", e);
            }
        }
        
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

        Object availableProcessorsObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_AVAILABLE_PROCESSORS);

        int val = ((int) availableProcessorsObj);
        map.put("System CPU count", getResource(Integer.toString(val)));
        return map;
    }



    private Object getJmxAttribute(String objectName, String attributeName) {
        ObjectName objNameActual;
        try {
            objNameActual = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            LOG.warn("Failed to query from object name " + objectName, e);
            return null;
        }
        try {
            return M_BEAN_SERVER.getAttribute(objNameActual, attributeName);
        } catch (InstanceNotFoundException | AttributeNotFoundException
                 | ReflectionException | MBeanException e) {
            LOG.warn("Failed to query from attribute name " + attributeName + " on object " + objectName, e);
            return null;
        }
    }
}
