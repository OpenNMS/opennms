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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;


public class AlarmEventToIndexIT extends AbstractEventToIndexITCase {
	public final static String DEFAULT_DIST_POLLER_ID = "00000000-0000-0000-0000-000000000000";
	public static final int INDEX_WAIT_SECONDS=10; // time to wait for index to catch up

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
		assertEquals(1L, SearchResultUtils.getTotal(result));
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
		assertEquals(1L, SearchResultUtils.getTotal(result));

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
		assertEquals(1L, SearchResultUtils.getTotal(result));
		final JsonArray jsonArray = result.getJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		assertEquals(jsonObject, jsonArray.get(0).getAsJsonObject().get("_source").getAsJsonObject().get("p_name").getAsJsonObject());
	}

	private static Event createDummyEvent(int eventId) {
		final Event event = new Event();
		event.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
		event.setCreationTime(new Date());
		event.setDistPoller(DEFAULT_DIST_POLLER_ID);
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
