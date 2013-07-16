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

package org.opennms.netmgt.syslogd;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);
    /**
     * Create message selector to set to the subscription
     */
    BroadcastEventProcessor() {
        // Create the selector for the ueis this service is interested in
        //
        List<String> ueiList = new ArrayList<String>();

        // nodeGainedInterface
        ueiList.add(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);

        // interfaceDeleted
        ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);

        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);
    }



    /**
     * Unsubscribe from eventd
     */
    public void close() {
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each
     * UEI.
     */
    @Override
    public void onEvent(Event event) {

        String eventUei = event.getUei();
        if (eventUei == null)
            return;


        LOG.debug("Received event: {}", eventUei);

        if (eventUei.equals(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)) {
            // add to known nodes
            if (Long.toString(event.getNodeid()) != null && event.getInterface() != null) {
                SyslogdIPMgr.setNodeId(event.getInterface(), event.getNodeid());
            }
            LOG.debug("Added {} to known node list", event.getInterface());
        } else if (eventUei.equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
            // remove from known nodes
            if (event.getInterface() != null) {
                SyslogdIPMgr.removeNodeId(event.getInterface());
            }
            LOG.debug("Removed {} from known node list", event.getInterface());
        } else if (eventUei.equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            // add to known nodes
            if (Long.toString(event.getNodeid()) != null && event.getInterface() != null) {
                SyslogdIPMgr.setNodeId(event.getInterface(), event.getNodeid());
            }
            LOG.debug("Reparented {} to known node list", event.getInterface());
        }
    }

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Syslogd:BroadcastEventProcessor";
    }
}
