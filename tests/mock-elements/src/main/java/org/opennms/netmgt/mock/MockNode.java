/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
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
public class MockNode extends MockContainer<MockNetwork,MockInterface> {

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
    @Override
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

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    // model
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
    @Override
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
    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("id", m_nodeid)
    		.append("label", m_label)
    		.append("members", getMembers()).toString();
    }

    // impl
    /** {@inheritDoc} */
    @Override
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
    @Override
    public Event createUpEvent() {
        return MockEventUtil.createNodeUpEvent("Test", this);
    }

    /**
     * <p>createDownEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Override
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
    @Override
    public Event createNewEvent() {
        return MockEventUtil.createNodeAddedEvent("Test", this);
    }

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Override
    public Event createDeleteEvent() {
        return MockEventUtil.createNodeDeletedEvent("Test", this);
    }

    /**
     * <p>createNodeLabelChangedEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createNodeLabelChangedEvent(String newLabel) {
        EventBuilder event = MockEventUtil.createEventBuilder("Test", EventConstants.NODE_LABEL_CHANGED_EVENT_UEI);
        event.setNodeid(m_nodeid);
        event.addParam(EventConstants.PARM_NODE_LABEL, newLabel);
        return event.getEvent();
    }
}
