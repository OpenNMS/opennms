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
package org.opennms.plugins.elasticsearch.rest.archive;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for importing events from OpenNMS ReST interface
 * (This is used to fill ES with old events from the OpenNMS database)
 *
 * @author cgallen
 *
 */
public class OnmsRestEventsClient {
	
	public static final String NODE_LABEL="nodelabel";
	public static final String EVENTS_URI="/opennms/rest/events";
	public static final String EVENT_COUNT_URI="/opennms/rest/events/count";

	private static final Logger LOG = LoggerFactory.getLogger(OnmsRestEventsClient.class);

	private final String baseUrl;

	private final String username;

	private final String password;

	public OnmsRestEventsClient(String baseUrl, String username, String password) {
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;
	}

	public Integer getEventCount(){
		try (CloseableHttpClient client = createHttpClient()) {
			final HttpGet request = new HttpGet(baseUrl + EVENT_COUNT_URI);
			request.addHeader("accept", "text/plain");

			LOG.debug("Executing request " + request.getRequestLine());
			try (CloseableHttpResponse response = client.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
				}
				String responseStr = EntityUtils.toString(response.getEntity());
				LOG.debug("Response status: {}, entity: {}", response.getStatusLine(), responseStr);
				return Integer.parseInt(responseStr);
			}
		} catch (Exception e){
			throw new RuntimeException("An Exception occurred while getting event count: " + e.getMessage(), e);
		}
	}
	

	public List<Event> getEvents(Integer limit, Integer offset){
		final List<Event> retrievedEvents= new ArrayList<>();
		try (CloseableHttpClient httpclient= createHttpClient()) {

			// Build query
			final List<String> queryParts = new ArrayList<>();
			if (limit != null) {
				queryParts.add("limit=" + limit);
			}
			if (offset != null) {
				queryParts.add("offset=" + offset);
			}
			final String query = queryParts.isEmpty() ? "" : "?" + queryParts.stream().collect(Collectors.joining("&"));

			// importing events generated from opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/EventRestService.java
			final HttpGet request = new HttpGet(baseUrl + EVENTS_URI + query);
			request.addHeader("accept", "application/xml");

			LOG.debug("Executing request " + request.getRequestLine());
			try (CloseableHttpResponse response = httpclient.execute(request)) {
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
				}
				final String responseStr = EntityUtils.toString(response.getEntity());
				LOG.debug("Response status: {}, entity: {}", response.getStatusLine(), responseStr);

				final StringReader reader = new StringReader(responseStr);
				final OnmsEventCollection eventCollection = JaxbUtils.unmarshal(OnmsEventCollection.class, reader);
				LOG.debug("Received event Collection:´with offset: {}, totalCount: {}, size: {}", eventCollection.getOffset(), eventCollection.getTotalCount(), eventCollection.size());

				// Convert
				for (int i = 0; i < eventCollection.size(); i++) {
					Event event = toEvent(eventCollection.get(i));
					retrievedEvents.add(event);
				}
			}
			return retrievedEvents;
		} catch (Exception e) {
			throw new RuntimeException("exception when getting event list: " + e.getMessage(), e);
		}
	}
	
	
	private static Event toEvent(OnmsEvent onmsEvent) {
		final Event event = new Event();
		if (onmsEvent.getId() != null) event.setDbid(onmsEvent.getId());
		if (onmsEvent.getEventUei() != null ) event.setUei(onmsEvent.getEventUei());
		if (onmsEvent.getEventCreateTime() != null ) event.setCreationTime(onmsEvent.getEventCreateTime());
		if (onmsEvent.getSeverityLabel() !=null ) event.setSeverity(onmsEvent.getSeverityLabel());
		if (onmsEvent.getEventDescr() !=null ) event.setDescr(onmsEvent.getEventDescr());
		if (onmsEvent.getEventHost() !=null ) event.setHost(onmsEvent.getEventHost());
		
		final List<Parm> parmColl = new ArrayList<>();
		if (onmsEvent.getEventParameters()!=null) {
			final List<OnmsEventParameter> params = onmsEvent.getEventParameters();
			for(OnmsEventParameter onmsEventParameter:params){
				final Value parmvalue = new Value();
				parmvalue.setType(onmsEventParameter.getType());
				parmvalue.setContent(onmsEventParameter.getValue());

				final Parm parm = new Parm();
				parm.setParmName(onmsEventParameter.getName());
				parm.setValue(parmvalue);
				parmColl.add(parm);
			}
		}
		
		// add node label as param
		if (onmsEvent.getNodeLabel() != null){
			final Value parmValue = new Value();
			parmValue.setType("string");
			parmValue.setEncoding("text");
			parmValue.setContent(onmsEvent.getNodeLabel());

			final Parm parm = new Parm();
			parm.setParmName(NODE_LABEL);
			parm.setValue(parmValue);
			parmColl.add(parm);
		}
		event.setParmCollection(parmColl);

		if (onmsEvent.getEventLogMsg() != null ) {
			final Logmsg logmsg = new Logmsg();
			logmsg.setContent(onmsEvent.getEventLogMsg());
			event.setLogmsg(logmsg );
		}

		if (onmsEvent.getNodeId() != null ) {
			event.setNodeid(onmsEvent.getNodeId().longValue());
		}
		return event;
	}

	private CloseableHttpClient createHttpClient() {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		return HttpClients.custom()
				.setDefaultCredentialsProvider(credentialsProvider)
				.build();

	}
}
