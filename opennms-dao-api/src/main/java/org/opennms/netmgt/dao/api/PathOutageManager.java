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
package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface PathOutageManager {

	/** Constant <code>NO_CRITICAL_PATH="Not Configured"</code> */
	static final String NO_CRITICAL_PATH = "Not Configured";

	List<String[]> getAllCriticalPaths() throws SQLException;

	String getPrettyCriticalPath(int nodeID) throws SQLException;

	CriticalPath getCriticalPath(int nodeId);

	Set<Integer> getNodesInPath(String criticalPathIp, String criticalPathServiceName) throws SQLException;

	String[] getLabelAndStatus(String nodeIDStr, Connection conn) throws SQLException;

	String[] getCriticalPathData(String criticalPathIp, String criticalPathServiceName) throws SQLException;

	/**
	 * This method is used when you are scheduling an outage for an interface so that you can have
	 * the choice of also extending the outage to all nodes that are dependent on that interface for
	 * connectivity.
	 * 
	 * @param criticalpathip IP address of the interface whose outages would affect other nodes
	 * @return List of node IDs that would be impacted by an outage on the specified interface
	 * @throws SQLException
	 */
	Set<Integer> getAllNodesDependentOnAnyServiceOnInterface(String criticalpathip) throws SQLException;

	/**
	 * This method is used when you are scheduling an outage for an entire node so that you can have
	 * the choice of also extending the outage to all nodes that are dependent on that node for
	 * connectivity.
	 * 
	 * @param nodeId ID of the node whose outages would affect other nodes
	 * @return List of node IDs that would be impacted by an outage on the specified node
	 * @throws SQLException
	 */
	Set<Integer> getAllNodesDependentOnAnyServiceOnNode(int nodeId) throws SQLException;

	InetAddress getDefaultCriticalPathIp();
}
