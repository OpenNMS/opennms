package org.opennms.netmgt.dao.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.config.EnterpriseIdPartition;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventLabelComparator;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Events.EventCallback;
import org.opennms.netmgt.xml.eventconf.Events.EventCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;

public class MockEventConfDao implements EventConfDao, InitializingBean {
    private Resource m_resource;
    private Events m_events;
    private EnterpriseIdPartition m_partition;

    public void setResource(final Resource resource) {
        m_resource = resource;
    }

    public Resource getResource() {
        return m_resource;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_resource);
        reload();
    }

    @Override
    public void reload() throws DataAccessException {
        InputStream is = null;
        InputStreamReader isr = null;
        try {
            is = m_resource.getInputStream();
            isr = new InputStreamReader(is);
            m_events = Events.unmarshal(isr);
            m_events.loadEventFiles(m_resource);
            m_partition = new EnterpriseIdPartition();
            m_events.initialize(m_partition);
        } catch (final IOException e) {
            throw new DataRetrievalFailureException("Failed to read from " + m_resource.toString(), e);
        } finally {
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public List<Event> getEvents(final String uei) {
        final List<Event> events = new ArrayList<Event>();
        m_events.forEachEvent(events, new EventCallback<List<Event>>() {
            @Override
            public List<Event> process(final List<Event> events, final Event event) {
                if (uei.equals(event.getUei())) {
                    events.add(event);
                }
                return events;
            }
        });
        return events;
    }

    @Override
    public List<String> getEventUEIs() {
        final Set<String> ueis = new HashSet<String>();
        m_events.forEachEvent(ueis, new EventCallback<Set<String>>() {
            @Override
            public Set<String> process(final Set<String> ueis, final Event event) {
                ueis.add(event.getUei());
                return ueis;
            }
        });
        return new ArrayList<String>(ueis);
    }

    @Override
    public Map<String, String> getEventLabels() {
        final Map<String,String> labels = new HashMap<String,String>();
        m_events.forEachEvent(labels, new EventCallback<Map<String,String>>() {
            @Override
            public Map<String,String> process(final Map<String,String> labels, final Event event) {
                labels.put(event.getUei(), event.getEventLabel());
                return labels;
            }
        });
        return labels;
    }

    @Override
    public String getEventLabel(final String uei) {
        return getEventLabels().get(uei);
    }

    @Override
    public void saveCurrent() {
            m_events.save(m_resource);
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
    public void addEvent(final Event event) {
        m_events.addEvent(event);
    }

    @Override
    public void addEventToProgrammaticStore(final Event event) {
        m_events.addEvent(event);
    }

    @Override
    public boolean removeEventFromProgrammaticStore(final Event event) {
        return m_events.removeEvent(event);
    }

    @Override
    public boolean isSecureTag(final String tag) {
        return m_events.isSecureTag(tag);
    }

    @Override
    public Event findByUei(final String uei) {
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

}
