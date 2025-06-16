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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.QueryManager;
import org.opennms.netmgt.poller.pollables.PollableService;

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
        final Set<Integer> serviceIds = new HashSet<>();

        MockVisitor gatherServices = new MockVisitorAdapter() {

            @Override
            public void visitService(MockService s) {
                if (ipaddr.equals(s.getInterface().getIpAddr())) {
                    serviceIds.add(Integer.valueOf(s.getSvcId()));
                }
            }

        };
        m_network.visit(gatherServices);

        return new ArrayList<Integer>(serviceIds);
    }

    List<IfKey> getInterfacesWithService(final String svcName) throws SQLException {
        final List<IfKey> ifKeys = new ArrayList<>();

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

    @Override
    public String getNodeLocation(int nodeId) {
        MockNode node = m_network.getNode(nodeId);
        return (node == null ? null : node.getLocation());
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

    @Override
    public void updateLastGoodOrFail(PollableService pollableService, PollStatus status) {
        // pass
    }

}
