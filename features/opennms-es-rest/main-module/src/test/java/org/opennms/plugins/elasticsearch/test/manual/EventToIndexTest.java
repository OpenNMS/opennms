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

package org.opennms.plugins.elasticsearch.test.manual;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.plugins.elasticsearch.rest.EventToIndex;
import org.opennms.plugins.elasticsearch.test.MockNodeCache;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import static org.junit.Assert.*;

import org.junit.Test;


public class EventToIndexTest {
	private static final Logger LOG = LoggerFactory.getLogger(EventToIndexTest.class);
	
	public static final String ALARM_INDEX_NAME = "opennms-alarms";
	public static final String ALARM_EVENT_INDEX_NAME = "opennms-events-alarmchange";
	public static final String EVENT_INDEX_NAME = "opennms-events-raw";
	public static final String ALARM_INDEX_TYPE = "alarmdata";
	public static final String EVENT_INDEX_TYPE = "eventdata";

	// uei definitions of alarm change events
	public static final String ALARM_DELETED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmDeleted";
	public static final String ALARM_CREATED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/NewAlarmCreated";
	public static final String ALARM_SEVERITY_CHANGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmSeverityChanged";
	public static final String ALARM_ACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmAcknowledged";
	public static final String ALARM_UNACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmUnAcknowledged";
	public static final String ALARM_SUPPRESSED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmSuppressed";
	public static final String ALARM_UNSUPPRESSED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmUnSuppressed";
	public static final String ALARM_TROUBLETICKET_STATE_CHANGE_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/TroubleTicketStateChange";
	public static final String ALARM_CHANGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmChanged";

	public static final String EVENT_SOURCE_NAME = "AlarmChangeNotifier";

	//raw json="{"alarmid":806,"eventuei":"uei.opennms.org/nodes/nodeLostService","nodeid":36,
	//"ipaddr":"142.34.5.19","serviceid":2,"reductionkey":"uei.opennms.org/nodes/nodeLostService::36:142.34.5.19:HTTP",
	//"alarmtype":1,"counter":1,"severity":5,"lasteventid":7003,"firsteventtime":"2016-08-15T18:30:04.252+01:00",
	//"lasteventtime":"2016-08-15T18:30:04.252+01:00","firstautomationtime":null,"lastautomationtime":null,
	//"description":"<p>A HTTP outage was identified on interface\n      142.34.5.19.</p> <p>A new Outage record has been\n      created and service level availability calculations will be\n      impacted until this outage is resolved.</p>",
	//"logmsg":"HTTP outage identified on interface 142.34.5.19 with reason code: Unknown.","operinstruct":null,
	//"tticketid":null,"tticketstate":null,"mouseovertext":null,
	//"suppresseduntil":"2016-08-15T18:30:04.252+01:00","suppresseduser":null,"suppressedtime":"2016-08-15T18:30:04.252+01:00",
	//"alarmackuser":null,"alarmacktime":null,"managedobjectinstance":null,"managedobjecttype":null,"applicationdn":null,
	//"ossprimarykey":null,"x733alarmtype":null,"x733probablecause":0,"qosalarmstate":null,"clearkey":null,"ifindex":null,
	//"eventparms":"eventReason=Unknown(string,text)","stickymemo":null,"systemid":"00000000-0000-0000-0000-000000000000"}";
	
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


