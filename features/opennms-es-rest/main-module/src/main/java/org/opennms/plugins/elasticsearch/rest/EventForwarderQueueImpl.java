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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queues events received from OpenNMS for forwarding to Elasticsearch. By default,
 * it blocks when inserting into Elasticsearch. If you set {@link #setBlockWhenFull(boolean)}
 * to false, the forwarder will drop events if ES not keeping up and queue has become full.
 * 
 * @author cgallen
 *
 */
public class EventForwarderQueueImpl implements EventForwarder {

	private static final Logger LOG = LoggerFactory
			.getLogger(EventForwarderQueueImpl.class);

	private Integer maxQueueLength = 1000;
	private boolean blockWhenFull = true;

	private LinkedBlockingQueue<Event> queue = null;
	private AtomicBoolean clientRunning = new AtomicBoolean(false);

	private RemovingConsumer removingConsumer = new RemovingConsumer();
	private Thread removingConsumerThread = new Thread(removingConsumer);

	private EventToIndex eventToIndex = null;

	private ElasticSearchInitialiser elasticSearchInitialiser = null;

	public EventToIndex getEventToIndex() {
		return eventToIndex;
	}

	public void setEventToIndex(EventToIndex eventToIndex) {
		this.eventToIndex = eventToIndex;
	}

	public Integer getMaxQueueLength() {
		return maxQueueLength;
	}

	public void setMaxQueueLength(Integer maxQueueLength) {
		this.maxQueueLength = maxQueueLength;
	}

	public boolean isBlockWhenFull() {
		return blockWhenFull;
	}

	public void setBlockWhenFull(boolean blockWhenFull) {
		this.blockWhenFull = blockWhenFull;
	}

	@Override
	public void sendNow(Event event) {

		// if no elasticSearchInitialiser defined then just run
		// else check if initialised then send event
		if (elasticSearchInitialiser != null
				&& elasticSearchInitialiser.isInitialised()) {

			if (LOG.isDebugEnabled())
				LOG.debug("Event received: queue.size() " + queue.size()
						+ " queue.remainingCapacity() "
						+ queue.remainingCapacity() + "\n   event:"
						+ event.toString());
			
			if (blockWhenFull) {
				try {
					queue.put(event);
				} catch (InterruptedException e) {
					LOG.warn("Elasticsearch interface discarding event dbid="
							+ event.getDbid()
							+ " Interrupted exception while trying to add event to queue, size="
							+ queue.size());
				}
			} else {
				if (!queue.offer(event)) {
					LOG.warn("Elasticsearch interface discarding event dbid="
							+ event.getDbid()
							+ " Cannot queue any more events. Event queue full. size="
							+ queue.size());
				}
			}
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("Not sending event received Elasticsearch not initialised"
						+ "\n   event:" + event.toString());
		}
	}

	@Override
	public void sendNow(Log eventLog) {
		// NOT USED
	}


	@Override
	public void sendNowSync(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendNowSync(Log eventLog) {
		throw new UnsupportedOperationException();
	}

	public void init() {
		LOG.debug("initialising EventFowarderQueue with queue size "
				+ maxQueueLength);
		queue = new LinkedBlockingQueue<Event>(maxQueueLength);

		// start consuming thread
		clientRunning.set(true);
		removingConsumerThread.start();

	}

	public void destroy() {
		LOG.debug("shutting down EventFowarderQueue");

		// signal consuming thread to stop
		clientRunning.set(false);
		removingConsumerThread.interrupt();
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

	/*
	 * Class run in separate thread to remove and process notifications from the
	 * queue
	 */
	private class RemovingConsumer implements Runnable {
		// TODO remove final Logger LOG =
		// LoggerFactory.getLogger(EventForwarderQueueImpl.class);

		@Override
		public void run() {

			// we remove elements from the queue until interrupted and
			// clientRunning==false.
			while (clientRunning.get()) {
				try {
					Event event = queue.take();

					if (LOG.isDebugEnabled())
						LOG.debug("Event received from queue by consumer thread :\n event:"
								+ event.toString());

					// send event to index processor
					if (eventToIndex != null) {
						eventToIndex.forwardEvent(event);
					} else {
						LOG.error("cannot send event eventToIndex is null");
					}

				} catch (InterruptedException e) {
				}

			}

			LOG.debug("shutting down event consumer thread");
		}
	}

}
