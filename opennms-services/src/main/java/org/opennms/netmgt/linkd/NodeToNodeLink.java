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

package org.opennms.netmgt.linkd;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * <p>NodeToNodeLink class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class NodeToNodeLink {

    final int m_nodeId;
    final int m_ifIndex;
	int m_nodeParentId;
	int m_parentIfIndex;


	/**
	 * <p>Constructor for NodeToNodeLink.</p>
	 *
	 * @param nodeId a int.
	 * @param ifindex a int.
	 */
	public NodeToNodeLink(final int nodeId, final int ifindex) {
		this.m_nodeId = nodeId;
		this.m_ifIndex = ifindex;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("Node Id = " + m_nodeId);
		str.append(" IfIndex = " + m_ifIndex);
		str.append(" Node ParentId = " + m_nodeParentId );
		str.append(" Parent IfIndex = " + m_parentIfIndex );
		return str.toString();
	}

	/**
	 * <p>getNodeparentid</p>
	 *
	 * @return Returns the nodeparentid.
	 */
	public int getNodeparentid() {
		return m_nodeParentId;
	}
	/**
	 * <p>setNodeparentid</p>
	 *
	 * @param nodeParentId The nodeparentid to set.
	 */
	public void setNodeparentid(final int nodeParentId) {
		this.m_nodeParentId = nodeParentId;
	}
	/**
	 * <p>getParentifindex</p>
	 *
	 * @return Returns the parentifindex.
	 */
	public int getParentifindex() {
		return m_parentIfIndex;
	}
	/**
	 * <p>setParentifindex</p>
	 *
	 * @param parentIfIndex The parentifindex to set.
	 */
	public void setParentifindex(final int parentIfIndex) {
		this.m_parentIfIndex = parentIfIndex;
	}
	/**
	 * <p>getIfindex</p>
	 *
	 * @return Returns the ifindex.
	 */
	public int getIfindex() {
		return m_ifIndex;
	}
	/**
	 * <p>getNodeId</p>
	 *
	 * @return a int.
	 */
	public int getNodeId() {
		return m_nodeId;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
	    return new HashCodeBuilder(17, 57)
	        .append(m_nodeId)
	        .append(m_ifIndex)
	        .append(m_nodeParentId)
	        .append(m_parentIfIndex)
	        .toHashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
	    if (o == null) return false;
	    if (!(o instanceof NodeToNodeLink)) return false;
	    final NodeToNodeLink nodelink = (NodeToNodeLink)o;

		if (this.m_nodeId == nodelink.getNodeId() && 
			this.m_ifIndex == nodelink.getIfindex()	&&
			this.m_nodeParentId == nodelink.getNodeparentid() &&
			this.m_parentIfIndex == nodelink.getParentifindex()) return true;

		if (this.m_nodeId == nodelink.getNodeparentid() && 
			this.m_ifIndex == nodelink.getParentifindex()	&&
			this.m_nodeParentId == nodelink.getNodeId() &&
			this.m_parentIfIndex == nodelink.getIfindex()) return true;
		
		return false;

	}
 
}
