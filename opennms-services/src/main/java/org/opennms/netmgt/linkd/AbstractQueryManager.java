/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;

import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dBasePortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dStpPortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dTpFdbTableEntry;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTableEntry;
import org.opennms.netmgt.linkd.snmp.IpRouteCollectorEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpMibConstants;
import org.opennms.netmgt.linkd.snmp.LldpRemTableEntry;
import org.opennms.netmgt.linkd.snmp.OspfNbrTableEntry;
import org.opennms.netmgt.linkd.snmp.QBridgeDot1dTpFdbTableEntry;
import org.opennms.netmgt.linkd.snmp.SnmpStore;
import org.opennms.netmgt.linkd.snmp.Vlan;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsVlan;

public abstract class AbstractQueryManager implements QueryManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractQueryManager.class);

    protected Linkd m_linkd;
    private static final InetAddress m_zeroAddress = InetAddressUtils.addr("0.0.0.0");

    @Override
    public void setLinkd(final Linkd linkd) {
        m_linkd = linkd;
    }

    @Override
    public Linkd getLinkd() {
        return m_linkd;
    }

    protected void sendNewSuspectEvent(final InetAddress ipaddress, final InetAddress ipowner, final String name) {
        getLinkd().sendNewSuspectEvent(ipaddress, ipowner, name);
    }

    public abstract NodeDao getNodeDao();

    public abstract IpInterfaceDao getIpInterfaceDao();

    public abstract AtInterfaceDao getAtInterfaceDao();
    
    public abstract SnmpInterfaceDao getSnmpInterfaceDao();

    protected abstract int getIfIndexByName(int targetCdpNodeId, String cdpTargetDevicePort);

    protected abstract List<Integer> getNodeidFromIp(InetAddress cdpTargetIpAddr);

    protected abstract List<RouterInterface> getRouteInterface(InetAddress nexthop, int ifindex);

    protected abstract int getSnmpIfType(int nodeId, Integer ifindex);

    protected abstract void saveIpRouteInterface(OnmsIpRouteInterface ipRouteInterface);

    protected abstract void saveVlan(final OnmsVlan vlan);

    protected abstract void saveStpNode(final OnmsStpNode stpNode);

    protected abstract void saveStpInterface(final OnmsStpInterface stpInterface);

    protected abstract List<String> getPhysAddrs(final int nodeId);

    protected abstract void markOldDataInactive(final Date now, final int nodeid);

    protected abstract void deleteOlderData(final Date now, final int nodeid);

    protected OnmsNode getNode(Integer nodeId) {
        return getNodeDao().get(nodeId);
    }

    protected void processIpNetToMediaTable(final LinkableNode node, final SnmpCollection snmpcoll, final Date scanTime) {

    	boolean hasPrimaryIpAsAtinterface = false;
        if (LOG.isDebugEnabled()) {
            if (snmpcoll.getIpNetToMediaTable().size() > 0) {
                LOG.debug("processIpNetToMediaTable: Starting ipNetToMedia table processing for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processIpNetToMediaTable: Zero ipNetToMedia table entries for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        // the AtInterfaces used by LinkableNode where to save info
        for (final IpNetToMediaTableEntry ent : snmpcoll.getIpNetToMediaTable()) {

            final int ifindex = ent.getIpNetToMediaIfIndex();

            if (ifindex < 0) {
                LOG.warn("processIpNetToMediaTable: invalid ifindex {}", ifindex);
                continue;
            }

            final InetAddress ipaddress = ent.getIpNetToMediaNetAddress(); 
            
            if (ipaddress.equals(node.getSnmpPrimaryIpAddr()))
            	hasPrimaryIpAsAtinterface = true;
            final String hostAddress = InetAddressUtils.str(ipaddress);

            if (ipaddress == null || ipaddress.isLoopbackAddress() || m_zeroAddress.equals(ipaddress)) {
                LOG.warn("processIpNetToMediaTable: invalid IP: {}", hostAddress);
                continue;
            }

            final String physAddr = ent.getIpNetToMediaPhysAddress();

            if (physAddr == null || physAddr.equals("000000000000") || physAddr.equalsIgnoreCase("ffffffffffff")) {
                LOG.warn("processIpNetToMediaTable: invalid MAC address {} for IP {}", physAddr, hostAddress);
                continue;
            }

            LOG.debug("processIpNetToMediaTable: trying save ipNetToMedia info: IP address {}, MAC address {}, ifIndex {}", hostAddress, physAddr, ifindex);

            // get an AtInterface but without setting MAC address
            final Collection<OnmsAtInterface> ats = getAtInterfaceDao().getAtInterfaceForAddress(ipaddress);
            if (ats.isEmpty()) {
                LOG.debug("processIpNetToMediaTable: no node found for IP address {}.", hostAddress);
                sendNewSuspectEvent(ipaddress, snmpcoll.getTarget(), snmpcoll.getPackageName());
                continue;
            }

            for (final OnmsAtInterface at : ats) {
            	at.setSourceNodeId(node.getNodeId());

	            if (at.getMacAddress() != null && !at.getMacAddress().equals(physAddr)) {
	                LOG.info("processIpNetToMediaTable: Setting OnmsAtInterface MAC address to {} but it used to be '{}' (IP Address = {}, ifIndex = {})", physAddr, at.getMacAddress(), hostAddress, ifindex);
	            }
	            at.setMacAddress(physAddr);

	            if (at.getIfIndex() != null && !at.getIfIndex().equals(ifindex)) {
	                LOG.info("processIpNetToMediaTable: Setting OnmsAtInterface ifIndex to {} but it used to be '{}' (IP Address = {}, MAC = {})", ifindex, at.getIfIndex(), hostAddress, physAddr);
	            }
	            at.setIfIndex(ifindex);

	            at.setLastPollTime(scanTime);
	            at.setStatus(StatusType.ACTIVE);

	            getAtInterfaceDao().saveOrUpdate(at);
            
	            // Now store the information that is needed to create link in linkd
	            AtInterface atinterface = new AtInterface(at.getNode().getId(), physAddr, at.getIpAddress());
	            atinterface.setIfIndex(getIfIndex(at.getNode().getId(), at.getIpAddress().getHostAddress()));
	            getLinkd().addAtInterface(atinterface);            
            }
        }
        
        if (!hasPrimaryIpAsAtinterface)
        	savePrimaryAddressAtInterface(node);
        
    }

	private void savePrimaryAddressAtInterface(final LinkableNode node) {
		LOG.info("savePrimaryAddressAtInterface: try to setting ifindex for linkednode primary ip address '{}' ", node.getSnmpPrimaryIpAddr().getHostAddress());
		OnmsIpInterface ipinterface = getIpInterfaceDao().findByNodeIdAndIpAddress(Integer.valueOf(node.getNodeId()), node.getSnmpPrimaryIpAddr().getHostAddress());
		if (ipinterface != null) {
		    OnmsSnmpInterface snmpinterface = ipinterface.getSnmpInterface();
		    if (snmpinterface != null && snmpinterface.getPhysAddr() != null ) {
		        AtInterface at = new AtInterface(node.getNodeId(), snmpinterface.getPhysAddr(), node.getSnmpPrimaryIpAddr());
		        at.setMacAddress(snmpinterface.getPhysAddr());
		        LOG.info("savePrimaryAddressAtInterface: Setting AtInterface ifIndex to {}, for primary IP Address {}, MAC = {})", at.getIfIndex(), at.getIpAddress().getHostAddress(), at.getMacAddress());
		        at.setIfIndex(snmpinterface.getIfIndex());
		        getLinkd().addAtInterface(at);
		    }
		}
	}

    // This method retrieve the right interface index from the OnmsIpInterface
    // This is required because the ifindex  walked in atInterface snmp table
    // is related to the node that holds the information and not to the 
    // effective node that hold the ip address.
    // This ifindex is saved in AtInterface object
    // that is used to find the right information for a linked node.
    // AR Dixit
    protected Integer getIfIndex(Integer nodeid, String ipaddress) {
        OnmsIpInterface ipinterface = getIpInterfaceDao().findByNodeIdAndIpAddress(nodeid, ipaddress);
        if (ipinterface != null && ipinterface.getIfIndex() != null) {
            LOG.info("getIfindex: found ip interface for address '{}' on ifindex {}", ipinterface.getIpAddress().getHostAddress(), ipinterface.getIfIndex());
            return ipinterface.getIfIndex();
        }
        LOG.info("getIfIndex: no (ipinterface)ifindex found for nodeid {}, address '{}'.",nodeid,ipaddress);
        return -1;
    }

    protected void processOspf(final LinkableNode node, final SnmpCollection snmpcoll, final Date scanTime) {
        
        InetAddress ospfRouterId = snmpcoll.getOspfGeneralGroup().getOspfRouterId();

        LOG.debug("processOspf: ospf node/ospfrouterid: {}/{}", node.getNodeId(), str(ospfRouterId));
        if (m_zeroAddress.equals(ospfRouterId)) {
            LOG.info("processOspf: invalid ospf ruoter id. node/ospfrouterid: {}/{}. Skipping!", node.getNodeId(), str(ospfRouterId));
            return;
        }
        
        node.setOspfRouterId(ospfRouterId);

        List<OspfNbrInterface> ospfinterfaces = new ArrayList<OspfNbrInterface>();
        
        for (final OspfNbrTableEntry ospfNbrTableEntry: snmpcoll.getOspfNbrTable()) {
            InetAddress ospfNbrRouterId = ospfNbrTableEntry.getOspfNbrRouterId();
            InetAddress ospfNbrIpAddr = ospfNbrTableEntry.getOspfNbrIpAddress();
            LOG.debug("processOspf: addind ospf node/ospfnbraddress/ospfnbrrouterid: {}/{}/{}", node.getNodeId(), str(ospfNbrIpAddr),str(ospfNbrRouterId));
            if (m_zeroAddress.equals(ospfNbrIpAddr) || m_zeroAddress.equals(ospfNbrRouterId)) {
                LOG.info("processOspf: ospf invalid ip address for node/ospfnbraddress/ospfnbrrouterid: {}/{}/{}", node.getNodeId(), str(ospfNbrIpAddr),str(ospfNbrRouterId));
                continue;
            }
            Integer ifIndex = ospfNbrTableEntry.getOspfNbrAddressLessIndex();
            LOG.debug("processOspf: ospf node/ospfnbraddress/ospfnbrrouterid/ospfnbrAddressLessIfIndex: {}/{}/{}/{}", node.getNodeId(), str(ospfNbrIpAddr),str(ospfNbrRouterId),ifIndex);
            List<OnmsIpInterface> ipinterfaces = getIpInterfaceDao().findByIpAddress(str(ospfNbrIpAddr));
            for (OnmsIpInterface ipinterface:ipinterfaces ) {
                
                if (ifIndex.intValue() == 0) 
                    ifIndex = ipinterface.getIfIndex();
                LOG.debug("processOspf: ospf node/ospfnbraddress/ospfnbrrouterid/ifIndex: {}/{}/{}/{}", ipinterface.getNode().getId(), str(ospfNbrIpAddr),str(ospfNbrRouterId),ifIndex);
                if (ifIndex != null && ifIndex.intValue() > 0) {
                    OspfNbrInterface ospfinterface = new OspfNbrInterface(ospfNbrRouterId);
                    ospfinterface.setOspfNbrNodeId(ipinterface.getNode().getId());
                    ospfinterface.setOspfNbrIpAddr(ospfNbrIpAddr);
                    ospfinterface.setOspfNbrNetMask(getSnmpInterfaceDao().findByNodeIdAndIfIndex(ipinterface.getNode().getId(), ifIndex).getNetMask());
                    ospfinterface.setOspfNbrIfIndex(ifIndex);
                    LOG.debug("processOspf: adding ospf interface. node/ospfinterface: {}/{}", node.getNodeId(), ospfinterface);
                    ospfinterfaces.add(ospfinterface);
                } else {
                    LOG.info("processOspf: ospf invalid if index. node/ospfnbraddress/ospfnbrrouterid/ifIndex: {}/{}/{}/{}. Skipping!", node.getNodeId(), str(ospfNbrIpAddr),str(ospfNbrRouterId),ifIndex);
                }
            }
        }
        node.setOspfinterfaces(ospfinterfaces);
    }
    
    protected void processLldp(final LinkableNode node, final SnmpCollection snmpcoll, final Date scanTime) {

        node.setLldpChassisId(snmpcoll.getLldpLocalGroup().getLldpLocChassisid());
        node.setLldpChassisIdSubtype(snmpcoll.getLldpLocalGroup().getLldpLocChassisidSubType());
        node.setLldpSysname(snmpcoll.getLldpLocalGroup().getLldpLocSysname());

        Map<Integer, LldpLocTableEntry> localPortNumberToLocTableEntryMap = getLocalPortNumberToLocalTableEntryMap(snmpcoll);
        List<LldpRemInterface> lldpRemInterfaces = new ArrayList<LldpRemInterface>();
        
        for (final LldpRemTableEntry lldpRemTableEntry: snmpcoll.getLldpRemTable()) {

            LOG.debug("processLldp: lldp remote entry node/localport/remporttype/remport: {}/{}/{}/{}", node.getNodeId(), lldpRemTableEntry.getLldpRemLocalPortNum(),lldpRemTableEntry.getLldpRemPortidSubtype(),lldpRemTableEntry.getLldpRemPortid());
            Integer lldpLocIfIndex = getLldpLocIfIndex(node.getLldpSysname(), localPortNumberToLocTableEntryMap.get(lldpRemTableEntry.getLldpRemLocalPortNum()));
            if (lldpLocIfIndex == null || lldpLocIfIndex.intValue() == -1) {
                LOG.warn("processLldp: lldp local ifindex not valid for local node/lldpLocalPortNumber: {}/{}", node.getNodeId(), lldpRemTableEntry.getLldpRemLocalPortNum());
                continue;
            }

            Integer lldpRemIfIndex = getLldpRemIfIndex(lldpRemTableEntry);
            if (lldpRemIfIndex == null || lldpRemIfIndex.intValue() == -1) {
                LOG.warn("processLldp: lldp remote ifindex not valid for local node/lldpLocalPortNumber: {}/{}", node.getNodeId(), lldpRemTableEntry.getLldpRemLocalPortNum());
                continue;
            }
            
            LldpRemInterface lldpremint = 
                new LldpRemInterface(lldpRemTableEntry.getLldpRemChassisidSubtype(), lldpRemTableEntry.getLldpRemChassiid(), lldpRemIfIndex, lldpLocIfIndex);
            lldpRemInterfaces.add(lldpremint);
        }
        node.setLldpRemInterfaces(lldpRemInterfaces);
    }

    private Map<Integer, LldpLocTableEntry> getLocalPortNumberToLocalTableEntryMap(
            SnmpCollection snmpcoll) {
        Map<Integer, LldpLocTableEntry> localPortNumberToLocTableEntryMap = new HashMap<Integer, LldpLocTableEntry>();
        for (final LldpLocTableEntry lldpLocTableEntry: snmpcoll.getLldpLocTable()) {
            localPortNumberToLocTableEntryMap.put(lldpLocTableEntry.getLldpLocPortNum(), lldpLocTableEntry);
        }
        return localPortNumberToLocTableEntryMap;

    }


    private Integer getLldpRemIfIndex(LldpRemTableEntry lldpRemTableEntry) {
        Integer ifindex = -1;
        switch (lldpRemTableEntry.getLldpRemPortidSubtype().intValue()) {
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
                ifindex = getFromSysnameIfAlias(lldpRemTableEntry.getLldpRemSysname(),
                                            lldpRemTableEntry.getLldpRemPortid());
                break;
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_PORTCOMPONENT:
                ifindex = getFromSysnamePortComponent(lldpRemTableEntry.getLldpRemSysname(),
                                                  lldpRemTableEntry.getLldpRemPortid());
                break;
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_MACADDRESS:
                ifindex = getFromSysnameMacAddress(lldpRemTableEntry.getLldpRemSysname(),
                                               lldpRemTableEntry.getLldpRemMacAddress());
                break;
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_NETWORKADDRESS: ifindex = getFromSysnameIpAddress(lldpRemTableEntry.getLldpRemSysname(),
                                              lldpRemTableEntry.getLldpRemIpAddress());
                break;
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACENAME:
                ifindex = getFromSysnameIfName(lldpRemTableEntry.getLldpRemSysname(),
                                           lldpRemTableEntry.getLldpRemPortid());
                break;
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID:
                ifindex = getFromSysnameAgentCircuitId(lldpRemTableEntry.getLldpRemSysname(),
                                                   lldpRemTableEntry.getLldpRemPortid());
                break;
            case LldpMibConstants.LLDP_PORTID_SUBTYPE_LOCAL:
                try {
                    ifindex = Integer.parseInt(lldpRemTableEntry.getLldpRemPortid());
                } catch (NumberFormatException e) {
                    ifindex = getFromSysnameIfName(lldpRemTableEntry.getLldpRemSysname(),
                                               lldpRemTableEntry.getLldpRemPortid());
                }
                break;
        }

        return ifindex;
    }
    
    private Integer getLldpLocIfIndex(String sysname,
            LldpLocTableEntry lldpLocTableEntry) {
        Integer ifindex = -1;
        switch (lldpLocTableEntry.getLldpLocPortIdSubtype().intValue()) {
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
            ifindex = getFromSysnameIfAlias(sysname,
                                            lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_PORTCOMPONENT:
            ifindex = getFromSysnamePortComponent(sysname,
                                                  lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_MACADDRESS:
            ifindex = getFromSysnameMacAddress(sysname,
                                               lldpLocTableEntry.getLldpLocMacAddress());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
            ifindex = getFromSysnameIpAddress(sysname,
                                              lldpLocTableEntry.getLldpLocIpAddress());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACENAME:
            ifindex = getFromSysnameIfName(sysname,
                                           lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID:
            ifindex = getFromSysnameAgentCircuitId(sysname,
                                                   lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_LOCAL:
            try {
                ifindex = Integer.parseInt(lldpLocTableEntry.getLldpLocPortid());
            } catch (NumberFormatException e) {
                ifindex = getFromSysnameIfName(sysname,
                                               lldpLocTableEntry.getLldpLocPortid());
            }
            break;
        }

        return ifindex;
    }
        
    protected Integer getFromSysnameAgentCircuitId(String lldpRemSysname,
            String lldpRemPortid) {
        LOG.warn("getFromSysnameAgentCircuitId: AgentCircuitId LLDP PortSubTypeId not supported");
        return null;
    }

    protected Integer getFromSysnameIfName(String lldpRemSysname,
            String lldpRemPortid) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.sysName", lldpRemSysname));
        criteria.add(Restrictions.eq("ifName", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()) {
            return interfaces.get(0).getIfIndex();
        }
        return null;
    }
    
    protected abstract Integer getFromSysnameIpAddress(String lldpRemSysname,
            InetAddress lldpRemIpAddr);

    protected Integer getFromSysnameMacAddress(String lldpRemSysname,
            String lldpRemPortid) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.sysName", lldpRemSysname));
        criteria.add(Restrictions.eq("physAddr", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()) {
            return interfaces.get(0).getIfIndex();
        }
        return null;
    }

    protected Integer getFromSysnamePortComponent(String lldpRemSysname,
            String lldpRemPortid) {
        LOG.warn("getFromSysnamePortComponent:PortComponent LLDP PortSubTypeId not supported");
        return null;
    }

    protected Integer getFromSysnameIfAlias(String lldpRemSysname,
            String lldpRemPortid) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.sysName", lldpRemSysname));
        criteria.add(Restrictions.eq("ifAlias", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()) {
            return interfaces.get(0).getIfIndex();
        }
        return null;
    }

    protected void processCdp(final LinkableNode node, final SnmpCollection snmpcoll, final Date scanTime) {
    	String cdpDeviceid = snmpcoll.getCdpGlobalGroup().getCdpDeviceId(); 
        LOG.debug("processCdp: Setting CDP device id {} for node {} with ip primary {}", cdpDeviceid,node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
    	node.setCdpDeviceId(cdpDeviceid);
        if (LOG.isDebugEnabled()) {
            if (snmpcoll.getCdpCacheTable().size() > 0) {
                LOG.debug("processCdp: Starting CDP cache table processing for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processCdp: Zero CDP cache table entries for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        List<CdpInterface> cdpInterfaces = new ArrayList<CdpInterface>();

        for (final CdpCacheTableEntry cdpEntry : snmpcoll.getCdpCacheTable()) {

            final int cdpIfIndex = cdpEntry.getCdpCacheIfIndex();
            if (cdpIfIndex < 0) {
                LOG.debug("processCdp: ifIndex not valid: {}", cdpIfIndex);
                continue;
            }
            LOG.debug("processCdp: ifIndex found: {}", cdpIfIndex);

            final String targetSysName = cdpEntry.getCdpCacheDeviceId();
            LOG.debug("processCdp: targetSysName found: {}", targetSysName);

            InetAddress cdpTargetIpAddr = cdpEntry.getCdpCacheIpv4Address();
            LOG.debug("processCdp: cdp cache ipa address found: {}", str(cdpTargetIpAddr));

            final int cdpAddrType = cdpEntry.getCdpCacheAddressType();

            Collection<Integer> targetCdpNodeIds = new ArrayList<Integer>();
            if (cdpAddrType != CdpInterface.CDP_ADDRESS_TYPE_IP_ADDRESS) {
                LOG.warn("processCdp: CDP address type not ip: {}", cdpAddrType);
            } else {
                if (cdpTargetIpAddr == null || cdpTargetIpAddr.isLoopbackAddress() || m_zeroAddress.equals(cdpTargetIpAddr)) {
                    LOG.debug("processCdp: IP address is not valid: {}", str(cdpTargetIpAddr));
                } else {
                    targetCdpNodeIds = getNodeidFromIp(cdpTargetIpAddr);
                    if (targetCdpNodeIds.isEmpty()) {
                        LOG.info("processCdp: No Target node IDs found: interface {} not added to linkable SNMP node. Skipping.", str(cdpTargetIpAddr));
                        sendNewSuspectEvent(cdpTargetIpAddr, snmpcoll.getTarget(), snmpcoll.getPackageName());
                        continue;
                    }
                }
            }

            if (targetCdpNodeIds.isEmpty()) {
                LOG.debug("processCdp: finding nodeids using CDP deviceid(sysname): {}", targetSysName);
                targetCdpNodeIds = getNodeIdsFromSysName(targetSysName);
            }

            for (final Integer targetCdpNodeId: targetCdpNodeIds) {
	            LOG.info("processCdp: Target node ID found: {}.", targetCdpNodeId);
	
	            final String cdpTargetDevicePort = cdpEntry.getCdpCacheDevicePort();
	
	            if (cdpTargetDevicePort == null) {
	                LOG.warn("processCdp: Target device port not found. Skipping.");
	                continue;
	            }
	
	            LOG.debug("processCdp: Target device port name found: {}", cdpTargetDevicePort);
	
	            final int cdpTargetIfindex = getIfIndexByName(targetCdpNodeId, cdpTargetDevicePort);
	
	            if (cdpTargetIfindex == -1) {
	                LOG.info("processCdp: No valid target ifIndex found but interface added to linkable SNMP node using ifindex  = -1.");
	            }
	            
	            if (cdpTargetIpAddr == null || cdpAddrType != CdpInterface.CDP_ADDRESS_TYPE_IP_ADDRESS) {
	                cdpTargetIpAddr = getIpInterfaceDao().findPrimaryInterfaceByNodeId(targetCdpNodeId).getIpAddress();
	            }
	            if (cdpTargetIpAddr != null && !m_linkd.isInterfaceInPackage(cdpTargetIpAddr, snmpcoll.getPackageName())) {
	                LOG.debug("processCdp: target IP address {} Not in package: {}.  Skipping.", str(cdpTargetIpAddr), snmpcoll.getPackageName());
	                continue;
	            }
	            
	            final CdpInterface cdpIface = new CdpInterface(cdpIfIndex);
	            cdpIface.setCdpTargetNodeId(targetCdpNodeId);
	            cdpIface.setCdpTargetIfIndex(cdpTargetIfindex);
	            cdpIface.setCdpTargetDeviceId(cdpEntry.getCdpCacheDeviceId());

	            LOG.debug("processCdp: Adding cdp interface {} to linkable node {}.", cdpIface, node.getNodeId());
	            cdpInterfaces.add(cdpIface);
            }
        }
        node.setCdpInterfaces(cdpInterfaces);
    }

    private List<Integer> getNodeIdsFromSysName(String targetSysName) {
        List<Integer> nodeids = new ArrayList<Integer>();
        final OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.add(Restrictions.eq("sysName", targetSysName));
        final List<OnmsNode> nodes = getNodeDao().findMatching(criteria);
        for (final OnmsNode node: nodes) {
            nodeids.add(node.getId());
        }
        return nodeids;
    }

    protected void processRouteTable(final OnmsNode onmsNode, final LinkableNode node, final SnmpCollection snmpcoll, final Date scanTime) {
        if (LOG.isDebugEnabled()) {
            final int routes = snmpcoll.getIpRouteTable().size();
            if (routes > 0) {
                LOG.debug("processRouteTable: Starting route table processing for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
                LOG.debug("processRouteTable: processing # {} routing interfaces", routes);
            } else {
                LOG.debug("processRouteTable: Zero route table entries for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        List<RouterInterface> routeInterfaces = new ArrayList<RouterInterface>();

        for (final SnmpStore ent : snmpcoll.getIpRouteTable()) {

        	IpRouteCollectorEntry route = (IpRouteCollectorEntry) ent;
         	
            final InetAddress nexthop = route.getIpRouteNextHop();
            final InetAddress routedest = route.getIpRouteDest();
            final InetAddress routemask = route.getIpRouteMask();

            LOG.debug("processRouteTable: processing routedest/routemask/routenexthop {}/{}/{}",str(routedest),str(routemask),str(nexthop));

            if (nexthop == null) {
                LOG.warn("processRouteTable: next hop not found on node {}. Skipping.", node.getNodeId());
                continue;
            } else if (nexthop.isLoopbackAddress()) {
                LOG.info("processRouteTable: next hop is a loopback address. Skipping.");
                continue;
            } else if (m_zeroAddress.equals(nexthop)) {
                LOG.info("processRouteTable: next hop is a broadcast address. Skipping.");
                continue;
            } else if (nexthop.isMulticastAddress()) {
                LOG.info("processRouteTable: next hop is a multicast address. Skipping.");
                continue;
            } else if (!getLinkd().isInterfaceInPackage(nexthop, snmpcoll.getPackageName())) {
                LOG.info("processRouteTable: nexthop address {} is not in package {}. Skipping.", str(nexthop), snmpcoll.getPackageName());
                continue;
            }

            if (routedest == null) {
                LOG.warn("processRouteTable: route destination not found on node {}. Skipping.", node.getNodeId());
                continue;
            }


            if (routemask == null) {
                LOG.warn("processRouteTable: route mask not found on node {}. Skipping.", node.getNodeId());
                continue;
            } else if (routemask.getHostAddress().equals("255.255.255.255")) {
                LOG.warn("processRouteTable: route mask 255.255.255.255 on node {}. Skipping.", node.getNodeId());
                continue;
            }


            Integer ifindex = route.getIpRouteIfIndex();
            
            if (ifindex == null) {
                LOG.warn("processRouteTable: Invalid ifIndex {} on node {}. Skipping.", ifindex, node.getNodeId());
                continue;
            }
        	
            final Integer routemetric1 = route.getIpRouteMetric1();
        	if (routemetric1 == null || routemetric1 < 0) {
                LOG.info("processRouteTable: Route metric is invalid. Skipping.");
                continue;
            } 

            LOG.debug("processRouteTable: parsing routeDest/routeMask/nextHop: {}/{}/{} - ifIndex = {}", str(routedest), str(routemask), str(nexthop), ifindex);

        	int snmpiftype = -2;
            if (ifindex == 0) {
			LOG.debug("processRouteTable: ifindex is 0. Looking local table to get a valid index.");
            	for (OnmsIpInterface ip : getIpInterfaceDao().findByNodeId(node.getNodeId())) {
            		InetAddress ipaddr = ip.getIpAddress();
            		InetAddress netmask = ip.getSnmpInterface().getNetMask();
				LOG.debug("processRouteTable: parsing ip {} with netmask {}.", str(ipaddr),str(netmask));
            		InetAddress net1 = Linkd.getNetwork(ip.getIpAddress(), netmask);
				LOG.debug("processRouteTable: found network {}.", str(net1));
        			
				LOG.debug("processRouteTable: getting network for nexthop {} with netmask {}.", str(nexthop),str(netmask));
        			InetAddress net2 = Linkd.getNetwork(nexthop, netmask);
				LOG.debug("processRouteTable: found network {}.", str(net2));
        			
            		if (str(net1).equals(str(net2))) {
            			ifindex = (ip.getIfIndex());
				LOG.debug("processRouteTable: ifindex {} found for local ip {}. ",ifindex, str(ip.getIpAddress()));
            			break;
            		}
            	}
            }
       	
            if (ifindex > 0)
                snmpiftype = getSnmpIfType(node.getNodeId(), ifindex);

            if (snmpiftype <= 0) {
                LOG.warn("processRouteTable: interface has an invalid ifType ({}).", snmpiftype);
            }
            
            if (getLinkd().forceIpRoutediscoveryOnEthernet(snmpcoll.getPackageName())) {
                LOG.debug("processRouteTable: forceIpRoutediscoveryOnEthernet is true, no validation for SNMP interface type");
            } else {
                LOG.debug("processRouteTable: forceIpRoutediscoveryOnEthernet is false, checking SNMP interface type");

                if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
                    LOG.debug("run: Ethernet interface for nexthop {}. Skipping.", nexthop);
                    continue;
                } else if (snmpiftype == SNMP_IF_TYPE_PROP_VIRTUAL) {
                    LOG.debug("run: PropVirtual interface for nodeid {}. Skipping.", nexthop);
                    continue;
                } else if (snmpiftype == SNMP_IF_TYPE_L2_VLAN) {
                    LOG.debug("run: Layer2 VLAN interface for nodeid {}. Skipping.", nexthop);
                    continue;
                } else if (snmpiftype == SNMP_IF_TYPE_L3_VLAN) {
                    LOG.debug("run: Layer3 VLAN interface for nodeid {}. Skipping.", nexthop);
                    continue;
                }
            }
            
            List<RouterInterface> routeIfaces = getRouteInterface(nexthop,ifindex);
            if (routeIfaces.isEmpty()) {
                LOG.info("processRouteTable: No node ID found for next hop IP address {}. Not adding the IP route interface to the linkable SNMP node.", str(nexthop));
                sendNewSuspectEvent(nexthop, snmpcoll.getTarget(), snmpcoll.getPackageName());
                continue;
            }
            for (RouterInterface routeIface: routeIfaces) {
                if (node.getNodeId() == routeIface.getNextHopNodeid()) {
                    LOG.debug("processRouteTable: node for IP next hop address {} is itself. Skipping.", str(nexthop));
                    continue;
                }
 	            routeInterfaces.add(routeIface);
            }
        }
        node.setRouteInterfaces(routeInterfaces);

        if (getLinkd().saveRouteTable(snmpcoll.getPackageName())) {
	        for (final SnmpStore ent : snmpcoll.getIpRouteTable()) {
	        	IpRouteCollectorEntry route = (IpRouteCollectorEntry) ent;
	            OnmsIpRouteInterface ipRouteInterface = route.getOnmsIpRouteInterface(new OnmsIpRouteInterface());
			LOG.debug("processRouteTable: persisting {}", ipRouteInterface);
	            ipRouteInterface.setNode(onmsNode);
	        	ipRouteInterface.setLastPollTime(scanTime);
	            ipRouteInterface.setStatus(StatusType.ACTIVE);
	            
	            saveIpRouteInterface(ipRouteInterface);
	        }
        }
    }

    protected void processVlanTable(final OnmsNode onmsNode, final LinkableNode node, final SnmpCollection snmpcoll, final Date scanTime) {
        if (LOG.isDebugEnabled()) {
            if (snmpcoll.getVlanTable().size() > 0) {
                LOG.debug("processVlanTable: Starting VLAN table processing for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processVlanTable: Zero VLAN table entries for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        final List<OnmsVlan> vlans = new ArrayList<OnmsVlan>();

        for (final SnmpStore ente : snmpcoll.getVlanTable()) {
        	
        	Vlan ent = (Vlan) ente;
            final OnmsVlan vlan = ent.getOnmsVlan();
            vlan.setLastPollTime(scanTime);
            vlan.setNode(onmsNode);
            vlan.setStatus(StatusType.ACTIVE);
            vlans.add(vlan);

            LOG.debug("processVlanTable: Saving VLAN entry: {}", vlan);

            saveVlan(vlan);

        }
    }

    protected void storeSnmpVlanCollection(final OnmsNode onmsNode, final LinkableNode node, final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl, final Date scanTime) {


        if (!snmpVlanColl.hasDot1dBase()) {
            LOG.debug("storeSnmpVlanCollection: No Bridge MIB informations found for Vlan: {}. Skipping...", vlan.getVlanName());
            return;
        }

        LOG.debug("storeSnmpVlanCollection: Starting Bridge MIB processing for Vlan: {}.", vlan.getVlanName());
        processDot1dBaseAndDot1dStp(onmsNode, node, vlan, snmpVlanColl,
				scanTime);
        
        if (snmpVlanColl.hasDot1dBasePortTable()) {
            processDot1dBasePortAndStpPortTables(onmsNode, node, vlan,
					snmpVlanColl, scanTime);
        }
        
        if (snmpVlanColl.hasDot1dTpFdbTable()) {
            processDot1DTpFdbTable(node, vlan, snmpVlanColl, scanTime);
        }

        if (snmpVlanColl.hasQBridgeDot1dTpFdbTable()) {
            processQBridgeDot1dTpFdbTable(node, vlan, snmpVlanColl);
        }

        // tt2295 removed adding physical mac address as bridge identifier 
        //for (final String physaddr : getPhysAddrs(node.getNodeId())) {
        //    LOG.debug("storeSnmpVlanCollection: Try to add Bridge Identifier \"{}\" for node {}", physaddr, node.getNodeId());
        //    if (physaddr == null || physaddr.equals("") || physaddr.equals("000000000000")) continue;
        //    LOG.info("storeSnmpVlanCollection: Adding Bridge Identifier {} for node {}", physaddr, node.getNodeId());
        //    node.addBridgeIdentifier(physaddr);
        //}

    }

	private void processDot1dBasePortAndStpPortTables(final OnmsNode onmsNode,
			final LinkableNode node, final OnmsVlan vlan,
			final SnmpVlanCollection snmpVlanColl, final Date scanTime) {
		Map<Integer, OnmsStpInterface> stpinterfaces = new HashMap<Integer, OnmsStpInterface>(snmpVlanColl.getDot1dBasePortTable().size());        
		stpinterfaces = processDot1DBasePortTable(onmsNode,node, scanTime, vlan, snmpVlanColl,stpinterfaces);
		    
		if (snmpVlanColl.hasDot1dStpPortTable()) {
		    stpinterfaces = processDot1StpPortTable(node, scanTime, vlan, snmpVlanColl, stpinterfaces);
		}

	    if (getLinkd().saveStpInterfaceTable(snmpVlanColl.getPackageName())) {
	    	for (OnmsStpInterface stpInterface: stpinterfaces.values()) {
		        LOG.debug("processDot1dBasePortAndStpPortTables: saving {} in stpinterface table", stpInterface);
		        saveStpInterface(stpInterface);
		    }
		}
	    
	    
    	for (OnmsStpInterface stpInterface: stpinterfaces.values()) {
    		if (stpInterface.getStpPortDesignatedBridge() == null ) continue;
    		if (stpInterface.getStpPortDesignatedBridge().substring(5, 16).equals(snmpVlanColl.getDot1dBase().getBridgeAddress())) {
		        LOG.debug("processDot1dBasePortAndStpPortTables: portdesignatedBridge is bridge itself {}. Nothing to add to linkable node ", snmpVlanColl.getDot1dBase().getBridgeAddress());
    			continue;
    		}
		LOG.debug("processDot1dBasePortAndStpPortTables: portdesignatedBridge/port {}/{} added to linkable node skipped", stpInterface.getStpPortDesignatedBridge(),stpInterface.getBridgePort());
    		node.addStpInterface(stpInterface);
    	}
	}

	private void processDot1dBaseAndDot1dStp(final OnmsNode onmsNode,
			final LinkableNode node, final OnmsVlan vlan,
			final SnmpVlanCollection snmpVlanColl, final Date scanTime) {
        
        final String baseBridgeAddress = snmpVlanColl.getDot1dBase().getBridgeAddress();
        if (baseBridgeAddress == null) {
            LOG.info("processDot1dBaseAndDot1dStp: Invalid base bridge address ({}) on node/vlan {}/{}", baseBridgeAddress, node.getNodeId(),vlan.getId());
            return;
        }

        LOG.debug("processDot1dBaseAndDot1dStp: Found Bridge Identifier {} for Vlan {}.", baseBridgeAddress, vlan.getVlanId());
        node.addBridgeIdentifier(baseBridgeAddress, vlan.getVlanId());
        
        if (snmpVlanColl.hasDot1dStp()) {
            LOG.debug("processDot1dBaseAndDot1dStp: processing Dot1dStpGroup in stpnode");
            final String stpDesignatedRoot = snmpVlanColl.getDot1dStp().getStpDesignatedRoot();

            if (stpDesignatedRoot != null ) {
                LOG.debug("processDot1dBaseAndDot1dStp: Dot1dStpGroup found valid stpDesignatedRoot {}, adding to Linkable node", stpDesignatedRoot);
                node.setVlanStpRoot(vlan.getVlanId(), stpDesignatedRoot);
            }
        }

        if (getLinkd().saveStpNodeTable(snmpVlanColl.getPackageName())) {
        	saveStpNode(getOnmsStpNode(onmsNode,node,scanTime, vlan, snmpVlanColl));
        }
	}

    protected void processQBridgeDot1dTpFdbTable(final LinkableNode node, final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getQBridgeDot1dFdbTable().size() > 0) {
                LOG.debug("processQBridgeDot1dTpFdbTable: Starting Q-BRIDGE-MIB dot1dTpFdb table processing for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processQBridgeDot1dTpFdbTable: Zero Q-BRIDGE-MIB dot1dTpFdb table entries for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final QBridgeDot1dTpFdbTableEntry dot1dfdbentry : snmpVlanColl.getQBridgeDot1dFdbTable()) {
            final String curMacAddress = dot1dfdbentry.getQBridgeDot1dTpFdbAddress();

            if (curMacAddress == null || curMacAddress.equals("000000000000")) {
                LOG.info("processQBridgeDot1DTpFdbTable: Invalid MAC addres {} on node {}. Skipping.", curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processQBridgeDot1DTpFdbTable: Found MAC address {} on node {}", curMacAddress, node.getNodeId());

            final int fdbport = dot1dfdbentry.getQBridgeDot1dTpFdbPort();

            if (fdbport == 0 || fdbport == -1) {
                LOG.debug("processQBridgeDot1DTpFdbTable: Invalid FDB port ({}) for MAC address {} on node {}. Skipping.", fdbport, curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processQBridgeDot1DTpFdbTable: Found bridge port {} on node {}.", fdbport, node.getNodeId());

            final int curfdbstatus = dot1dfdbentry.getQBridgeDot1dTpFdbStatus();

            if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_LEARNED) {
                node.addMacAddress(fdbport, curMacAddress, vlan.getVlanId());
                LOG.debug("processQBridgeDot1DTpFdbTable: Found learned status on bridge port.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_SELF) {
                node.addBridgeIdentifier(curMacAddress);
                LOG.debug("processQBridgeDot1DTpFdbTable: MAC address ({}) is used as bridge identifier.", curMacAddress);
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_INVALID) {
                LOG.debug("processQBridgeDot1DTpFdbTable: Found 'INVALID' status. Skipping.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_MGMT) {
                LOG.debug("processQBridgeDot1DTpFdbTable: Found 'MGMT' status. Skipping.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_OTHER) {
                LOG.debug("processQBridgeDot1DTpFdbTable: Found 'OTHER' status. Skipping.");
            } else if (curfdbstatus == -1) {
                LOG.warn("processQBridgeDot1DTpFdbTable: Unable to determine status. Skipping.");
            }
        }
    }

    protected void processDot1DTpFdbTable(LinkableNode node, final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl, Date scanTime) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getDot1dFdbTable().size() > 0) {
                LOG.debug("processDot1DTpFdbTable: Starting dot1dTpFdb table processing for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processDot1DTpFdbTable: Zero dot1dTpFdb table entries for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final Dot1dTpFdbTableEntry dot1dfdbentry : snmpVlanColl.getDot1dFdbTable()) {
            final String curMacAddress = dot1dfdbentry.getDot1dTpFdbAddress();
            final int fdbport = dot1dfdbentry.getDot1dTpFdbPort();
            final int curfdbstatus = dot1dfdbentry.getDot1dTpFdbStatus();

            if (curMacAddress == null || curMacAddress.equals("000000000000")) {
                LOG.info("processDot1DTpFdbTable: Invalid MAC address {} on node {}. Skipping.", curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processDot1DTpFdbTable: Found valid MAC address {} on node {}", curMacAddress, node.getNodeId());

            if (fdbport == 0 || fdbport == -1) {
                LOG.debug("processDot1DTpFdbTable: Invalid FDB port ({}) for MAC address {} on node {}. Skipping.", fdbport, curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processDot1DTpFdbTable: MAC address ({}) found on bridge port {} on node {}", curMacAddress, fdbport, node.getNodeId());

            if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_LEARNED && vlan.getVlanId() != null) {
                node.addMacAddress(fdbport, curMacAddress, vlan.getVlanId());
                LOG.debug("processDot1DTpFdbTable: Found learned status on bridge port.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_SELF) {
                node.addBridgeIdentifier(curMacAddress);
                LOG.debug("processDot1DTpFdbTable: MAC address ({}) is used as bridge identifier.", curMacAddress);
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_INVALID) {
                LOG.debug("processDot1DTpFdbTable: Found 'INVALID' status. Skipping.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_MGMT) {
                LOG.debug("processDot1DTpFdbTable: Found 'MGMT' status. Skipping.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_OTHER) {
                LOG.debug("processDot1DTpFdbTable: Found 'OTHER' status. Skipping.");
            } else if (curfdbstatus == -1) {
                LOG.warn("processDot1DTpFdbTable: Unable to determine status. Skipping.");
            }
        }
    }

    protected Map<Integer, OnmsStpInterface> processDot1StpPortTable(final LinkableNode node, final Date scanTime, final OnmsVlan vlan,SnmpVlanCollection snmpVlanColl, Map<Integer, OnmsStpInterface>stpinterfaces) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getDot1dStpPortTable().size() > 0) {
                LOG.debug("processDot1StpPortTable: Processing dot1StpPortTable for nodeid/ip for {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processDot1StpPortTable: Zero dot1StpPort table entries for nodeid/ip {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final Dot1dStpPortTableEntry dot1dstpptentry : snmpVlanColl.getDot1dStpPortTable()) {

            final Integer stpport = dot1dstpptentry.getDot1dStpPort();

            if (stpport == null || stpinterfaces.get(stpport) == null ) {
                LOG.info("processDot1StpPortTable: Found invalid bridge port. Skipping.");
                continue;
            }

            final OnmsStpInterface stpInterface = dot1dstpptentry.getOnmsStpInterface(stpinterfaces.get(stpport));

            LOG.debug("processDot1StpPortTable: found stpport/designatedbridge/designatedport {}/{}/{}", stpport ,stpInterface.getStpPortDesignatedBridge(), stpInterface.getStpPortDesignatedPort());
        }
        return stpinterfaces;
    }

    protected Map<Integer, OnmsStpInterface> processDot1DBasePortTable(final OnmsNode onmsNode, final LinkableNode node, final Date scanTime, final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl,Map<Integer, OnmsStpInterface>stpinterfaces) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getDot1dBasePortTable().size() > 0) {
                LOG.debug("processDot1DBasePortTable: Processing dot1BasePortTable for nodeid/ip {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processDot1DBasePortTable: Zero dot1BasePort table entries for nodeid/ip {}/{}", node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final Dot1dBasePortTableEntry dot1dbaseptentry : snmpVlanColl.getDot1dBasePortTable()) {
            int baseport = dot1dbaseptentry.getBaseBridgePort();
            int ifindex = dot1dbaseptentry.getBaseBridgePortIfindex();
            LOG.debug("processDot1DBasePortTable: processing bridge port ({}) with ifIndex ({}).", baseport, ifindex);

            if (baseport == -1 || ifindex == -1) {
                LOG.info("processDot1DBasePortTable: Invalid base port ({}) or ifIndex ({}). Skipping.", baseport, ifindex);
                continue;
            }

            node.setIfIndexBridgePort(ifindex, baseport);
                        
            final OnmsStpInterface stpInterface = new OnmsStpInterface(onmsNode, baseport, vlan.getVlanId());
            stpInterface.setBridgePort(baseport);
            stpInterface.setVlan(vlan.getVlanId());
            stpInterface.setIfIndex(ifindex);
            stpInterface.setStatus(StatusType.ACTIVE);
            stpInterface.setLastPollTime(scanTime);

            stpinterfaces.put(baseport, stpInterface);
        }
        return stpinterfaces;
    }

    protected OnmsStpNode getOnmsStpNode(final OnmsNode onmsNode,final LinkableNode node, final Date scanTime, final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl) {
        LOG.debug("getOnmsStpNode: Starting stpnode processing for Vlan: {}", vlan.getVlanName());

        LOG.debug("getOnmsStpNode: processing Dot1dBaseGroup in stpnode");
        OnmsStpNode stpNode = new OnmsStpNode(onmsNode, vlan.getVlanId());
        stpNode = snmpVlanColl.getDot1dBase().getOnmsStpNode(stpNode);
        stpNode.setLastPollTime(scanTime);
        stpNode.setStatus(StatusType.ACTIVE);
        stpNode.setBaseVlanName(vlan.getVlanName());

        if (snmpVlanColl.hasDot1dStp()) {
            LOG.debug("getOnmsStpNode: processing Dot1dStpGroup in stpnode");

            stpNode = snmpVlanColl.getDot1dStp().getOnmsStpNode(stpNode);

            if (stpNode.getStpDesignatedRoot() == null ) {
                LOG.debug("getOnmsStpNode: Dot1dStpGroup found stpDesignatedRoot null, not adding to Linkable node");
                stpNode.setStpDesignatedRoot("0000000000000000");
            } 
            LOG.debug("getOnmsStpNode: stpDesignatedRoot = {}", stpNode.getStpDesignatedRoot());
        }
        return stpNode;
    }

}
