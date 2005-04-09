package org.opennms.netmgt.mock;

import java.util.Iterator;

import org.opennms.netmgt.xml.event.Event;

/**
 * Need this class because Event doesn't properly implement hashCode
 */
public class EventWrapper {
    private Event m_event;

    public EventWrapper(Event event) {
        m_event = event;
    }

    public boolean equals(Object o) {
        EventWrapper w = (EventWrapper) o;
        return MockUtil.eventsMatch(m_event, w.m_event);
    }

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
    		if (m_event.getCreationTime() != null) {
    			b.append(" CreationTime: " + m_event.getCreationTime() + "\n");
        }
		b.append(" Dbid: " + m_event.getDbid() + "\n");
		if (m_event.getDescr() != null) {
			b.append(" Descr: " + m_event.getDescr() + "\n");
		}
		if (m_event.getDistPoller() != null) {
			b.append(" DistPoller: " + m_event.getDistPoller() + "\n");
		}
		if (m_event.getForwardCount() > 0) {
			b.append(" Forwards:");
			for (Iterator i = m_event.getForwardCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getHost() != null) {
			b.append(" Host: " + m_event.getHost() + "\n");
		}
		if (m_event.getInterface() != null) {
			b.append(" Interface: " + m_event.getInterface() + "\n");
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
		if (m_event.getMasterStation() != null) {
			b.append(" MasterStation: " + m_event.getMasterStation() + "\n");
		}
		if (m_event.getMouseovertext() != null) {
			b.append(" Mouseovertext: " + m_event.getMouseovertext() + "\n");
		}
		b.append(" Nodeid: " + m_event.getNodeid() + "\n");
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
		if (m_event.getParms() != null) {
			b.append(" Parms: " + new ParmsWrapper(m_event.getParms()) + "\n");
		}
		if (m_event.getScriptCount() > 0) {
			b.append(" Script:");
			for (Iterator i = m_event.getScriptCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getService() != null) {
			b.append(" Service: " + m_event.getService() + "\n");
		}
		if (m_event.getSeverity() != null) {
			b.append(" Severity: " + m_event.getSeverity() + "\n");
		}
		if (m_event.getSnmp() != null) {
			b.append(" Snmp: " + new SnmpWrapper(m_event.getSnmp()) + "\n");
		}
		if (m_event.getSnmphost() != null) {
			b.append(" Snmphost: " + m_event.getSnmphost() + "\n");
		}
		if (m_event.getSource() != null) {
			b.append(" Source: " + m_event.getSource() + "\n");
		}
		if (m_event.getTime() != null) {
			b.append(" Time: " + m_event.getTime() + "\n");
		}
		if (m_event.getTticket() != null) {
			b.append(" Tticket: " + m_event.getTticket() + "\n");
		}
		if (m_event.getUei() != null) {
			b.append(" Uei: " + m_event.getUei() + "\n");
		}
		if (m_event.getUuid() != null) {
			b.append(" Uuid: " + m_event.getUuid() + "\n");
		}
        
		b.append("End Event\n");
        return b.toString();
    }
}