package org.opennms.netmgt.threshd;

import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

/**
 * <p>ThresholdingEventProxy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdingEventProxy implements EventProxy {

    private List<Event> m_events;
    
    /**
     * <p>Constructor for ThresholdingEventProxy.</p>
     */
    public ThresholdingEventProxy() {
        m_events = new LinkedList<Event>();
    }
    
    /** {@inheritDoc} */
    public void send(Event event) throws EventProxyException {
        add(event);
    }

    /**
     * <p>send</p>
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public void send(Log eventLog) throws EventProxyException {
        for (Event e : eventLog.getEvents().getEventCollection()) {
            add(e);
        }
    }
    
    /**
     * <p>add</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void add(Event event) {
        m_events.add(event);
    }

    /**
     * <p>add</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void add(List<Event> events) {
        m_events.addAll(events);
    }

    /**
     * <p>removeAllEvents</p>
     */
    public void removeAllEvents() {
        m_events.clear();
    }
    
    /**
     * <p>sendAllEvents</p>
     */
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
