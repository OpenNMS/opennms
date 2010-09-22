package org.opennms.systemreport.opennms;

import java.util.Set;
import java.util.TreeMap;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.opennms.systemreport.dao.CountedObject;
import org.opennms.systemreport.dao.EventCountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class TopEventReportPlugin extends AbstractSystemReportPlugin {
    @Autowired
    public EventCountDao m_eventCountDao;

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

        if (m_eventCountDao != null) {
            final Set<CountedObject<String>> objs = m_eventCountDao.getUeiCounts(20);
            for (final CountedObject<String> obj : objs) {
                map.put(obj.getObject(), new ByteArrayResource(obj.getCount().toString().getBytes()));
            }
        }
        return map;
    }

}
