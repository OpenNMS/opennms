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

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.naming.event.EventContext;

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

		//node status events
		ueiList.add(EventConstants.DUP_NODE_DELETED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_ADDED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_DOWN_EVENT_UEI);
		ueiList.add(EventConstants.NODE_DELETED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);
		ueiList.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);
		ueiList.add(EventConstants.NODE_INFO_CHANGED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_LABEL_CHANGED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_LABEL_SOURCE_CHANGED_EVENT_UEI);
		ueiList.add(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
		ueiList.add(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI);
		ueiList.add(EventConstants.NODE_UP_EVENT_UEI);
		ueiList.add(EventConstants.NODE_UPDATED_EVENT_UEI);
		
		//interface status events
		ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);
		ueiList.add(EventConstants.INTERFACE_DOWN_EVENT_UEI);
		ueiList.add(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);
		ueiList.add(EventConstants.INTERFACE_UP_EVENT_UEI);
		
		//component provisioning events
		ueiList.add(EventConstants.COMPONENT_ADDED_UEI);
		ueiList.add(EventConstants.COMPONENT_DELETED_UEI);
		ueiList.add(EventConstants.COMPONENT_UPDATED_UEI);
		
		//threshold status events
		ueiList.add(EventConstants.HIGH_THRESHOLD_EVENT_UEI);
		ueiList.add(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI);
		ueiList.add(EventConstants.LOW_THRESHOLD_EVENT_UEI);
		ueiList.add(EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI);

		ueiList.add(EventConstants.PATH_OUTAGE_EVENT_UEI);
		
		ueiList.add(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI);
		
		ueiList.add(EventConstants.SERVICE_DELETED_EVENT_UEI);
		ueiList.add(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI);
		ueiList.add(EventConstants.SERVICE_STATUS_UNKNOWN);
		ueiList.add(EventConstants.SERVICE_UNMANAGED_EVENT_UEI);
		ueiList.add(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI);
		
		ueiList.add(EventConstants.SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI);
		ueiList.add(EventConstants.SNMP_INTERFACE_ADMIN_UP_EVENT_UEI);
		ueiList.add(EventConstants.SNMP_INTERFACE_OPER_DOWN_EVENT_UEI);
		ueiList.add(EventConstants.SNMP_INTERFACE_OPER_UP_EVENT_UEI);
		
		ueiList.add(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
		ueiList.add(EventConstants.TOPOLOGY_LINK_UP_EVENT_UEI);
		
		ueiList.add(EventConstants.UPDATE_SERVER_EVENT_UEI);
		
		ueiList.add(EventConstants.UPDATE_SERVICE_EVENT_UEI);
		
		ueiList.add(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI);

        //Remote Poller Events
        ueiList.add(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI);
        ueiList.add(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_REGISTERED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_STARTED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_STOPPED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_PAUSED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_RECONNECTED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_CONFIG_CHANGE_DETECTED_UEI);
        ueiList.add(EventConstants.LOCATION_MONITOR_CONNECTION_ADDRESS_CHANGED_UEI);


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
		LOG.debug("Forwarding Event %s", event.getUei());
		eventForwarder.sendNow(event);
	}

	@Override
	public String getName() {
		return "ActiveMQEventForwarder";
	}
}
