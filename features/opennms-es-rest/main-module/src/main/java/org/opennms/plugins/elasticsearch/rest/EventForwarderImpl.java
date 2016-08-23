package org.opennms.plugins.elasticsearch.rest;


import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventForwarderImpl implements EventForwarder {


	private static final Logger LOG = LoggerFactory.getLogger(EventForwarderImpl.class);

	private EventToIndex eventToIndex=null;
	
	public EventToIndex getEventToIndex() {
		return eventToIndex;
	}

	public void setEventToIndex(EventToIndex eventToIndex) {
		this.eventToIndex = eventToIndex;
	}
	
	@Override
	public void sendNow(Event event) {		
		LOG.debug("Event to send received: " + event.toString());
		if (eventToIndex!=null) eventToIndex.forwardEvent(event);
	}

	@Override
	public void sendNow(Log arg0) {
		// TODO Auto-generated method stub

	}

}
