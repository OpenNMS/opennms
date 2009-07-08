/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

/*
 * Created on 9-mar-2006
 *
 * Class that holds informations for node bridges
 */
package org.opennms.netmgt.linkd;

import java.net.InetAddress;

/**
 * @author antonio
 *
 */

public class CdpInterface {
	
	/**
	 * the ip address 
	 */
	
	int  cdpIfIndex; 
	
	InetAddress cdpTargetIpAddr;
	
	int cdpTargetNodeId;
	
	int cdpTargetIfIndex;
	
	CdpInterface(int ifindex) {
		this.cdpIfIndex = ifindex;
	}
	
	/**
	 * @return Returns the cdpIfIndex.
	 */
	public int getCdpIfIndex() {
		return cdpIfIndex;
	}
	/**
	 * @return Returns the cdpTargetDevicePort.
	 */
	public int getCdpTargetIfIndex() {
		return cdpTargetIfIndex;
	}
	/**
	 * @param cdpTargetDevicePort The cdpTargetDevicePort to set.
	 */
	public void setCdpTargetIfIndex(int ifindex) {
		this.cdpTargetIfIndex = ifindex;
	}
	/**
	 * @return Returns the cdpTargetIpAddr.
	 */
	public InetAddress getCdpTargetIpAddr() {
		return cdpTargetIpAddr;
	}
	/**
	 * @param cdpTargetIpAddr The cdpTargetIpAddr to set.
	 */
	public void setCdpTargetIpAddr(InetAddress cdpTargetIpAddr) {
		this.cdpTargetIpAddr = cdpTargetIpAddr;
	}
	/**
	 * @return Returns the cdpTargetNodeId.
	 */
	public int getCdpTargetNodeId() {
		return cdpTargetNodeId;
	}
	/**
	 * @param cdpTargetNodeId The cdpTargetNodeId to set.
	 */
	public void setCdpTargetNodeId(int cdpTargetNodeId) {
		this.cdpTargetNodeId = cdpTargetNodeId;
	}
	
	public String toString() {
		return "ifindex:"+cdpIfIndex+"TargetIpAddress:"+cdpTargetIpAddr+"targetNodeid:"
				+cdpTargetNodeId+"cdptargetIfIndex:"+cdpTargetIfIndex;
	} 
}
