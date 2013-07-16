/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.events.EventIpcBroadcaster;
import org.opennms.netmgt.model.events.EventProcessor;
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
    private static final Logger LOG = LoggerFactory.getLogger(EventIpcBroadcastProcessor.class);
    private EventIpcBroadcaster m_eventIpcBroadcaster;
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_eventIpcBroadcaster != null, "property eventIpcBroadcaster must be set");
    }

    /** {@inheritDoc} */
    @Override
    public void process(Header eventHeader, Event event) {
        if (event.getLogmsg() != null && event.getLogmsg().getDest().equals("suppress")) {
            LOG.debug("process: skip sending event {} to other daemons because is marked as suppress", event.getUei());
        } else {
            m_eventIpcBroadcaster.broadcastNow(event);
        }
    }

    /**
     * <p>getEventIpcBroadcaster</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcBroadcaster} object.
     */
    public EventIpcBroadcaster getEventIpcBroadcaster() {
        return m_eventIpcBroadcaster;
    }

    /**
     * <p>setEventIpcBroadcaster</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcBroadcaster} object.
     */
    public void setEventIpcBroadcaster(EventIpcBroadcaster eventIpcManager) {
        m_eventIpcBroadcaster = eventIpcManager;
    }
}
