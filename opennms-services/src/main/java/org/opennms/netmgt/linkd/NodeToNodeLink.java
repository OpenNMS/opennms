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

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class NodeToNodeLink {

	int nodeId;
	int ifindex;
	int nodeparentid;
	int parentifindex;


	@SuppressWarnings("unused")
    private NodeToNodeLink() {
		throw new UnsupportedOperationException("default constructor not supported");
	}

	public NodeToNodeLink(int nodeId, int ifindex) {
		this.nodeId = nodeId;
		this.ifindex = ifindex;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Node Id = " + nodeId);
		str.append(" IfIndex = " + ifindex);
		str.append(" Node ParentId = " + nodeparentid );
		str.append(" Parent IfIndex = " + parentifindex );
		return str.toString();
	}

	/**
	 * @return Returns the nodeparentid.
	 */
	public int getNodeparentid() {
		return nodeparentid;
	}
	/**
	 * @param nodeparentid The nodeparentid to set.
	 */
	public void setNodeparentid(int nodeparentid) {
		this.nodeparentid = nodeparentid;
	}
	/**
	 * @return Returns the parentifindex.
	 */
	public int getParentifindex() {
		return parentifindex;
	}
	/**
	 * @param parentifindex The parentifindex to set.
	 */
	public void setParentifindex(int parentifindex) {
		this.parentifindex = parentifindex;
	}
	/**
	 * @return Returns the ifindex.
	 */
	public int getIfindex() {
		return ifindex;
	}
	/**
	 * @return
	 */
	public int getNodeId() {
		return nodeId;
	}
	
	public boolean equals(NodeToNodeLink nodelink) {
		if (this.nodeId == nodelink.getNodeId() && 
			this.ifindex == nodelink.getIfindex()	&&
			this.nodeparentid == nodelink.getNodeparentid() &&
			this.parentifindex == nodelink.getParentifindex()) return true;

		if (this.nodeId == nodelink.getNodeparentid() && 
			this.ifindex == nodelink.getParentifindex()	&&
			this.nodeparentid == nodelink.getNodeId() &&
			this.parentifindex == nodelink.getIfindex()) return true;
		
		return false;

	}
 
}