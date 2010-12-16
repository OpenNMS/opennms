package org.opennms.systemreport.dao;

import java.util.Set;

import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.model.OnmsEvent;

public interface EventCountDao extends OnmsDao<OnmsEvent, Integer> {
    
    Set<CountedObject<String>> getUeiCounts(final Integer limit);

}
