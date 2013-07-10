package org.opennms.netmgt.dao.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.CountedObject;
import org.opennms.netmgt.dao.api.EventCountDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;

public class MockEventDao extends AbstractMockDao<OnmsEvent, Integer> implements EventDao, EventCountDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsEvent event) {
        event.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsEvent event) {
        final Integer id = event.getId();
        return id == null || id == 0? null : id;
    }

    @Override
    public int deletePreviousEventsForAlarm(final Integer id, final OnmsEvent e) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Set<CountedObject<String>> getUeiCounts(final Integer limit) {
        final Map<String,Long> counts = new HashMap<String,Long>();
        for (final OnmsEvent event : findAll()) {
            final String eventUei = event.getEventUei();
            if (!counts.containsKey(eventUei)) {
                counts.put(eventUei, 0L);
            }
            counts.put(eventUei, counts.get(eventUei) + 1);
        }
        
        final Set<CountedObject<String>> countedObjects = new HashSet<CountedObject<String>>();
        for (final String uei : counts.keySet()) {
            countedObjects.add(new CountedObject<String>(uei, counts.get(uei)));
        }
        return countedObjects;
    }

}
