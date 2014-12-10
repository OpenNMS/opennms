/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;


import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

public class DeleteEventVisitor extends AbstractEntityVisitor {
    private final EventForwarder m_eventForwarder;
    private static final String m_eventSource = "Provisiond";

    public DeleteEventVisitor(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    @Override
    public void visitMonitoredServiceComplete(final OnmsMonitoredService monSvc) {
        m_eventForwarder.sendNow(EventUtils.createServiceDeletedEvent(m_eventSource, monSvc.getNodeId(), monSvc.getIpAddress(), monSvc.getServiceType().getName()));
    }

    @Override
    public void visitIpInterfaceComplete(final OnmsIpInterface iface) {
        m_eventForwarder.sendNow(EventUtils.createInterfaceDeletedEvent(m_eventSource, iface.getNode().getId(), iface.getIpAddress()));
    }

    @Override
    public void visitNodeComplete(final OnmsNode node) {
        m_eventForwarder.sendNow(EventUtils.createNodeDeletedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabel()));
    }
}
