/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.plugins.elasticsearch.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.core.ipc.sink.aggregation.AggregatingMessageProducer;
import org.opennms.core.ipc.sink.aggregation.ArrayListAggregationPolicy;
import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.template.TemplateInitializer;
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

	private static final Logger LOG = LoggerFactory.getLogger(EventForwarderQueueImpl.class);

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
			try {
				// Ensure we are initialized correctly.
				if (!elasticSearchInitializer.isInitialized()) {
					elasticSearchInitializer.initialize(); // blocks until initialized properly
				}
			} catch (ConnectionPoolShutdownException ex) { // Connection Pool is gone, nothing we can do
				ExceptionUtils.handle(getClass(), ex, events);
				return;
			}
			eventToIndex.forwardEvents(events);
		}
	}
}
