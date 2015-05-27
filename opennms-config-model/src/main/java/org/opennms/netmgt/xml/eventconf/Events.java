/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.ValidateUsing;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;

@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={})
public class Events implements Serializable {
    public interface EventCallback<T> {
		
		public T process(T accum, Event event);

	}

	public interface EventCriteria {
		
		public boolean matches(Event e);

	}

	private static final long serialVersionUID = -3725006529763434264L;

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Event[] EMPTY_EVENT_ARRAY = new Event[0];

	/**
     * Global settings for this configuration
     */
	@XmlElement(name="global", required=false)
    private Global m_global;

	// @Size(min=1)
	@XmlElement(name="event", required=false)
    private List<Event> m_events = new ArrayList<Event>();

	// @Size(min=0)
	@XmlElement(name="event-file", required=false)
    private List<String> m_eventFiles = new ArrayList<String>();
	
	@XmlTransient
	private Map<String, Events> m_loadedEventFiles = new LinkedHashMap<String, Events>();

	@XmlTransient
	private Partition m_partition;
	
	@XmlTransient
	private Map<String, List<Event>> m_partitionedEvents;
	
	@XmlTransient
	private List<Event> m_nullPartitionedEvents;
	
        @XmlTransient
        private List<Event> m_wildcardEvents;
        
	@XmlTransient
	private EventOrdering m_ordering;
	
	public EventOrdering getOrdering() {
	    return m_ordering;
	}
	
    public void addEvent(final Event event) throws IndexOutOfBoundsException {
        m_events.add(event);
    }

    public void addEvent(final int index, final Event event) throws IndexOutOfBoundsException {
        m_events.add(index, event);
    }

    public void addEventFile(final String eventFile) throws IndexOutOfBoundsException {
        m_eventFiles.add(eventFile.intern());
    }

    public void addEventFile(final int index, final String eventFile) throws IndexOutOfBoundsException {
        m_eventFiles.add(index, eventFile.intern());
    }

    public Enumeration<Event> enumerateEvent() {
        return Collections.enumeration(m_events);
    }

    public Enumeration<String> enumerateEventFile() {
        return Collections.enumeration(m_eventFiles);
    }

