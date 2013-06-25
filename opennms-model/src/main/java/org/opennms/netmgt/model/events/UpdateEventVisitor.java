/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>UpdateEventVisitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class UpdateEventVisitor extends AbstractEntityVisitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(UpdateEventVisitor.class);

    
    private static final String m_eventSource = "Provisiond";
    private EventForwarder m_eventForwarder;

    /**
     * <p>Constructor for UpdateEventVisitor.</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public UpdateEventVisitor(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitNode(OnmsNode node) {
        LOG.info("Sending nodeAdded Event for {}\n", node);
        m_eventForwarder.sendNow(createNodeUpdatedEvent(node));
    }

    /** {@inheritDoc} */
    @Override
    public void visitIpInterface(OnmsIpInterface iface) {
        //TODO decide what to do here and when to do it
    }

    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        //TODO decide what to do here and when to do it
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitSnmpInterface(org.opennms.netmgt.model.OnmsEntity snmpIface) {
        //TODO decide what to do here and when to do it
    }

    private Event createNodeUpdatedEvent(OnmsNode node) {
        return EventUtils.createNodeUpdatedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabelSource());
    }

    @SuppressWarnings("unused")
    private Event createIpInterfaceUpdatedEvent(OnmsIpInterface iface) {
        return null;
    }
    
    @SuppressWarnings("unused")
    private Event createSnmpInterfaceUpdatedEvent(OnmsSnmpInterface iface) {
        return null;
    }

    @SuppressWarnings("unused")
    private Event createMonitoredServiceUpdatedEvent(OnmsMonitoredService monSvc) {
        return null;
    }

}
