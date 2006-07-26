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

