/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.mock;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.NodeLabelChangedEventBuilder;
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
public class MockNode extends MockContainer<MockNetwork, MockElement> {

    String m_label;
    String m_location;

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
        // org.opennms.netmgt.dao.api.MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID
        m_location = "Default";
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
        return Integer.valueOf(m_nodeid);
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

    /**
     * <p>getLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocation() {
        return m_location;
    }

    /**
     * <p>setLocation</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLocation(String location) {
        m_location = location;
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
    		.append("location", m_location)
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
        NodeLabelChangedEventBuilder event = new NodeLabelChangedEventBuilder("Test");
        event.setNodeid(m_nodeid);
        event.setNewNodeLabel(newLabel);
        event.setOldNodeLabel("oldLabel");
        return event.getEvent();
    }
    
}
