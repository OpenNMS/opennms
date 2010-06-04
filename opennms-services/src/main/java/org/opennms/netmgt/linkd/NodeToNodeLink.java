/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 26, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.linkd;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class NodeToNodeLink implements Serializable {
    private static final long serialVersionUID = 1L;

    int m_nodeId;
	int m_ifIndex;
	int m_nodeParentId;
	int m_parentIfIndex;


	@SuppressWarnings("unused")
    private NodeToNodeLink() {
		throw new UnsupportedOperationException("default constructor not supported");
	}

	public NodeToNodeLink(final int nodeId, final int ifindex) {
		this.m_nodeId = nodeId;
		this.m_ifIndex = ifindex;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("Node Id = " + m_nodeId);
		str.append(" IfIndex = " + m_ifIndex);
		str.append(" Node ParentId = " + m_nodeParentId );
		str.append(" Parent IfIndex = " + m_parentIfIndex );
		return str.toString();
	}

	/**
	 * @return Returns the nodeparentid.
	 */
	public int getNodeparentid() {
		return m_nodeParentId;
	}
	/**
	 * @param nodeParentId The nodeparentid to set.
	 */
	public void setNodeparentid(final int nodeParentId) {
		this.m_nodeParentId = nodeParentId;
	}
	/**
	 * @return Returns the parentifindex.
	 */
	public int getParentifindex() {
		return m_parentIfIndex;
	}
	/**
	 * @param parentIfIndex The parentifindex to set.
	 */
	public void setParentifindex(final int parentIfIndex) {
		this.m_parentIfIndex = parentIfIndex;
	}
	/**
	 * @return Returns the ifindex.
	 */
	public int getIfindex() {
		return m_ifIndex;
	}
	/**
	 * @return
	 */
	public int getNodeId() {
		return m_nodeId;
	}

	@Override
	public int hashCode() {
	    return new HashCodeBuilder(17, 57)
	        .append(m_nodeId)
	        .append(m_ifIndex)
	        .append(m_nodeParentId)
	        .append(m_parentIfIndex)
	        .toHashCode();
	}

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