//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
	public void visitNode(OnmsNode node) {
		m_events.add(EventUtils.createNodeAddedEvent(node.getId().intValue(), node.getLabel(), node.getLabelSource()));
	}

	/** {@inheritDoc} */
	public void visitIpInterface(OnmsIpInterface iface) {
		m_events.add(EventUtils.createNodeGainedInterfaceEvent("ModelImporter", iface.getNode().getId().intValue(), iface.getIpAddress()));
	}

	/** {@inheritDoc} */
	public void visitMonitoredService(OnmsMonitoredService monSvc) {
		OnmsIpInterface iface = monSvc.getIpInterface();
		OnmsNode node = iface.getNode();
		m_events.add(EventUtils.createNodeGainedServiceEvent("ModelImporter", monSvc.getNodeId().intValue(), iface.getIpAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription()));
	}
}
