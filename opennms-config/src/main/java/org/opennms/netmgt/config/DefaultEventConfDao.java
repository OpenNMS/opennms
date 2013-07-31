/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Events.EventCallback;
import org.opennms.netmgt.xml.eventconf.Events.EventCriteria;
import org.opennms.netmgt.xml.eventconf.Partition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

public class DefaultEventConfDao implements EventConfDao, InitializingBean {
	private static final String DEFAULT_PROGRAMMATIC_STORE_RELATIVE_PATH = "events/programmatic.events.xml";

    /**
     * Relative URL for the programmatic store configuration, relative to the
     * root configuration resource (which must be resolvable to a URL).
     */
    private String m_programmaticStoreRelativePath = DEFAULT_PROGRAMMATIC_STORE_RELATIVE_PATH;

	private Events m_events;

	private Resource m_configResource;

	private Partition m_partition;

    public String getProgrammaticStoreRelativeUrl() {
		return m_programmaticStoreRelativePath;
	}

	public void setProgrammaticStoreRelativeUrl(String programmaticStoreRelativeUrl) {
		m_programmaticStoreRelativePath = programmaticStoreRelativeUrl;
	}

	@Override
	public void reload() throws DataAccessException {
		try {
			loadConfig();
		} catch (Exception e) {
			throw new DataRetrievalFailureException("Unabled to load " + m_configResource, e);
		}
	}

	@Override
	public List<Event> getEvents(final String uei) {
		List<Event> events = m_events.forEachEvent(new ArrayList<Event>(), new EventCallback<List<Event>>() {

			@Override
			public List<Event> process(List<Event> accum, Event event) {
				if (uei.equals(event.getUei())) {
					accum.add(event);
				}
				return accum;
			}
		});
		
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
		Event event = findByUei(uei);
		return event == null ? null : event.getEventLabel();

	}

	@Override
	public void saveCurrent() {
		m_events.save(m_configResource);
	}
	
	
	
	public List<Event> getAllEvents() {
		return m_events.forEachEvent(new ArrayList<Event>(), new EventCallback<List<Event>>() {

			@Override
			public List<Event> process(List<Event> accum, Event event) {
				accum.add(event);
				return accum;
			}
		});
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
		m_events.initialize(m_partition);
	}

	@Override
	public void addEventToProgrammaticStore(Event event) {
		Events programmaticEvents = m_events.getLoadEventsByFile(m_programmaticStoreRelativePath);
		if (programmaticEvents == null) {
			programmaticEvents = new Events();
			m_events.addLoadedEventFile(m_programmaticStoreRelativePath, programmaticEvents);
		}

		programmaticEvents.addEvent(event);
		programmaticEvents.initialize(m_partition);

	}

	@Override
	public boolean removeEventFromProgrammaticStore(Event event) {
		Events programmaticEvents = m_events.getLoadEventsByFile(m_programmaticStoreRelativePath);
		if (programmaticEvents == null) return false;

		programmaticEvents.removeEvent(event);
		if (programmaticEvents.getEventCount() <= 0) {
			m_events.removeLoadedEventFile(m_programmaticStoreRelativePath);
		} else {
			programmaticEvents.initialize(m_partition);
		}
		return true;

	}

	@Override
	public boolean isSecureTag(String tag) {
		return m_events.isSecureTag(tag);
	}

	@Override
	public Event findByUei(final String uei) {
		return m_events.findFirstMatchingEvent(new EventCriteria() {

			@Override
			public boolean matches(Event e) {
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

	public void setConfigResource(Resource configResource) throws IOException {
		m_configResource = configResource;
	}
	
	@Override
	public void afterPropertiesSet() throws DataAccessException {
		loadConfig();
	}

	private synchronized void loadConfig() throws DataAccessException {
		try {
			Events events = JaxbUtils.unmarshal(Events.class, m_configResource);
			events.loadEventFiles(m_configResource);
			
			m_partition = new EnterpriseIdPartition();
			events.initialize(m_partition);

			m_events = events;

		} catch (Exception e) {
			throw new DataRetrievalFailureException("Unabled to load " + m_configResource, e);
		}

	}

}

