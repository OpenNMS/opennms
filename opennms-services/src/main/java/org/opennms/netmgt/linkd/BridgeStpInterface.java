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
 * by the Free Software Foundation, either version 2 of the License,
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

/*
 * Created on 9-mar-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.linkd;

/**
 * <p>BridgeStpInterface class.</p>
 *
 * @author antonio
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
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
	 * <p>Getter for the field <code>stpPortDesignatedBridge</code>.</p>
	 *
	 * @return Returns the stpPortDesignatedBridge.
	 */
	public String getStpPortDesignatedBridge() {
		return stpPortDesignatedBridge;
	}

	/**
	 * <p>Setter for the field <code>stpPortDesignatedBridge</code>.</p>
	 *
	 * @param stpPortDesignatedBridge The stpPortDesignatedBridge to set.
	 */
	public void setStpPortDesignatedBridge(String stpPortDesignatedBridge) {
		this.stpPortDesignatedBridge = stpPortDesignatedBridge;
	}

	/**
	 * <p>Getter for the field <code>stpPortDesignatedPort</code>.</p>
	 *
	 * @return Returns the stpPortDesignatedPort.
	 */
	public String getStpPortDesignatedPort() {
		return stpPortDesignatedPort;
	}

	/**
	 * <p>Setter for the field <code>stpPortDesignatedPort</code>.</p>
	 *
	 * @param stpPortDesignatedPort The stpPortDesignatedPort to set.
	 */
	public void setStpPortDesignatedPort(String stpPortDesignatedPort) {
		this.stpPortDesignatedPort = stpPortDesignatedPort;
	}

	/**
	 * <p>Getter for the field <code>bridgeport</code>.</p>
	 *
	 * @return Returns the bridgeport.
	 */
	public int getBridgeport() {
		return bridgeport;
	}
	
	/**
	 * <p>Getter for the field <code>vlan</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getVlan() {
		return vlan;
	}
}

