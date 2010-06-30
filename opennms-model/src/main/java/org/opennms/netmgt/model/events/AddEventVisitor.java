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
package org.opennms.netmgt.model.events;


import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
public class AddEventVisitor extends AbstractEntityVisitor {
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
	public void visitNode(OnmsNode node) {
        LogUtils.infof(this, "Sending nodeAdded Event for %s\n", node);
	    m_eventForwarder.sendNow(createNodeAddedEvent(node));
	}

    /** {@inheritDoc} */
    public void visitIpInterface(OnmsIpInterface iface) {
        LogUtils.infof(this, "Sending nodeGainedInterface Event for %s\n", iface);
        m_eventForwarder.sendNow(createNodeGainedInterfaceEvent(iface));
    }

    /** {@inheritDoc} */
    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        LogUtils.infof(this, "Sending nodeGainedService Event for %s\n", monSvc);
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
        return EventUtils.createNodeGainedInterfaceEvent(m_eventSource, iface.getNode().getId(), iface.getInetAddress());
    }

    /**
     * <p>createNodeGainedServiceEvent</p>
     *
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeGainedServiceEvent(OnmsMonitoredService monSvc) {
        OnmsIpInterface iface = monSvc.getIpInterface();
		OnmsNode node = iface.getNode();
		return EventUtils.createNodeGainedServiceEvent(m_eventSource, monSvc.getNodeId(), iface.getInetAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
    }
	

}
