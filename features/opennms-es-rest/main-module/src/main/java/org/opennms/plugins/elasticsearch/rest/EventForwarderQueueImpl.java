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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opennms.core.ipc.sink.aggregation.AggregatingMessageProducer;
import org.opennms.core.ipc.sink.aggregation.ArrayListAggregationPolicy;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queues events received from OpenNMS for forwarding to Elasticsearch.
 * 
 * @author cgallen
 * @author Seth
 */
public class EventForwarderQueueImpl implements EventForwarder, AutoCloseable {

	private static final Logger LOG = LoggerFactory
			.getLogger(EventForwarderQueueImpl.class);

	private AggregatingMessageProducer<Event,List<Event>> producer;

	private EventToIndex eventToIndex = null;

	private ElasticSearchInitialiser elasticSearchInitialiser = null;

	/**
	 * Disable batching by default
	 */
	private int batchSize = 1;

	private int batchInterval = 0;

	public EventToIndex getEventToIndex() {
		return eventToIndex;
	}

	public void setEventToIndex(EventToIndex eventToIndex) {
		this.eventToIndex = eventToIndex;
	}

	@Override
	public void sendNow(Event event) {
		// if no elasticSearchInitialiser defined then just run
		// else check if initialised then send event
		if (elasticSearchInitialiser != null && elasticSearchInitialiser.isInitialised()) {
			producer.send(event);
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("Not sending event received: Elasticsearch is not initialised"
						+ "\n   event:" + event.toString());
		}
	}

	@Override
	public void sendNow(Log eventLog) {
		if (eventLog != null && eventLog.getEvents() != null) {
			for (Event event : eventLog.getEvents().getEvent()) {
				sendNow(event);
			}
		}
	}

	/**
	 * Call {@link AggregatingMessageProducer#dispatch(Object)} to synchronously
	 * dispatch the event.
	 */
	@Override
	public void sendNowSync(Event event) {
		producer.dispatch(Collections.singletonList(event));
	}

	/**
	 * Call {@link AggregatingMessageProducer#dispatch(Object)} to synchronously
	 * dispatch the events.
	 */
	@Override
	public void sendNowSync(Log eventLog) {
		if (eventLog != null && eventLog.getEvents() != null) {
			producer.dispatch(Arrays.asList(eventLog.getEvents().getEvent()));
		}
	}

	public void init() {
		producer = new EventIndexProducer(batchSize, batchInterval);
	}

	@Override
	public void close() throws Exception {
		if (producer != null) {
			producer.close();
			producer = null;
		}
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getBatchInterval() {
		return batchInterval;
	}

	public void setBatchInterval(int batchInterval) {
		this.batchInterval = batchInterval;
	}

	/**
	 * @return the elasticSearchInitialiser
	 */
	public ElasticSearchInitialiser getElasticSearchInitialiser() {
		return elasticSearchInitialiser;
	}

	/**
	 * @param elasticSearchInitialiser
	 *            the elasticSearchInitialiser to set
	 */
	public void setElasticSearchInitialiser(
			ElasticSearchInitialiser elasticSearchInitialiser) {
		this.elasticSearchInitialiser = elasticSearchInitialiser;
	}

	/**
	 * This {@link AggregatingMessageProducer} aggregates using a single
	 * bucket for all received events.
	 */
	private class EventIndexProducer extends AggregatingMessageProducer<Event, List<Event>> {

		public EventIndexProducer(int batchSize, int batchInterval) {
			super(
				EventIndexProducer.class.getName(), 
				new ArrayListAggregationPolicy<Event>(batchSize, batchInterval, e -> { return "event"; })
			);
		}

		@Override
		public void dispatch(List<Event> events) {
			// TODO: HZN-998: Use batch index command to insert all events at once
			for (Event event : events) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Event dispatched to producer:\n event: {}", event.toString());
				}

				// send event to index processor
				if (eventToIndex != null) {
					eventToIndex.forwardEvent(event);
				} else {
					LOG.error("cannot send event, eventToIndex is null");
				}
			}
		}
	}
}
