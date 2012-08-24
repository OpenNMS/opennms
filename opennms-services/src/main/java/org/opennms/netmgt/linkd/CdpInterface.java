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

/*
 * Created on 9-mar-2006
 *
 * Class that holds informations for node bridges
 */
package org.opennms.netmgt.linkd;

import java.net.InetAddress;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>CdpInterface class.</p>
 *
 * @author antonio
 * @version $Id: $
 */
public class CdpInterface {
	
	/**
	 * the ip address 
	 */
	
	private final int cdpIfIndex; 
	
	InetAddress cdpTargetIpAddr;
	
	int cdpTargetNodeId;
	
	int cdpTargetIfIndex;
	
	CdpInterface(int ifindex) {
		this.cdpIfIndex = ifindex;
	}
	
	/**
	 * <p>Getter for the field <code>cdpIfIndex</code>.</p>
	 *
	 * @return Returns the cdpIfIndex.
	 */
	public int getCdpIfIndex() {
		return cdpIfIndex;
	}
	/**
	 * <p>Getter for the field <code>cdpTargetIfIndex</code>.</p>
	 *
	 * @return Returns the cdpTargetDevicePort.
	 */
	public int getCdpTargetIfIndex() {
		return cdpTargetIfIndex;
	}
	/**
	 * <p>Setter for the field <code>cdpTargetIfIndex</code>.</p>
	 *
	 * @param ifindex a int.
	 */
	public void setCdpTargetIfIndex(int ifindex) {
		this.cdpTargetIfIndex = ifindex;
	}
	/**
	 * <p>Getter for the field <code>cdpTargetIpAddr</code>.</p>
	 *
	 * @return Returns the cdpTargetIpAddr.
	 */
	public InetAddress getCdpTargetIpAddr() {
		return cdpTargetIpAddr;
	}
	/**
	 * <p>Setter for the field <code>cdpTargetIpAddr</code>.</p>
	 *
	 * @param cdpTargetIpAddr The cdpTargetIpAddr to set.
	 */
	public void setCdpTargetIpAddr(InetAddress cdpTargetIpAddr) {
		this.cdpTargetIpAddr = cdpTargetIpAddr;
	}
	/**
	 * <p>Getter for the field <code>cdpTargetNodeId</code>.</p>
	 *
	 * @return Returns the cdpTargetNodeId.
	 */
	public int getCdpTargetNodeId() {
		return cdpTargetNodeId;
	}
	/**
	 * <p>Setter for the field <code>cdpTargetNodeId</code>.</p>
	 *
	 * @param cdpTargetNodeId The cdpTargetNodeId to set.
	 */
	public void setCdpTargetNodeId(int cdpTargetNodeId) {
		this.cdpTargetNodeId = cdpTargetNodeId;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return new ToStringBuilder(this)
	                .append("ifindex",cdpIfIndex)
	                .append("TargetIpAddress",cdpTargetIpAddr)
	                .append("targetNodeid",cdpTargetNodeId)
	                .append("cdptargetIfIndex:",cdpTargetIfIndex)
	                .toString();
	} 
}
