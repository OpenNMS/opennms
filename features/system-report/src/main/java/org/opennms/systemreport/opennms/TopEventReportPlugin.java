package org.opennms.systemreport.opennms;

import java.util.Set;
import java.util.TreeMap;

import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.CountedObject;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class TopEventReportPlugin extends AbstractSystemReportPlugin {
    @Autowired
    public EventDao m_eventDao;

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
            final Set<CountedObject<String>> objs = m_eventDao.getUeiCounts(20);
            for (final CountedObject<String> obj : objs) {
                map.put(obj.getObject(), new ByteArrayResource(obj.getCount().toString().getBytes()));
            }
        }
        return map;
    }

}
