//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.poller.QueryManager;
import org.opennms.netmgt.xml.event.Event;

/**
 * A test network configuration
 * 
 * @author brozow
 * 
 */
public class MockNetwork extends MockContainer {

    private MockInterface m_currentInterface;

    private MockNode m_currentNode;

    private long m_defaultInterval = 1234L;

    private MockEventIpcManager m_eventMgr = new MockEventIpcManager();

    private Map m_idToNameMap = new HashMap();

    private int m_invalidPollCount;

    private Map m_nameToIdMap = new HashMap();

    private int m_nextServiceId = 1;

    private MockPollerConfig m_pollerConfig = new MockPollerConfig();

    private MockQueryManager m_queryMgr = new MockQueryManager(this);

    public MockNetwork() {
        super(null);
    }

    public void addDowntime(long interval, long begin, long end, boolean delete) {
        m_pollerConfig.addDowntime(interval, begin, end, delete);
    }

    public MockInterface addInterface(int nodeId, String ipAddr) {
        return getNode(nodeId).addInterface(ipAddr);
    }

    /**
     * @param string
     */
    public MockInterface addInterface(String ipAddr) {
        m_currentInterface = m_currentNode.addInterface(ipAddr);
        return m_currentInterface;
    }

    /**
     * @param i
     * @param string
     */
    public MockNode addNode(int nodeid, String label) {
        m_currentNode = (MockNode) addMember(new MockNode(this, nodeid, label));
        return m_currentNode;
    }

    /**
     * @param string
     * @param begin1
     * @param end1
     * @param string2
     */
    public void addOutage(String outageName, long begin, long end, String ipAddr) {
        m_pollerConfig.addOutage(outageName, begin, end, ipAddr);
    }

    /**
     * @param string
     */
    public void addPackage(String pkgName) {
        m_pollerConfig.createPackage(pkgName);
    }

    public MockService addService(int nodeId, String ipAddr, String svcName) {
        if (!m_pollerConfig.hasService(svcName))
            m_pollerConfig.addService(svcName, m_defaultInterval, new MockMonitor(this, svcName));

        int serviceId = getServiceId(svcName);
        return getInterface(nodeId, ipAddr).addService(svcName, serviceId);
    }

    /**
     * @param string
     */
    public MockService addService(String svcName) {
        if (!m_pollerConfig.hasService(svcName))
            m_pollerConfig.addService(svcName, m_defaultInterval, new MockMonitor(this, svcName));

        int serviceId = getServiceId(svcName);
        return m_currentInterface.addService(svcName, serviceId);

    }

    public void clearDowntime() {
        m_pollerConfig.clearDowntime();
    }

    /**
     * @return
     */
    public String getCriticalService() {
        return m_pollerConfig.getCriticalService();
    }

    /**
     * @return
     */
    public EventAnticipator getEventAnticipator() {
        return m_eventMgr.getEventAnticipator();
    }

    /**
     * @return
     */
    public EventIpcManager getEventMgr() {
        return m_eventMgr;
    }

    /**
     * @return
     */
    public Map getIdToNameMap() {
        return Collections.unmodifiableMap(m_idToNameMap);
    }

    /**
     * @param nodeid
     * @param string
     * @return
     */
    public MockInterface getInterface(int nodeid, String ipAddr) {
        MockNode node = getNode(nodeid);
        return (node == null ? null : node.getInterface(ipAddr));
    }

    /**
     * @return
     */
    public int getInvalidPollCount() {
        return m_invalidPollCount;
    }

    Object getKey() {
        return this;
    }

    /**
     * @return
     */
    public Map getNameToIdMap() {
        return Collections.unmodifiableMap(m_nameToIdMap);
    }

    /**
     * @param i
     * @return
     */
    public MockNode getNode(int i) {
        return (MockNode) getMember(new Integer(i));
    }

    /**
     * @param manager
     * @param ipAddr
     * @return
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
        ;
        NodeFinder finder = new NodeFinder();
        visit(finder);
        return finder.getNode() == null ? -1 : finder.getNode().getNodeId();
    }

    /**
     * @param string
     * @return
     */
    public Package getPackage(String name) {
        return m_pollerConfig.getPackage(name);
    }

    /**
     * @return
     */
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * @return
     */
    public PollOutagesConfig getPollOutagesConfig() {
        return m_pollerConfig;
    }

    /**
     * @return
     */
    public QueryManager getQueryManager() {
        return m_queryMgr;
    }

    /**
     * @param string
     * @param string2
     * @return
     */
    public MockService getService(int nodeid, String ipAddr, String svcName) {
        MockInterface iface = getInterface(nodeid, ipAddr);
        return (iface == null ? null : iface.getService(svcName));
    }

    /**
     * @param svcName
     * @return
     */
    private int getServiceId(String svcName) {
        int serviceId;
        if (m_nameToIdMap.containsKey(svcName)) {
            serviceId = ((Integer) m_nameToIdMap.get(svcName)).intValue();
        } else {
            serviceId = m_nextServiceId++;
            Integer serviceIdObj = new Integer(serviceId);
            m_nameToIdMap.put(svcName, serviceIdObj);
            m_idToNameMap.put(serviceIdObj, svcName);
        }
        return serviceId;
    }

    public void receivedInvalidPoll(String ipAddr, String svcName) {
        m_invalidPollCount++;
    }

    /**
     * @param iface
     */
    public void removeElement(MockElement element) {
        MockContainer parent = element.getParent();
        parent.removeMember(element);
    }

    public void removeInterface(MockInterface iface) {
        removeElement(iface);
    }

    public void removeNode(MockNode node) {
        removeElement(node);
    }

    public void removeService(MockService svc) {
        removeElement(svc);
    }

    /**
     * 
     */
    public void resetInvalidPollCount() {
        m_invalidPollCount = 0;
    }

    /**
     * @param event
     */
    public void sendEventToListeners(Event event) {

        m_eventMgr.sendEventToListeners(event);
    }

    /**
     * @param svcName
     */
    public void setCriticalService(String svcName) {
        m_pollerConfig.setCriticalService(svcName);

    }

    /**
     * @param l
     */
    public void setDefaultPollInterval(long defaultInterval) {
        m_defaultInterval = defaultInterval;
    }

    /**
     * @param b
     */
    public void setNodeOutageProcessingEnabled(boolean b) {
        m_pollerConfig.setOutageProcessingEnabled(b);
    }

    /**
     * @param threadCount
     */
    public void setPollerThreads(int threadCount) {
        m_pollerConfig.setThreads(threadCount);

    }

    /**
     * @param string
     * @param l
     */
    public void setPollInterval(String svcName, long interval) {
        if (m_pollerConfig.hasService(svcName))
            m_pollerConfig.setPollInterval(svcName, interval);
        else
            m_pollerConfig.addService(svcName, interval, new MockMonitor(this, svcName));

    }

    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitNetwork(this);
        visitMembers(v);
    }

}
