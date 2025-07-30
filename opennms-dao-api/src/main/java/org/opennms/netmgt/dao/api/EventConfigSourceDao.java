package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.EventConfSource;

import java.util.List;
import java.util.Map;

public interface EventConfigSourceDao extends LegacyOnmsDao<EventConfSource, Long> {

    EventConfSource get(Long id);
    EventConfSource findByName(String name);
    List<EventConfSource> findAllEnabled();
    Map<Integer,String> getIdToNameMap();
    int getTotalEventCount(int sourceId);

}
