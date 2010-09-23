package org.opennms.systemreport.opennms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeMap;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

public class OpenNMSReportPlugin extends AbstractSystemReportPlugin {
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

    public String getName() {
        return "OpenNMS";
    }

    public String getDescription() {
        return "OpenNMS Core, Version, and Basic Configuration";
    }

    public int getPriority() {
        return 3;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        final InputStream is = this.getClass().getResourceAsStream("/version.properties");
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                map.put("Version", getResource(p.getProperty("version.display")));
            } catch (final IOException e) {
                LogUtils.warnf(this, e, "Unable to load from version.properties");
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
