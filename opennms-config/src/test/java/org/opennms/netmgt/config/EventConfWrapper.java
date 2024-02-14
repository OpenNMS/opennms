/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config;

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
    		final StringBuilder b = new StringBuilder("Event: ");
    		if (m_event.getAutoacknowledge() != null) {
    			b.append(" Autoacknowledge: " + m_event.getAutoacknowledge() + "\n");
    		}
    		if (m_event.getAutoactions().size() > 0) {
    			b.append(" Autoactions:");
    			for (Iterator<Autoaction> i = m_event.getAutoactions().iterator(); i.hasNext(); ) {
    				b.append(" " + i.next().toString());
    			}
			b.append("\n");
    		}
		if (m_event.getDescr() != null) {
			b.append(" Descr: " + m_event.getDescr() + "\n");
		}
		if (m_event.getForwards().size() > 0) {
			b.append(" Forwards:");
			for (Iterator<Forward> i = m_event.getForwards().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getLoggroups().size() > 0) {
			b.append(" Loggroup:");
			for (Iterator<String> i = m_event.getLoggroups().iterator(); i.hasNext(); ) {
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
		if (m_event.getOperactions().size() > 0) {
			b.append(" Operaction:");
			for (Iterator<Operaction> i = m_event.getOperactions().iterator(); i.hasNext(); ) {
				b.append(" " + i.next().toString());
			}
			b.append("\n");
		}
		if (m_event.getOperinstruct() != null) {
			b.append(" Operinstruct: " + m_event.getOperinstruct() + "\n");
		}
		if (m_event.getScripts().size() > 0) {
			b.append(" Script:");
			for (Iterator<Script> i = m_event.getScripts().iterator(); i.hasNext(); ) {
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