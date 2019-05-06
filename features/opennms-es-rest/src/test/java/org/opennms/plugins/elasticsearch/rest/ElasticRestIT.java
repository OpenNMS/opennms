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

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticRestIT extends AbstractEventToIndexTest{
	
	private static final Logger LOG = LoggerFactory.getLogger(RawEventToIndexTest.class);

	private static final String EVENT_INDEX_TYPE = "eventdata";

	private static final String EVENT_SOURCE_NAME = "AlarmChangeNotifier";

	private static final String NODE_LOST_SERVICE_EVENT="uei.opennms.org/nodes/nodeLostService";
	
	/**
	 * simple test to create a raw event in the raw event index and test that the event is added
	 */
	@Test
	public void jestClientRawEventToESTest() throws Exception {
		LOG.debug("***************** start of test jestClientRawEventToESTestt");

			final NodeCache nodeCache = new MockNodeCache();

			eventToIndex.setNodeCache(nodeCache);
			eventToIndex.setIndexStrategy(IndexStrategy.MONTHLY);
			eventToIndex.setLogEventDescription(true);
			eventToIndex.setArchiveRawEvents(true);
			eventToIndex.setArchiveAlarms(true);
			eventToIndex.setArchiveAlarmChangeEvents(true);
			eventToIndex.setArchiveOldAlarmValues(true);
			eventToIndex.setArchiveNewAlarmValues(true);
			eventToIndex.seteventIndexName("onms-cert1");

			EventBuilder eb = new EventBuilder( NODE_LOST_SERVICE_EVENT, EVENT_SOURCE_NAME);
		
			eb.setUei("uei.opennms.org/nodes/nodeLostService");
			eb.setNodeid(36);
			InetAddress ipAddress = InetAddressUtils.getInetAddress("142.34.5.19");
			eb.setInterface(ipAddress);
			eb.setSource("mock event test");
			eb.setHost("localhost");
			eb.setLogDest("logndisplay");
			eb.setLogMessage("this is a test log message");
			eb.setDescription("this is a test description");
			eb.setTime(new Date());
			eb.setUuid("00000000-0000-0000-0000-000000000000");	
			//eb.addParam("forwarder", "onms-cert1");

			Event event = eb.getEvent();
			event.setDbid(101);
			
			LOG.debug("ecreated node lost service event:"+event.toString());

			// forward event to Elasticsearch
			eventToIndex.forwardEvents(Collections.singletonList(event));
			
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) { }
			
			

	}
		

}
