/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.springframework.util.Assert;

/**
 * <p>LinkableNode class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class LinkableNode {

    private final int m_nodeId;

    private final InetAddress m_snmpprimaryaddr;
	
    private final String m_sysoid;
    
    private String m_cdpDeviceId;
    
	private String m_lldpSysname;
    
    private String m_lldpChassisId;
    
    private Integer m_lldpChassisIdSubtype;
    
    private InetAddress m_ospfRouterId;
    
    public String getCdpDeviceId() {
		return m_cdpDeviceId;
	}
    public void setCdpDeviceId(String cdpDeviceId) {
		m_cdpDeviceId = cdpDeviceId;
	}
    public InetAddress getOspfRouterId() {
        return m_ospfRouterId;
    }
    public void setOspfRouterId(InetAddress ospfRouterId) {
        m_ospfRouterId = ospfRouterId;
    }
    public void setLldpSysname(String lldpSysname) {
        m_lldpSysname = lldpSysname;
    }
    public void setLldpChassisId(String lldpChassisId) {
        m_lldpChassisId = lldpChassisId;
    }
    public void setLldpChassisIdSubtype(Integer lldpChassisIdSubtype) {
        m_lldpChassisIdSubtype = lldpChassisIdSubtype;
    }
    public String getLldpSysname() {
        return m_lldpSysname;
    }
    public String getLldpChassisId() {
        return m_lldpChassisId;
    }
    public Integer getLldpChassisIdSubtype() {
        return m_lldpChassisIdSubtype;
    }

    private List<CdpInterface> m_cdpinterfaces = new ArrayList<CdpInterface>();

    private List<LldpRemInterface> m_lldpreminterfaces = new ArrayList<LldpRemInterface>();

    private boolean m_hascdpinterfaces = false;

    private List<RouterInterface> m_routeinterfaces = new ArrayList<RouterInterface>();

    private List<OspfNbrInterface> m_ospfinterfaces = new ArrayList<OspfNbrInterface>();
        
    private boolean m_hasrouteinterfaces = false;
	
    private boolean m_isBridgeNode = false;
	
	/**
	 * the list of bridge port that are backbone bridge ports ou that are
	 * link between switches
	 */
	private List<Integer> m_backBoneBridgePorts = new java.util.ArrayList<Integer>();
	private List<String> m_bridgeIdentifiers = new java.util.ArrayList<String>();
	private Map<Integer,List<OnmsStpInterface>> m_bridgeStpInterfaces = new HashMap<Integer,List<OnmsStpInterface>>();
	private Map<Integer,String> m_vlanBridgeIdentifiers = new HashMap<Integer,String>();
	private Map<Integer,Set<String>> m_portMacs = new HashMap<Integer,Set<String>>();
	private Map<String,Integer> m_macsVlan = new HashMap<String,Integer>();
	private Map<Integer,String> m_vlanStpRoot = new HashMap<Integer,String>();
	private Map<Integer,Integer> m_bridgePortIfindex = new HashMap<Integer,Integer>();

	/**
	 * <p>Constructor for LinkableNode.</p>
	 *
	 * @param nodeId a int.
	 * @param snmprimaryaddr a {@link java.net.InetAddress} object.
	 * @param sysoid a {@link java.lang.String} object.
	 */
	public LinkableNode(final int nodeId, final InetAddress snmpPrimaryAddr, final String sysoid) {
		m_nodeId = nodeId;
		m_snmpprimaryaddr = snmpPrimaryAddr;
		m_sysoid = sysoid;
	}
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("nodeId", m_nodeId)
	        .append("snmpPrimaryAddr", str(m_snmpprimaryaddr))
	        .append("sysOid", m_sysoid)
	        .toString();
	}

	/**
	 * <p>getNodeId</p>
	 *
	 * @return a int.
	 */
	public int getNodeId() {
		return m_nodeId;
	}

	/**
	 * <p>getSnmpPrimaryIpAddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public InetAddress getSnmpPrimaryIpAddr() {
		return m_snmpprimaryaddr;
	}

	    public List<LldpRemInterface> getLldpRemInterfaces() {
	        return m_lldpreminterfaces;
	    }
	    
	    public void setLldpRemInterfaces(List<LldpRemInterface> lldpreminterfaces) {
	        m_lldpreminterfaces = lldpreminterfaces;
	    }
	    
	    public List<OspfNbrInterface> getOspfinterfaces() {
	        return m_ospfinterfaces;
	    }

	    public void setOspfinterfaces(List<OspfNbrInterface> ospfinterfaces) {
	        m_ospfinterfaces = ospfinterfaces;
	    }
	    
	/**
	 * <p>getCdpInterfaces</p>
	 *
	 * @return Returns the m_cdpinterfaces.
	 */
	public List<CdpInterface> getCdpInterfaces() {
		return m_cdpinterfaces;
	}
	/**
	 * <p>setCdpInterfaces</p>
	 *
	 * @param cdpinterfaces The m_cdpinterfaces to set.
	 */
	public void setCdpInterfaces(List<CdpInterface> cdpinterfaces) {
		if (cdpinterfaces == null || cdpinterfaces.isEmpty()) return;
		m_hascdpinterfaces = true;
		m_cdpinterfaces = cdpinterfaces;
	}
	
	/**
	 * <p>hasCdpInterfaces</p>
	 *
	 * @return Returns the m_hascdpinterfaces.
	 */
	public boolean hasCdpInterfaces() {
		return m_hascdpinterfaces;
	}

	/**
	 * <p>getRouteInterfaces</p>
	 *
	 * @return Returns the m_routeinterfaces.
	 */
	public List<RouterInterface> getRouteInterfaces() {
		return m_routeinterfaces;
	}
	/**
	 * <p>setRouteInterfaces</p>
	 *
	 * @param routeinterfaces a {@link java.util.List} object.
	 */
	public void setRouteInterfaces(List<RouterInterface> routeinterfaces) {
		if (routeinterfaces == null || routeinterfaces.isEmpty()) return;
		m_hasrouteinterfaces = true;
		m_routeinterfaces = routeinterfaces;
	}
	
	/**
	 * <p>hasRouteInterfaces</p>
	 *
	 * @return Returns the m_hascdpinterfaces.
	 */
	public boolean hasRouteInterfaces() {
		return m_hasrouteinterfaces;
	}


	/**
	 * <p>isBridgeNode</p>
	 *
	 * @return Returns the isBridgeNode.
	 */
	public boolean isBridgeNode() {
		return m_isBridgeNode;
	}

	/**
	 * @return Returns the backBoneBridgePorts.
	 */
	public List<Integer> getBackBoneBridgePorts() {
		return m_backBoneBridgePorts;
	}

	/**
	 * @param backBoneBridgePorts
	 *            The backBoneBridgePorts to set.
	 */
	public void setBackBoneBridgePorts(final List<Integer> backBoneBridgePorts) {
		m_backBoneBridgePorts = backBoneBridgePorts;
	}

	/**
	 * return true if bridgeport is a backbone port
	 * @param bridgeport
	 * @return
	 */
	public boolean isBackBoneBridgePort(final int bridgeport) {
		return m_backBoneBridgePorts.contains(bridgeport);
	}

	/**
	 * add bridgeport to backbone ports
	 * @param bridgeport
	 */
	public void addBackBoneBridgePorts(final int bridgeport) {
		if (m_backBoneBridgePorts.contains(bridgeport)) return;
		m_backBoneBridgePorts.add(bridgeport);
	}

	/**
	 * @return Returns the bridgeIdentifiers.
	 */
	public List<String> getBridgeIdentifiers() {
		return m_bridgeIdentifiers;
	}

	/**
	 * @param bridgeIdentifiers
	 *            The bridgeIdentifiers to set.
	 */
	public void setBridgeIdentifiers(final List<String> bridgeIdentifiers) {
		if (bridgeIdentifiers == null || bridgeIdentifiers.isEmpty() ) return;
		m_bridgeIdentifiers = bridgeIdentifiers;
		m_isBridgeNode = true;
	}

	public void addBridgeIdentifier(final String bridge, final Integer vlan) {
		m_vlanBridgeIdentifiers.put(vlan, bridge);
		addBridgeIdentifier(bridge);
	}

	public boolean isBridgeIdentifier(final String bridge) {
		return m_bridgeIdentifiers.contains(bridge);
	}

	public void addBridgeIdentifier(final String bridge) {
		if (m_bridgeIdentifiers.contains(bridge)) return;
		m_bridgeIdentifiers.add(bridge);
		m_isBridgeNode = true;
	}

	public String getBridgeIdentifier(final Integer vlan) {
		return m_vlanBridgeIdentifiers.get(vlan);
	}

	public void addMacAddress(final int bridgeport, final String macAddress, final Integer vlan) {
		Set<String> macs = new HashSet<String>();
		if (m_portMacs.containsKey(bridgeport)) {
			macs = m_portMacs.get(bridgeport);
		}
		macs.add(macAddress);

		m_portMacs.put(bridgeport, macs);
		m_macsVlan.put(macAddress, vlan);
	}

	public boolean hasMacAddress(final String macAddress) {
	    for (final Set<String> macs : m_portMacs.values()) {
	        if (macs.contains(macAddress)) {
	            return true;
	        }
	    }
		return false;
	}

	public boolean hasMacAddresses() {
		return !m_portMacs.isEmpty();
	}
	
	public Integer getVlan(final String macAddress) {
		return m_macsVlan.get(macAddress);
	}

	public Set<String> getMacAddressesOnBridgePort(final int bridgeport) {
		return m_portMacs.get(bridgeport);
	}

	public boolean hasMacAddressesOnBridgePort(final int bridgeport) {
		return (m_portMacs.containsKey(bridgeport) && m_portMacs.get(bridgeport) != null );
	}

	public List<Integer> getBridgePortsFromMac(final String macAddress) {
		List<Integer> ports = new ArrayList<Integer>();
		for (final Integer intePort : m_portMacs.keySet()) {
			if (m_portMacs.get(intePort).contains(macAddress)) {
				ports.add(intePort);
			}
		}
		return ports;
	}

	public int getIfindex(final int bridgeport) {
		if (m_bridgePortIfindex.containsKey(bridgeport)) {
		    return m_bridgePortIfindex.get(bridgeport).intValue();
		}
		return -1;
	}

	public int getBridgePort(final int ifindex) {
	    for (final Integer curBridgePort : m_bridgePortIfindex.keySet()) {
	        final Integer curIfIndex = m_bridgePortIfindex.get(curBridgePort);
			if (curIfIndex.intValue() == ifindex) return curBridgePort.intValue();
		}
		return -1;
	}

	void setIfIndexBridgePort(final Integer ifindex, final Integer bridgeport) {
	    Assert.notNull(ifindex);
	    Assert.notNull(bridgeport);
		m_bridgePortIfindex.put(bridgeport, ifindex);
	}

	/**
	 * @return Returns the portMacs.
	 */
	public Map<Integer, Set<String>> getPortMacs() {
		return m_portMacs;
	}

	/**
	 * @param portMacs
	 *            The portMacs to set.
	 */
	public void setPortMacs(final Map<Integer,Set<String>> portMacs) {
		m_portMacs = portMacs;
	}

	public void setVlanStpRoot(final Integer vlan, final String stproot) {
		if (stproot != null) m_vlanStpRoot.put(vlan, stproot);
	}

	public boolean hasStpRoot(final Integer vlan) {
		return m_vlanStpRoot.containsKey(vlan);
	}

	public String getStpRoot(final Integer vlan) {
		if (m_vlanStpRoot.containsKey(vlan)) {
			return m_vlanStpRoot.get(vlan);
		}
		return null;
	}

	/**
	 * <p>getStpInterfaces</p>
	 *
	 * @return Returns the stpInterfaces.
	 */
	public Map<Integer,List<OnmsStpInterface>> getStpInterfaces() {
		return m_bridgeStpInterfaces;
	}
	/**
	 * <p>setStpInterfaces</p>
	 *
	 * @param stpInterfaces The stpInterfaces to set.
	 */
	public void setStpInterfaces(Map<Integer,List<OnmsStpInterface>> stpInterfaces) {
		m_bridgeStpInterfaces = stpInterfaces;
	}
	
	/**
	 * <p>addStpInterface</p>
	 *
	 * @param stpIface a {@link org.opennms.netmgt.model.OnmsStpInterface} object.
	 */
	public void addStpInterface(final OnmsStpInterface stpIface) {
		final Integer vlanindex = 
				stpIface.getVlan() == null ? 0 : stpIface.getVlan();
		List<OnmsStpInterface> stpifs = new ArrayList<OnmsStpInterface>();
		if (m_bridgeStpInterfaces.containsKey(vlanindex)) {
			stpifs = m_bridgeStpInterfaces.get(vlanindex);
		}
		stpifs.add(stpIface);
		m_bridgeStpInterfaces.put(vlanindex, stpifs);
	}

	/**
	 * <p>getSysoid</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSysoid() {
		return m_sysoid;
	}

}
