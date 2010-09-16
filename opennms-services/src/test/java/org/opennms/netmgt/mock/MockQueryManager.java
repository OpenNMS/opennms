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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.poller.IfKey;
import org.opennms.netmgt.poller.QueryManager;

public class MockQueryManager implements QueryManager {

    public void setDataSource(DataSource dataSource) {
        // Don't do anything because this one doesn't use the database.
    }
    
    public DataSource getDataSource() {
        return null;
    }

    /**
     * Comment for <code>m_network</code>
     */
    private final MockNetwork m_network;

    /**
     * @param network
     */
    public MockQueryManager(MockNetwork network) {
        this.m_network = network;
    }

    public boolean activeServiceExists(String whichEvent, int nodeId, String ipAddr, String serviceName) {
        return m_network.getService(nodeId, ipAddr, serviceName) != null;
    }

    public List<Integer> getActiveServiceIdsForInterface(final String ipaddr) throws SQLException {
        final Set<Integer> serviceIds = new HashSet<Integer>();

        MockVisitor gatherServices = new MockVisitorAdapter() {

            public void visitService(MockService s) {
                if (ipaddr.equals(s.getInterface().getIpAddr())) {
                    serviceIds.add(new Integer(s.getId()));
                }
            }

        };
        m_network.visit(gatherServices);

        return new ArrayList<Integer>(serviceIds);
    }

    public List<IfKey> getInterfacesWithService(final String svcName) throws SQLException {
        final List<IfKey> ifKeys = new ArrayList<IfKey>();

        MockVisitor gatherInterfaces = new MockVisitorAdapter() {

            public void visitService(MockService s) {

                if (s.getSvcName().equals(svcName)) {
                    int nodeId = s.getInterface().getNode().getNodeId();
                    String ipAddr = s.getInterface().getIpAddr();
                    ifKeys.add(new IfKey(nodeId, ipAddr));
                }
            }

        };
        m_network.visit(gatherInterfaces);

        return ifKeys;
    }

    public int getNodeIDForInterface(final String ipaddr) throws SQLException {
        return m_network.getNodeIdForInterface(ipaddr);

    }

    public String getNodeLabel(int nodeId) throws SQLException {
        MockNode node = m_network.getNode(nodeId);
        return (node == null ? null : node.getLabel());
    }

    public int getServiceCountForInterface(String ipaddr) throws SQLException {
        return getActiveServiceIdsForInterface(ipaddr).size();
    }

    public Date getServiceLostDate(int nodeId, String ipAddr, String svcName, int serviceId) {
        return null;
    }
    public void openOutage(String outageIdSQL, int nodeId, String ipAddr, String svcName, int dbid, String time) {
        // TODO Auto-generated method stub

    }
    
    
    public void resolveOutage(int nodeId, String ipAddr, String svcName, int dbid, String time) {
        // TODO Auto-generated method stub

    }
    
    
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        // TODO Auto-generated method stub

    }

    public String[] getCriticalPath(int nodeId) {
        throw new UnsupportedOperationException("MockQueryManager.getCriticalPath is not yet implemented");
    }

    public List<java.lang.String[]> getNodeServices(int nodeId) {
        return null;
    }
}
