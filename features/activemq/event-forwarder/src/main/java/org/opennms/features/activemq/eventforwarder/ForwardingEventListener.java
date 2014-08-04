/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.activemq.eventforwarder;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * This event sends incoming events to an {@link EventForwarder} that uses Camel+ActiveMQ to 
 * forward events to an external ActiveMQ broker. 
 */
public class ForwardingEventListener implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(ForwardingEventListener.class);

	private volatile EventForwarder eventForwarder;
	private volatile EventIpcManager eventIpcManager;
	private volatile NodeDao nodeDao;
	private volatile TransactionTemplate transactionTemplate;

	public EventForwarder getEventForwarder() {
		return eventForwarder;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
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

	/**
	 * <p>init</p>
	 */
	public void init() {
		Assert.notNull(eventIpcManager, "eventIpcManager must not be null");
		//Assert.notNull(nodeDao, "nodeDao must not be null");
		Assert.notNull(eventForwarder, "eventForwarder must not be null");
		//Assert.notNull(transactionTemplate, "transactionTemplate must not be null");

		installMessageSelectors();
	}

	private void installMessageSelectors() {
		// Add the EventListeners for the UEIs in which this service is
		// interested
		List<String> ueiList = new ArrayList<String>();

		// nodeGainedService
		ueiList.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);

		// primarySnmpInterfaceChanged
		ueiList.add(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI);

		// reinitializePrimarySnmpInterface
		ueiList.add(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI);

		// interfaceReparented
		ueiList.add(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);

		// nodeDeleted
		ueiList.add(EventConstants.NODE_DELETED_EVENT_UEI);

		// duplicateNodeDeleted
		ueiList.add(EventConstants.DUP_NODE_DELETED_EVENT_UEI);

		// interfaceDeleted
		ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);

		// serviceDeleted
		ueiList.add(EventConstants.SERVICE_DELETED_EVENT_UEI);

		// outageConfigurationChanged
		ueiList.add(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);

		// configureSNMP
		ueiList.add(EventConstants.CONFIGURE_SNMP_EVENT_UEI);

		// thresholds configuration change
		ueiList.add(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);

		// daemon configuration change
		ueiList.add(EventConstants.RELOAD_DAEMON_CONFIG_UEI);

		// node category membership changes
		ueiList.add(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI);

		getEventIpcManager().addEventListener(this, ueiList);
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
		// Send the event to the event forwarder
		eventForwarder.sendNow(event);
	}

	@Override
	public String getName() {
		return "ActiveMQEventForwarder";
	}
}
