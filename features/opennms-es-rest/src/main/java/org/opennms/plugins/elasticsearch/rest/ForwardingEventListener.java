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
package org.opennms.plugins.elasticsearch.rest;

import java.util.Objects;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
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
	public void onEvent(final IEvent event) {
		eventForwarder.sendNow(Event.copyFrom(event));
	}


	@Override
	public String getName() {
		return "ElasticsearchRestEventForwarder";
	}
}
