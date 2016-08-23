package org.opennms.plugins.elasticsearch.rest.archive.manual;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.plugins.elasticsearch.rest.EventForwarderImpl;
import org.opennms.plugins.elasticsearch.rest.archive.OpenNMSHistoricEventsToEs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenNMSHistoricEventsToEsTest {
	private static final Logger LOG = LoggerFactory.getLogger(OpenNMSHistoricEventsToEsTest.class);
	
	private String onmsUrl="http://localhost:8980";

	private String onmsUserName="admin";

	private String onmsPassWord="admin";
	
	private Integer limit = 100;
	
	private Integer offset = 0;
	
	@Test
	public void test() {
		LOG.debug("start of test OpenNMSHistoricEventsToEsTest");
		
		OpenNMSHistoricEventsToEs eventsToES= new OpenNMSHistoricEventsToEs();
		
		eventsToES.setOnmsUserName(onmsUserName);
		
		eventsToES.setOnmsPassWord(onmsPassWord);
		
		eventsToES.setOnmsUrl(onmsUrl);
		
		eventsToES.setLimit(limit);
		
		eventsToES.setOffset(offset);
		
		EventForwarderImpl eventForwarder = new EventForwarderImpl();
		
		eventsToES.setEventForwarder(eventForwarder);
		
		String msg = eventsToES.sendEventsToEs();
		
		LOG.debug("message from forwarder: "+msg);

		LOG.debug("end of test OpenNMSHistoricEventsToEsTest");
	}

}
