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
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.EventLabelComparator;
import org.opennms.netmgt.xml.eventconf.EventMatchers;
import org.opennms.netmgt.xml.eventconf.EventOrdering;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Events.EventCallback;
import org.opennms.netmgt.xml.eventconf.Events.EventCriteria;
import org.opennms.netmgt.xml.eventconf.Field;
import org.opennms.netmgt.xml.eventconf.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

public class DefaultEventConfDao implements EventConfDao, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEventConfDao.class);

	private Events m_events;

	private Partition m_partition;

    public DefaultEventConfDao() {
        m_events = new Events();
        m_partition = new EnterpriseIdPartition();
        m_events.initialize(m_partition, new EventOrdering());
    }


	@Override
	public void reload() throws DataAccessException {
		// Reload happens whenever DB gets updated, no need for explicit reload
	}

	@Override
	public List<Event> getEvents(final String uei) {
		List<Event> events = m_events.forEachEvent(new ArrayList<>(), (EventCallback<List<Event>>) (accum, event) -> {
					if (uei.equals(event.getUei())) {
						accum.add(event);
					}
					return accum;
				}).stream()
				// remove duplicates:
				// event definitions with priority > 0 are copied up the configuration tree.
				// if they do not match we do not want to re-compare them when matching events to definitions.
				.distinct()
				.collect(Collectors.toList());

		return events.isEmpty() ? null : events;
	}

	@Override
	public List<String> getEventUEIs() {
		return m_events.forEachEvent(new ArrayList<String>(), new EventCallback<List<String>>() {

			@Override
			public List<String> process(List<String> ueis, Event event) {
				ueis.add(event.getUei());
				return ueis;
			}
		});

	}

	@Override
	public Map<String, String> getEventLabels() {
		return m_events.forEachEvent(new TreeMap<String, String>(), new EventCallback<Map<String, String>>() {

			@Override
			public Map<String, String> process(Map<String, String> ueiToLabelMap, Event event) {
				ueiToLabelMap.put(event.getUei(), event.getEventLabel());
				return ueiToLabelMap;
			}

		});
	}

	@Override
	public String getEventLabel(final String uei) {
		// Optimistic lookup
		Event event = m_events.getEventByUeiOptimistic(uei);
		if (event == null) {
			// Fallback to search if no match was found
			event = findByUei(uei);
		}
		return event == null ? null : event.getEventLabel();
	}

	public List<Event> getAllEvents() {
		return m_events.forEachEvent(new ArrayList<>(), (EventCallback<List<Event>>) (accum, event) -> {
					accum.add(event);
					return accum;
				}).stream()
				// remove duplicates:
				// event definitions with priority > 0 are copied up the configuration tree.
				// if they do not match we do not want to re-compare them when matching events to definitions.
				.distinct().collect(Collectors.toList());
	}

	@Override
	public List<Event> getEventsByLabel() {
		SortedSet<Event> events = m_events.forEachEvent(new TreeSet<Event>(new EventLabelComparator()), new EventCallback<SortedSet<Event>>() {

			@Override
			public SortedSet<Event> process(SortedSet<Event> accum, Event event) {
				accum.add(event);
				return accum;
			}
		});
		return new ArrayList<Event>(events);
	}

	@Override
	public void addEvent(Event event) {
		m_events.addEvent(event);
		m_events.initialize(m_partition, new EventOrdering());
	}

	@Override
	public boolean isSecureTag(String tag) {
		return m_events.isSecureTag(tag);
	}

	@Override
	public Event findByUei(final String uei) {
	    if (uei == null) {
	        return null;
	    }
	    return m_events.findFirstMatchingEvent(new EventCriteria() {
	        @Override public boolean matches(final Event e) {
	            return uei.equals(e.getUei());
	        }
	    });
	}

	@Override
	public Event findByEvent(final org.opennms.netmgt.xml.event.Event matchingEvent) {
		return m_events.findFirstMatchingEvent(matchingEvent);
	}

	@Override
	public Events getRootEvents() {
		return m_events;
	}

	@Override
	public void loadEventsFromDB(List<EventConfEvent> dbEvents) {

		// Group events by source and sort by source fileOrder
		Map<String, List<EventConfEvent>> eventsBySource = dbEvents.stream()
				.collect(Collectors.groupingBy(
						event -> event.getSource().getName(),
						LinkedHashMap::new,
						Collectors.toList()
				));

		// Sort sources by fileOrder
		List<Map.Entry<String, List<EventConfEvent>>> sortedSources = sortSourcesByFileOrder(eventsBySource);

		Events rootEvents = new Events();
		// Build Events per source
		for (Map.Entry<String, List<EventConfEvent>> sourceEntry : sortedSources) {
			Events eventsForSource = buildEventsForSource(sourceEntry.getValue());
			rootEvents.addLoadedEventFile(sourceEntry.getKey(), eventsForSource);
		}
		
		synchronized (this) {
			m_partition = new EnterpriseIdPartition();
			rootEvents.initialize(m_partition, new EventOrdering());
			m_events = rootEvents;
		}
	}

	private List<Map.Entry<String, List<EventConfEvent>>> sortSourcesByFileOrder(Map<String, List<EventConfEvent>> eventsBySource) {
		return eventsBySource.entrySet().stream()
				.sorted(Map.Entry.comparingByValue((events1, events2) -> {
					Integer order1 = events1.get(0).getSource().getFileOrder();
					Integer order2 = events2.get(0).getSource().getFileOrder();
					return Integer.compare(order1 != null ? order1 : 0, order2 != null ? order2 : 0);
				}))
				.toList();
	}

	private Events buildEventsForSource(List<EventConfEvent> sourceEvents) {
		Events eventsForSource = new Events();
		for (EventConfEvent dbEvent : sourceEvents) {
			parseAndAddEvent(eventsForSource, dbEvent);
		}
		return eventsForSource;
	}

	private void parseAndAddEvent(Events eventsForSource, EventConfEvent dbEvent) {
		String xmlContent = dbEvent.getXmlContent();
		if (xmlContent != null && !xmlContent.trim().isEmpty()) {
			try {
				Event event = JaxbUtils.unmarshal(Event.class, xmlContent);
				if (event != null) {
					eventsForSource.addEvent(event);
				}
			} catch (Exception e) {
				LOG.warn("Failed to parse event XML content for UEI {}", dbEvent.getUei(), e);
			}
		}
	}


	@Override
	public void afterPropertiesSet() throws DataAccessException {
		// Event Conf gets loaded by loadEventsFromDB.
		// Since this Class can't access DB at bean creation time, this is delegated to EventConfPersistenceService
	}

	private static class EnterpriseIdPartition implements Partition {

		private Field m_field = EventMatchers.field("id");

		@Override
		public List<String> group(Event eventConf) {
			List<String> keys = eventConf.getMaskElementValues("id");
			if (keys == null) return null;
			for(String key : keys) {
			    // if this issue is a wildcard issue we need to test against
			    // all events so return null here so it isn't pigeon-holed into
			    // a particular partition
			    if (key.endsWith("%")) return null;
			    if (key.startsWith("~")) return null;
			}
			return keys;
		}

		@Override
		public String group(org.opennms.netmgt.xml.event.Event matchingEvent) {
			return m_field.get(matchingEvent);
		}

	}

}

