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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;


public class AlarmEventToIndexTest extends AbstractEventToIndexTest {
	private static final Logger LOG = LoggerFactory.getLogger(AlarmEventToIndexTest.class);
	
	public static final int INDEX_WAIT_SECONDS=10; // time to wait for index to catch up

	public static final String EVENT_INDEX_TYPE = "eventdata";

	// uei definitions of alarm change events
	public static final String ALARM_ACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmAcknowledged";

	public static final String EVENT_SOURCE_NAME = "AlarmChangeNotifier";

	public static final String TEST_ALARM_JSON_1="{\"alarmid\":807,\"eventuei\":\"uei.opennms.org/nodes/nodeLostService\",\"nodeid\":36,"
			+ "\"ipaddr\":\"142.34.5.19\",\"serviceid\":2,\"reductionkey\":\"uei.opennms.org/nodes/nodeLostService::36:142.34.5.19:HTTP\","
			+ "\"alarmtype\":1,\"counter\":1,\"severity\":5,\"lasteventid\":7003,"
			+ "\"firsteventtime\":\"2016-08-15T18:30:04.252+01:00\","
			+ "\"lasteventtime\":\"2016-08-15T18:30:04.252+01:00\",\"firstautomationtime\":null,\"lastautomationtime\":null,"
			+ "\"description\":\"<p>A HTTP outage was identified on interface\n      142.34.5.19.</p> <p>A new Outage record has been\n"
			+ "      created and service level availability calculations will be\n      impacted until this outage is resolved.</p>\","
			+ "\"logmsg\":\"HTTP outage identified on interface 142.34.5.19 with reason code: Unknown.\","
			+ "\"operinstruct\":null,\"tticketid\":null,\"tticketstate\":null,\"mouseovertext\":null,"
			+ "\"suppresseduntil\":\"2016-08-15T18:30:04.252+01:00\",\"suppresseduser\":null,\"suppressedtime\":\"2016-08-15T18:30:04.252+01:00\","
			+ "\"alarmackuser\":null,\"alarmacktime\":null,\"managedobjectinstance\":null,"
			+ "\"managedobjecttype\":null,\"applicationdn\":null,\"ossprimarykey\":null,\"x733alarmtype\":null,\"x733probablecause\":0,"
			+ "\"qosalarmstate\":null,\"clearkey\":null,\"ifindex\":null,\"eventparms\":\"eventReason=Unknown(string,text)\","
			+ "\"stickymemo\":null,\"systemid\":\"00000000-0000-0000-0000-000000000000\"}";

