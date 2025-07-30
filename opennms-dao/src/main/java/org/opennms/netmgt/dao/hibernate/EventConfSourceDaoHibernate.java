package org.opennms.netmgt.dao.hibernate;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.opennms.netmgt.dao.api.EventConfigSourceDao;
import org.opennms.netmgt.model.EventConfSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventConfSourceDaoHibernate
        extends AbstractDaoHibernate<EventConfSource, Long>
        implements EventConfigSourceDao {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfSourceDaoHibernate.class);

    public EventConfSourceDaoHibernate() {
        super(EventConfSource.class);
    }

    @Override
    public EventConfSource findByName(String name) {
        List<EventConfSource> list = find("from EventConfSource s where s.name = ?", name);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Map<Integer, String> getIdToNameMap() {
        return findObjects(Object[].class,
                "select s.id, s.name from EventConfSource s").stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (String) row[1]
                ));
    }

    @Override
    public List<EventConfSource> findAllEnabled() {
        return find("from EventConfSource s where s.enabled = true order by s.fileOrder");
    }

    @Override
    public int getTotalEventCount(int sourceId) {
        Long count = findObjects(Long.class,
                "select sum(e.eventCount) from EventConfSource s join s.events e where s.id = ?", sourceId)
                .stream().findFirst().orElse(0L);
        return count.intValue();
    }
}
