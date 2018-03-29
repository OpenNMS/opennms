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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.action.BulkableAction;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.BulkResult.BulkResultItem;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Update;
import io.searchbox.indices.CreateIndex;

public class EventToIndex implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(EventToIndex.class);

	public static enum Indices {
		ALARMS,
		ALARM_EVENTS,
		EVENTS
	}

	@SuppressWarnings("serial")
	public static final EnumMap<Indices,String> INDEX_NAMES = new EnumMap<Indices,String>(Indices.class) {{
		this.put(Indices.ALARMS, "opennms-alarms");
		this.put(Indices.ALARM_EVENTS, "opennms-events-alarmchange");
		this.put(Indices.EVENTS, "opennms-events-raw");
	}};

	@SuppressWarnings("serial")
	public static final EnumMap<Indices,String> INDEX_TYPES = new EnumMap<Indices,String>(Indices.class) {{
		this.put(Indices.ALARMS, "alarmdata");
		this.put(Indices.ALARM_EVENTS, "eventdata");
		this.put(Indices.EVENTS, "eventdata");
	}};


	// stem of all alarm change notification uei's
	// TODO: Move these into EventConstants
	public static final String ALARM_NOTIFICATION_UEI_STEM = "uei.opennms.org/plugin/AlarmChangeNotificationEvent";

	// uei definitions of alarm change events
	// TODO: Move these into EventConstants
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

	// uei definitions of memo change events
	// TODO: Move these into EventConstants
	public static final String STICKY_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/StickyMemoUpdate";
	public static final String JOURNAL_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/JournalMemoUpdate";

	// param names in memo change events
	// TODO: Move these into EventConstants
	public static final String MEMO_VALUES_PARAM="memovalues";
	public static final String MEMO_ALARMID_PARAM="alarmid";
	public static final String MEMO_BODY_PARAM="body";
	public static final String MEMO_AUTHOR_PARAM="author";
	public static final String MEMO_REDUCTIONKEY_PARAM="reductionkey";

	public static final String OLD_ALARM_VALUES_PARAM="oldalarmvalues";
	public static final String NEW_ALARM_VALUES_PARAM="newalarmvalues";

	public static final String NODE_LABEL_PARAM="nodelabel";
	public static final String INITIAL_SEVERITY_PARAM="initialseverity";
	public static final String INITIAL_SEVERITY_PARAM_TEXT="initialseverity_text";
	public static final String SEVERITY_TEXT="severity_text";
	public static final String SEVERITY="severity";
	public static final String ALARM_SEVERITY_PARAM="alarmseverity";
	public static final String FIRST_EVENT_TIME="firsteventtime";
	public static final String EVENT_PARAMS="eventparms";
	public static final String ALARM_ACK_TIME_PARAM="alarmacktime";
	public static final String ALARM_ACK_USER_PARAM="alarmackuser";
	public static final String ALARM_ACK_DURATION="alarmackduration"; // duration from alarm raise to acknowledge
	public static final String ALARM_CLEAR_TIME="alarmcleartime";
	public static final String ALARM_CLEAR_DURATION="alarmclearduration"; //duration from alarm raise to clear
	public static final String ALARM_DELETED_TIME="alarmdeletedtime";


	public static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() * 2;

	private boolean logEventDescription=false;

	private boolean logAllEvents=false;

	private boolean archiveRawEvents=true;

	private boolean archiveAlarms=true;

	private boolean archiveAlarmChangeEvents=true;

	private boolean archiveOldAlarmValues=true;

	private boolean archiveNewAlarmValues=true;

	private boolean groupOidParameters = false;

	private NodeCache nodeCache=null;

	private JestClient jestClient = null;

	private RestClientFactory restClientFactory = null;

	private int threads = DEFAULT_NUMBER_OF_THREADS;

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
		threads,
		threads,
		0L, TimeUnit.MILLISECONDS,
		new SynchronousQueue<>(true),
		new ThreadFactory() {
			final AtomicInteger index = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, EventToIndex.class.getSimpleName() + "-Thread-" + String.valueOf(index.incrementAndGet()));
			}
		},
		// Throttle incoming tasks by running them on the caller thread
		new ThreadPoolExecutor.CallerRunsPolicy()
	);

	private IndexNameFunction indexNameFunction = new IndexNameFunction();

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

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		if (threads > 0) {
			this.threads = threads;
			// Resize the executor pool
			executor.setCorePoolSize(threads);
			executor.setMaximumPoolSize(threads);
		} else {
			setThreads(DEFAULT_NUMBER_OF_THREADS);
		}
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

	public void setGroupOidParameters(boolean groupOidParameters) {
		this.groupOidParameters = groupOidParameters;
	}


	/**
	 * returns a singleton jest client from factory for use by this class
	 * @return
	 */
	private JestClient getJestClient(){
		if (jestClient == null) {
			synchronized(this){
				if (jestClient == null){
					if (restClientFactory == null) throw new RuntimeException("JestClientFactory must be set");
					jestClient = restClientFactory.getJestClient();
				}
			}
		}
		return jestClient;
	}

	private void closeJestClient() {
		if (jestClient != null) {
			synchronized(this){
				try{
					jestClient.shutdownClient();
				} catch (Throwable e) {
					LOG.warn("Unexpected exception while shutting down REST client", e);
				}
				jestClient = null;
			}
		}
	}

	@Override
	public void close(){
		closeJestClient();

		// Shutdown the thread pool
		executor.shutdown();
	}

	/**
	 * @param events
	 */
	public void forwardEvents(final List<Event> events) {
		CompletableFuture.completedFuture(events)
			// Convert the events to ES actions
			.thenApplyAsync(this::convertEventsToEsActions, executor)
			// Log any uncaught exceptions
			.exceptionally(e -> {
				LOG.error("Unexpected exception during task execution: " + e.getMessage(), e);
				return null;
			})
			// Send the events to Elasticsearch
			.thenAcceptAsync(this::sendEvents, executor)
			// Log any uncaught exceptions
			.exceptionally(e -> {
				LOG.error("Unexpected exception during task completion: " + e.getMessage(), e);
				return null;
			});
	}

	private void sendEvents(final List<BulkableAction<DocumentResult>> actions) {
		if (actions != null && actions.size() > 0) {
			// Split the actions up by index
			for (Map.Entry<String,List<BulkableAction<DocumentResult>>> entry : actions.stream().collect(Collectors.groupingBy(BulkableAction::getIndex)).entrySet()) {

				try {
					final List<BulkableAction<DocumentResult>> actionList = entry.getValue();

					if (entry.getValue().size() == 1) {
						// Not bulk
						final BulkableAction<DocumentResult> action = actionList.get(0);
						executeSingleAction(getJestClient(), action);
					} else {
						// The type will be identical for all events in the same index
						final String type = actionList.get(0).getType();

						final Bulk.Builder builder = new Bulk.Builder()
								.defaultIndex(entry.getKey())
								.defaultType(type);

						// Add all actions to the bulk operation
						builder.addAction(actionList);

						final Bulk bulk = builder.build();

						BulkResult result = getJestClient().execute(bulk);

						// If the bulk command fails completely...
						if (result == null || !result.isSucceeded()) {
							if (result == null) {
								logEsError("Bulk API action", entry.getKey(), type, null, -1, null);
							} else {
								logEsError("Bulk API action", entry.getKey(), type, result.getJsonString(), result.getResponseCode(), result.getErrorMessage());
							}

							// Try and issue the bulk actions individually as a fallback
							for (BulkableAction<DocumentResult> action : entry.getValue()) {
								executeSingleAction(getJestClient(), action);
							}

							continue;
						}

						// Check the return codes of the results
						List<BulkResultItem> items = result.getItems();
						boolean all404s = true;
						for (BulkResultItem item : items) {
							if (item.status != 404) {
								all404s = false;
								break;
							}
						}

						if(all404s){
							// index doesn't exist for upsert command so create new index and try again

							if(LOG.isDebugEnabled()) {
								LOG.debug("index name "+entry.getKey() + " doesn't exist, creating new index");
							}

							createIndex(getJestClient(), entry.getKey(), type);

							result = getJestClient().execute(bulk);
						}

						// If the bulk command fails completely...
						if (result == null || !result.isSucceeded()) {
							if (result == null) {
								logEsError("Bulk API action", entry.getKey(), type, null, -1, null);
							} else {
								logEsError("Bulk API action", entry.getKey(), type, result.getJsonString(), result.getResponseCode(), result.getErrorMessage());
							}

							// Try and issue the bulk actions individually as a fallback
							for (BulkableAction<DocumentResult> action : entry.getValue()) {
								executeSingleAction(getJestClient(), action);
							}

							continue;
						}

						// Log any unsuccessful completions as errors
						for (BulkResultItem item : result.getItems()) {
							if(item.status >= 200 && item.status < 300){
								if (LOG.isDebugEnabled()) {
									// If debug is enabled, log all completions
									logEsDebug(item.operation, entry.getKey(), item.type, "none", item.status, item.error);
								}
							} else {
								logEsError(item.operation, entry.getKey(), item.type, "none", item.status, item.error);
							}
						}
					}
				} catch (Throwable ex){
					LOG.error("Unexpected problem sending event to Elasticsearch", ex);
					// Shutdown the ES client, it will be recreated as needed
					closeJestClient();
				}
			}
		}
	}

	private static final void logEsError(String operation, String index, String type, String result, int responseCode, String errorMessage) {
		LOG.error("Error while performing {} on Elasticsearch index: {}, type: {}\n" +
				"   received result: {}\n" + 
				"   response code: {}\n" + 
				"   error message: {}",
				operation, index, type, result, responseCode, errorMessage
		);
	}

	private static final void logEsDebug(String operation, String index, String type, String result, int responseCode, String errorMessage) {
		LOG.debug("Performed {} on Elasticsearch index: {}, type: {}\n" +
				"   received result: {}\n" + 
				"   response code: {}\n" + 
				"   error message: {}",
				operation, index, type, result, responseCode, errorMessage
		);
	}

	/**
	 * This executes single Elasticsearch actions, creating indices as needed.
	 * 
	 * @param client
	 * @param action
	 * @throws IOException
	 */
	private static void executeSingleAction(JestClient client, BulkableAction<DocumentResult> action) throws IOException {

		DocumentResult result = client.execute(action);

		if(result == null || result.getResponseCode() == 404){
			// index doesn't exist for upsert command so create new index and try again

			if(LOG.isDebugEnabled()) {
				if (result == null) {
					logEsDebug(action.getRestMethodName(), action.getIndex(), action.getType(), null, -1, null);
				} else {
					logEsDebug(action.getRestMethodName(), action.getIndex(), action.getType(), result.getJsonString(), result.getResponseCode(), result.getErrorMessage());
				}
				LOG.debug("index name "+action.getIndex() + " doesn't exist, creating new index");
			}

			createIndex(client, action.getIndex(), action.getType());

			result = client.execute(action);
		}

		if (result == null) {
			logEsError(action.getRestMethodName(), action.getIndex(), action.getType(), null, -1, null);
		} else if (!result.isSucceeded()){
			logEsError(action.getRestMethodName(), action.getIndex(), action.getType(), result.getJsonString(), result.getResponseCode(), result.getErrorMessage());
		} else if(LOG.isDebugEnabled()) {
			logEsDebug(action.getRestMethodName(), action.getIndex(), action.getType(), result.getJsonString(), result.getResponseCode(), result.getErrorMessage());
		}
	}

	/** 
	 * <p>This method converts events into a sequence of Elasticsearch index/update commands.
	 * Three types of actions are possible:</p>
	 * <ul>
	 * <li>Updating an alarm document based on an {@link #ALARM_NOTIFICATION_UEI_STEM} event</li>
	 * <li>Indexing the {@link #ALARM_NOTIFICATION_UEI_STEM} events</li>
	 * <li>Indexing all other events</li>
	 * </ul>
	 * 
	 * @param event
	 */
	private List<BulkableAction<DocumentResult>> convertEventsToEsActions(List<Event> events) {

		final List<BulkableAction<DocumentResult>> retval = new ArrayList<>();

		for (Event event : events) {

			maybeRefreshCache(event);

			final String uei = event.getUei();

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

					if (LOG.isDebugEnabled()) {
						if (ALARM_CREATED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Created Event to ES:"+event.toString());
						} else if( ALARM_DELETED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Deleted Event to ES:"+event.toString());
						} else if (ALARM_SEVERITY_CHANGED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Changed Severity Event to ES:"+event.toString());
						} else if (ALARM_CLEARED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Cleared Event to ES:"+event.toString());
						} else if (ALARM_ACKNOWLEDGED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Acknowledged Event to ES:"+event.toString());
						} else if (ALARM_UNACKNOWLEDGED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Unacknowledged Event to ES:"+event.toString());
						} else if (ALARM_SUPPRESSED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Suppressed Event to ES:"+event.toString());
						} else if (ALARM_UNSUPPRESSED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Unsuppressed Event to ES:"+event.toString());
						} else if (ALARM_TROUBLETICKET_STATE_CHANGE_EVENT.equals(uei)){
							LOG.debug("Sending Alarm TroubleTicked state changed Event to ES:"+event.toString());
						} else if (ALARM_CHANGED_EVENT.equals(uei)){
							LOG.debug("Sending Alarm Changed Event to ES:"+event.toString());
						}
					}

					if(archiveAlarms){
						// if an alarm change event, use the alarm change fields to update the alarm index
						Update alarmUpdate = populateAlarmIndexBodyFromAlarmChangeEvent(event, INDEX_NAMES.get(Indices.ALARMS), INDEX_TYPES.get(Indices.ALARMS));
						retval.add(alarmUpdate);
					}
				}

				// save all Alarm Change Events including memo change events
				if(archiveAlarmChangeEvents){
					Index eventIndex = populateEventIndexBodyFromEvent(event, INDEX_NAMES.get(Indices.ALARM_EVENTS), INDEX_TYPES.get(Indices.ALARM_EVENTS));
					retval.add(eventIndex);
				}

			} else {
				// Handle all other event types
				if(archiveRawEvents){
					// only send events to ES if they are persisted to database or logAllEvents is set to true
					if(logAllEvents || (event.getDbid()!=null && event.getDbid()!=0)) {
						Index eventIndex = populateEventIndexBodyFromEvent(event, INDEX_NAMES.get(Indices.EVENTS), INDEX_TYPES.get(Indices.EVENTS));
						retval.add(eventIndex);
					} else {
						if (LOG.isDebugEnabled()) LOG.debug("Not Sending Event to ES: null event.getDbid()="+event.getDbid()+ " Event="+event.toString());
					}
				}
			}
		}

		return retval;
	}

	/**
	 * utility method to populate a Map with the most import event attributes
	 *
	 * @param body the map
	 * @param event the event object
	 */
	public Index populateEventIndexBodyFromEvent( Event event, String rootIndexName, String indexType) {

		final JSONObject body = new JSONObject();

		Integer id=(event.getDbid()==null ? null: event.getDbid());

		body.put("id",Integer.toString(id));
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

		// Parse event parameters
		final JSONParser jsonParser = new JSONParser();
		handleParameters(event, body);

		// remove old and new alarm values parms if not needed
		if(! archiveNewAlarmValues){
			body.remove("p_"+OLD_ALARM_VALUES_PARAM);
		}

		if(! archiveOldAlarmValues){
			body.remove("p_"+NEW_ALARM_VALUES_PARAM);
		}

		body.put("interface", event.getInterface());
		body.put("logmsg", ( event.getLogmsg()!=null ? event.getLogmsg().getContent() : null ));
		body.put("logmsgdest", ( event.getLogmsg()!=null ? event.getLogmsg().getDest() : null ));

		if(event.getNodeid()!=null){
			body.put("nodeid", Long.toString(event.getNodeid()));

			// if the event contains nodelabel parameter then do not use node cache
			if(body.containsKey("p_"+NODE_LABEL_PARAM)){
				body.put(NODE_LABEL_PARAM,body.get("p_"+NODE_LABEL_PARAM));
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
					+ "\n   body: \n" + body.toJSONString();
			LOG.debug(str);
		}

		Index.Builder builder = new Index.Builder(body)
				.index(completeIndexName)
				.type(indexType);

		// NMS-9015: If the event is a database event, set the ID of the
		// document to the event's database ID. Otherwise, allow ES to
		// generate a unique ID value.
		if (id > 0) {
			builder = builder.id(Integer.toString(id));
		}

		Index index = builder.build();

		return index;
	}

	private void handleParameters(Event event, JSONObject body) {
		// Decide if oids should be grouped in a single JsonArray or flattened
		if (groupOidParameters) {
			final List<Parm> oidParameters = event.getParmCollection().stream().filter(p -> isOID(p.getParmName())).collect(Collectors.toList());
			final List<Parm> normalParameters = event.getParmCollection();
			normalParameters.removeAll(oidParameters);

			// Handle non oid paramaters as always
			handleParameters(event, normalParameters, body);

			// Special treatment for oid parameters
			if (!oidParameters.isEmpty()) {
				final JSONArray jsonArray = new JSONArray();
				for (Parm eachOid : oidParameters) {
					final JSONObject eachOidObject = new JSONObject();
					eachOidObject.put("oid", eachOid.getParmName());
					eachOidObject.put("value", eachOid.getValue().getContent());
					jsonArray.add(eachOidObject);
				}
				body.put("p_oids", jsonArray);
			}
		} else { // flattened
			handleParameters(event, event.getParmCollection(), body);
		}
	}

	private void handleParameters(Event event, List<Parm> parameters, JSONObject body) {
		final JSONParser jsonParser = new JSONParser();
		for(Parm parm : parameters) {
			final String parmName = "p_" + parm.getParmName().replaceAll("\\.", "_");

			// Some parameter values are of type json and should be decoded properly.
			// See HZN-1272
			if ("json".equalsIgnoreCase(parm.getValue().getType())) {
				try {
					JSONObject tmpJson = (JSONObject) jsonParser.parse(parm.getValue().getContent());
					body.put(parmName, tmpJson);
				} catch (ParseException ex) {
					LOG.error("Cannot parse parameter content '{}' of parameter '{}' from eventid {} to json: {}",
									parm.getValue().getContent(), parm.getParmName(), event.getDbid(), ex.getMessage(), ex);
					// To not lose the data, just use as is
					body.put(parmName, parm.getValue().getContent());
				}
			} else {
				body.put(parmName, parm.getValue().getContent());
			}
		}
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

		String oldValuesStr=parmsMap.get(OLD_ALARM_VALUES_PARAM);
		String newValuesStr=parmsMap.get(NEW_ALARM_VALUES_PARAM);

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
				try {
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
			} else if((ALARM_SEVERITY_PARAM.equals(key) && value!=null)){ 
				try{
					int id= Integer.parseInt(value);
					String label = OnmsSeverity.get(id).getLabel();
					// note alarm index uses severity even though alarm severity param is p_alarmseverity
					body.put(SEVERITY,value);
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
			// duration time from alarm raise to clear
			try{
				Date alarmclearDate = event.getTime();
				String alarmCreationTime = alarmValues.get(FIRST_EVENT_TIME).toString();
				Calendar alarmCreationCal = DatatypeConverter.parseDateTime(alarmCreationTime);
				Date alarmCreationDate=alarmCreationCal.getTime();
				//duration in milliseconds
				Long duration = alarmclearDate.getTime() - alarmCreationDate.getTime();
				body.put(ALARM_CLEAR_DURATION, duration.toString());
			} catch (Exception e){
				LOG.error("problem calculating alarm clear duration for event "+event.getDbid(), e);
			}
		}

		// set alarm deleted time if an alarm delete event
		if(ALARM_DELETED_EVENT.equals(event.getUei())){
			Calendar alarmDeletionCal=Calendar.getInstance();
			alarmDeletionCal.setTime(event.getTime());
			body.put(ALARM_DELETED_TIME, DatatypeConverter.printDateTime(alarmDeletionCal));			
		}
		
		//  calculate duration from alarm raise to acknowledge
		if(ALARM_ACKNOWLEDGED_EVENT.equals(event.getUei())){
			try{
				Date alarmAckDate = event.getTime();
				String alarmCreationTime = alarmValues.get(FIRST_EVENT_TIME).toString();
				Calendar alarmCreationCal = DatatypeConverter.parseDateTime(alarmCreationTime);
				Date alarmCreationDate=alarmCreationCal.getTime();
				//duration in milliseconds
				Long duration = alarmAckDate.getTime() - alarmCreationDate.getTime();
				body.put(ALARM_ACK_DURATION, duration.toString());
			} catch (Exception e){
				LOG.error("problem calculating alarm acknowledge duration for event "+event.getDbid(), e);
			}
		}

		// remove ack if not in parameters i,e alarm not acknowleged
		if(parmsMap.get(ALARM_ACK_TIME_PARAM)==null || "".equals(parmsMap.get(ALARM_ACK_TIME_PARAM)) ){
			body.put(ALARM_ACK_TIME_PARAM, null);
			body.put(ALARM_ACK_USER_PARAM, null);
		}

		// add "initialseverity"
		if(parmsMap.get(INITIAL_SEVERITY_PARAM)!=null){
			try{
				String severityId = parmsMap.get(INITIAL_SEVERITY_PARAM);
				int id= Integer.parseInt(severityId);
				String label = OnmsSeverity.get(id).getLabel();
				body.put(INITIAL_SEVERITY_PARAM,severityId);
				body.put(INITIAL_SEVERITY_PARAM_TEXT,label);
			}
			catch (Exception e){
				LOG.error("cannot parse initial severity for alarm change event id"+event.getDbid());
			}
		}

		// if the event contains nodelabel parameter then do not use node cache
		if (parmsMap.get(NODE_LABEL_PARAM)!=null){
			body.put(NODE_LABEL_PARAM, parmsMap.get(NODE_LABEL_PARAM));
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
			LOG.error("No alarmid param - cannot create alarm Elasticsearch record from event content:"+ event.toString());
		} else{
			String id = alarmValues.get("alarmid").toString();

			// add the p_alarmid so that we can easily match alarm change events with same alarmid
			body.put("p_alarmid", id);

			String alarmCreationTime=null;
			Date alarmCreationDate=null;
			Calendar alarmCreationCal=null;

			// try to parse firsteventtime but if not able then use current date
			try{
				alarmCreationTime = alarmValues.get(FIRST_EVENT_TIME).toString();
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

	private static void createIndex(JestClient client, String name, String type) throws IOException {
		// create new index
		CreateIndex createIndex = new CreateIndex.Builder(name).build();
		JestResult result = new OnmsJestResult(client.execute(createIndex));
		if(LOG.isDebugEnabled()) {
			LOG.debug("created new alarm index: {} type: {}" +
					"\n   received search result: {}" +
					"\n   response code: {}" + 
					"\n   error message: {}", 
					name, type, result.getJsonString(), result.getResponseCode(), result.getErrorMessage());
		}
	}

	public static boolean isOID(String input) {
		return input.matches("^(\\.[0-9]+)+$");
	}

}
