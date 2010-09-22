package org.opennms.systemreport.opennms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeMap;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

public class TopEventReportPlugin extends AbstractSystemReportPlugin {
    @Autowired
    public EventDao m_eventDao;

    public EventdConfiguration m_eventConfig;

    public String getName() {
        return "TopEvent";
    }

    public String getDescription() {
        return "Top 20 Most Reported Events";
    }

    public int getPriority() {
        return 40;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();

        if (m_eventDao != null) {
            map.put("Number of Events", getResource(Integer.toString(m_eventDao.countAll())));
        }
        return map;
    }

}
