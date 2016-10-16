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

package org.opennms.plugins.elasticsearch.rest.archive;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
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
 * @author cgallen
 *
 */
public class OnmsRestEventsClient {
	
	public static final String NODE_LABEL="nodelabel";

	private static final Logger LOG = LoggerFactory.getLogger(OnmsRestEventsClient.class);


	public static final String EVENTS_URI="/opennms/rest/events";
	
	public static final String EVENT_COUNT_URI="/opennms/rest/events/count";

	private String onmsUrl="http://localhost:8980";

	private String onmsUserName="admin";

	private String onmsPassWord="admin";

	public String getOnmsUrl() {
		return onmsUrl;
	}

	public void setOnmsUrl(String onmsUrl) {
		this.onmsUrl = onmsUrl;
	}

	public String getOnmsUserName() {
		return onmsUserName;
	}

	public void setOnmsUserName(String onmsUserName) {
		this.onmsUserName = onmsUserName;
	}

	public String getOnmsPassWord() {
		return onmsPassWord;
	}

	public void setOnmsPassWord(String onmsPassWord) {
		this.onmsPassWord = onmsPassWord;
	}

	private CloseableHttpClient getNewClient(){
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(onmsUserName, onmsPassWord));
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider)
				.build();
		
		return httpclient;

	}
	
	/**
	 * Returns event count or null if failed to retrieve data
	 * @return
	 */
	public Integer getEventCount(){
		
		Integer eventCount=null;

		CloseableHttpClient httpclient=getNewClient();
		try {

			HttpGet getRequest = new HttpGet(onmsUrl+EVENT_COUNT_URI);
			getRequest.addHeader("accept", "text/plain");

			LOG.debug("Executing request " + getRequest.getRequestLine());

			CloseableHttpResponse response = httpclient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			String responseStr=null;

			try {
				LOG.debug("response status:" +response.getStatusLine().toString());
				responseStr= EntityUtils.toString(response.getEntity());
				LOG.debug("response string:" + responseStr);
				eventCount = Integer.parseInt(responseStr);
			} finally {
				response.close();
			}

		} catch (Exception e){
			throw new RuntimeException("exception when getting event count",e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) { }
		}
		
		return eventCount;
	}
	

	public List<Event> getEvents(Integer limit, Integer offset){

		List<Event> retrievedEvents= new ArrayList<Event>();
		
		CloseableHttpClient httpclient=getNewClient();

		String query = "";

		String limitStr= (limit==null) ? null : Integer.toString(limit);
		String offsetStr= (offset==null) ? null : Integer.toString(offset);

		if(limitStr!=null){
			query = "?limit="+limitStr;
			if(offset!=null){
				query=query+"&offset="+offsetStr;
			}
		} else {
			if(offset!=null) {
				query="?offset="+offsetStr;
			}
		}

		try {

			// importing events generated from opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/EventRestService.java

			HttpGet getRequest = new HttpGet(onmsUrl+EVENTS_URI+query);
			getRequest.addHeader("accept", "application/XML");
			

			LOG.debug("Executing request " + getRequest.getRequestLine());

			CloseableHttpResponse response = httpclient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			String responseStr=null;

			try {
				LOG.debug("----------------------------------------");
				LOG.debug(response.getStatusLine().toString());

				responseStr= EntityUtils.toString(response.getEntity());
				LOG.debug("response String: "+responseStr);
				LOG.debug("----------------------------------------");
			} finally {
				response.close();
			}

			StringReader reader = new StringReader(responseStr);
			
			OnmsEventCollection eventCollection = JaxbUtils.unmarshal(OnmsEventCollection.class, reader);
			
			LOG.debug("received xml OnmsEvent's ----------------------------------------");
			LOG.debug("eventCollection offset:"+eventCollection.getOffset()
		 			+ " totalCount:"+eventCollection.getTotalCount()
			         + " size"+eventCollection.size());

			for(int i=0 ; i< eventCollection.size(); i++){
				LOG.debug("event:"+eventCollection.get(i));
			}

			LOG.debug("converting to events ----------------------------------------");

			for(int i=0 ; i< eventCollection.size(); i++){
			    Event event= toEvent(eventCollection.get(i));
			    LOG.debug(event.toString());
			    retrievedEvents.add(event);
		    }
			
		} catch (Exception e){
			throw new RuntimeException("exception when getting event list",e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) { }
		}
		return retrievedEvents;
	}
	
	
	public Event toEvent(OnmsEvent onmsEvent){
		
		

		Event event = new Event();

		/** used in EventToIndex
		//event.getDbid();
		//event.getUei();
		//event.getCreationTime();
		//event.getSource();
		event.getInterfaceAddress();
		event.getService();
		//event.getSeverity();
		//event.getDescr();
		//event.getHost();
		//event.getParmCollection();
		event.getInterface();
		//event.getLogmsg().getContent();
		//event.getLogmsg().getDest();
		event.getNodeid();
		 **/

		if (onmsEvent.getId()!=null) event.setDbid(onmsEvent.getId());
		if (onmsEvent.getEventUei() !=null ) event.setUei(onmsEvent.getEventUei());
		if (onmsEvent.getEventCreateTime() !=null ) event.setCreationTime(onmsEvent.getEventCreateTime());
		//event.setSource(onmsEvent.getEventSource();
		//event.setInterfaceAddress()
		//event.setService()
		if (onmsEvent.getSeverityLabel() !=null ) event.setSeverity(onmsEvent.getSeverityLabel());
		if (onmsEvent.getEventDescr() !=null ) event.setDescr(onmsEvent.getEventDescr());
		if (onmsEvent.getEventHost()!=null ) event.setHost(onmsEvent.getEventHost());
		
		List<Parm> parmColl=new ArrayList<Parm>();
		if (onmsEvent.getEventParameters()!=null) {
			List<OnmsEventParameter> params = onmsEvent.getEventParameters();
			
			for(OnmsEventParameter onmsEventParameter:params){
				
				String parmName = onmsEventParameter.getName();
				String type = onmsEventParameter.getType();
				String value = onmsEventParameter.getValue();
				
				Parm parm = new Parm();
				parm.setParmName(parmName);
				Value parmvalue = new Value();
				parmvalue.setType(type);
				parmvalue.setContent(value);
				parm.setValue(parmvalue);
				
				parmColl.add(parm);
			}
			
		}
		
		// add node label as param
		if ( onmsEvent.getNodeLabel()!=null){
			Parm parm = new Parm();
			parm.setParmName(NODE_LABEL);
			Value parmValue = new Value();
			parm.setValue(parmValue);
			parmValue.setType("string");
			parmValue.setEncoding("text");
			parmValue.setContent(onmsEvent.getNodeLabel());
			parmColl.add(parm);
		}
		event.setParmCollection(parmColl);
			
		//event.getInterface(onmsEvent.getI)

		if (onmsEvent.getEventLogMsg() !=null ) {
			Logmsg logmsg = new Logmsg();
			logmsg.setContent(onmsEvent.getEventLogMsg());
			event.setLogmsg(logmsg );
		}

		if (onmsEvent.getNodeId() !=null ) {
			Integer i = onmsEvent.getNodeId();
			Long l = Long.valueOf(i.longValue());
			event.setNodeid(l);
		}

		return event;
	}
	
	
	
	
	
	
	
	
	
	
}
