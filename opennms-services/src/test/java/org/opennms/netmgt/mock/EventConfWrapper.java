/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.util.Iterator;

import org.opennms.netmgt.xml.eventconf.Autoaction;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Forward;
import org.opennms.netmgt.xml.eventconf.Operaction;
import org.opennms.netmgt.xml.eventconf.Script;

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

    @Override
    public int hashCode() {
        return m_event.getUei().hashCode();
    }
    
    @Override
    public String toString() {
    		StringBuffer b = new StringBuffer("Event: ");
    		if (m_event.getAutoacknowledge() != null) {
    			b.append(" Autoacknowledge: " + m_event.getAutoacknowledge() + "\n");
    		}
    		if (m_event.getAutoactionCount() > 0) {
    			b.append(" Autoactions:");
    			for (Iterator<Autoaction> i = m_event.getAutoactionCollection().iterator(); i.hasNext(); ) {
    				b.append(" " + i.next().toString());
    			}
			b.append("\n");
    		}
		if (m_event.getDescr() != null) {
			b.append(" Descr: " + m_event.getDescr() + "\n");
		}
		if (m_event.getForwardCount() > 0) {
			b.append(" Forwards:");
			for (Iterator<Forward> i = m_event.getForwardCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getLoggroupCount() > 0) {
			b.append(" Loggroup:");
			for (Iterator<String> i = m_event.getLoggroupCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next());
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
			for (Iterator<Operaction> i = m_event.getOperactionCollection().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getOperinstruct() != null) {
			b.append(" Operinstruct: " + m_event.getOperinstruct() + "\n");
		}
		if (m_event.getScriptCount() > 0) {
			b.append(" Script:");
			for (Iterator<Script> i = m_event.getScriptCollection().iterator(); i.hasNext(); ) {
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
    
    public static String toString(Event e) {
    	return e == null ? null : new EventConfWrapper(e).toString();
    }
}