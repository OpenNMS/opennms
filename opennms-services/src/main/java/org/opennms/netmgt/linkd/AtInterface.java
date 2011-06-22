/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

/**
 * <p>AtInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class AtInterface {

	int nodeid;
	int ifindex;
	String macAddress;
	String ipAddress;


	AtInterface() {
		throw new UnsupportedOperationException(
		"default constructor not supported");
	}

	/**
	 * <p>Constructor for AtInterface.</p>
	 *
	 * @param nodeid a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param macAddress a {@link java.lang.String} object.
	 */
	public AtInterface(int nodeid, String ipAddress, String macAddress) {
		this.nodeid = nodeid;
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.ifindex=-1;
	}

	/**
	 * <p>Constructor for AtInterface.</p>
	 *
	 * @param nodeid a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 */
	public AtInterface(int nodeid, String ipAddress) {
		this.nodeid = nodeid;
		this.macAddress = "";
		this.ipAddress = ipAddress;
		this.ifindex=-1;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		StringBuffer str = new StringBuffer("Mac Address = " + macAddress + "\n");
		str.append("Node Id = " + nodeid + "\n");
		str.append("Ip Address = " + ipAddress + "\n");
		str.append("IfIndex = " + ifindex + "\n");
		return str.toString();
	}

	/**
	 * <p>getNodeId</p>
	 *
	 * @return Returns the nodeparentid.
	 */
	public int getNodeId() {
		return nodeid;
	}
	/**
	 * <p>Getter for the field <code>macAddress</code>.</p>
	 *
	 * @return Returns the ifindex.
	 */
	public String getMacAddress() {
		return macAddress;
	}
	
	/**
	 * <p>Getter for the field <code>ipAddress</code>.</p>
	 *
	 * @return Returns the ifindex.
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * <p>Getter for the field <code>ifindex</code>.</p>
	 *
	 * @return a int.
	 */
	public int getIfindex() {
		return ifindex;
	}

	/**
	 * <p>Setter for the field <code>ifindex</code>.</p>
	 *
	 * @param ifindex a int.
	 */
	public void setIfindex(int ifindex) {
		this.ifindex = ifindex;
	}

	/**
	 * <p>Setter for the field <code>macAddress</code>.</p>
	 *
	 * @param macAddress a {@link java.lang.String} object.
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

}