	@Test
	public void jestClientAlarmToESTest(){
		LOG.debug("***************** start of test jestClientAlarmToESTest");

		try {
			
			// Get Jest client
			String esusername="";
			String espassword="";
			
			HttpClientConfig clientConfig = new HttpClientConfig.Builder(
					"http://localhost:9200").multiThreaded(true).defaultCredentials(esusername, espassword).build();
			JestClientFactory factory = new JestClientFactory();
			factory.setHttpClientConfig(clientConfig);
			JestClient jestClient = factory.getObject();

			try {
				// create alarm event send to index
				Update update = alarmEventToUpdate();
				String actualindex = update.getIndex();
				LOG.debug("actualindex: "+actualindex);
				
				DocumentResult dresult = jestClient.execute(update);
				LOG.debug("received dresult: "+dresult.getJsonString()+ "\n   response code:" +dresult.getResponseCode() +"\n   error message: "+dresult.getErrorMessage());

				// search for resulting alarm
				String query = "{\n" 
				        +"\n       \"query\": {"
						+ "\n         \"match\": {"
					    + "\n         \"alarmid\": \"807\""
					    + "\n          }"
					    + "\n        }"
					    + "\n     }";

				Search search = new Search.Builder(query)
				// multiple index or types can be added.
				.addIndex(actualindex)
				.build();

				SearchResult sresult = jestClient.execute(search);

				LOG.debug("received search sresult: "+sresult.getJsonString()
						+ "\n   response code:" +sresult.getResponseCode() 
						+ "\n   error message: "+sresult.getErrorMessage());

			} finally {
				// shutdown client
				jestClient.shutdownClient();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		LOG.debug("***************** end of test jestClientAlarmToESTest");
	}
	
	public Update alarmEventToUpdate() {

		EventBuilder eb = new EventBuilder( ALARM_ACKNOWLEDGED_EVENT, EVENT_SOURCE_NAME);

		//copy in all values as json in params
		eb.addParam("oldalarmvalues",TEST_ALARM_JSON_1);
		eb.addParam("newalarmvalues",TEST_ALARM_JSON_1);
		Event event = eb.getEvent();
		event.setDbid(100);
		event.setNodeid((long) 34);

		LOG.debug("alarmEventToIndex created event:"+event.toString());

		String indexName=ALARM_INDEX_NAME;
		String indexType=ALARM_INDEX_TYPE;
		
		EventToIndex eti = new EventToIndex();
		eti.setNodeCache(new MockNodeCache());
		Update index = eti.populateAlarmIndexBodyFromAlarmChangeEvent(event, indexName, indexType);
		LOG.debug("created event index:"+index.toString());

		return index;
	}

	@Test
	public void jestClientEventToESTest(){
		LOG.debug("***************** start of test jestClientEventToESTest");

		try {
			
			// Get Jest client
			String esusername="";
			String espassword="";
			
			HttpClientConfig clientConfig = new HttpClientConfig.Builder(
					"http://localhost:9200").multiThreaded(true).defaultCredentials(esusername, espassword).build();
			JestClientFactory factory = new JestClientFactory();
			factory.setHttpClientConfig(clientConfig);
			JestClient jestClient = factory.getObject();

			try {
				Index index = eventToIndex();

				DocumentResult dresult = jestClient.execute(index);

				LOG.debug("jestClientEventToESTest received dresult: "+dresult.getJsonString()
						+ "\n   response code:" +dresult.getResponseCode() 
						+ "\n   error message: "+dresult.getErrorMessage());

				String query = "{\n" 
				        +"\n       \"query\": {"
						+ "\n         \"match\": {"
					    + "\n         \"id\": \"101\""
					    + "\n          }"
					    + "\n        }"
					    + "\n     }";

				String actualindex = index.getIndex();
				Search search = new Search.Builder(query)
				// multiple index or types can be added.
				.addIndex(actualindex)
				.build();

				SearchResult sresult = jestClient.execute(search);

				LOG.debug("jestClientEventToESTest received search sresult: "+sresult.getJsonString()
						+ "\n   response code:" +sresult.getResponseCode() 
						+ "\n   error message: "+sresult.getErrorMessage());

			} finally {
				// shutdown client
				jestClient.shutdownClient();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		LOG.debug("***************\n end of jestClientEventToESTest");
	}


	
	public Index eventToIndex() {

		EventBuilder eb = new EventBuilder( ALARM_ACKNOWLEDGED_EVENT, EVENT_SOURCE_NAME);

		//raw json="{"alarmid":806,"eventuei":"uei.opennms.org/nodes/nodeLostService","nodeid":36,"ipaddr":"142.34.5.19","serviceid":2,"reductionkey":"uei.opennms.org/nodes/nodeLostService::36:142.34.5.19:HTTP","alarmtype":1,"counter":1,"severity":5,"lasteventid":7003,"firsteventtime":"2016-07-27 22:20:52.282+01","lasteventtime":"2016-07-27 22:20:52.282+01","firstautomationtime":null,"lastautomationtime":null,"description":"<p>A HTTP outage was identified on interface\n      142.34.5.19.</p> <p>A new Outage record has been\n      created and service level availability calculations will be\n      impacted until this outage is resolved.</p>","logmsg":"HTTP outage identified on interface 142.34.5.19 with reason code: Unknown.","operinstruct":null,"tticketid":null,"tticketstate":null,"mouseovertext":null,"suppresseduntil":"2016-07-27 22:20:52.282+01","suppresseduser":null,"suppressedtime":"2016-07-27 22:20:52.282+01","alarmackuser":null,"alarmacktime":null,"managedobjectinstance":null,"managedobjecttype":null,"applicationdn":null,"ossprimarykey":null,"x733alarmtype":null,"x733probablecause":0,"qosalarmstate":null,"clearkey":null,"ifindex":null,"eventparms":"eventReason=Unknown(string,text)","stickymemo":null,"systemid":"00000000-0000-0000-0000-000000000000"}";

		eb.setCreationTime(new Date());
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
		
		//copy in all values as json in params
		eb.addParam("oldalarmvalues",TEST_ALARM_JSON_1);
		eb.addParam("newalarmvalues",TEST_ALARM_JSON_1);
		Event event = eb.getEvent();
		event.setDbid(101);

		LOG.debug("eventToIndex 2 created event:"+event.toString());

		String indexName=EVENT_INDEX_NAME;
		String indexType=EVENT_INDEX_TYPE;
		
		EventToIndex eti = new EventToIndex();
		eti.setNodeCache(new MockNodeCache());
		Index index = eti.populateEventIndexBodyFromEvent(event, indexName, indexType);
		LOG.debug("created alarm index:"+index.toString());


		return index;
	}

}
