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
}
