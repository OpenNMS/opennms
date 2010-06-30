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
// Modifications:
//
// 2008 Feb 09: Use Java 5 generics. - dj@opennms.org
// 2008 Jan 27: Move createStandardNetwork from OpenNMSTestCase to here. - dj@opennms.org
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.xml.event.Event;

/**
 * A test network configuration
 *
 * @author brozow
 * @version $Id: $
 */
public class MockNetwork extends MockContainer {

    /**
     * <p>createDownEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createDownEvent() {
        throw new UnsupportedOperationException("Cannot generate down event for the network");
    }
    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createUpEvent() {
        throw new UnsupportedOperationException("Cannot generate up event for the network");
    }
    /**
     * <p>createNewEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createNewEvent() {
        throw new UnsupportedOperationException("Cannot generate new event for the network");
    }
    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createDeleteEvent() {
        throw new UnsupportedOperationException("Cannot generate delete event for the network");
    }
    private MockInterface m_currentInterface;

    private MockNode m_currentNode;

    private Map<Integer, String> m_idToNameMap = new HashMap<Integer, String>();

    private int m_invalidPollCount;

    private Map<String, Integer> m_nameToIdMap = new HashMap<String, Integer>();
    
    private String m_criticalService;
	
    private String m_ifAlias;

    private int m_nextServiceId = 1;

    /**
     * <p>Constructor for MockNetwork.</p>
     */
    public MockNetwork() {
        super(null);
        m_criticalService = "ICMP";
    }
    
    /**
     * <p>getCriticalService</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCriticalService() {
        return m_criticalService;
    }
    
    /**
     * <p>setCriticalService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void setCriticalService(String svcName) {
        m_criticalService = svcName;
    }
    
    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return m_ifAlias;
    }
    
    /**
     * <p>setIfAlias</p>
     *
     * @param ifAlias a {@link java.lang.String} object.
     */
    public void setIfAlias(String ifAlias) {
        m_currentInterface.setIfAlias(ifAlias);
    }

    // model
    /**
     * <p>addInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public MockInterface addInterface(int nodeId, String ipAddr) {
        return getNode(nodeId).addInterface(ipAddr);
    }

    // model
    /**
     * <p>addInterface</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public MockInterface addInterface(String ipAddr) {
        m_currentInterface = m_currentNode.addInterface(ipAddr);
        return m_currentInterface;
    }

    // model
    /**
     * <p>addNode</p>
     *
     * @param nodeid a int.
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockNode} object.
     */
    public MockNode addNode(int nodeid, String label) {
        m_currentNode = (MockNode) addMember(new MockNode(this, nodeid, label));
        return m_currentNode;
    }

    // model 
    /**
     * <p>addService</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public MockService addService(int nodeId, String ipAddr, String svcName) {
        int serviceId = getServiceId(svcName);
        return getInterface(nodeId, ipAddr).addService(svcName, serviceId);
    }

    // model
    /**
     * <p>addService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public MockService addService(String svcName) {
        int serviceId = getServiceId(svcName);
        return m_currentInterface.addService(svcName, serviceId);

    }

    // model
    /**
     * <p>getIdToNameMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, String> getIdToNameMap() {
        return Collections.unmodifiableMap(m_idToNameMap);
    }

    // model
    /**
     * <p>getInterface</p>
     *
     * @param nodeid a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public MockInterface getInterface(int nodeid, String ipAddr) {
        MockNode node = getNode(nodeid);
        return (node == null ? null : node.getInterface(ipAddr));
    }

    // stats
    /**
     * <p>getInvalidPollCount</p>
     *
     * @return a int.
     */
    public int getInvalidPollCount() {
        return m_invalidPollCount;
    }

    // impl
    Object getKey() {
        return this;
    }

    // model
    /**
     * <p>getNameToIdMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Integer> getNameToIdMap() {
        return Collections.unmodifiableMap(m_nameToIdMap);
    }

    // model
    /**
     * <p>getNode</p>
     *
     * @param i a int.
     * @return a {@link org.opennms.netmgt.mock.MockNode} object.
     */
    public MockNode getNode(int i) {
        return (MockNode) getMember(new Integer(i));
    }

