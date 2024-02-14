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
package org.opennms.plugins.elasticsearch.rest.archive.manual;

import java.util.List;

import org.junit.Test;
import org.junit.Ignore;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.plugins.elasticsearch.rest.archive.OnmsRestEventsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore("manual test meant to be run against a real elasticsearch")
public class OnmsRestEventsToEsIT {
	private static final Logger LOG = LoggerFactory.getLogger(OnmsRestEventsToEsIT.class);

	private String opennmsUrl = "http://localhost:8980";
	private String userName = "admin";
	private String passWord = "admin";

	/**
	 * test if events can be received from OpenNMS ReST UI
	 */
	@Test
	public void getEventsTest() {
		LOG.debug("starting getEventsTest");
		final OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient(opennmsUrl, userName, passWord);
		final List<Event> receivedEvents = onmsRestEventsClient.getEvents(1, 5);
		if (receivedEvents.isEmpty()){
			LOG.debug("\nNO TEST RECEIVED EVENTS ----------------------------------------");
		} else {
			LOG.debug("\nTEST RECEIVED EVENTS ----------------------------------------");
			for(Event event : receivedEvents){
				LOG.debug("Event id: "+ event.getDbid()+" uei: "+ event.getUei());
			}
			LOG.debug("\nEND OF TEST RECEIVED EVENTS ----------------------------------------");
		}
		LOG.debug("finished getEventsTest");

	}

	@Test
	public void getEventCountTest() {
		LOG.debug("starting getEventCountTest");
		OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient(opennmsUrl, userName, passWord);
		Integer eventCount = onmsRestEventsClient.getEventCount();
		LOG.debug("event count={}", eventCount);
		LOG.debug("finished getEventCountTest");

	}

}
