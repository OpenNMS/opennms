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
 * <p>MacToNodeLink class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class MacToNodeLink {

	private final String macAddress;
	int nodeparentid;
	int parentifindex;

	/**
	 * <p>Constructor for MacToNodeLink.</p>
	 *
	 * @param macAddress a {@link java.lang.String} object.
	 */
	public MacToNodeLink(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		StringBuffer str = new StringBuffer("Mac Address = " + macAddress + "\n");
		str.append("Node ParentId = " + nodeparentid + "\n");
		str.append("Parent IfIndex = " + parentifindex + "\n");
		return str.toString();
	}

	/**
	 * <p>Getter for the field <code>nodeparentid</code>.</p>
	 *
	 * @return Returns the nodeparentid.
	 */
	public int getNodeparentid() {
		return nodeparentid;
	}
	/**
	 * <p>Setter for the field <code>nodeparentid</code>.</p>
	 *
	 * @param nodeparentid The nodeparentid to set.
	 */
	public void setNodeparentid(int nodeparentid) {
		this.nodeparentid = nodeparentid;
	}
	/**
	 * <p>Getter for the field <code>parentifindex</code>.</p>
	 *
	 * @return Returns the parentifindex.
	 */
	public int getParentifindex() {
		return parentifindex;
	}
	/**
	 * <p>Setter for the field <code>parentifindex</code>.</p>
	 *
	 * @param parentifindex The parentifindex to set.
	 */
	public void setParentifindex(int parentifindex) {
		this.parentifindex = parentifindex;
	}
	/**
	 * <p>Getter for the field <code>macAddress</code>.</p>
	 *
	 * @return Returns the ifindex.
	 */
	public String getMacAddress() {
		return macAddress;
	}
}
