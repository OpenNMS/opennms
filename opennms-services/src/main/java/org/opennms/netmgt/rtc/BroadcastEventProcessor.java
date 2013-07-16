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

package org.opennms.netmgt.rtc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * BroadcastEventProcessor is responsible for receiving events from eventd and
 * queuing them to the data updaters.
 * 
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class BroadcastEventProcessor implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);
    /**
     * The location where incoming events of interest are enqueued
     */
    private final ExecutorService m_updater;

    /**
     * Constructor
     * 
     * @param updaterQ
     *            The queue where events of interest are added.
     */
    BroadcastEventProcessor(ExecutorService updaterQ) {
        m_updater = updaterQ;
    }

    /**
     * Create a list of UEIs of interest to the RTC and subscribe to eventd
     */
    public void start() {
        List<String> ueisOfInterest = new ArrayList<String>();

        // add the nodeGainedService event
        ueisOfInterest.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);

        // add the nodeLostService event
        ueisOfInterest.add(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);

        // add the interfaceDown event
        ueisOfInterest.add(EventConstants.INTERFACE_DOWN_EVENT_UEI);

        // add the nodeDown event
        ueisOfInterest.add(EventConstants.NODE_DOWN_EVENT_UEI);

        // add the nodeUp event
        ueisOfInterest.add(EventConstants.NODE_UP_EVENT_UEI);
        
        // add the nodeCategoryMembershipChanged event
        ueisOfInterest.add(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI);

        // add the interfaceUp event
        ueisOfInterest.add(EventConstants.INTERFACE_UP_EVENT_UEI);

        // add the nodeRegainedService event
        ueisOfInterest.add(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI);

        // add the serviceDeleted event
        ueisOfInterest.add(EventConstants.SERVICE_DELETED_EVENT_UEI);

        // add the serviceDeleted event
        ueisOfInterest.add(EventConstants.SERVICE_UNMANAGED_EVENT_UEI);

        // add the interfaceReparented event
        ueisOfInterest.add(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);

        // add the rtc subscribe event
        ueisOfInterest.add(EventConstants.RTC_SUBSCRIBE_EVENT_UEI);

        // add the rtc unsubscribe event
        ueisOfInterest.add(EventConstants.RTC_UNSUBSCRIBE_EVENT_UEI);

        // add the asset info changed event
        ueisOfInterest.add(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI);

        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueisOfInterest);
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
     * Event Identifier and the appropriate action is taking based on each UEI.
     */
    @Override
    public void onEvent(Event event) {
        if (event == null)
            return;



        LOG.debug("About to start processing recd. event");

        try {
            // check on timertasks and events counter
            RTCManager.getInstance().checkTimerTasksOnEventReceipt();

            String uei = event.getUei();
            if (uei == null)
                return;

            m_updater.execute(new DataUpdater(event));


            LOG.debug("Event {} added to updater queue", uei);

            // Reset the user timer
            RTCManager.getInstance().resetUserTimer();
        } catch (RejectedExecutionException ex) {
            LOG.error("Failed to process event", ex);
            return;
        } catch (Throwable t) {
            LOG.error("Failed to process event", t);
            return;
        }

    } // end onEvent()

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "RTCManager:BroadcastEventProcessor";
    }

} // end class
