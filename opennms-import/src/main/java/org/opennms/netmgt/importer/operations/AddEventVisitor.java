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
 * <p>AddEventVisitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
public final class AddEventVisitor extends AbstractEntityVisitor {
	private final List<Event> m_events;

	AddEventVisitor(List<Event> events) {
		m_events = events;
	}

	/** {@inheritDoc} */
        @Override
	public void visitNode(OnmsNode node) {
		m_events.add(EventUtils.createNodeAddedEvent(node.getId().intValue(), node.getLabel(), node.getLabelSource()));
	}

	/** {@inheritDoc} */
        @Override
	public void visitIpInterface(OnmsIpInterface iface) {
		m_events.add(EventUtils.createNodeGainedInterfaceEvent("ModelImporter", iface.getNode().getId().intValue(), iface.getIpAddress()));
	}

	/** {@inheritDoc} */
        @Override
	public void visitMonitoredService(OnmsMonitoredService monSvc) {
		OnmsIpInterface iface = monSvc.getIpInterface();
		OnmsNode node = iface.getNode();
		m_events.add(EventUtils.createNodeGainedServiceEvent("ModelImporter", monSvc.getNodeId().intValue(), iface.getIpAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription()));
	}
}
