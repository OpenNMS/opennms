package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.EventConfEvents;

import java.util.List;

public interface EventConfigDao extends LegacyOnmsDao<EventConfEvents, Long> {

    EventConfEvents get(Long id);
    List<EventConfEvents> findBySourceId(int sourceId);
    EventConfEvents findByUei(String uei);
    List<EventConfEvents> findEnabledEvents();
    void deleteBySourceId(int sourceId);
}
