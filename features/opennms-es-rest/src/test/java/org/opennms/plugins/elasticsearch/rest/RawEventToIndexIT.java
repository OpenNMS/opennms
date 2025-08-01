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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;


public class RawEventToIndexIT extends AbstractEventToIndexITCase {
	private static final Logger LOG = LoggerFactory.getLogger(RawEventToIndexIT.class);

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

			EventBuilder eb = new EventBuilder( NODE_LOST_SERVICE_EVENT, EVENT_SOURCE_NAME);

			//raw json="{"alarmid":806,"eventuei":"uei.opennms.org/nodes/nodeLostService","nodeid":36,"ipaddr":"142.34.5.19","serviceid":2,"reductionkey":"uei.opennms.org/nodes/nodeLostService::36:142.34.5.19:HTTP","alarmtype":1,"counter":1,"severity":5,"lasteventid":7003,"firsteventtime":"2016-07-27 22:20:52.282+01","lasteventtime":"2016-07-27 22:20:52.282+01","firstautomationtime":null,"lastautomationtime":null,"description":"<p>A HTTP outage was identified on interface\n      142.34.5.19.</p> <p>A new Outage record has been\n      created and service level availability calculations will be\n      impacted until this outage is resolved.</p>","logmsg":"HTTP outage identified on interface 142.34.5.19 with reason code: Unknown.","operinstruct":null,"tticketid":null,"tticketstate":null,"mouseovertext":null,"suppresseduntil":"2016-07-27 22:20:52.282+01","suppresseduser":null,"suppressedtime":"2016-07-27 22:20:52.282+01","alarmackuser":null,"alarmacktime":null,"managedobjectinstance":null,"managedobjecttype":null,"applicationdn":null,"ossprimarykey":null,"x733alarmtype":null,"x733probablecause":0,"qosalarmstate":null,"clearkey":null,"ifindex":null,"eventparms":"eventReason=Unknown(string,text)","stickymemo":null,"systemid":"00000000-0000-0000-0000-000000000000"}";

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

			Event event = eb.getEvent();
			event.setDbid(101L);

			LOG.debug("ecreated node lost service event:"+event.toString());

			// forward event to Elasticsearch
			eventToIndex.forwardEvents(Collections.singletonList(event));

			// waiting 5 seconds for index 
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) { }

			// send query to check that event has been created
			// search for resulting event
			String eventquery = "{\n" 
					+"\n       \"query\": {"
					+ "\n         \"match\": {"
					+ "\n         \"id\": \"101\""
					+ "\n          }"
					+ "\n        }"
					+ "\n     }";

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
			assertEquals(1L, SearchResultUtils.getTotal(eventsresult));

			JSONParser parser = new JSONParser();
			Object obj2 = parser.parse(eventsresult.getJsonString());
			JSONObject eventsresultValues = (JSONObject) obj2;

			JSONObject eventhits = (JSONObject) eventsresultValues.get("hits");
			JSONArray eventhitsvalues = (JSONArray) eventhits.get("hits");
			LOG.debug("   eventhitsvalues: "+eventhitsvalues.toJSONString());

			JSONObject hitObj = (JSONObject) eventhitsvalues.get(0);
			LOG.debug("   hitsObj: "+hitObj.toJSONString());

			JSONObject sourceObj = (JSONObject) hitObj.get("_source");
			LOG.debug("   sourceObj: "+sourceObj.toJSONString());

			String eventUeiStr =  sourceObj.get("eventuei").toString();

			LOG.debug("search result event eventueistr="+eventUeiStr);
			assertEquals(NODE_LOST_SERVICE_EVENT, eventUeiStr);

		LOG.debug("***************** end of test jestClientRawEventToESTest");
	}

}