    public Event getEvent(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_events.size()) {
            throw new IndexOutOfBoundsException("getEvent: Index value '" + index + "' not in range [0.." + (m_events.size() - 1) + "]");
        }
        return m_events.get(index);
    }

    public Event[] getEvent() {
        return m_events.toArray(EMPTY_EVENT_ARRAY);
    }

    public List<Event> getEventCollection() {
        return m_events;
    }

    public int getEventCount() {
        return m_events.size();
    }

    public String getEventFile(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_eventFiles.size()) {
            throw new IndexOutOfBoundsException("getEventFile: Index value '" + index + "' not in range [0.." + (m_eventFiles.size() - 1) + "]");
        }
        return m_eventFiles.get(index);
    }

    public String[] getEventFile() {
        return m_eventFiles.toArray(EMPTY_STRING_ARRAY);
    }

    public List<String> getEventFileCollection() {
        return m_eventFiles;
    }

    public int getEventFileCount() {
        return m_eventFiles.size();
    }

    public Global getGlobal() {
        return m_global;
    }

    /**
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        return true;
    }

    public Iterator<Event> iterateEvent() {
        return m_events.iterator();
    }

    public Iterator<String> iterateEventFile() {
        return m_eventFiles.iterator();
    }

    public void marshal(final Writer out) {
        JaxbUtils.marshal(this, out);
    }

    public void removeAllEvent() {
        m_events.clear();
    }

    public void removeAllEventFile() {
        m_eventFiles.clear();
    }

    public boolean removeEvent(final Event event) {
        return m_events.remove(event);
    }

    public Event removeEventAt(final int index) {
        return m_events.remove(index);
    }

    public boolean removeEventFile(final String eventFile) {
        return m_eventFiles.remove(eventFile);
    }

    public String removeEventFileAt(final int index) {
        return m_eventFiles.remove(index);
    }

    public void setEvent(final int index, final Event event) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_events.size()) {
            throw new IndexOutOfBoundsException("setEvent: Index value '" + index + "' not in range [0.." + (m_events.size() - 1) + "]");
        }
        m_events.set(index, event);
    }

    public void setEvent(final Event[] events) {
        m_events.clear();
        for (final Event event : events) {
        	m_events.add(event);
        }
    }

    public void setEvent(final List<Event> events) {
        if (m_events == events) return;
        m_events.clear();
        m_events.addAll(events);
    }

    public void setEventCollection(final List<Event> events) {
        setEvent(events);
    }

    public void setEventFile(final int index, final String eventFile) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_eventFiles.size()) {
            throw new IndexOutOfBoundsException("setEventFile: Index value '" + index + "' not in range [0.." + (m_eventFiles.size() - 1) + "]");
        }
        m_eventFiles.set(index, eventFile.intern());
    }

    public void setEventFile(final String[] eventFiles) {
        m_eventFiles.clear();
        for (final String eventFile : eventFiles) {
        	m_eventFiles.add(eventFile.intern());
        }
    }

    public void setEventFile(final List<String> eventFiles) {
        if (m_eventFiles == eventFiles) return;
        m_eventFiles.clear();
        m_eventFiles.addAll(eventFiles);
    }

    public void setEventFileCollection(final List<String> eventFiles) {
    	setEventFile(eventFiles);
    }

    public void setGlobal(final Global global) {
        m_global = global;
    }

    public static Events unmarshal(final Reader reader) {
        return JaxbUtils.unmarshal(Events.class, reader);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_eventFiles == null) ? 0 : m_eventFiles.hashCode());
		result = prime * result + ((m_events == null) ? 0 : m_events.hashCode());
		result = prime * result + ((m_global == null) ? 0 : m_global.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Events)) return false;
		final Events other = (Events) obj;
		if (m_eventFiles == null) {
			if (other.m_eventFiles != null) return false;
		} else if (!m_eventFiles.equals(other.m_eventFiles)) {
			return false;
		}
		if (m_events == null) {
			if (other.m_events != null) return false;
		} else if (!m_events.equals(other.m_events)) {
			return false;
		}
		if (m_global == null) {
			if (other.m_global != null) return false;
		} else if (!m_global.equals(other.m_global)) {
			return false;
		}
		return true;
	}
	
	Resource getRelative(Resource baseRef, String relative) {
        try {
        	if (relative.startsWith("classpath:")) {
        		DefaultResourceLoader loader = new DefaultResourceLoader();
        		return loader.getResource(relative);
        	} else {
        		return baseRef.createRelative(relative);
        	}
        } catch (final IOException e) {
            throw new ObjectRetrievalFailureException(Resource.class, baseRef, "Resource location has a relative path, however the configResource does not reference a file, so the relative path cannot be resolved.  The location is: " + relative, null);
        }

	}

    public Map<String, Long> loadEventFiles(Resource configResource) throws IOException {
        Map<String, Long> lastModifiedEventFiles = new LinkedHashMap<String, Long>();
        loadEventFilesIfModified(configResource, lastModifiedEventFiles);
        return lastModifiedEventFiles;
    }

    public void loadEventFilesIfModified(Resource configResource, Map<String, Long> lastModifiedEventFiles) throws IOException {
        // Remove any event files that we're previously loaded, and no
        // longer appear in the list of event files
        for(Iterator<Map.Entry<String, Events>> it = m_loadedEventFiles.entrySet().iterator(); it.hasNext(); ) {
            String eventFile = it.next().getKey();
            if(!m_eventFiles.contains(eventFile)) {
                // The event file was previously loaded and has been removed
                // from the list of event files
                it.remove();
            }
        }

        // Conditionally load or reload the event files
        for(String eventFile : m_eventFiles) {
            Resource eventResource = getRelative(configResource, eventFile);
            long lastModified = eventResource.lastModified();

            // Determine whether or not the file should be loaded
            boolean shouldLoadFile = true;
            if (lastModifiedEventFiles.containsKey(eventFile)
                    && lastModifiedEventFiles.get(eventFile) == lastModified) {
                shouldLoadFile = false;
                // If we opt out to load a particular file, it must
                // be already loaded
                assert(m_loadedEventFiles.containsKey(eventFile));
            }

            // Skip any files that don't need to be loaded
            if (!shouldLoadFile) {
                continue;
            }

            lastModifiedEventFiles.put(eventFile, lastModified);

            Events events = JaxbUtils.unmarshal(Events.class, eventResource);
            if (events.getEventCount() <= 0) {
                throw new IllegalStateException("Uh oh! An event file "+eventResource.getFile()+" with no events has been laoded!");
            }
            if (events.getGlobal() != null) {
                throw new ObjectRetrievalFailureException(Resource.class, eventResource, "The event resource " + eventResource + " included from the root event configuration file cannot have a 'global' element", null);
            }
            if (events.getEventFileCollection().size() > 0) {
                throw new ObjectRetrievalFailureException(Resource.class, eventResource, "The event resource " + eventResource + " included from the root event configuration file cannot include other configuration files: " + StringUtils.collectionToCommaDelimitedString(events.getEventFileCollection()), null);
            }

            m_loadedEventFiles.put(eventFile, events);
        }
    }

	public boolean isSecureTag(String tag) {
		return m_global == null ? false : m_global.isSecureTag(tag);
	}
	
	private void partitionEvents(Partition partition) {
		m_partition = partition;

		m_partitionedEvents = new LinkedHashMap<String, List<Event>>();
		m_nullPartitionedEvents = new ArrayList<Event>();
		
		for(Event event : m_events) {
			List<String> keys = partition.group(event);
			if (keys == null) {
				m_nullPartitionedEvents.add(event);
			} else {
				for(String key : keys) {
					List<Event> events = m_partitionedEvents.get(key);
					if (events == null) {
						events = new ArrayList<Event>(1);
						m_partitionedEvents.put(key, events);
					}
					events.add(event);
				}
			}
		}
		
		
	}
	
	public Event findFirstMatchingEvent(org.opennms.netmgt.xml.event.Event matchingEvent) {
		String key = m_partition.group(matchingEvent);
		SortedSet<Event> potentialMatches = new TreeSet<Event>(m_nullPartitionedEvents);
		if (key != null) {
			List<Event> events = m_partitionedEvents.get(key);
			if (events != null) {
			    potentialMatches.addAll(events);
			}
		}
			
			
		
		for(Event event : potentialMatches) {
			if (event.matches(matchingEvent)) {
				return event;
			}
		}
		
		for(Entry<String, Events> loadedEvents : m_loadedEventFiles.entrySet()) {
			Events subEvents = loadedEvents.getValue();
			Event event = subEvents.findFirstMatchingEvent(matchingEvent);
			if (event != null) {
				return event;
			}
		}
		
		return null;
	}
	
	public Event findFirstMatchingEvent(EventCriteria criteria) {
		for(Event event : m_events) {
			if (criteria.matches(event)) {
				return event;
			}
		}
		
		for(Entry<String, Events> loadedEvents : m_loadedEventFiles.entrySet()) {
			Events events = loadedEvents.getValue();
			Event result = events.findFirstMatchingEvent(criteria);
			if (result != null) {
				return result;
			}
		}
		
		
		return null;
		
	}

	public <T> T forEachEvent(T initial, EventCallback<T> callback) {
		T result = initial;
		for(Event event : m_events) {
			result = callback.process(result, event);
		}
		
		for(Entry<String, Events> loadedEvents : m_loadedEventFiles.entrySet()) {
			Events events = loadedEvents.getValue();
			result = events.forEachEvent(result, callback);
		}
		
		
		return result;
	}
	
	public void initialize(Partition partition, EventOrdering eventOrdering) {
	    
	        m_ordering = eventOrdering;
	    
		for(Event event : m_events) {
			event.initialize(m_ordering.next());
		}
		
		partitionEvents(partition);
		
		for(Entry<String, Events> loadedEvents : m_loadedEventFiles.entrySet()) {
			Events events = loadedEvents.getValue();
			events.initialize(partition, m_ordering.subsequence());
		}

	}

	public Events getLoadEventsByFile(String relativePath) {
		return m_loadedEventFiles.get(relativePath);
	}

    public void addLoadedEventFile(String relativePath, Events events) {
        if (!m_eventFiles.contains(relativePath)) {
            m_eventFiles.add(relativePath);
        }
        m_loadedEventFiles.put(relativePath, events);
    }

	public void removeLoadedEventFile(String relativePath) {
		m_eventFiles.remove(relativePath);
		m_loadedEventFiles.remove(relativePath);
	}

	public void saveEvents(Resource resource) {
		final StringWriter stringWriter = new StringWriter();
		JaxbUtils.marshal(this, stringWriter);

		if (stringWriter.toString() != null) {
			File file;
			try {
				file = resource.getFile();
			} catch (final IOException e) {
				throw new DataAccessResourceFailureException("Event resource '" + resource + "' is not a file resource and cannot be saved.  Nested exception: " + e, e);
			}

			Writer fileWriter = null;
			try {
				try {
					fileWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
				} catch (final IOException e) {
					throw new DataAccessResourceFailureException("Event file '" + file + "' could not be opened.  Nested exception: " + e, e);
				}

				try {
					fileWriter.write(stringWriter.toString());
				} catch (final IOException e) {
					throw new DataAccessResourceFailureException("Event file '" + file + "' could not be written to.  Nested exception: " + e, e);
				}

				try {
					fileWriter.close();
				} catch (final IOException e) {
					throw new DataAccessResourceFailureException("Event file '" + file + "' could not be closed.  Nested exception: " + e, e);
				}
			} finally {
				if (fileWriter != null) try { fileWriter.close(); } catch(Exception e) {}
			}
		}

	}
	
	public void save(Resource resource) {
		for(Entry<String, Events> entry : m_loadedEventFiles.entrySet()) {
			String eventFile = entry.getKey();
			Events events = entry.getValue();
			
			Resource eventResource = getRelative(resource, eventFile);
			events.save(eventResource);
			
		}
		
		saveEvents(resource);
	}
}
