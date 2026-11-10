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
package org.opennms.features.amqp.eventforwarder;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
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
	public void onEvent(final IEvent event) {
		LOG.debug("Forwarding event with uei: {}", event.getUei());
		eventForwarder.sendNow(Event.copyFrom(event));
	}

	@Override
	public String getName() {
		return "AMQPEventForwarder";
	}
}
