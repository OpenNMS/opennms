/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.poller.QueryManager;

public class MockQueryManager implements QueryManager {

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

    boolean activeServiceExists(String whichEvent, int nodeId, String ipAddr, String serviceName) {
        return m_network.getService(nodeId, ipAddr, serviceName) != null;
    }

    List<Integer> getActiveServiceIdsForInterface(final String ipaddr) throws SQLException {
        final Set<Integer> serviceIds = new HashSet<Integer>();

        MockVisitor gatherServices = new MockVisitorAdapter() {

            @Override
            public void visitService(MockService s) {
                if (ipaddr.equals(s.getInterface().getIpAddr())) {
                    serviceIds.add(Integer.valueOf(s.getId()));
                }
            }

        };
        m_network.visit(gatherServices);

        return new ArrayList<Integer>(serviceIds);
    }

    List<IfKey> getInterfacesWithService(final String svcName) throws SQLException {
        final List<IfKey> ifKeys = new ArrayList<IfKey>();

        MockVisitor gatherInterfaces = new MockVisitorAdapter() {

            @Override
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

    int getNodeIDForInterface(final String ipaddr) throws SQLException {
        return m_network.getNodeIdForInterface(ipaddr);
    }

    @Override
    public String getNodeLabel(int nodeId) throws SQLException {
        MockNode node = m_network.getNode(nodeId);
        return (node == null ? null : node.getLabel());
    }

    int getServiceCountForInterface(String ipaddr) throws SQLException {
        return getActiveServiceIdsForInterface(ipaddr).size();
    }

    @Override
    public Integer openOutagePendingLostEventId(int nodeId, String ipAddr,
            String svcName, Date lostTime) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateOpenOutageWithEventId(int outageId, int lostEventId) {
        // TODO Auto-generated method stub
    }

    @Override
    public Integer resolveOutagePendingRegainEventId(int nodeId, String ipAddr,
            String svcName, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateResolvedOutageWithEventId(int outageId,
            int regainedEventId) {
        // TODO Auto-generated method stub
    }

    @Override
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<java.lang.String[]> getNodeServices(int nodeId) {
        return null;
    }

	@Override
	public void closeOutagesForUnmanagedServices() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeOutagesForNode(Date closeDate, int eventId, int nodeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeOutagesForInterface(Date closeDate, int eventId,
			int nodeId, String ipAddr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeOutagesForService(Date closeDate, int eventId, int nodeId,
			String ipAddr, String serviceName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateServiceStatus(int nodeId, String ipAddr,
			String serviceName, String status) {
		// TODO Auto-generated method stub
		
	}

}
