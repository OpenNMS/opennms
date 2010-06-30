//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 02: Move broadcastNow to EventIpcBroadcaster. - dj@opennms.org
// 2008 Jan 27: Created this file. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.eventd.processor;

import org.opennms.netmgt.eventd.EventIpcBroadcaster;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * EventProcessor that braodcasts events to other interested
 * daemons with EventIpcBroadcaster.broadcastNow(Event).
 *
 * @author ranger
 * @version $Id: $
 */
public class EventIpcBroadcastProcessor implements EventProcessor, InitializingBean {
    private EventIpcBroadcaster m_eventIpcBroadcaster;
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_eventIpcBroadcaster != null, "property eventIpcBroadcaster must be set");
    }

    /** {@inheritDoc} */
    public void process(Header eventHeader, Event event) {
        m_eventIpcBroadcaster.broadcastNow(event);
    }

    /**
     * <p>getEventIpcBroadcaster</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventIpcBroadcaster} object.
     */
    public EventIpcBroadcaster getEventIpcBroadcaster() {
        return m_eventIpcBroadcaster;
    }

    /**
     * <p>setEventIpcBroadcaster</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.eventd.EventIpcBroadcaster} object.
     */
    public void setEventIpcBroadcaster(EventIpcBroadcaster eventIpcManager) {
        m_eventIpcBroadcaster = eventIpcManager;
    }
}
