package org.opennms.netmgt.correlation;

import java.util.List;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;

public abstract class AbstractCorrelationEngine implements CorrelationEngine {

    private EventIpcManager m_eventIpcManager;

    abstract public void correlate(Event e);

    abstract public List<String> getInterestingEvents();
    
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }
    
    public void sendEvent(Event e) {
        m_eventIpcManager.sendNow(e);
    }

}
