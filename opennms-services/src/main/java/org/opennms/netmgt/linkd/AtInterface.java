//
// Copyright (C) 2002 Sortova Consulting Group, Inc. All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

package org.opennms.netmgt.linkd;

public class AtInterface {

	int nodeid;
	int ifindex;
	String macAddress;
	String ipAddress;


	private AtInterface() {
		throw new UnsupportedOperationException(
		"default constructor not supported");
	}

	public AtInterface(int nodeid, String ipAddress, String macAddress) {
		this.nodeid = nodeid;
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.ifindex=-1;
	}

	public AtInterface(int nodeid, String ipAddress) {
		this.nodeid = nodeid;
		this.macAddress = "";
		this.ipAddress = ipAddress;
		this.ifindex=-1;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Mac Address = " + macAddress + "\n");
		str.append("Node Id = " + nodeid + "\n");
		str.append("Ip Address = " + ipAddress + "\n");
		str.append("IfIndex = " + ifindex + "\n");
		return str.toString();
	}

	/**
	 * @return Returns the nodeparentid.
	 */
	public int getNodeId() {
		return nodeid;
	}
	/**
	 * @return Returns the ifindex.
	 */
	public String getMacAddress() {
		return macAddress;
	}
	
	/**
	 * @return Returns the ifindex.
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	public int getIfindex() {
		return ifindex;
	}

	public void setIfindex(int ifindex) {
		this.ifindex = ifindex;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

}