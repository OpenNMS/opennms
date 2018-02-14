/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.amqp.eventforwarder;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * This event sends incoming events to an {@link EventForwarder} that uses Camel to 
 * forward events to an external AMQP compatible broker.
 */
public class ForwardingEventListener implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ForwardingEventListener.class);

	private volatile EventForwarder eventForwarder;
	private volatile EventIpcManager eventIpcManager;

	public EventForwarder getEventForwarder() {
		return eventForwarder;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

	public EventIpcManager getEventIpcManager() {
		return eventIpcManager;
	}

	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		this.eventIpcManager = eventIpcManager;
	}

	/**
	 * Called when the bean is initialized.
	 */
	public void init() {
	    Preconditions.checkNotNull(eventIpcManager, "eventIpcManager must not be null");
	    Preconditions.checkNotNull(eventForwarder, "eventForwarder must not be null");

	    eventIpcManager.addEventListener(this);
	}

    /**
     * Called when the bean is destroyed.
     */
	public void destroy() {
	    if (eventIpcManager != null) {
	        eventIpcManager.removeEventListener(this);
	    }
	}

	@Override
	public void onEvent(final Event event) {
		LOG.debug("Forwarding event with uei: {}", event.getUei());
		eventForwarder.sendNow(event);
	}

	@Override
	public String getName() {
		return "AMQPEventForwarder";
	}
}
