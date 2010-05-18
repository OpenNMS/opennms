package org.opennms.netmgt.threshd;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class ThresholdingEventProxy implements EventProxy {

    private List<Event> m_events;
    
    public ThresholdingEventProxy() {
        m_events = new LinkedList<Event>();
    }
    
    public void send(Event event) throws EventProxyException {
        add(event);
    }

    public void send(Log eventLog) throws EventProxyException {
        for (Event e : eventLog.getEvents().getEventCollection()) {
            add(e);
        }
    }
    
    public void add(Event event) {
        m_events.add(event);
    }

    public void add(List<Event> events) {
        m_events.addAll(events);
    }

    public void removeAllEvents() {
        m_events.clear();
    }
    
    public void sendAllEvents() {
        if (m_events.size() > 0) {
            try {
                Log log = new Log();
                Events events = new Events();
                for (Event e : m_events) {
                    events.addEvent(e);
                }
                log.setEvents(events);
                EventIpcManagerFactory.getIpcManager().sendNow(log);
            } catch (Exception e) {
                log().info("sendAllEvents: Failed sending threshold events: " + e, e);
            }
            removeAllEvents();
        }
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
