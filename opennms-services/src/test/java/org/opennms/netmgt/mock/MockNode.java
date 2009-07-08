/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.mock;


import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MockNode extends MockContainer {

    String m_label;

    int m_nodeid;
    int m_nextIfIndex = 1;

    public MockNode(MockNetwork network, int nodeid, String label) {
        super(network);
        m_nodeid = nodeid;
        m_label = label;
    }

    // model
    public MockInterface addInterface(String ipAddr) {
        return (MockInterface) addMember(new MockInterface(this, ipAddr));
    }

    // model
    public MockInterface getInterface(String ipAddr) {
        return (MockInterface) getMember(ipAddr);
    }

    // impl
    Object getKey() {
        return new Integer(m_nodeid);
    }

    // model
    public String getLabel() {
        return m_label;
    }

    // model
    public MockNetwork getNetwork() {
        return (MockNetwork) getParent();
    }

    // model
    public int getNodeId() {
        return m_nodeid;
    }
    
    public int getNextIfIndex() {
        return m_nextIfIndex++;
    }

    // model
    public void removeInterface(MockInterface iface) {
        removeMember(iface);
    }

    // impl
    public String toString() {
        return "Node[" + m_nodeid + "," + m_label + "]";

    }

    // impl
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitNode(this);
        visitMembers(v);
    }

    public Event createUpEvent() {
        return MockEventUtil.createNodeUpEvent("Test", this);
    }

    public Event createDownEvent() {
        return MockEventUtil.createNodeDownEvent("Test", this);
    }
    
    public Event createDownEventWithReason(String reason) {
        return MockEventUtil.createNodeDownEventWithReason("Test", this, reason);
    }
    
    public Event createNewEvent() {
        return MockEventUtil.createNodeAddedEvent("Test", this);
    }

    public Event createDeleteEvent() {
        return MockEventUtil.createNodeDeletedEvent("Test", this);
    }
    

}
