/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.features.correlator;

import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;

import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Neumann
 */
@EventListener(name = "Correlator", logPrefix = "Correlator-LogPrefix")
public class Correlator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Correlator.class);
    private EventProxy eventProxy;

    public Correlator(EventProxy eventProxy) {

        //TODO DEBUG REMOVE  SystemOut
        System.out.println("DEBUG testing correlator");
        LOGGER.debug("DEBUG testing correlator");

        if (eventProxy == null) {
            throw new RuntimeException("eventProxy cannot be null.");
        }
        this.eventProxy = eventProxy;
    }

    @EventHandler(uei = "uei.opennms.org/nodes/nodeDown")
    public void handleEventNodeDown(Event e) {
        //TODO DEBUG
        LOGGER.debug("Received nodeDown configuration event: {}", e);
        System.out.println("DEBUG event nodeDown Received: {}" + e);
    }
    
    @EventHandler(uei = "*")
    public void handleAllEvent(Event e) {
        //TODO DEBUG
        LOGGER.info("Received reload configuration event: {}", e);
        System.out.println("DEBUG event gateway Received reload configuration event: {}" + e);
        LOGGER.debug("DEBUG event gateway Received reload configuration event: {}" + e);
    }

    public EventProxy getEventProxy() {
        return eventProxy;
    }

    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }
}
