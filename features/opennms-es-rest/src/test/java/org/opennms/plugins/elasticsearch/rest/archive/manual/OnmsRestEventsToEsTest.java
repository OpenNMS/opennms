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

package org.opennms.plugins.elasticsearch.rest.archive.manual;

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.plugins.elasticsearch.rest.archive.OnmsRestEventsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnmsRestEventsToEsTest {
	private static final Logger LOG = LoggerFactory.getLogger(OnmsRestEventsToEsTest.class);

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
		OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient(opennmsUrl, passWord, userName);
		Integer eventCount = onmsRestEventsClient.getEventCount();
		LOG.debug("event count={}", eventCount);
		LOG.debug("finished getEventCountTest");

	}

}
