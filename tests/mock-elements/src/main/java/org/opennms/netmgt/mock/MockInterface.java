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

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>MockInterface class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public class MockInterface extends MockContainer<MockNode,MockService> {

	private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);

	private final int m_id;
	private String m_ifAlias;
    private final InetAddress m_inetAddr;
    private int m_ifIndex;

    private int m_ifType;
    

    /**
     * <p>Constructor for MockInterface.</p>
     *
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @param ipAddr a {@link java.lang.String} object.
     */
    public MockInterface(MockNode node, String ipAddr) {
        super(node);
        m_id = ID_COUNTER.getAndIncrement();
        m_ifIndex = node.getNextIfIndex();
        m_inetAddr = InetAddressUtils.addr(ipAddr);
        if (m_inetAddr == null) {
            throw new IllegalArgumentException("unable to convert "+ipAddr+" to an InetAddress.");
        }
    }

    // model
    /**
     * <p>addService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public MockService addService(String svcName, int serviceId) {
        return (MockService) addMember(new MockService(this, svcName, serviceId));
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public int getId() {
        return m_id;
    }

    // model
    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return InetAddressUtils.toIpAddrString(m_inetAddr);
    }

    // impl
        @Override
    Object getKey() {
        return getIpAddr();
    }

    // model
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
        @Override
    public MockNetwork getNetwork() {
        return getNode().getNetwork();
    }

    // model
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNode} object.
     */
    public MockNode getNode() {
        return (MockNode) getParent();
    }

    // model
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return getNode().getNodeId();
    }

    // model
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return getNode().getLabel();
    }

    // FIXME: model?
    /**
     * <p>getPollStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
        @Override
    public PollStatus getPollStatus() {
        final String critSvcName = getNetwork().getCriticalService();
        final MockService critSvc = getService(critSvcName);
        class IFStatusCalculator extends MockVisitorAdapter {
            PollStatus status = PollStatus.down();

            public PollStatus getStatus() {
                return status;
            }

            @Override
            public void visitService(MockService svc) {
                if (critSvc == null || critSvc.equals(svc)) {
                    if (svc.getPollStatus().isUp())
                        status = PollStatus.up();
                }
            }

        }

        IFStatusCalculator calc = new IFStatusCalculator();
        visit(calc);
        return calc.getStatus();
    }

    // model
    /**
     * <p>getService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public MockService getService(String svcName) {
        return (MockService) getMember(svcName);
    }

    // model
    /**
     * <p>getServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<MockService> getServices() {
        return getMembers();
    }

    // model
    /**
     * <p>removeService</p>
     *
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public void removeService(MockService svc) {
        removeMember(svc);
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
    		.append("ifAlias", m_ifAlias)
    		.append("ifIndex", m_ifIndex)
    		.append("inetAddr", InetAddressUtils.str(m_inetAddr))
    		.append("members", getMembers())
                .append("ifType", m_ifType)
    		.toString();
    }

    // impl
    /** {@inheritDoc} */
        @Override
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitInterface(this);
        visitMembers(v);
    }

    /**
     * <p>createDownEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
        @Override
    public Event createDownEvent() {
        return MockEventUtil.createInterfaceDownEvent("Test", this);
    }

    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
        @Override
    public Event createUpEvent() {
        return MockEventUtil.createInterfaceUpEvent("Test", this);
    }
    
    /**
     * <p>createNewEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
        @Override
    public Event createNewEvent() {
        return MockEventUtil.createNodeGainedInterfaceEvent("Test", this);
    }

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
        @Override
    public Event createDeleteEvent() {
        return MockEventUtil.createInterfaceDeletedEvent("Test", this);
    }

	/**
	 * <p>setIfAlias</p>
	 *
	 * @param ifAlias a {@link java.lang.String} object.
	 */
	public void setIfAlias(String ifAlias) {
		// ifAlias for an interface
		m_ifAlias = ifAlias;
	}

    /**
     * <p>getIfAlias</p>
     *
     * @return Returns the ifAlias.
     */
    public String getIfAlias() {
        return m_ifAlias;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_inetAddr;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex
     */
    public void setIfIndex(int ifIndex) {
        m_ifIndex = ifIndex;
    }

    public int getIfType() {
        return m_ifType;
    }

    public void setIfType(int ifType) {
        m_ifType = ifType;
    }
}
