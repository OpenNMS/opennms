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
package org.opennms.netmgt.model;

/**
 * <p>PathOutage class.</p>
 */
public class PathOutage {
	private int nodeId;
	private String criticalPathIp;
	private String criticalPathServiceName;
	
	//Operations
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(50);
		result.append("pathOutage { nodeID: ");
		result.append(nodeId);
		result.append(", criticalPathIp: ");
		result.append(criticalPathIp);
		result.append(", criticalPathServiceName: ");
		result.append(criticalPathServiceName);
		result.append(" }");
		return result.toString();
	}

	/**
	 * <p>Getter for the field <code>criticalPathIp</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCriticalPathIp() {
		return criticalPathIp;
	}

	/**
	 * <p>Setter for the field <code>criticalPathIp</code>.</p>
	 *
	 * @param criticalPathIp a {@link java.lang.String} object.
	 */
	public void setCriticalPathIp(String criticalPathIp) {
		this.criticalPathIp = criticalPathIp;
	}

	/**
	 * <p>Getter for the field <code>criticalPathServiceName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCriticalPathServiceName() {
		return criticalPathServiceName;
	}

	/**
	 * <p>Setter for the field <code>criticalPathServiceName</code>.</p>
	 *
	 * @param criticalPathServiceName a {@link java.lang.String} object.
	 */
	public void setCriticalPathServiceName(String criticalPathServiceName) {
		this.criticalPathServiceName = criticalPathServiceName;
	}

	/**
	 * <p>Getter for the field <code>nodeId</code>.</p>
	 *
	 * @return a int.
	 */
	public int getNodeId() {
		return nodeId;
	}

	/**
	 * <p>Setter for the field <code>nodeId</code>.</p>
	 *
	 * @param nodeId a int.
	 */
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
}
