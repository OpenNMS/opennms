/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.dao.api.CountedObject;
import org.opennms.netmgt.dao.api.EventCountDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;

public class MockEventDao extends AbstractMockDao<OnmsEvent, Long> implements EventDao, EventCountDao {
    private AtomicLong m_id = new AtomicLong(0);

    @Override
    protected void generateId(final OnmsEvent event) {
        event.setId(m_id.incrementAndGet());
    }

    @Override
    protected Long getId(final OnmsEvent event) {
        final Long id = event.getId();
        return id == null || id == 0? null : id;
    }

    @Override
    public int deletePreviousEventsForAlarm(final Integer id, final OnmsEvent e) {
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