    // model
    /**
     * <p>getNodeIdForInterface</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a int.
     */
    public int getNodeIdForInterface(final String ipAddr) {
        class NodeFinder extends MockVisitorAdapter {
            MockNode node;

            public MockNode getNode() {
                return node;
            }

            public void visitInterface(MockInterface iface) {
                if (iface.getIpAddr().equals(ipAddr)) {
                    node = iface.getNode();
                }
            }

        }

        NodeFinder finder = new NodeFinder();
        visit(finder);
        return finder.getNode() == null ? -1 : finder.getNode().getNodeId();
    }

    // model
    /**
     * <p>getService</p>
     *
     * @param nodeid a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public MockService getService(int nodeid, String ipAddr, String svcName) {
        MockInterface iface = getInterface(nodeid, ipAddr);
        return (iface == null ? null : iface.getService(svcName));
    }

    // model
    private int getServiceId(String svcName) {
        int serviceId;
        if (m_nameToIdMap.containsKey(svcName)) {
            serviceId = m_nameToIdMap.get(svcName).intValue();
        } else {
            serviceId = m_nextServiceId++;
            Integer serviceIdObj = new Integer(serviceId);
            m_nameToIdMap.put(svcName, serviceIdObj);
            m_idToNameMap.put(serviceIdObj, svcName);
        }
        return serviceId;
    }

    // stats
    /**
     * <p>receivedInvalidPoll</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void receivedInvalidPoll(String ipAddr, String svcName) {
        m_invalidPollCount++;
    }

    // model
    /**
     * <p>removeElement</p>
     *
     * @param element a {@link org.opennms.netmgt.mock.MockElement} object.
     */
    public synchronized void removeElement(MockElement element) {
        MockContainer parent = element.getParent();
        parent.removeMember(element);
    }

    // model
    /**
     * <p>removeInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public void removeInterface(MockInterface iface) {
        removeElement(iface);
    }

    // model
    /**
     * <p>removeNode</p>
     *
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     */
    public void removeNode(MockNode node) {
        removeElement(node);
    }

    // model
    /**
     * <p>removeService</p>
     *
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public void removeService(MockService svc) {
        removeElement(svc);
    }

    // stats
    /**
     * <p>resetInvalidPollCount</p>
     */
    public void resetInvalidPollCount() {
        m_invalidPollCount = 0;
    }

    // impl
    /** {@inheritDoc} */
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitNetwork(this);
        visitMembers(v);
    }
    
    /**
     * <p>getNodeCount</p>
     *
     * @return a int.
     */
    public int getNodeCount() {
        class NodeCounter extends MockVisitorAdapter {
            int count = 0;
            public void visitNode(MockNode node) {
                count++;
            }
            public int getCount() {
                return count;
            }
        }
        NodeCounter counter = new NodeCounter();
        visit(counter);
        return counter.getCount();
    }

    /**
     * <p>getInterfaceCount</p>
     *
     * @return a int.
     */
    public int getInterfaceCount() {
        class InterfaceCounter extends MockVisitorAdapter {
            int count = 0;
            public void visitInterface(MockInterface iface) {
                count++;
            }
            public int getCount() {
                return count;
            }
        }
        InterfaceCounter counter = new InterfaceCounter();
        visit(counter);
        return counter.getCount();
    }

    /**
     * <p>getServiceCount</p>
     *
     * @return a int.
     */
    public int getServiceCount() {
        class ServiceCounter extends MockVisitorAdapter {
            int count = 0;
            public void visitService(MockService svc) {
                count++;
            }
            public int getCount() {
                return count;
            }
        }
        ServiceCounter counter = new ServiceCounter();
        visit(counter);
        return counter.getCount();
    }

    /**
     * <p>createStandardNetwork</p>
     */
    public void createStandardNetwork() {
        setCriticalService("ICMP");
        addNode(1, "Router");
        addInterface("192.168.1.1");
        setIfAlias("dot1 interface alias");
        addService("ICMP");
        addService("SMTP");
        addInterface("192.168.1.2");
        setIfAlias("dot2 interface alias");
        addService("ICMP");
        addService("SMTP");
        addNode(2, "Server");
        addInterface("192.168.1.3");
        setIfAlias("dot3 interface alias");
        addService("ICMP");
        addService("HTTP");
        addNode(3, "Firewall");
        addInterface("192.168.1.4");
        addService("SMTP");
        addService("HTTP");
        addInterface("192.168.1.5");
        addService("SMTP");
        addService("HTTP");
    }

}
