/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.dao.api.CountedObject;
import org.opennms.netmgt.dao.api.EventCountDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;

public class MockEventDao extends AbstractMockDao<OnmsEvent, UUID> implements EventDao, EventCountDao {

    @Override
    protected void generateId(final OnmsEvent event) {
        event.setId(UUID.randomUUID());
    }

    @Override
    protected UUID getId(final OnmsEvent event) {
        return event.getId();
    }

    @Override
    public int deletePreviousEventsForAlarm(final UUID id, final OnmsEvent e) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsEvent> getEventsAfterDate(List<String> ueiList, Date date) {
        List<OnmsEvent> matchingEvents = new ArrayList<OnmsEvent>();
        List<OnmsEvent> allEvents = findAll();
        for (OnmsEvent eachEvent : allEvents) {
            if (ueiList.contains(eachEvent.getEventUei()) && eachEvent.getEventTime().after(date)) {
                matchingEvents.add(eachEvent);
            }
        }
        return matchingEvents;
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

    @Override
    public List<OnmsEvent> getEventsForEventParameters(Map<String, String> eventParameters) {
        Stream<OnmsEvent> stream = findAll().stream();

        for (final Map.Entry<String, String> entry : eventParameters.entrySet()) {
            stream = stream.filter(e -> e.getEventParameters().stream().anyMatch(p ->
                        p.getName().matches(entry.getKey().replaceAll("%", ".*")) &&
                        p.getValue().matches(entry.getValue().replace("%", ".*"))));
        }

        return stream.distinct().collect(Collectors.toList());
    }
}
