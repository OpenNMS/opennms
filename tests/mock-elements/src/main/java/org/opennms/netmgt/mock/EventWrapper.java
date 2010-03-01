//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.mock;


import org.opennms.netmgt.model.events.EventUtils;
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
        return MockEventUtil.eventsMatch(m_event, w.m_event);
    }

    public Event getEvent() {
        return m_event;
    }

    public int hashCode() {
        return m_event.getUei().hashCode();
    }
    
    public String toString() {
        Event event = m_event;
    		return EventUtils.toString(event);
    }
}
