/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model.topology;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsStpInterface;
import org.springframework.util.Assert;

/**
 * <p>
 * LinkableNode class.
 * </p>
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class LinkableNode {

    private final String m_packageName;
    private final LinkableSnmpNode m_snmpnode;

    private String m_cdpDeviceId;
    private List<CdpInterface> m_cdpinterfaces = new ArrayList<CdpInterface>();

    private String m_lldpSysname;
    private String m_lldpChassisId;
    private Integer m_lldpChassisIdSubtype;
    private List<LldpRemInterface> m_lldpreminterfaces = new ArrayList<LldpRemInterface>();

    private InetAddress m_ospfRouterId;
    private List<OspfNbrInterface> m_ospfinterfaces = new ArrayList<OspfNbrInterface>();

    private String m_isisSysId;
    private List<IsisISAdjInterface> m_isisinterfaces = new ArrayList<IsisISAdjInterface>();

    private List<RouterInterface> m_routeinterfaces = new ArrayList<RouterInterface>();

    private Map<Integer,String> m_macIdentifiers = new HashMap<Integer,String>();
    private Map<Integer, List<OnmsStpInterface>> m_vlanStpInterfaces = new HashMap<Integer, List<OnmsStpInterface>>();
    private Map<Integer, String> m_vlanBridgeIdentifier = new HashMap<Integer, String>();
    private Map<Integer, Set<String>> m_portMacs = new HashMap<Integer, Set<String>>();
    private Map<Integer, String> m_vlanStpRoot = new HashMap<Integer, String>();
    private Map<Integer, Integer> m_bridgePortIfindex = new HashMap<Integer, Integer>();

    /**
     * The Wifi Mac address to Interface Index map
     */
    private Map<Integer, Set<String>> m_wifiIfIndexMac = new HashMap<Integer,Set<String>>();

    /**
     * <p>
     * Constructor for LinkableNode.
     * </p>
     * 
     * @param nodeId
     *            a int.
     * @param snmprimaryaddr
     *            a {@link java.net.InetAddress} object.
     * @param sysoid
     *            a {@link java.lang.String} object.
     */
    public LinkableNode(final LinkableSnmpNode snmpnode, final String packageName) {
        m_snmpnode = snmpnode;
        m_packageName = packageName;
    }

    public String getIsisSysId() {
        return m_isisSysId;
    }

    public void setIsisSysId(String isisSysId) {
        m_isisSysId = isisSysId;
    }

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

    public String getPackageName() {
        return m_packageName;
    }

    public LinkableSnmpNode getLinkableSnmpNode() {
        return m_snmpnode;
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

    public List<IsisISAdjInterface> getIsisInterfaces() {
        return m_isisinterfaces;
    }

    public void setIsisInterfaces(List<IsisISAdjInterface> isisinterfaces) {
        m_isisinterfaces = isisinterfaces;
    }

    /**
     * <p>
     * getCdpInterfaces
     * </p>
     * 
     * @return Returns the m_cdpinterfaces.
     */
    public List<CdpInterface> getCdpInterfaces() {
        return m_cdpinterfaces;
    }

    /**
     * <p>
     * setCdpInterfaces
     * </p>
     * 
     * @param cdpinterfaces
     *            The m_cdpinterfaces to set.
     */
    public void setCdpInterfaces(List<CdpInterface> cdpinterfaces) {
        if (cdpinterfaces == null || cdpinterfaces.isEmpty())
            return;
        m_cdpinterfaces = cdpinterfaces;
    }

    /**
     * <p>
     * hasCdpInterfaces
     * </p>
     * 
     * @return Returns the m_hascdpinterfaces.
     */
    public boolean hasCdpInterfaces() {
        return !m_cdpinterfaces.isEmpty();
    }

    /**
     * <p>
     * getRouteInterfaces
     * </p>
     * 
     * @return Returns the m_routeinterfaces.
     */
    public List<RouterInterface> getRouteInterfaces() {
        return m_routeinterfaces;
    }

    /**
     * <p>
     * setRouteInterfaces
     * </p>
     * 
     * @param routeinterfaces
     *            a {@link java.util.List} object.
     */
    public void setRouteInterfaces(List<RouterInterface> routeinterfaces) {
        if (routeinterfaces == null || routeinterfaces.isEmpty())
            return;
        m_routeinterfaces = routeinterfaces;
    }

    /**
     * <p>
     * hasRouteInterfaces
     * </p>
     * 
     * @return Returns the m_hascdpinterfaces.
     */
    public boolean hasRouteInterfaces() {
        return !m_routeinterfaces.isEmpty();
    }

    /**
     * <p>
     * isBridgeNode
     * </p>
     * 
     * @return Returns the isBridgeNode.
     */
    public boolean isBridgeNode() {
        return !m_vlanBridgeIdentifier.isEmpty();
    }

    /**
     * @return Returns the bridgeIdentifiers.
     */
    public Collection<String> getBridgeIdentifiers() {
        return m_vlanBridgeIdentifier.values();
    }

    public void addBridgeIdentifier(final String bridge, final Integer vlan) {
        m_vlanBridgeIdentifier.put(vlan, bridge);
    }

    public boolean isBridgeIdentifier(final String bridge) {
        return m_vlanBridgeIdentifier.containsValue(bridge);
    }

    public String getBridgeIdentifier(final Integer vlan) {
        return m_vlanBridgeIdentifier.get(vlan);
    }

    public void setMacIdentifiers(final Map<Integer,String> macIdentifiers) {
        m_macIdentifiers = macIdentifiers;
    }

    public Map<Integer,String> getMacIdentifiers() {
        return m_macIdentifiers;
    }

    public boolean isMacIdentifier(final String mac) {
        return m_macIdentifiers.values().contains(mac);
    }

    public void addWifiMacAddress(final Integer ifindex, final String macAddress) {
        Set<String> macs = new HashSet<String>();
        if (m_wifiIfIndexMac.containsKey(ifindex))
            macs = m_wifiIfIndexMac.get(ifindex);
        macs.add(macAddress);
        m_wifiIfIndexMac.put(ifindex, macs);
    }
 
    public Map<Integer,Set<String>> getWifiMacIfIndexMap() {
        return m_wifiIfIndexMac;
    }

    public void addBridgeForwardingTableEntry(final int bridgeport, final String macAddress) {
        Set<String> macs = new HashSet<String>();
        if (m_portMacs.containsKey(bridgeport)) {
            macs = m_portMacs.get(bridgeport);
        }
        macs.add(macAddress);

        m_portMacs.put(bridgeport, macs);
    }

    public boolean hasBridgeForwardingTableEntryFor(final String macAddress) {
        for (final Set<String> macs : m_portMacs.values()) {
            if (macs.contains(macAddress)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBridgeForwardingTable() {
        return !m_portMacs.isEmpty();
    }

    public Set<String> getBridgeForwadingTableOnBridgePort(final int bridgeport) {
        if (hasBridgeForwardingTableOnBridgePort(bridgeport))
            return m_portMacs.get(bridgeport);
        return Collections.emptySet();
    }

    public boolean hasBridgeForwardingTableOnBridgePort(final int bridgeport) {
        return (m_portMacs.containsKey(bridgeport) && m_portMacs.get(bridgeport) != null);
    }

    public List<Integer> getBridgeForwardingTablePortsFromMac(final String macAddress) {
        List<Integer> ports = new ArrayList<Integer>();
        for (final Integer intePort : m_portMacs.keySet()) {
            if (m_portMacs.get(intePort).contains(macAddress)) {
                ports.add(intePort);
            }
        }
        return ports;
    }

    public int getIfindexFromBridgePort(final int bridgeport) {
        if (m_bridgePortIfindex.containsKey(bridgeport)) {
            return m_bridgePortIfindex.get(bridgeport).intValue();
        }
        return -1;
    }

    public int getBridgePortFromIfindex(final int ifindex) {
        for (final Integer curBridgePort : m_bridgePortIfindex.keySet()) {
            final Integer curIfIndex = m_bridgePortIfindex.get(curBridgePort);
            if (curIfIndex.intValue() == ifindex)
                return curBridgePort.intValue();
        }
        return -1;
    }

    public void setIfIndexBridgePort(final Integer ifindex, final Integer bridgeport) {
        Assert.notNull(ifindex);
        Assert.notNull(bridgeport);
        m_bridgePortIfindex.put(bridgeport, ifindex);
    }

    /**
     * @return Returns the portMacs.
     */
    public Map<Integer, Set<String>> getBridgeForwardingTable() {
        return m_portMacs;
    }

    /**
     * @param portMacs
     *            The portMacs to set.
     */
    public void setBridgeForwardingTable(final Map<Integer, Set<String>> portMacs) {
        m_portMacs = portMacs;
    }

    public void setVlanStpRoot(final Integer vlan, final String stproot) {
        if (stproot != null)
            m_vlanStpRoot.put(vlan, stproot);
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
     * <p>
     * getStpInterfaces
     * </p>
     * 
     * @return Returns the stpInterfaces.
     */
    public Map<Integer, List<OnmsStpInterface>> getStpInterfaces() {
        return m_vlanStpInterfaces;
    }

    /**
     * <p>
     * addStpInterface
     * </p>
     * 
     * @param stpIface
     *            a {@link org.opennms.netmgt.model.OnmsStpInterface} object.
     */
    public void addStpInterface(final OnmsStpInterface stpIface) {
        final Integer vlanindex = stpIface.getVlan() == null ? 0
                                                            : stpIface.getVlan();
        List<OnmsStpInterface> stpifs = new ArrayList<OnmsStpInterface>();
        if (m_vlanStpInterfaces.containsKey(vlanindex)) {
            stpifs = m_vlanStpInterfaces.get(vlanindex);
        }
        stpifs.add(stpIface);
        m_vlanStpInterfaces.put(vlanindex, stpifs);
    }

    /**
     * <p>
     * getNodeId
     * </p>
     * 
     * @return a int.
     */
    public int getNodeId() {
        return getLinkableSnmpNode().getNodeId();
    }

    /**
     * <p>
     * getSnmpPrimaryIpAddr
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public InetAddress getSnmpPrimaryIpAddr() {
        return getLinkableSnmpNode().getSnmpPrimaryIpAddr();
    }

    /**
     * <p>
     * getSysoid
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getSysoid() {
        return getLinkableSnmpNode().getSysoid();
    }

}
