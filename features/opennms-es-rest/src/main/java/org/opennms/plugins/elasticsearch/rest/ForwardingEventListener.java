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

package org.opennms.plugins.elasticsearch.rest;

import java.util.Objects;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This event sends incoming events to an {@link EventForwarder} that uses Jest to
 * forward events to an external Elasticsearch.
 */
public class ForwardingEventListener implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ForwardingEventListener.class);

	private final EventForwarder eventForwarder;
	private final EventIpcManager eventIpcManager;

	public ForwardingEventListener(EventForwarder eventForwarder, EventIpcManager eventIpcManager) {
		this.eventForwarder = Objects.requireNonNull(eventForwarder);
		this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
	}

	public void init() {
		eventIpcManager.addEventListener(this);
		LOG.debug("Elasticsearch event forwarder initialized");
	}

	public void destroy() {
		if (eventIpcManager != null) {
			eventIpcManager.removeEventListener(this);
		}
		LOG.debug("Elasticsearch event forwarder unregisted for events");
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is invoked by the JMS topic session when a new event is
	 * available for processing. Currently only text based messages are
	 * processed by this callback. Each message is examined for its Universal
	 * Event Identifier and the appropriate action is taking based on each
	 * UEI.
	 */
	@Override
	public void onEvent(final Event event) {
		eventForwarder.sendNow(event);
	}


	@Override
	public String getName() {
		return "ElasticsearchRestEventForwarder";
	}
}
