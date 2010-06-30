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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.opennms.netmgt.model.PollStatus;
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

    private String m_ipAddr;
	private String m_ifAlias;
    private InetAddress m_inetAddr;
    private int m_ifIndex;
    

    /**
     * <p>Constructor for MockInterface.</p>
     *
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @param ipAddr a {@link java.lang.String} object.
     */
    public MockInterface(MockNode node, String ipAddr) {
        super(node);
        m_ipAddr = ipAddr;
        m_ifIndex = node.getNextIfIndex();
        try {
            m_inetAddr = InetAddress.getByName(ipAddr);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("unable to convert "+ipAddr+" to an InetAddress: "+e.getMessage());
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

    // model
    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    // impl
    Object getKey() {
        return m_ipAddr;
    }

    // model
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
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
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus getPollStatus() {
        final String critSvcName = getNetwork().getCriticalService();
        final MockService critSvc = getService(critSvcName);
        class IFStatusCalculator extends MockVisitorAdapter {
            PollStatus status = PollStatus.down();

            public PollStatus getStatus() {
                return status;
            }

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
    public String toString() {
        return "If[" + m_ipAddr + "]";
    }

    // impl
    /** {@inheritDoc} */
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
    public Event createDownEvent() {
        return MockEventUtil.createInterfaceDownEvent("Test", this);
    }

    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createUpEvent() {
        return MockEventUtil.createInterfaceUpEvent("Test", this);
    }
    
    /**
     * <p>createNewEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createNewEvent() {
        return MockEventUtil.createNodeGainedInterfaceEvent("Test", this);
    }

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
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
    

}
