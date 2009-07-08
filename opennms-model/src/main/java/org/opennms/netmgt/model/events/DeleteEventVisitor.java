/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

/**
 * 
 */
package org.opennms.netmgt.model.events;


import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

public final class DeleteEventVisitor extends AbstractEntityVisitor {
    private final EventForwarder m_eventForwarder;
    private static final String m_eventSource = "Provisiond";

	public DeleteEventVisitor(EventForwarder eventForwarder) {
	    m_eventForwarder = eventForwarder;
	}

	public void visitMonitoredServiceComplete(OnmsMonitoredService monSvc) {
	    m_eventForwarder.sendNow(EventUtils.createServiceDeletedEvent(m_eventSource, monSvc.getNodeId(), monSvc.getIpAddress(), monSvc.getServiceType().getName()));
	}

	public void visitIpInterfaceComplete(OnmsIpInterface iface) {
	    m_eventForwarder.sendNow(EventUtils.createInterfaceDeletedEvent(m_eventSource, iface.getNode().getId(), iface.getIpAddress()));
	}

	public void visitNodeComplete(OnmsNode node) {
	    m_eventForwarder.sendNow(EventUtils.createNodeDeletedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabel()));
	}
}