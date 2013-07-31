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

package org.opennms.systemreport.opennms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeMap;

import org.opennms.core.utils.BeanUtils;
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
    public int getPriority() {
        return 3;
    }

    @Override
    public TreeMap<String, Resource> getEntries() {
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
        return map;
    }

}
