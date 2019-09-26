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
import java.util.Objects;

import org.opennms.core.ipc.sink.aggregation.AggregatingMessageProducer;
import org.opennms.core.ipc.sink.aggregation.ArrayListAggregationPolicy;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.plugins.elasticsearch.rest.template.TemplateInitializer;

/**
 * Queues events received from OpenNMS for forwarding to Elasticsearch.
 * 
 * @author cgallen
 * @author Seth
 */
public class EventForwarderQueueImpl implements EventForwarder, AutoCloseable {

	private final EventToIndex eventToIndex;
	private final TemplateInitializer elasticSearchInitializer;
	private AggregatingMessageProducer<Event,List<Event>> producer;

	public EventForwarderQueueImpl(EventToIndex eventToIndex, TemplateInitializer initializer, int batchSize, int batchInterval) {
		this.elasticSearchInitializer = Objects.requireNonNull(initializer);
		this.eventToIndex = Objects.requireNonNull(eventToIndex);
		this.producer = new EventIndexProducer(batchSize, batchInterval);
	}

	@Override
	public void sendNow(Event event) {
		producer.send(event);
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

	@Override
	public void close() throws Exception {
		if (producer != null) {
			producer.close();
			producer = null;
		}
	}

	/**
	 * This {@link AggregatingMessageProducer} aggregates using a single
	 * bucket for all received events.
	 */
	private class EventIndexProducer extends AggregatingMessageProducer<Event, List<Event>> {

		public EventIndexProducer(int batchSize, int batchInterval) {
			super(
				EventIndexProducer.class.getName(), new ArrayListAggregationPolicy<>(batchSize, batchInterval, e -> "event")
			);
		}

		/**
		 * Send events to the index processor.
		 */
		@Override
		public void dispatch(List<Event> events) {
			// Ensure we are initialized correctly.
			if (!elasticSearchInitializer.isInitialized()) {
				elasticSearchInitializer.initialize(); // blocks until initialized properly
			}
			eventToIndex.forwardEvents(events);
		}
	}
}
