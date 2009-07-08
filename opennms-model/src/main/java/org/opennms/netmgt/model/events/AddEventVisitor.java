/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.model.events;


import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;

public class AddEventVisitor extends AbstractEntityVisitor {
    private static final String m_eventSource = "Provisiond";
	private final EventForwarder m_eventForwarder;

	public AddEventVisitor(EventForwarder eventForwarder) {
		m_eventForwarder = eventForwarder;
	}

	public void visitNode(OnmsNode node) {
        System.out.printf("Sending nodeAdded Event for %s\n", node);
	    m_eventForwarder.sendNow(createNodeAddedEvent(node));
	}

    public void visitIpInterface(OnmsIpInterface iface) {
        System.out.printf("Sending nodeGainedInterface Event for %s\n", iface);
        m_eventForwarder.sendNow(createNodeGainedInterfaceEvent(iface));
    }

    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        System.out.printf("Sending nodeGainedService Event for %s\n", monSvc);
        m_eventForwarder.sendNow(createNodeGainedServiceEvent(monSvc));
    }

    protected Event createNodeAddedEvent(OnmsNode node) {
        return EventUtils.createNodeAddedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabelSource());
    }

    protected Event createNodeGainedInterfaceEvent(OnmsIpInterface iface) {
        return EventUtils.createNodeGainedInterfaceEvent(m_eventSource, iface.getNode().getId(), iface.getInetAddress());
    }

    protected Event createNodeGainedServiceEvent(OnmsMonitoredService monSvc) {
        OnmsIpInterface iface = monSvc.getIpInterface();
		OnmsNode node = iface.getNode();
		return EventUtils.createNodeGainedServiceEvent(m_eventSource, monSvc.getNodeId(), iface.getInetAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
    }
	

}
