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

package org.opennms.plugins.elasticsearch.rest;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Update;
import io.searchbox.indices.CreateIndex;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.events.api.EventParameterUtils;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opennms.plugins.elasticsearch.rest.NodeCache;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventToIndex {

	private static final Logger LOG = LoggerFactory.getLogger(EventToIndex.class);

	public static final String ALARM_INDEX_NAME = "opennms-alarms";
	public static final String ALARM_EVENT_INDEX_NAME = "opennms-events-alarmchange";
	public static final String EVENT_INDEX_NAME = "opennms-events-raw";
	public static final String ALARM_INDEX_TYPE = "alarmdata";
	public static final String EVENT_INDEX_TYPE = "eventdata";

	// stem of all alarm change notification uei's
	public static final String ALARM_NOTIFICATION_UEI_STEM = "uei.opennms.org/plugin/AlarmChangeNotificationEvent";

	// uei definitions of alarm change events
	public static final String ALARM_DELETED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmDeleted";
	public static final String ALARM_CREATED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/NewAlarmCreated";
	public static final String ALARM_SEVERITY_CHANGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmSeverityChanged";
	public static final String ALARM_CLEARED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmCleared";
	public static final String ALARM_ACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmAcknowledged";
	public static final String ALARM_UNACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmUnAcknowledged";
	public static final String ALARM_SUPPRESSED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmSuppressed";
	public static final String ALARM_UNSUPPRESSED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmUnSuppressed";
	public static final String ALARM_TROUBLETICKET_STATE_CHANGE_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/TroubleTicketStateChange";
	public static final String ALARM_CHANGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmChanged";

	public static final String OLD_ALARM_VALUES="oldalarmvalues";
	public static final String NEW_ALARM_VALUES="newalarmvalues";

	public static final String NODE_LABEL="nodelabel";
	public static final String INITIAL_SEVERITY="initialseverity";
	public static final String INITIAL_SEVERITY_TEXT="initialseverity_text";
	public static final String SEVERITY_TEXT="severity_text";
	public static final String SEVERITY="severity";
	public static final String EVENT_PARAMS="eventparms";
	public static final String ALARM_ACK_TIME="alarmacktime";
	public static final String ALARM_ACK_USER="alarmackuser";
	public static final String ALARM_CLEAR_TIME="alarmcleartime";
	public static final String ALARM_DELETED_TIME="alarmdeletedtime";

	// uei definitions of memo change events
	public static final String STICKY_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/StickyMemoUpdate";
	public static final String JOURNAL_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/JournalMemoUpdate";

	// param names in memo change events
	public static final String MEMO_VALUES_PARAM="memovalues";
	public static final String MEMO_ALARMID_PARAM="alarmid";
	public static final String MEMO_BODY_PARAM="body";
	public static final String MEMO_AUTHOR_PARAM="author";
	public static final String MEMO_REDUCTIONKEY_PARAM="reductionkey";

	private boolean logEventDescription=false;

	private boolean logAllEvents=false;

	private boolean archiveRawEvents=true;

	private boolean archiveAlarms=true;

	private boolean archiveAlarmChangeEvents=true;

	private boolean archiveOldAlarmValues=true;

	private boolean archiveNewAlarmValues=true;

	private NodeCache nodeCache=null;

	private JestClient jestClient = null;

	private RestClientFactory restClientFactory = null;

	IndexNameFunction indexNameFunction = new IndexNameFunction();

	public IndexNameFunction getIndexNameFunction() {
		return indexNameFunction;
	}

	public void setIndexNameFunction(IndexNameFunction indexNameFunction) {
		this.indexNameFunction = indexNameFunction;
	}

	public boolean isLogEventDescription() {
		return logEventDescription;
	}

	public void setLogEventDescription(boolean logEventDescription) {
		this.logEventDescription = logEventDescription;
	}

	public boolean isLogAllEvents() {
		return logAllEvents;
	}

	public void setLogAllEvents(boolean logAllEvents) {
		this.logAllEvents = logAllEvents;
	}

	public NodeCache getNodeCache() {
		return nodeCache;
	}

	public void setNodeCache(NodeCache cache) {
		this.nodeCache = cache;
	}

	public RestClientFactory getRestClientFactory() {
		return restClientFactory;
	}

	public void setRestClientFactory(RestClientFactory restClientFactory) {
		this.restClientFactory = restClientFactory;
	}

	public boolean getArchiveAlarms() {
		return archiveAlarms;
	}

	public void setArchiveAlarms(boolean archiveAlarms) {
		this.archiveAlarms = archiveAlarms;
	}

	public boolean getArchiveAlarmChangeEvents() {
		return archiveAlarmChangeEvents;
	}

	public void setArchiveAlarmChangeEvents(boolean archiveAlarmChangeEvents) {
		this.archiveAlarmChangeEvents = archiveAlarmChangeEvents;
	}

	public boolean getArchiveRawEvents() {
		return archiveRawEvents;
	}

	public void setArchiveRawEvents(boolean archiveRawEvents) {
		this.archiveRawEvents = archiveRawEvents;
	}

	public boolean getArchiveOldAlarmValues() {
		return archiveOldAlarmValues;
	}

	public void setArchiveOldAlarmValues(boolean archiveOldAlarmValues) {
		this.archiveOldAlarmValues = archiveOldAlarmValues;
	}

	public boolean getArchiveNewAlarmValues() {
		return archiveNewAlarmValues;
	}

	public void setArchiveNewAlarmValues(boolean archiveNewAlarmValues) {
		this.archiveNewAlarmValues = archiveNewAlarmValues;
	}



	/**
	 * returns a singleton jest client from factory for use by this class
	 * @return
	 */
	private JestClient getJestClient(){
		if (jestClient==null) {
			synchronized(this){
				if (jestClient==null){
					if (restClientFactory==null) throw new RuntimeException("JestClientFactory must be set");
					jestClient= restClientFactory.getJestClient();
				}
			}
		}
		return jestClient;
	}

	public void destroy(){
		if (jestClient!=null)
			try{
				jestClient.shutdownClient();
			}catch (Exception e){}
		jestClient=null;
	}


	/** 
	 * this handles the incoming event and deals with it as an alarm change event or a normal event
	 * @param event
	 */
	public void forwardEvent(Event event){

		try{
			maybeRefreshCache(event);

			// handling uei definitions of alarm change events

			String uei=event.getUei();
			Update alarmUpdate=null;
			Index eventIndex=null;
			DocumentResult alarmIndexresult=null;
			DocumentResult eventIndexresult=null;

			// if alarm change notification then handle change
			// change alarm index and add event to alarm change event index
			if(uei.startsWith(ALARM_NOTIFICATION_UEI_STEM)) {
				if (STICKY_MEMO_EVENT.equals(uei)|| JOURNAL_MEMO_EVENT.equals(uei) ){
					// handle memo change events
					// TODO may want to create a sticky and journal memo field in alarms index and update accordingly
					// currently we just save the event as an event to ES with no other processing
					if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm MEMO Event to ES:"+event.toString());

				} else {
					// handle alarm change events

					if (ALARM_CREATED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Created Event to ES:"+event.toString());

					} else if( ALARM_DELETED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Deleted Event to ES:"+event.toString());

					} else if (ALARM_SEVERITY_CHANGED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Changed Severity Event to ES:"+event.toString());

					} else if (ALARM_CLEARED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Cleared Event to ES:"+event.toString());

					} else if (ALARM_ACKNOWLEDGED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Acknowledged Event to ES:"+event.toString());

					} else if (ALARM_UNACKNOWLEDGED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Unacknowledged Event to ES:"+event.toString());

					} else if (ALARM_SUPPRESSED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Suppressed Event to ES:"+event.toString());

					} else if (ALARM_UNSUPPRESSED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Unsuppressed Event to ES:"+event.toString());

					} else if (ALARM_TROUBLETICKET_STATE_CHANGE_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm TroubleTicked state changed Event to ES:"+event.toString());

					} else if (ALARM_CHANGED_EVENT.equals(uei)){
						if (LOG.isDebugEnabled()) LOG.debug("Sending Alarm Changed Event to ES:"+event.toString());

					}

					if(archiveAlarms){
						// if an alarm change event, use the alarm change fields to update the alarm index
						alarmUpdate = populateAlarmIndexBodyFromAlarmChangeEvent(event, ALARM_INDEX_NAME, ALARM_INDEX_TYPE);
						String alarmindexname=alarmUpdate.getIndex();

						alarmIndexresult = getJestClient().execute(alarmUpdate);

						if(alarmIndexresult.getResponseCode()==404){
							// index doesn't exist for upsert command so create new index and try again

							if(LOG.isDebugEnabled()) {
								LOG.debug("trying to update alarm"
										+ "\n   received search result: "+alarmIndexresult.getJsonString()
										+ "\n   response code:" +alarmIndexresult.getResponseCode() 
										+ "\n   error message: "+alarmIndexresult.getErrorMessage());
								LOG.debug("index name "+alarmindexname + " doesnt exist creating new index");
							}

							// create new index
							CreateIndex createIndex = new CreateIndex.Builder(alarmindexname).build();
							JestResult result = getJestClient().execute(createIndex);
							if(LOG.isDebugEnabled()) {
								LOG.debug("created new alarm index:"+alarmindexname+" type:"+ ALARM_INDEX_TYPE
										+ "\n   received search result: "+result.getJsonString()
										+ "\n   response code:" +result.getResponseCode() 
										+ "\n   error message: "+result.getErrorMessage());
							}
							alarmIndexresult = getJestClient().execute(alarmUpdate);
						}

						if(alarmIndexresult.getResponseCode()!=200){
							LOG.error("Problem sending alarm to es index:" +alarmindexname+ " type:"+ ALARM_INDEX_TYPE
									+ "\n   received search result: "+alarmIndexresult.getJsonString()
									+ "\n   response code:" +alarmIndexresult.getResponseCode() 
									+ "\n   error message: "+alarmIndexresult.getErrorMessage());
						} else if(LOG.isDebugEnabled()) {
							LOG.debug("Alarm sent to es index:" +alarmindexname+ " type:"+ ALARM_INDEX_TYPE
									+ "\n   received search result: "+alarmIndexresult.getJsonString()
									+ "\n   response code:" +alarmIndexresult.getResponseCode() 
									+ "\n   error message: "+alarmIndexresult.getErrorMessage());

						}
					}
				}

				// save all Alarm Change Events including memo change events
				if(archiveAlarmChangeEvents){
					eventIndex = populateEventIndexBodyFromEvent(event, ALARM_EVENT_INDEX_NAME, EVENT_INDEX_TYPE);
					String alarmeventindexname=eventIndex.getIndex();
					eventIndexresult = getJestClient().execute(eventIndex);

					if(eventIndexresult.getResponseCode()==404){
						// index doesn't exist for upsert command so create new index first

						if(LOG.isDebugEnabled()) {
							LOG.debug("trying to update alarm event index"
									+ "\n   received search result: "+eventIndexresult.getJsonString()
									+ "\n   response code:" +eventIndexresult.getResponseCode() 
									+ "\n   error message: "+eventIndexresult.getErrorMessage());
							LOG.debug("index name "+alarmeventindexname + " doesnt exist creating new index");
						}

						// create new index
						CreateIndex createIndex = new CreateIndex.Builder(alarmeventindexname).build();
						JestResult result = getJestClient().execute(createIndex);
						if(LOG.isDebugEnabled()) {
							LOG.debug("created new alarm change event index:"+alarmeventindexname+" type:"+ EVENT_INDEX_TYPE
									+ "\n   received search result: "+result.getJsonString()
									+ "\n   response code:" +result.getResponseCode() 
									+ "\n   error message: "+result.getErrorMessage());
						}
						eventIndexresult = getJestClient().execute(eventIndex);
					}


					if(eventIndexresult.getResponseCode()!=200){
						LOG.error("Problem sending Alarm Event to es index:" +alarmeventindexname+ " type:"+ EVENT_INDEX_TYPE
								+ "\n   received search result: "+alarmIndexresult.getJsonString()
								+ "\n   response code:" +alarmIndexresult.getResponseCode() 
								+ "\n   error message: "+alarmIndexresult.getErrorMessage());
					} else if(LOG.isDebugEnabled()) {
						LOG.debug("Alarm Event sent to es index:"+alarmeventindexname+" type:"+ EVENT_INDEX_TYPE
								+ "\n   received search result: "+eventIndexresult.getJsonString()
								+ "\n   response code:" +eventIndexresult.getResponseCode() 
								+ "\n   error message: "+eventIndexresult.getErrorMessage());

					}
				}

			} else {
				// else handle all other event types

				if(archiveRawEvents){
					// only send events to ES which are persisted to database
					if(logAllEvents || (event.getDbid()!=null && event.getDbid()!=0)) {
						if (LOG.isDebugEnabled()) LOG.debug("Sending Event to ES:"+event.toString());
						// Send the event to the event forwarder
						eventIndex = populateEventIndexBodyFromEvent(event, EVENT_INDEX_NAME, EVENT_INDEX_TYPE);
						String eventindexname=eventIndex.getIndex();
						eventIndexresult = getJestClient().execute(eventIndex);

						if(eventIndexresult.getResponseCode()==404){
							// index doesn't exist for upsert command so create new index first

							if(LOG.isDebugEnabled()) {
								LOG.debug("trying to update event index"
										+ "\n   received search result: "+eventIndexresult.getJsonString()
										+ "\n   response code:" +eventIndexresult.getResponseCode() 
										+ "\n   error message: "+eventIndexresult.getErrorMessage());
								LOG.debug("index name "+eventindexname + " doesnt exist creating new index");
							}

							// create new index
							CreateIndex createIndex = new CreateIndex.Builder(eventindexname).build();
							JestResult result = getJestClient().execute(createIndex);
							if(LOG.isDebugEnabled()) {
								LOG.debug("created new event index:"+eventindexname+" type:"+ EVENT_INDEX_TYPE
										+ "\n   received search result: "+result.getJsonString()
										+ "\n   response code:" +result.getResponseCode() 
										+ "\n   error message: "+result.getErrorMessage());
							}
							eventIndexresult = getJestClient().execute(eventIndex);
						}

						if(LOG.isDebugEnabled()) {
							LOG.debug("Event sent to es index:"+eventindexname+" type:"+ EVENT_INDEX_TYPE
									+ "\n   received search result: "+eventIndexresult.getJsonString()
									+ "\n   response code:" +eventIndexresult.getResponseCode() 
									+ "\n   error message: "+eventIndexresult.getErrorMessage());
						}
					}

				} else {
					if (LOG.isDebugEnabled()) LOG.debug("Not Sending Event to ES: null event.getDbid()="+event.getDbid()+ " Event="+event.toString());
				}
			}



		} catch (Exception ex){
			LOG.error("problem sending event to Elastic Search",ex);
		}

	}

	/**
	 * utility method to populate a Map with the most import event attributes
	 *
	 * @param body the map
	 * @param event the event object
	 */
	public Index populateEventIndexBodyFromEvent( Event event, String rootIndexName, String indexType) {

		Map<String,String> body=new HashMap<String,String>();

		String id=(event.getDbid()==null ? null: Integer.toString(event.getDbid()));

		body.put("id",id);
		body.put("eventuei",event.getUei());

		Calendar cal=Calendar.getInstance();
		if (event.getTime()==null) {
			if(LOG.isDebugEnabled()) LOG.debug("using local time because no event creation time for event.toString: "+ event.toString());
			cal.setTime(new Date());

		} else 	cal.setTime(event.getTime()); // javax.xml.bind.DatatypeConverter.parseDateTime("2010-01-01T12:00:00Z");


		body.put("@timestamp", DatatypeConverter.printDateTime(cal));

		body.put("dow", Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));
		body.put("hour",Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
		body.put("dom", Integer.toString(cal.get(Calendar.DAY_OF_MONTH))); 
		body.put("eventsource", event.getSource());
		body.put("ipaddr", event.getInterfaceAddress()!=null ? event.getInterfaceAddress().toString() : null );
		body.put("servicename", event.getService());
		// params are exported as attributes, see below
		body.put("eventseverity_text", event.getSeverity());
		body.put("eventseverity", Integer.toString(OnmsSeverity.get(event.getSeverity()).getId()));

		if(isLogEventDescription()) {
			body.put("eventdescr", event.getDescr());
		}

		body.put("host",event.getHost());

		//get params from event
		for(Parm parm : event.getParmCollection()) {
			body.put("p_" + parm.getParmName(), parm.getValue().getContent());
		}

		// remove old and new alarm values parms if not needed
		if(! archiveNewAlarmValues){
			body.remove("p_"+OLD_ALARM_VALUES);
		}

		if(! archiveOldAlarmValues){
			body.remove("p_"+NEW_ALARM_VALUES);
		}

		body.put("interface", event.getInterface());
		body.put("logmsg", ( event.getLogmsg()!=null ? event.getLogmsg().getContent() : null ));
		body.put("logmsgdest", ( event.getLogmsg()!=null ? event.getLogmsg().getDest() : null ));

		if(event.getNodeid()!=null){
			body.put("nodeid", Long.toString(event.getNodeid()));

			// if the event contains nodelabel parameter then do not use node cache
			if(body.containsKey("p_"+NODE_LABEL)){
				body.put(NODE_LABEL,body.get("p_"+NODE_LABEL));
			} else {
				// add node details from cache
				if (nodeCache!=null){
					Map nodedetails = nodeCache.getEntry(event.getNodeid());
					for (Object key: nodedetails.keySet()){
						String keyStr = (String) key;
						String value = (String) nodedetails.get(key);
						body.put(keyStr, value);
					}
				}
			}
		}

		String completeIndexName=indexNameFunction.apply(rootIndexName, cal.getTime());

		if (LOG.isDebugEnabled()){
			String str = "populateEventIndexBodyFromEvent - index:"
					+ "/"+completeIndexName
					+ "/"+indexType
					+ "/"+id
					+ "\n   body: ";
			for (String key:body.keySet()){
				str=str+"["+ key+" : "+body.get(key)+"]";
			}
			LOG.debug(str);
		}

		Index index = new Index.Builder(body).index(completeIndexName)
				.type(indexType).id(id).build();

		return index;


	}

	/**
	 * An alarm change event will have a payload corresponding to the json representation of the
	 * Alarms table row for this alarm id. Both "oldalarmvalues" and "newalarmvalues" params may be populated
	 * The alarm index body will be populated with the "newalarmvalues" but if "newalarmvalues" is null then the
	 * "oldalarmvalues" json string will be used
	 * If cannot parse event into alarm then null index is returned
	 * @param body
	 * @param event
	 */
	public Update populateAlarmIndexBodyFromAlarmChangeEvent(Event event, String rootIndexName, String indexType) {

		Update update=null;

		Map<String,String> body = new HashMap<String,String>();

		//get alarm change params from event
		Map<String,String> parmsMap = new HashMap<String,String>();
		for(Parm parm : event.getParmCollection()) {
			parmsMap.put( parm.getParmName(), parm.getValue().getContent());
		}

		String oldValuesStr=parmsMap.get(OLD_ALARM_VALUES);
		String newValuesStr=parmsMap.get(NEW_ALARM_VALUES);

		if (LOG.isDebugEnabled()) LOG.debug("AlarmChangeEvent from eventid "+event.getDbid()
				+ "\n  newValuesStr="+newValuesStr
				+ "\n  oldValuesStr="+oldValuesStr);

		JSONObject alarmValues=null ;
		JSONObject newAlarmValues=null;
		JSONObject oldAlarmValues=null;

		JSONParser parser = new JSONParser();
		if (newValuesStr!=null){
			try{
				Object obj = parser.parse(newValuesStr);
				newAlarmValues = (JSONObject) obj;
			} catch (ParseException e1) {
				LOG.error("cannot parse newValuesStr from eventid "+event.getDbid()
						+ " to json object. newValuesStr="+ newValuesStr, e1);
			}
		}

		if(newAlarmValues!=null && ! newAlarmValues.isEmpty()) {
			alarmValues=newAlarmValues;
		} else {
			if (oldValuesStr==null){
				LOG.error("newValuesStr and oldValuesStr both empty in AlarmChangeEvent from eventid "+event.getDbid()
						+ "\n  newValuesStr="+newValuesStr
						+ "\n  oldValuesStr="+oldValuesStr);
				return null;
			} else {
				try{
					Object obj = parser.parse(oldValuesStr);
					oldAlarmValues = (JSONObject) obj;
				} catch (ParseException e1) {
					LOG.error("cannot parse oldValuesStr from eventid "+event.getDbid()
							+ " to json object. oldValuesStr="+ oldValuesStr, e1);
					return null;
				}
				if (! oldAlarmValues.isEmpty()){
					alarmValues=oldAlarmValues;
				} else {
					LOG.error("oldValuesStr and newValuesStr both empty in AlarmChangeEvent from eventid "+event.getDbid()
							+ "\n  newValuesStr="+newValuesStr
							+ "\n  oldValuesStr="+oldValuesStr);
					return null;
				}
			}
		}


		for (Object x: alarmValues.keySet()){
			String key=(String) x;
			String value = (alarmValues.get(key)==null) ? null : alarmValues.get(key).toString();

			if (EVENT_PARAMS.equals(key) && value!=null){
				//decode event parms into alarm record
				List<Parm> params = EventParameterUtils.decode(value);
				for(Parm parm : params) {
					body.put("p_" + parm.getParmName(), parm.getValue().getContent());
				}
			} else if((SEVERITY.equals(key) && value!=null)){ 
				try{
					int id= Integer.parseInt(value);
					String label = OnmsSeverity.get(id).getLabel();
					body.put(SEVERITY_TEXT,label);
				}
				catch (Exception e){
					LOG.error("cannot parse severity for alarm change event id"+event.getDbid());
				}
			} else{
				body.put(key, value);
			}

		}

		// set alarm cleared / deleted time null if an alarm create event
		if(ALARM_CREATED_EVENT.equals(event.getUei())){
			body.put(ALARM_CLEAR_TIME, null);
			body.put(ALARM_DELETED_TIME, null);
		}

		// set alarm cleared time if an alarm clear event
		if(ALARM_CLEARED_EVENT.equals(event.getUei())){
			Calendar alarmClearCal=Calendar.getInstance();
			alarmClearCal.setTime(event.getTime());
			body.put(ALARM_CLEAR_TIME, DatatypeConverter.printDateTime(alarmClearCal));
		}

		// set alarm deleted time if an alarm clear event
		if(ALARM_DELETED_EVENT.equals(event.getUei())){
			Calendar alarmDeletionCal=Calendar.getInstance();
			alarmDeletionCal.setTime(event.getTime());
			body.put(ALARM_DELETED_TIME, DatatypeConverter.printDateTime(alarmDeletionCal));
		}


		// remove ack if not in parameters i,e alarm not acknowleged
		if(parmsMap.get(ALARM_ACK_TIME)==null || "".equals(parmsMap.get(ALARM_ACK_TIME)) ){
			body.put(ALARM_ACK_TIME, null);
			body.put(ALARM_ACK_USER, null);
		}

		// add "initialseverity"
		if(parmsMap.get(INITIAL_SEVERITY)!=null){
			String severityId = parmsMap.get(INITIAL_SEVERITY);
			body.put(INITIAL_SEVERITY,severityId);

			try{
				int id= Integer.parseInt(severityId);
				String label = OnmsSeverity.get(id).getLabel();
				body.put(INITIAL_SEVERITY_TEXT,label);
			}
			catch (Exception e){
				LOG.error("cannot parse initial severity for alarm change event id"+event.getDbid());
			}
		}

		// if the event contains nodelabel parameter then do not use node cache
		if (parmsMap.get(NODE_LABEL)!=null){
			body.put(NODE_LABEL, parmsMap.get(NODE_LABEL));
		} else {
			// add node details from cache
			if (nodeCache!=null && event.getNodeid()!=null){
				Map nodedetails = nodeCache.getEntry(event.getNodeid());
				for (Object key: nodedetails.keySet()){
					String keyStr = (String) key;
					String value = (String) nodedetails.get(key);
					body.put(keyStr, value);
				}
			}
		}


		if (alarmValues.get("alarmid")==null){
			LOG.error("No alarmid param - cannot create alarm elastic search record from event content:"+ event.toString());
		} else{
			String id = alarmValues.get("alarmid").toString();

			// add the p_alarmid so that we can easily match alarm change events with same alarmid
			body.put("p_alarmid", id);

			String alarmCreationTime=null;
			Date alarmCreationDate=null;
			Calendar alarmCreationCal=null;

			// try to parse firsteventtime but if not able then use current date
			try{
				alarmCreationTime = alarmValues.get("firsteventtime").toString();
				alarmCreationCal = DatatypeConverter.parseDateTime(alarmCreationTime);
			} catch (Exception e){
				LOG.error("using current Date() for @timestamp because problem creating date from alarmchange event "+event.getDbid()
						+ " from firsteventtime="+alarmCreationTime, e);
			}

			if (alarmCreationCal==null){
				alarmCreationCal=Calendar.getInstance();
				alarmCreationCal.setTime(new Date());
			}

			body.put("@timestamp", DatatypeConverter.printDateTime(alarmCreationCal));
			body.put("dow", Integer.toString(alarmCreationCal.get(Calendar.DAY_OF_WEEK)));
			body.put("hour",Integer.toString(alarmCreationCal.get(Calendar.HOUR_OF_DAY)));
			body.put("dom", Integer.toString(alarmCreationCal.get(Calendar.DAY_OF_MONTH))); 

			alarmCreationDate = alarmCreationCal.getTime();
			String completeIndexName=indexNameFunction.apply(rootIndexName, alarmCreationDate);

			if (LOG.isDebugEnabled()){
				String str = "populateAlarmIndexBodyFromAlarmChangeEvent - index:"
						+ "/"+completeIndexName
						+ "/"+indexType
						+ "/"+id
						+ "\n   body: ";
				for (String key:body.keySet()){
					str=str+"["+ key+" : "+body.get(key)+"]";
				}
				LOG.debug(str);
			}

			//index = new Index.Builder(body).index(completeIndexName)
			//		.type(indexType).id(id).build();

			// generates an update for specific values
			// see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
			JSONObject doc= new JSONObject(body);
			JSONObject updateQuery= new JSONObject();
			updateQuery.put("doc", doc);
			updateQuery.put("doc_as_upsert", true);

			if (LOG.isDebugEnabled())LOG.debug("update query sent:"+updateQuery.toJSONString());

			update= new Update.Builder(updateQuery.toJSONString()).index(completeIndexName)
					.type(indexType).id(id).build();

		}

		return update;
	}

	private void maybeRefreshCache(Event event) {
		String uei=event.getUei();
		if(uei!=null && uei.startsWith("uei.opennms.org/nodes/")) {
			if (
					uei.endsWith("Added")
					|| uei.endsWith("Deleted")
					|| uei.endsWith("Updated")
					|| uei.endsWith("Changed")
					) {
				nodeCache.refreshEntry(event.getNodeid());
			}
		}
	}



}
