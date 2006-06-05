package org.opennms.netmgt.mock;

import java.util.Iterator;

import org.opennms.netmgt.xml.eventconf.Event;

/**
 * Need this class because Event doesn't properly implement hashCode
 */
public class EventConfWrapper {
    private Event m_event;

    public EventConfWrapper(Event event) {
        m_event = event;
    }

	/*
    public boolean equals(Object o) {
        EventConfWrapper w = (EventConfWrapper) o;
        return MockUtil.eventsMatch(m_event, w.m_event);
    }
    */

    public Event getEvent() {
        return m_event;
    }

    public int hashCode() {
        return m_event.getUei().hashCode();
    }
    
    public String toString() {
    		StringBuffer b = new StringBuffer("Event: ");
    		if (m_event.getAutoacknowledge() != null) {
    			b.append(" Autoacknowledge: " + m_event.getAutoacknowledge() + "\n");
    		}
    		if (m_event.getAutoactionCount() > 0) {
    			b.append(" Autoactions:");
    			for (Iterator i = m_event.getAutoactionCollection().iterator(); i.hasNext(); ) {
    				b.append(" " + i.next().toString());
    			}
			b.append("\n");
    		}
		if (m_event.getDescr() != null) {
			b.append(" Descr: " + m_event.getDescr() + "\n");
		}
		if (m_event.getForwardCount() > 0) {
			b.append(" Forwards:");
			for (Iterator i = m_event.getForwardCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getLoggroupCount() > 0) {
			b.append(" Loggroup:");
			for (Iterator i = m_event.getLoggroupCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getLogmsg() != null) {
			b.append(" Logmsg: " + m_event.getLogmsg() + "\n");
		}
		if (m_event.getMask() != null) {
			b.append(" Mask: " + m_event.getMask() + "\n");
		}
		if (m_event.getMouseovertext() != null) {
			b.append(" Mouseovertext: " + m_event.getMouseovertext() + "\n");
		}
		if (m_event.getOperactionCount() > 0) {
			b.append(" Operaction:");
			for (Iterator i = m_event.getOperactionCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getOperinstruct() != null) {
			b.append(" Operinstruct: " + m_event.getOperinstruct() + "\n");
		}
		if (m_event.getScriptCount() > 0) {
			b.append(" Script:");
			for (Iterator i = m_event.getScriptCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getSeverity() != null) {
			b.append(" Severity: " + m_event.getSeverity() + "\n");
		}
		/*
		if (m_event.getSnmp() != null) {
			b.append(" Snmp: " + new EventConfSnmpWrapper(m_event.getSnmp()) + "\n");
		}
		*/
		if (m_event.getTticket() != null) {
			b.append(" Tticket: " + m_event.getTticket() + "\n");
		}
		if (m_event.getUei() != null) {
			b.append(" Uei: " + m_event.getUei() + "\n");
		}
        
		b.append("End Event\n");
        return b.toString();
    }
}