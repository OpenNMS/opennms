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
 * Class that holds informations for node bridges
 */
package org.opennms.netmgt.linkd;

import java.net.InetAddress;

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
	
	int  cdpIfIndex; 
	
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
		return "ifindex:"+cdpIfIndex+"TargetIpAddress:"+cdpTargetIpAddr+"targetNodeid:"
				+cdpTargetNodeId+"cdptargetIfIndex:"+cdpTargetIfIndex;
	} 
}
