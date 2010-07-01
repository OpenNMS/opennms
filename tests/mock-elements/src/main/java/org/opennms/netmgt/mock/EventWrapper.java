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
 *
 * @author ranger
 * @version $Id: $
 */
public class EventWrapper {
    private Event m_event;

    /**
     * <p>Constructor for EventWrapper.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public EventWrapper(Event event) {
        m_event = event;
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        EventWrapper w = (EventWrapper) o;
        return MockEventUtil.eventsMatch(m_event, w.m_event);
    }

    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
        return m_event;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_event.getUei().hashCode();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        Event event = m_event;
    		return EventUtils.toString(event);
    }
}
