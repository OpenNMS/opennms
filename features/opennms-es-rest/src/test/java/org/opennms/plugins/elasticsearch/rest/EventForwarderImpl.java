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

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventForwarderImpl implements EventForwarder {

	private static final Logger LOG = LoggerFactory.getLogger(EventForwarderImpl.class);

	private EventToIndex eventToIndex;
	
	@Override
	public void sendNow(Event event) {
		if (event != null) {
			LOG.debug("Event to send received: " + event.toString());
			sendNow(Collections.singletonList(event));
		}
	}

	@Override
	public void sendNow(Log eventLog) {
		if (eventLog != null && eventLog.getEvents() != null) {
			for (Event event : eventLog.getEvents().getEvent()) {
				sendNow(event);
			}
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

	public void setEventToIndex(EventToIndex eventToIndex) {
		this.eventToIndex = eventToIndex;
	}

	private void sendNow(List<Event> events) {
		if (eventToIndex != null) {
			eventToIndex.forwardEvents(events);
		}
	}

}
