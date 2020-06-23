/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rtc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BroadcastEventProcessor is responsible for receiving events from eventd and
 * queuing them to the data updaters.
 * 
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
@EventListener(name="RTC:BroadcastEventProcessor", logPrefix="rtc")
public class BroadcastEventProcessor implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);

    /**
     * The location where incoming events of interest are enqueued
     */
    private ExecutorService m_updater;

    @Autowired
    private DataManager m_dataManager;

    @Autowired
    private RTCConfigFactory m_configFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        m_updater = Executors.newFixedThreadPool(
            m_configFactory.getUpdaters(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), m_configFactory.getUpdaters())
        );
    }

    @EventHandler(ueis={
        // add the outageCreated event
        EventConstants.OUTAGE_CREATED_EVENT_UEI,
        // add the outageResolved event
        EventConstants.OUTAGE_RESOLVED_EVENT_UEI,
        // add the nodeGainedService event
        EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
        // add the nodeCategoryMembershipChanged event
        EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI,
        // add the interfaceUp event
        EventConstants.SERVICE_DELETED_EVENT_UEI,
        // add the serviceDeleted event
        EventConstants.SERVICE_UNMANAGED_EVENT_UEI,
        // add the interfaceReparented event
        EventConstants.INTERFACE_REPARENTED_EVENT_UEI,
        // add the asset info changed event
        EventConstants.ASSET_INFO_CHANGED_EVENT_UEI
    })
    public void onEvent(Event event) {
        if (event == null) {
            return;
        }

        LOG.debug("About to start processing recd. event");

        try {

            String uei = event.getUei();
            if (uei == null) {
                return;
            }

            m_updater.execute(new DataUpdater(m_dataManager, event));

            LOG.debug("Event {} added to updater queue", uei);

        } catch (RejectedExecutionException ex) {
            LOG.error("Failed to process event", ex);
            return;
        } catch (Throwable t) {
            LOG.error("Failed to process event", t);
            return;
        }

    } // end onEvent()

} // end class
