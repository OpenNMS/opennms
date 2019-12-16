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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.bulk.BulkException;
import org.opennms.features.jest.client.bulk.BulkRequest;
import org.opennms.features.jest.client.bulk.BulkWrapper;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.searchbox.action.BulkableAction;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.BulkResult.BulkResultItem;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;

public class EventToIndex implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(EventToIndex.class);

	private static final String INDEX_NAME = "opennms-events-raw";

	private static final String NODE_LABEL_PARAM="nodelabel";

	private static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() * 2;

	private boolean logEventDescription = false;

	private boolean logAllEvents = false;

	private boolean groupOidParameters = false;

	private NodeCache nodeCache = null;

	private final JestClient jestClient;

	private final int bulkRetryCount;

	private int threads = DEFAULT_NUMBER_OF_THREADS;

	private IndexStrategy indexStrategy = IndexStrategy.MONTHLY;

	private IndexSettings indexSettings = new IndexSettings();

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
			threads,
			threads,
			0L, TimeUnit.MILLISECONDS,
			new SynchronousQueue<>(true),
			new ThreadFactoryBuilder().setNameFormat(EventToIndex.class.getSimpleName() + "-Thread-%d").build(),
			// Throttle incoming tasks by running them on the caller thread
			new ThreadPoolExecutor.CallerRunsPolicy()
	);

	public EventToIndex(JestClient jestClient, int bulkRetryCount) {
		this.jestClient = Objects.requireNonNull(jestClient);
		this.bulkRetryCount = bulkRetryCount;
	}

	public void setIndexStrategy(IndexStrategy indexStrategy) {
		this.indexStrategy = indexStrategy;
	}

	public boolean isLogEventDescription() {
		return logEventDescription;
	}

	public void setLogEventDescription(boolean logEventDescription) {
		this.logEventDescription = logEventDescription;
	}

	public void setLogAllEvents(boolean logAllEvents) {
		this.logAllEvents = logAllEvents;
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

	public void setNodeCache(NodeCache cache) {
		this.nodeCache = cache;
	}

	public void setGroupOidParameters(boolean groupOidParameters) {
		this.groupOidParameters = groupOidParameters;
	}

	@Override
	public void close(){
		// Shutdown the thread pool
		executor.shutdown();
	}

	public void forwardEvents(final List<Event> events) {
		CompletableFuture.completedFuture(events)
				// Send the events to Elasticsearch
				.thenAcceptAsync(this::sendEvents, executor)
				.exceptionally(e -> {
					LOG.error("Unexpected exception during task completion: " + e.getMessage(), e);
					if (e.getCause() instanceof ConnectionPoolShutdownException) {
						ExceptionUtils.handle(getClass(), (ConnectionPoolShutdownException) e.getCause(), events);
					}
					return null;
				});
	}

	private void sendEvents(final List<Event> allEvents) {
		final BulkRequest<Event> request = new BulkRequest<>(jestClient, allEvents,
				(events) -> new BulkWrapper(new Bulk.Builder().addAction(convertEventsToEsActions(events))),
				bulkRetryCount);
		try {
			request.execute();
		} catch (BulkException ex) {
			final BulkResult result = ex.getBulkResult().getRawResult();
			LOG.error("Bulk API action failed. Error response was: {}", result.getErrorMessage());
			// Log any unsuccessful completions as errors
			if (LOG.isDebugEnabled() && result != null) {
				for (BulkResultItem item : result.getItems()) {
					if (item.status >= 200 && item.status < 300) {
						logEsDebug(item.operation, item.index, item.type, "none", item.status, item.error);
					} else {
						logEsError(item.operation, item.index, item.type, "none", item.status, item.error);
					}
				}
			}
		} catch (IOException ex) {
			LOG.error("Bulk API action failed. An exception occurred: {}", ex.getMessage(), ex);
		}
	}

	/**
	 * <p>This method converts events into a sequence of Elasticsearch index/update commands.
	 *
	 * @param events
	 */
	private List<BulkableAction<DocumentResult>> convertEventsToEsActions(List<Event> events) {

		final List<BulkableAction<DocumentResult>> retval = new ArrayList<>();

		for (Event event : events) {

			refreshCacheIfNecessary(event);

			// Only send events to ES if they are persisted to database or logAllEvents is set to true
			if(logAllEvents || (event.getDbid() !=null && event.getDbid()!=0)) {
				Index eventIndex = createEventIndexFromEvent(event);
				retval.add(eventIndex);
			} else {
				LOG.debug("Not Sending Event to ES. Event is not persisted to database, or logAllEvents is false. Event: {}", event);
			}
		}

		return retval;
	}

	/**
	 * Utility method to populate a Map with the most import event attributes
	 */
	private Index createEventIndexFromEvent(final Event event) {
		final JSONObject body = new JSONObject();

		final Integer id = event.getDbid();
		body.put("id", Integer.toString(id));
		body.put("eventuei", event.getUei());

		final Calendar cal=Calendar.getInstance();
		if (event.getTime() == null) {
			LOG.debug("using local time because no event creation time for event.getTime: {}", event);
			cal.setTime(new Date());
		} else {
			cal.setTime(event.getTime());
		}
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

		// Include the time at which the event was actually created, which may differ from the time of the event
		if (event.getCreationTime() != null) {
			cal.setTime(event.getCreationTime());
			body.put("eventcreationtime", DatatypeConverter.printDateTime(cal));
		}

		// Include alarm data, which allows us to correlate events to alarms
		final AlarmData alarmData = event.getAlarmData();
		if (alarmData != null) {
			body.put("alarmreductionkey", alarmData.getReductionKey());
			body.put("alarmclearkey", alarmData.getClearKey());
			body.put("alarmtype", alarmData.getAlarmType());
		}

		// Parse event parameters
		handleParameters(event, body);

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
				if (nodeCache != null){
					Map nodedetails = nodeCache.getEntry(event.getNodeid());
					for (Object key: nodedetails.keySet()){
						String keyStr = (String) key;
						String value = (String) nodedetails.get(key);
						body.put(keyStr, value);
					}
				}
			}
		}

		String completeIndexName = indexStrategy.getIndex(indexSettings, INDEX_NAME, cal.toInstant());

		if (LOG.isDebugEnabled()){
			String str = "populateEventIndexBodyFromEvent - index:"
					+ "/"+completeIndexName
					+ "/"+id
					+ "\n   body: \n" + body.toJSONString();
			LOG.debug(str);
		}

		Index.Builder builder = new Index.Builder(body)
				.index(completeIndexName);

		// NMS-9015: If the event is a database event, set the ID of the
		// document to the event's database ID. Otherwise, allow ES to
		// generate a unique ID value.
		if (id != null && id > 0) {
			builder = builder.id(Integer.toString(id));
		}

		Index index = builder.build();

		return index;
	}

	void handleParameters(Event event, JSONObject body) {
		// Decide if oids should be grouped in a single JsonArray or flattened
		if (groupOidParameters) {
			final List<Parm> oidParameters = event.getParmCollection().stream().filter(p -> isOID(p.getParmName())).collect(Collectors.toList());
			final List<Parm> normalParameters = new ArrayList<>(event.getParmCollection());
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

	void handleParameters(Event event, List<Parm> parameters, JSONObject body) {
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

	private void refreshCacheIfNecessary(Event event) {
		final String uei = event.getUei();
		if(uei != null && uei.startsWith("uei.opennms.org/nodes/")) {
			if (uei.endsWith("Added")
					|| uei.endsWith("Deleted")
					|| uei.endsWith("Updated")
					|| uei.endsWith("Changed")) {
				nodeCache.refreshEntry(event.getNodeid());
			}
		}
	}

	public IndexSettings getIndexSettings() {
		return indexSettings;
	}

	public void setIndexSettings(IndexSettings indexSettings) {
		this.indexSettings = Objects.requireNonNull(indexSettings);
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

	protected static boolean isOID(String input) {
		return input.matches("^(\\.[0-9]+)+$");
	}
}
