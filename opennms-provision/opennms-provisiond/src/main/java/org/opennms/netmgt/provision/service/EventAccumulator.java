package org.opennms.netmgt.provision.service;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EventAccumulator implements EventForwarder {
    private static final Logger LOG = LoggerFactory.getLogger(EventAccumulator.class);

    private final EventForwarder m_eventForwarder;
    private final List<Event> m_events = new ArrayList<>();

    public EventAccumulator(final EventForwarder forwarder) {
        m_eventForwarder = forwarder;
    }

    @Override
    public synchronized void sendNow(final Event event) {
        m_events.add(event);
    }

    @Override
    public synchronized void sendNow(final Log log) {
        if (log != null && log.getEvents() != null && log.getEvents().getEventCount() > 0) {
            for (final Event e : log.getEvents().getEventCollection()) {
                m_events.add(e);
            }
        }
    }
    
    public synchronized void flush() {
        LOG.debug("flush(): sending {} events: {}", m_events.size(), m_events);
        for (final Event e : m_events) {
            m_eventForwarder.sendNow(e);
        }
        m_events.clear();
    }
}