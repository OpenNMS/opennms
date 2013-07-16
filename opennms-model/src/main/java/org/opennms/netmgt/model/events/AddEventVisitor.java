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
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AddEventVisitor extends AbstractEntityVisitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(AddEventVisitor.class);

    private static final String m_eventSource = "Provisiond";
	private final EventForwarder m_eventForwarder;

	/**
	 * <p>Constructor for AddEventVisitor.</p>
	 *
	 * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
	 */
	public AddEventVisitor(EventForwarder eventForwarder) {
		m_eventForwarder = eventForwarder;
	}

	/** {@inheritDoc} */
    @Override
	public void visitNode(OnmsNode node) {
        LOG.info("Sending nodeAdded Event for {}\n", node);
	    m_eventForwarder.sendNow(createNodeAddedEvent(node));
	}

    /** {@inheritDoc} */
    @Override
    public void visitIpInterface(OnmsIpInterface iface) {
        LOG.info("Sending nodeGainedInterface Event for {}\n", iface);
        m_eventForwarder.sendNow(createNodeGainedInterfaceEvent(iface));
    }

    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        LOG.info("Sending nodeGainedService Event for {}\n", monSvc);
        m_eventForwarder.sendNow(createNodeGainedServiceEvent(monSvc));
    }

    /**
     * <p>createNodeAddedEvent</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeAddedEvent(OnmsNode node) {
        return EventUtils.createNodeAddedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabelSource());
    }

    /**
     * <p>createNodeGainedInterfaceEvent</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeGainedInterfaceEvent(OnmsIpInterface iface) {
        return EventUtils.createNodeGainedInterfaceEvent(m_eventSource, iface.getNode().getId(), iface.getIpAddress());
    }

    /**
     * <p>createNodeGainedServiceEvent</p>
     *
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeGainedServiceEvent(final OnmsMonitoredService monSvc) {
    	final OnmsIpInterface iface = monSvc.getIpInterface();
		final OnmsNode node = iface.getNode();
		LOG.debug("ipinterface = {}", iface);
		LOG.debug("snmpinterface = {}", iface.getSnmpInterface());
		LOG.debug("node = {}", node);
		return EventUtils.createNodeGainedServiceEvent(m_eventSource, monSvc.getNodeId(), iface.getIpAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
    }
	

}
