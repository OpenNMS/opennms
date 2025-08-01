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
package org.opennms.features.topology.plugins.topo.pathoutage;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * This class with is responsible for generation of test {@link OnmsNode} objects
 */
class TestNodeGenerator {
	/**
	 * This method generates 9 {@link OnmsNode} objects for testing of
	 * {@link PathOutageProvider} and {@link PathOutageStatusProvider}
	 * @param location {@link OnmsMonitoringLocation} or null
	 * @return Resulting map - values are nodes' levels in the path outage hierarchy
	 */
	public static Map<OnmsNode, Integer> generateNodes(OnmsMonitoringLocation location) {
		Map<OnmsNode, Integer> nodesMap = new HashMap<>();

		OnmsNode node0 = createNode(1, "Node-0", location, null);
		nodesMap.put(node0, 0);

		OnmsNode node0_0 = createNode(2, "Node-0-0", location, node0);
		nodesMap.put(node0_0, 1);

		OnmsNode node0_1 = createNode(3, "Node-0-1", location, node0);
		nodesMap.put(node0_1, 1);

		OnmsNode node0_1_0 = createNode(4, "Node-0-1-0", location, node0_1);
		nodesMap.put(node0_1_0, 2);

		OnmsNode node0_1_1 = createNode(5, "Node-0-1-1", location, node0_1);
		nodesMap.put(node0_1_1, 2);

		OnmsNode node1 = createNode(6, "Node-1", location, null);
		nodesMap.put(node1, 0);

		OnmsNode node2 = createNode(7, "Node-2", location, null);
		nodesMap.put(node2, 0);

		OnmsNode node2_0 = createNode(8, "Node-2-0", location, node2);
		nodesMap.put(node2_0, 1);

		OnmsNode node2_1 = createNode(9, "Node-3", location, node2_0);
		nodesMap.put(node2_1, 2);

		return nodesMap;
	}

	/**
	 * This method generates an {@link OnmsNode} with specified parameters
	 * @param id ID
	 * @param label Label
	 * @param location Location (can be null)
	 * @param parent Parent node (can be null)
	 * @return Resulting node
	 */
	private static OnmsNode createNode(int id, String label, OnmsMonitoringLocation location, OnmsNode parent) {
		OnmsNode node = new OnmsNode();
		node.setId(id);
		node.setLabel(label);
		if (parent != null) {
			node.setParent(parent);
		}
		if (location != null) {
			node.setLocation(location);
		}
		return node;
	}
}