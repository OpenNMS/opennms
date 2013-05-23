/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

/**
 * <p>DeleteEventVisitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
public final class DeleteEventVisitor extends AbstractEntityVisitor {
	private final List<Event> m_events;

	DeleteEventVisitor(List<Event> events) {
		m_events = events;
	}

	/** {@inheritDoc} */
        @Override
	public void visitMonitoredServiceComplete(OnmsMonitoredService monSvc) {
		m_events.add(EventUtils.createServiceDeletedEvent("ModelImporter", monSvc.getNodeId().longValue(), InetAddressUtils.str(monSvc.getIpAddress()), monSvc.getServiceType().getName(), -1L));
	}

	/** {@inheritDoc} */
        @Override
	public void visitIpInterfaceComplete(OnmsIpInterface iface) {
		m_events.add(EventUtils.createInterfaceDeletedEvent("ModelImporter", iface.getNode().getId().longValue(), InetAddressUtils.str(iface.getIpAddress()), -1L));
	}

	/** {@inheritDoc} */
        @Override
	public void visitNodeComplete(OnmsNode node) {
		m_events.add(EventUtils.createNodeDeletedEvent("ModelImporter", node.getId().intValue(), node.getLabel(), node.getLabel(), -1L));
	}
}
