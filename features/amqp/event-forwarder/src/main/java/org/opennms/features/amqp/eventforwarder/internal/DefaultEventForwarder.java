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

package org.opennms.features.amqp.eventforwarder.internal;

import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class DefaultEventForwarder extends DefaultDispatcher implements EventForwarder {

	@Produce(property="endpointUri")
	EventForwarder m_proxy;

	public DefaultEventForwarder(final String endpointUri) {
		super(endpointUri);
	}

	/**
	 * Send the incoming {@link Event} message into the Camel route
	 * specified by the {@link #m_endpointUri} property.
	 */
	@Override
	public void sendNow(Event event) {
		m_proxy.sendNow(event);
	}

	/**
	 * Send the incoming {@link Log} message into the Camel route
	 * specified by the {@link #m_endpointUri} property.
	 */
	@Override
	public void sendNow(Log eventLog) {
		for (Event event : eventLog.getEvents().getEventCollection()) {
			m_proxy.sendNow(event);
		}
	}

	@Override
	public void sendNowSync(Event event) {
		sendNow(event);
	}

	@Override
	public void sendNowSync(Log eventLog) {
		sendNow(eventLog);
	}
}