	/**
	 * simple test to create an alarm change event which will create a new alarm in the alarm index
	 * and create an alarm change event in the alarm change index
	 */
	@Test
	public void jestClientAlarmToESTest() throws Exception {
		LOG.debug("***************** start of test jestClientAlarmToESTest");

			NodeCache nodeCache = new MockNodeCache();

			eventToIndex.setNodeCache(nodeCache);
			eventToIndex.setIndexStrategy(IndexStrategy.MONTHLY);
			eventToIndex.setLogEventDescription(true);
			eventToIndex.setArchiveRawEvents(true);
			eventToIndex.setArchiveAlarms(true);
			eventToIndex.setArchiveAlarmChangeEvents(true);
			eventToIndex.setArchiveOldAlarmValues(true);
			eventToIndex.setArchiveNewAlarmValues(true);

			// create an alarm change event
			EventBuilder eb = new EventBuilder( ALARM_ACKNOWLEDGED_EVENT, EVENT_SOURCE_NAME);

			//copy in all values as json in params
			eb.addParam("oldalarmvalues",TEST_ALARM_JSON_1);
			eb.addParam("newalarmvalues",TEST_ALARM_JSON_1);
			Event event = eb.getEvent();
			event.setDbid(100);
			event.setNodeid((long) 34);

			// forward event to Elasticsearch
			eventToIndex.forwardEvents(Collections.singletonList(event));

			// waiting INDEX_WAIT_SECONDS seconds for index 
			try {
				TimeUnit.SECONDS.sleep(INDEX_WAIT_SECONDS);
			} catch (InterruptedException e) { }

			// search for resulting alarm
			String query = "{\n" 
					+"\n       \"query\": {"
					+ "\n         \"match\": {"
					+ "\n         \"alarmid\": \"807\""
					+ "\n          }"
					+ "\n        }"
					+ "\n     }";
			
			LOG.debug("alarm check search query: "+query);

			Search search = new Search.Builder(query)
			// multiple index or types can be added.
			.addIndex("opennms-*")
			.build();

			SearchResult sresult = jestClient.execute(search);

			LOG.debug("received search sresult: "+sresult.getJsonString()
					+ "\n   response code:" +sresult.getResponseCode() 
					+ "\n   error message: "+sresult.getErrorMessage());
			
			assertEquals(200, sresult.getResponseCode());

			JSONParser parser = new JSONParser();
			Object obj = parser.parse(sresult.getJsonString());
			JSONObject resultValues = (JSONObject) obj;
			JSONObject hits = (JSONObject) resultValues.get("hits");

			LOG.debug("search result hits:total="+hits.get("total"));

			assertEquals(Long.valueOf(1), hits.get("total"));
			
			
			// waiting INDEX_WAIT_SECONDS seconds for index 
            try {
            	TimeUnit.SECONDS.sleep(INDEX_WAIT_SECONDS);
            } catch (InterruptedException e) { }
			
			// search for resulting alarm change event
			final String eventquery = buildSearchQuery(100);
			LOG.debug("event check search query: "+eventquery);

			Search eventsearch = new Search.Builder(eventquery)
			// multiple index or types can be added.
			.addIndex("opennms-*")
			.build();

			SearchResult eventsresult = jestClient.execute(eventsearch);

			LOG.debug("received search eventsresult: "+eventsresult.getJsonString()
					+ "\n   response code:" +eventsresult.getResponseCode() 
					+ "\n   error message: "+eventsresult.getErrorMessage());
			
			assertEquals(200, eventsresult.getResponseCode());

			Object obj2 = parser.parse(eventsresult.getJsonString());
			JSONObject eventsresultValues = (JSONObject) obj2;
			
			JSONObject eventhits = (JSONObject) eventsresultValues.get("hits");
			LOG.debug("search result event hits:total="+eventhits.get("total"));
			assertEquals(Long.valueOf(1), eventhits.get("total"));
			
			JSONArray eventhitsvalues = (JSONArray) eventhits.get("hits");
			LOG.debug("   eventhitsvalues: "+eventhitsvalues.toJSONString());
			
			JSONObject hitObj = (JSONObject) eventhitsvalues.get(0);
			LOG.debug("   hitObj: "+hitObj.toJSONString());
			
			String typeStr =  hitObj.get("_type").toString();

			LOG.debug("search result index type="+typeStr);
			assertEquals(EVENT_INDEX_TYPE, typeStr);
			
			JSONObject sourceObj = (JSONObject) hitObj.get("_source");
			LOG.debug("   sourceObj: "+sourceObj.toJSONString());
			
			String eventUeiStr =  sourceObj.get("eventuei").toString();

			LOG.debug("search result event eventueistr="+eventUeiStr);
			assertEquals(ALARM_ACKNOWLEDGED_EVENT, eventUeiStr);
		LOG.debug("***************** end of test jestClientAlarmToESTest");
	}

	// See NMS-9831 for more information
	@Test
	public void verifyOidMapping() throws InterruptedException, IOException {
		int eventId = 13;
		final Event event = createDummyEventWithOids(eventId);
		getEventToIndex().forwardEvents(Arrays.asList(event));

		TimeUnit.SECONDS.sleep(INDEX_WAIT_SECONDS);

		final String query = buildSearchQuery(eventId);
		final Search search = new Search.Builder(query)
				.addIndex("opennms-events-raw-*")
				.build();
		final SearchResult result = jestClient.execute(search);
		assertEquals(200, result.getResponseCode());
		assertEquals(Long.valueOf(1), result.getTotal());
	}



