package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.api.EventConfigDao;
import org.opennms.netmgt.model.EventConfEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventConfEventDaoHibernate
        extends AbstractDaoHibernate<EventConfEvents, Long>
        implements EventConfigDao {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfEventDaoHibernate.class);

    public EventConfEventDaoHibernate() {
        super(EventConfEvents.class);
    }

    @Override
    public List<EventConfEvents> findBySourceId(int sourceId) {
        return find("from EventConfEvent e where e.source.id = ? order by e.createdTime desc", sourceId);
    }

    @Override
    public EventConfEvents findByUei(String uei) {
        List<EventConfEvents> list = find("from EventConfEvent e where e.uei = ?", uei);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<EventConfEvents> findEnabledEvents() {
        return find("from EventConfEvent e where e.enabled = true order by e.createdTime desc");
    }

    @Override
    public void deleteBySourceId(int sourceId) {
        getHibernateTemplate().bulkUpdate("delete from EventConfEvent e where e.source.id = ?", sourceId);
    }
}
