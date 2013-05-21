/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
		StringBuffer result = new StringBuffer(50);
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
