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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LinkableNode extends Object {

	int m_nodeId;

	String m_snmpprimaryaddr;

	List<CdpInterface> m_cdpinterfaces = new ArrayList<CdpInterface>();
	
	boolean m_hascdpinterfaces = false;

	List<RouterInterface> m_routeinterfaces = new ArrayList<RouterInterface>();
	
	boolean m_hasrouteinterfaces = false;

	List<AtInterface> m_atinterfaces = new ArrayList<AtInterface>();
	
	boolean m_hasatinterfaces = false;

	boolean isBridgeNode = false;
	
	/**
	 * the list of bridge port that are backbone bridge ports ou that are
	 * link between switches
	 */
	List<Integer> BackBoneBridgePorts = new java.util.ArrayList<Integer>();

	/**
	 * the list of port that are CDP ports ou that are
	 * link between Cisco Devices
	 */

	List<Integer> CdpPorts = new java.util.ArrayList<Integer>();
	
	List<String> BridgeIdentifiers = new java.util.ArrayList<String>();
	
	HashMap<String,List<BridgeStpInterface>> BridgeStpInterfaces = new HashMap<String,List<BridgeStpInterface>>();

	HashMap<String,String> vlanBridgeIdentifier = new HashMap<String,String>();

	HashMap<Integer,Set<String>> portMacs = new HashMap<Integer,Set<String>>();

	HashMap<String,String> macsVlan = new HashMap<String,String>();

	HashMap<String,String> vlanStpRoot = new HashMap<String,String>();

	HashMap<Integer,Integer> bridgePortIfindex = new HashMap<Integer,Integer>();


	private LinkableNode() {
		throw new UnsupportedOperationException(
		"default constructor not supported");
	}

	public LinkableNode(int nodeId, String snmprimaryaddr) {
		m_nodeId = nodeId;
		m_snmpprimaryaddr = snmprimaryaddr;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n");
		str.append("Snmp Primary Ip Address = " + m_snmpprimaryaddr + "\n");
		return str.toString();
	}

	/**
	 * @return
	 */
	public int getNodeId() {
		return m_nodeId;
	}

	/**
	 * @return
	 */
	public String getSnmpPrimaryIpAddr() {
		return m_snmpprimaryaddr;
	}

	/**
	 * @return Returns the m_cdpinterfaces.
	 */
	public List<CdpInterface> getCdpInterfaces() {
		return m_cdpinterfaces;
	}
	/**
	 * @param m_cdpinterfaces The m_cdpinterfaces to set.
	 */
	public void setCdpInterfaces(List<CdpInterface> m_cdpinterfaces) {
		if (m_cdpinterfaces == null || m_cdpinterfaces.isEmpty()) return;
		this.m_hascdpinterfaces = true;
		this.m_cdpinterfaces = m_cdpinterfaces;
	}
	
	/**
	 * @return Returns the m_hascdpinterfaces.
	 */
	public boolean hasCdpInterfaces() {
		return m_hascdpinterfaces;
	}

	/**
	 * @return Returns the m_routeinterfaces.
	 */
	public List<RouterInterface> getRouteInterfaces() {
		return m_routeinterfaces;
	}
	/**
	 * @param m_cdpinterfaces The m_cdpinterfaces to set.
	 */
	public void setRouteInterfaces(List<RouterInterface> m_routeinterfaces) {
		if (m_routeinterfaces == null || m_routeinterfaces.isEmpty()) return;
		this.m_hasrouteinterfaces = true;
		this.m_routeinterfaces = m_routeinterfaces;
	}
	
	/**
	 * @return Returns the m_hasatinterfaces.
	 */
	public boolean hasAtInterfaces() {
		return m_hasatinterfaces;
	}

	/**
	 * @return Returns the m_routeinterfaces.
	 */
	public List<AtInterface> getAtInterfaces() {
		return m_atinterfaces;
	}
	/**
	 * @param m_cdpinterfaces The m_cdpinterfaces to set.
	 */
	public void setAtInterfaces(List<AtInterface> m_atinterfaces) {
		if (m_atinterfaces == null || m_atinterfaces.isEmpty()) return;
		this.m_hasatinterfaces = true;
		this.m_atinterfaces = m_atinterfaces;
	}
	
	/**
	 * @return Returns the m_hascdpinterfaces.
	 */
	public boolean hasRouteInterfaces() {
		return m_hasrouteinterfaces;
	}


	/**
	 * @return Returns the isBridgeNode.
	 */
	public boolean isBridgeNode() {
		return isBridgeNode;
	}

	/**
	 * @return Returns the backBoneBridgePorts.
	 */
	List<Integer> getBackBoneBridgePorts() {
		return BackBoneBridgePorts;
	}

	/**
	 * @param backBoneBridgePorts
	 *            The backBoneBridgePorts to set.
	 */
	void setBackBoneBridgePorts(List<Integer> backBoneBridgePorts) {
		BackBoneBridgePorts = backBoneBridgePorts;
	}

	boolean isBackBoneBridgePort(int bridgeport) {
		return BackBoneBridgePorts.contains(new Integer(bridgeport));
	}

	void addBackBoneBridgePorts(int bridgeport) {
		if (BackBoneBridgePorts.contains(new Integer(bridgeport)))
			return;
		BackBoneBridgePorts.add(new Integer(bridgeport));
	}

	/**
	 * @return Returns the backBoneBridgePorts.
	 */
	List getCdpPorts() {
		return CdpPorts;
	}

	/**
	 * @param backBoneBridgePorts
	 *            The backBoneBridgePorts to set.
	 */
	void setCdpPorts(List<Integer> cdpPorts) {
		CdpPorts = cdpPorts;
	}

	boolean isCdpPort(int ifindex) {
		return CdpPorts.contains(new Integer(ifindex));
	}

	void addCdpPorts(int ifindex) {
		if (CdpPorts.contains(new Integer(ifindex)))
			return;
		CdpPorts.add(new Integer(ifindex));
	}

	/**
	 * @return Returns the bridgeIdentifiers.
	 */
	List getBridgeIdentifiers() {
		return BridgeIdentifiers;
	}

	/**
	 * @param bridgeIdentifiers
	 *            The bridgeIdentifiers to set.
	 */
	void setBridgeIdentifiers(List<String> bridgeIdentifiers) {
		if (bridgeIdentifiers == null || bridgeIdentifiers.isEmpty() ) return;
		BridgeIdentifiers = bridgeIdentifiers;
		isBridgeNode = true;
	}

	void addBridgeIdentifier(String bridge, String vlan) {
		vlanBridgeIdentifier.put(vlan, bridge);
		if (BridgeIdentifiers.contains(bridge))
			return;
		BridgeIdentifiers.add(bridge);
		isBridgeNode = true;
	}

	boolean isBridgeIdentifier(String bridge) {
		return BridgeIdentifiers.contains(bridge);
	}

	void addBridgeIdentifier(String bridge) {
		if (BridgeIdentifiers.contains(bridge))
			return;
		BridgeIdentifiers.add(bridge);
		isBridgeNode = true;
	}

	String getBridgeIdentifier(String vlan) {
		return (String) vlanBridgeIdentifier.get(vlan);
	}

	void addMacAddress(int bridgeport, String macAddress, String vlan) {

		Set<String> macs = new HashSet<String>();
		if (portMacs.containsKey(new Integer(bridgeport))) {
			macs = portMacs.get(new Integer(bridgeport));
		}
		macs.add(macAddress);

		portMacs.put(new Integer(bridgeport), macs);
		macsVlan.put(macAddress, vlan);
	}

	boolean hasMacAddress(String macAddress) {
		List macs = new ArrayList();
		Iterator ite = portMacs.values().iterator();
		while (ite.hasNext()) {
			macs = (List) ite.next();
			if (macs.contains(macAddress))
				return true;
		}
		return false;
	}

	String getVlan(String macAddress) {
		return (String) macsVlan.get(macAddress);
	}

	Set<String> getMacAddressesOnBridgePort(int bridgeport) {
		return  portMacs.get(new Integer(bridgeport));
	}

	boolean hasMacAddressesOnBridgePort(int bridgeport) {
		return (portMacs.containsKey(new Integer(bridgeport)) && portMacs.get(new Integer(bridgeport)) != null );
	}

	int getBridgePort(String macAddress) {
		Iterator ite = portMacs.keySet().iterator();
		while (ite.hasNext()) {
			Integer intePort = (Integer) ite.next();
			List macs = (List) portMacs.get(intePort);
			if (macs.contains(macAddress)) {
				return intePort.intValue();
			}
		}
		return -1;
	}

	int getIfindex(int bridgeport) {
		if (bridgePortIfindex.containsKey(new Integer(bridgeport))) {
			Integer ifindex = (Integer) bridgePortIfindex.get(new Integer(
					bridgeport));
			return ifindex.intValue();
		}
		return -1;
	}

	int getBridgePort(int ifindex) {
		Iterator ite = bridgePortIfindex.keySet().iterator();
		while (ite.hasNext() ) {
			Integer curBridgePort = (Integer) ite.next();
			Integer curIfIndex = (Integer) bridgePortIfindex.get(curBridgePort);
			if (curIfIndex.intValue() == ifindex) return curBridgePort.intValue();
		}
		return -1;
	}

	void setIfIndexBridgePort(Integer ifindex, Integer bridgeport) {
        
        if ( ifindex == null ) {
            throw new NullPointerException("ifindex is null");
        } else if (bridgeport == null) {
            throw new NullPointerException("bridgeport is null");
        }
		bridgePortIfindex.put(bridgeport, ifindex);
	}

	/**
	 * @return Returns the portMacs.
	 */
	HashMap getPortMacs() {
		return portMacs;
	}

	/**
	 * @param portMacs
	 *            The portMacs to set.
	 */
	void setPortMacs(HashMap<Integer,Set<String>> portMacs) {
		this.portMacs = portMacs;
	}

	void setVlanStpRoot(String vlan, String stproot) {
		if (stproot != null) vlanStpRoot.put(vlan, stproot);
	}

	boolean hasStpRoot(String vlan) {
		return vlanStpRoot.containsKey(vlan);
	}

	String getStpRoot(String vlan) {
		if (vlanStpRoot.containsKey(vlan)) {
			return (String) vlanStpRoot.get(vlan);
		}
		return null;
	}

	/**
	 * @return Returns the stpInterfaces.
	 */
	public HashMap getStpInterfaces() {
		return BridgeStpInterfaces;
	}
	/**
	 * @param stpInterfaces The stpInterfaces to set.
	 */
	public void setStpInterfaces(HashMap<String,List<BridgeStpInterface>> stpInterfaces) {
		BridgeStpInterfaces = stpInterfaces;
	}
	
	public void addStpInterface(BridgeStpInterface stpIface) {
		String vlanindex = stpIface.getVlan();
		List<BridgeStpInterface> stpifs = new ArrayList<BridgeStpInterface>();;
		if (BridgeStpInterfaces.containsKey(vlanindex)) {
			stpifs = BridgeStpInterfaces.get(vlanindex);
		}
		stpifs.add(stpIface);
		BridgeStpInterfaces.put(vlanindex, stpifs);
	}
}