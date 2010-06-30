//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.mock;


import org.opennms.netmgt.xml.event.Event;

/**
 * <p>MockNode class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public class MockNode extends MockContainer {

    String m_label;

    int m_nodeid;
    int m_nextIfIndex = 1;

    /**
     * <p>Constructor for MockNode.</p>
     *
     * @param network a {@link org.opennms.netmgt.mock.MockNetwork} object.
     * @param nodeid a int.
     * @param label a {@link java.lang.String} object.
     */
    public MockNode(MockNetwork network, int nodeid, String label) {
        super(network);
        m_nodeid = nodeid;
        m_label = label;
    }

    // model
    /**
     * <p>addInterface</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public MockInterface addInterface(String ipAddr) {
        return (MockInterface) addMember(new MockInterface(this, ipAddr));
    }

    // model
    /**
     * <p>getInterface</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public MockInterface getInterface(String ipAddr) {
        return (MockInterface) getMember(ipAddr);
    }

    // impl
    Object getKey() {
        return new Integer(m_nodeid);
    }

    // model
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    // model
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
    public MockNetwork getNetwork() {
        return (MockNetwork) getParent();
    }

    // model
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeid;
    }
    
    /**
     * <p>getNextIfIndex</p>
     *
     * @return a int.
     */
    public int getNextIfIndex() {
        return m_nextIfIndex++;
    }

    // model
    /**
     * <p>removeInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public void removeInterface(MockInterface iface) {
        removeMember(iface);
    }

    // impl
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "Node[" + m_nodeid + "," + m_label + "]";

    }

    // impl
    /** {@inheritDoc} */
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitNode(this);
        visitMembers(v);
    }

    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createUpEvent() {
        return MockEventUtil.createNodeUpEvent("Test", this);
    }

    /**
     * <p>createDownEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createDownEvent() {
        return MockEventUtil.createNodeDownEvent("Test", this);
    }
    
    /**
     * <p>createDownEventWithReason</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createDownEventWithReason(String reason) {
        return MockEventUtil.createNodeDownEventWithReason("Test", this, reason);
    }
    
    /**
     * <p>createNewEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createNewEvent() {
        return MockEventUtil.createNodeAddedEvent("Test", this);
    }

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createDeleteEvent() {
        return MockEventUtil.createNodeDeletedEvent("Test", this);
    }
    

}
