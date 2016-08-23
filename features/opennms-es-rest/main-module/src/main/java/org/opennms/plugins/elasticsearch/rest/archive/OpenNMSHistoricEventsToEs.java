package org.opennms.plugins.elasticsearch.rest.archive;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.plugins.elasticsearch.rest.archive.OnmsRestEventsClient;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * takes events from OpenNMS historic events through the OpennMS
 * rest interface and sends them to elastic search using the same queue
 * as live incoming events use.
 * @author admin
 *
 */
public class OpenNMSHistoricEventsToEs {
	private static final Logger LOG = LoggerFactory.getLogger(OpenNMSHistoricEventsToEs.class);

	private static final int EVENT_RETREIVAL_LIMIT=10; // retrieve and process max 10 events at a time

	private String onmsUrl="http://localhost:8980";

	private String onmsUserName="admin";

	private String onmsPassWord="admin";

	private Integer limit=10;

	private Integer offset=0;

	private EventForwarder eventForwarder=null;
	
	private boolean useNodeLabel=true;

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

	public EventForwarder getEventForwarder() {
		return eventForwarder;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	public boolean getUseNodeLabel() {
		return useNodeLabel;
	}

	public void setUseNodeLabel(boolean useCache) {
		this.useNodeLabel = useCache;
	}
	

	public OpenNMSHistoricEventsToEs(){
		super();
	}

	/**
	 * sends events to elastic search returns true if successful
	 * @return
	 */
	public String sendEventsToEs(){

		OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient();
		onmsRestEventsClient.setOnmsUrl(onmsUrl);
		onmsRestEventsClient.setOnmsPassWord(onmsPassWord);
		onmsRestEventsClient.setOnmsUserName(onmsUserName);
		Event firstEvent=null;
		Event lastEvent=null;

		boolean endofEvents=false;
		int eventsSent=0;

		int eventOffset=offset;

		while (!endofEvents && eventsSent<=limit){

			List<Event> events = onmsRestEventsClient.getEvents(EVENT_RETREIVAL_LIMIT, eventOffset);

			endofEvents = events.isEmpty();

			for(Event event:events){
				if(firstEvent==null) firstEvent=event;
				lastEvent=event;
				eventsSent++;
				
				// remove node label param if included in event
				if (! useNodeLabel){
					List<Parm> parmCollection = event.getParmCollection();
					ListIterator<Parm> iter = parmCollection.listIterator();
					while(iter.hasNext()){
					    if(XmlOnmsEvent.NODE_LABEL.equals(iter.next().getParmName())){
					        iter.remove();
					    }
					}
					
				}
				
				LOG.debug("sending event to es: eventid="+event.getDbid());
				getEventForwarder().sendNow(event);
			}

			eventOffset=eventOffset+EVENT_RETREIVAL_LIMIT;

		}

		return "Sent "+eventsSent
				+ " events to Elastic Search. First event id="+firstEvent.getDbid()+ " last event id="+lastEvent.getDbid();
	}







}