	// See NMS-9831 for more information
	@Test
	public void verifyOidGrouping() throws InterruptedException, IOException {
		int eventId = 15;
		final Event event = createDummyEventWithOids(eventId);
		getEventToIndex().setGroupOidParameters(true);
		getEventToIndex().forwardEvents(Arrays.asList(event));

		TimeUnit.SECONDS.sleep(INDEX_WAIT_SECONDS);

		final String query = buildSearchQuery(eventId);
		final Search search = new Search.Builder(query)
				.addIndex("opennms-events-raw-*")
				.build();
		final SearchResult result = jestClient.execute(search);
		assertEquals(200, result.getResponseCode());
		assertEquals(Long.valueOf(1), result.getTotal());

		// Verify oids
		final JsonArray oids = result.getJsonObject()
				.get("hits").getAsJsonObject()
					.get("hits").getAsJsonArray()
						.get(0).getAsJsonObject()
							.get("_source").getAsJsonObject()
								.get("p_oids").getAsJsonArray();
		assertNotNull(oids);
		assertEquals(99, oids.size());
	}

	// See HZN-1272
	@Test
	public void verifyJsonEventParameters() throws InterruptedException, IOException {
		final int eventId = 17;
		final Event event = createDummyEvent(eventId);

		final JsonObject addressObject = new JsonObject();
		addressObject.addProperty("street", "950 Windy Rd, Ste 300");
		addressObject.addProperty("city", "Apex");
		addressObject.addProperty("state", "NC");
		final JsonObject jsonObject = new JsonObject();
		jsonObject.add("address", addressObject);
		jsonObject.addProperty("name", "The OpenNMS Group");

		// Create Event Parameter, which carries a json representation
		final Value value = new Value();
		value.setType("json");
		value.setEncoding("text");
		value.setContent(jsonObject.toString());
		final Parm p = new Parm();
		p.setValue(value);
		p.setParmName("name");
		event.addParm(p);

		// Forward event...
		getEventToIndex().forwardEvents(Arrays.asList(event));
		TimeUnit.SECONDS.sleep(INDEX_WAIT_SECONDS);

		// ... and verify that the json was actually persisted as json and not as string
		final String query = buildSearchQuery(eventId);
		final Search search = new Search.Builder(query)
				.addIndex("opennms-events-raw-*")
				.build();
		final SearchResult result = jestClient.execute(search);
		assertEquals(200, result.getResponseCode());
		assertEquals(Long.valueOf(1), result.getTotal());
		final JsonArray jsonArray = result.getJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		assertEquals(jsonObject, jsonArray.get(0).getAsJsonObject().get("_source").getAsJsonObject().get("p_name").getAsJsonObject());
	}

	private static Event createDummyEvent(int eventId) {
		final Event event = new Event();
		event.setUei(EventConstants.ALARM_CLEARED_UEI);
		event.setCreationTime(new Date());
		event.setDistPoller(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		event.setDescr("Dummy Event");
		event.setSeverity(OnmsSeverity.WARNING.getLabel());
		event.setDbid(eventId);
		return event;
	}

	private static Event createDummyEventWithOids(int eventId) {
		final Event event = createDummyEvent(eventId);
		IntStream.range(1, 100).forEach(i -> {
			Parm parm = new Parm();
			parm.setParmName("." + i + ".0.0.0.0.0.0.0.0.0"); // 10 * 100 -> 1000 fields at least
			parm.setValue(new Value("dummy value"));
			event.addParm(parm);
		});
		return event;
	}

	private static String buildSearchQuery(int eventId) {
		return "{\n"
				+"\n       \"query\": {"
				+ "\n         \"match\": {"
				+ "\n         \"id\": \"" + eventId + "\""
				+ "\n          }"
				+ "\n        }"
				+ "\n     }";
	}
}
