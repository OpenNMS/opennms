/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.elasticsearch.eventforwarder;

import org.opennms.features.elasticsearch.eventforwarder.internal.CamelEventForwarder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * This event sends incoming events to an {@link EventForwarder} that uses Camel+Elasticsearch to
 * forward events to an external Elasticsearch.
 */
public class ForwardingEventListener implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ForwardingEventListener.class);

	private volatile CamelEventForwarder eventForwarder;
	private volatile EventIpcManager eventIpcManager;
	private volatile NodeDao nodeDao;
	private volatile TransactionTemplate transactionTemplate;
	private volatile boolean logAllEvents = false;

	public CamelEventForwarder getEventForwarder() {
		return eventForwarder;
	}

	public void setEventForwarder(CamelEventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

	public EventIpcManager getEventIpcManager() {
		return eventIpcManager;
	}

	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		this.eventIpcManager = eventIpcManager;
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	public boolean isLogAllEvents() {
		return logAllEvents;
	}

	public void setLogAllEvents(boolean logAllEvents) {
		this.logAllEvents = logAllEvents;
	}

	/**
	 * <p>init</p>
	 */
	public void init() {
		Assert.notNull(eventIpcManager, "eventIpcManager must not be null");
		Assert.notNull(eventForwarder, "eventForwarder must not be null");

		installMessageSelectors();

		LOG.info("Elasticsearch event forwarder initialized");
	}

	private void installMessageSelectors() {
		getEventIpcManager().addEventListener(this);
	}

	public void destroy() {
		Assert.notNull(eventIpcManager, "eventIpcManager must not be null");
		Assert.notNull(eventForwarder, "eventForwarder must not be null");

		getEventIpcManager().removeEventListener(this);

		LOG.info("Elasticsearch event forwarder uninstalled");
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is invoked by the JMS topic session when a new event is
	 * available for processing. Currently only text based messages are
	 * processed by this callback. Each message is examined for its Universal
	 * Event Identifier and the appropriate action is taking based on each
	 * UEI.
	 */
	@Override
	public void onEvent(final Event event) {
		// only process events persisted to database
		if(logAllEvents || (event.getDbid() != null && event.getDbid() > 0)) {
			// Send the event to the event forwarder
			eventForwarder.sendNow(event);
		}
	}

	@Override
	public String getName() {
		return "ElasticsearchEventForwarder";
	}
}
