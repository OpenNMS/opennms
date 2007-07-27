//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on 9-mar-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.linkd;

/**
 * @author antonio
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BridgeStpInterface {

	int bridgeport;
	
	String vlan;

	String stpPortDesignatedPort;

	String stpPortDesignatedBridge;

	BridgeStpInterface(int bridgeport, String vlan) {
		this.bridgeport = bridgeport;
		this.vlan = vlan;
	}

	/**
	 * @return Returns the stpPortDesignatedBridge.
	 */
	public String getStpPortDesignatedBridge() {
		return stpPortDesignatedBridge;
	}

	/**
	 * @param stpPortDesignatedBridge The stpPortDesignatedBridge to set.
	 */
	public void setStpPortDesignatedBridge(String stpPortDesignatedBridge) {
		this.stpPortDesignatedBridge = stpPortDesignatedBridge;
	}

	/**
	 * @return Returns the stpPortDesignatedPort.
	 */
	public String getStpPortDesignatedPort() {
		return stpPortDesignatedPort;
	}

	/**
	 * @param stpPortDesignatedPort The stpPortDesignatedPort to set.
	 */
	public void setStpPortDesignatedPort(String stpPortDesignatedPort) {
		this.stpPortDesignatedPort = stpPortDesignatedPort;
	}

	/**
	 * @return Returns the bridgeport.
	 */
	public int getBridgeport() {
		return bridgeport;
	}
	
	public String getVlan() {
		return vlan;
	}
}

