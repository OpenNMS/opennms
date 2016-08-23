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
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for importing events from OpenNMS ReST interface
 * (This is used to fill ES with old events from the OpenNMS database)
 * @author cgallen
 *
 */
public class OnmsRestEventsClient {

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
				LOG.debug(responseStr);
				LOG.debug("----------------------------------------");
			} finally {
				response.close();
			}

			JAXBContext jc = JAXBContext.newInstance(XmlOnmsEventCollection.class);

			Unmarshaller unmarshaller = jc.createUnmarshaller();
			StringReader reader = new StringReader(responseStr);
			XmlOnmsEventCollection eventCollection = (XmlOnmsEventCollection) unmarshaller.unmarshal(reader);
			
			eventCollection.getOffset();
			eventCollection.getTotalCount();
			
			LOG.debug("received xml events ----------------------------------------");
			for(XmlOnmsEvent xmlOnmsevent : eventCollection){
				LOG.debug("event:"+xmlOnmsevent);
			}

			//			JAXBContext jc = JAXBContext.newInstance(Events.class);
			//
			//	        Unmarshaller unmarshaller = jc.createUnmarshaller();
			//	        StringReader reader = new StringReader(responseStr);
			//	        Events eventCollection = (Events) unmarshaller.unmarshal(reader);
			//			
			//			for(Event event : eventCollection.getEventCollection()){
			//				System.out.println("event:"+event);
			//			}

			LOG.debug("converting to events ----------------------------------------");
			for(XmlOnmsEvent xmlOnmsevent : eventCollection){
				Event event= xmlOnmsevent.toEvent();
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
}
