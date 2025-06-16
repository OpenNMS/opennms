/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
