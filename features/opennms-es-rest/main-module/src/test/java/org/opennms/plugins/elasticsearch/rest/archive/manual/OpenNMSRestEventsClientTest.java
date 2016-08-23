package org.opennms.plugins.elasticsearch.rest.archive.manual;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.plugins.elasticsearch.rest.archive.OnmsRestEventsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenNMSRestEventsClientTest {
	private static final Logger LOG = LoggerFactory.getLogger(OpenNMSRestEventsClientTest.class);

	private String opennmsUrl="http://localhost:8980";

	private String userName="admin";

	private String passWord="admin";

	/**
	 * test if events can be received from OpenNMS ReST UI
	 */
	@Test
	public void getEventsTest() {
		LOG.debug("starting getEventsTest");
		OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient();

		onmsRestEventsClient.setOnmsUrl(opennmsUrl);
		onmsRestEventsClient.setOnmsPassWord(passWord);
		onmsRestEventsClient.setOnmsUserName(userName);


		List<Event> receivedEvents = onmsRestEventsClient.getEvents(1, 10);

		if (receivedEvents.isEmpty()){
			LOG.debug("\nNO RECEIVED EVENTS ----------------------------------------");
		} else {

			LOG.debug("\nRECEIVED EVENTS ("+receivedEvents
					+ ") ----------------------------------------");
			for(Event event : receivedEvents){
				LOG.debug(event.toString());
			}
			LOG.debug("\nEND OF RECEIVED EVENTS ----------------------------------------");
		}
		LOG.debug("finished getEventsTest");

	}

	@Test
	public void getEventCountTest() {
		LOG.debug("starting getEventCountTest");
		OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient();

		onmsRestEventsClient.setOnmsUrl(opennmsUrl);
		onmsRestEventsClient.setOnmsPassWord(passWord);
		onmsRestEventsClient.setOnmsUserName(userName);

		Integer eventCount = onmsRestEventsClient.getEventCount();

		LOG.debug("event count="+eventCount);

		LOG.debug("finished getEventCountTest");

	}

}
