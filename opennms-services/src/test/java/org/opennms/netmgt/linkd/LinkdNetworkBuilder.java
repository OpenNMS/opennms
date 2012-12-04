/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.SnmpInterfaceBuilder;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 */

public abstract class LinkdNetworkBuilder {

    private SnmpInterfaceDao m_snmpInterfaceDao;

    private NodeDao m_nodeDao;
    
    NetworkBuilder m_networkBuilder;

    void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    NetworkBuilder getNetworkBuilder() {
        if ( m_networkBuilder == null )
            m_networkBuilder = new NetworkBuilder();
        return m_networkBuilder;
    }

    OnmsNode getNode(String name, String sysoid, String primaryip,
            Map<InetAddress, Integer> ipinterfacemap,
            Map<Integer,String> ifindextoifnamemap,
            Map<Integer,String> ifindextomacmap, 
            Map<Integer,String> ifindextoifdescrmap,
            Map<Integer,String> ifindextoifalias) {
        return getNode(name, sysoid, primaryip, ipinterfacemap, ifindextoifnamemap, ifindextomacmap, ifindextoifdescrmap, ifindextoifalias, new HashMap<Integer, InetAddress>());
    }
    
    OnmsNode getNode(String name, String sysoid, String primaryip,
            Map<InetAddress, Integer> ipinterfacemap,
            Map<Integer,String> ifindextoifnamemap,
            Map<Integer,String> ifindextomacmap, 
            Map<Integer,String> ifindextoifdescrmap,
            Map<Integer,String> ifindextoifalias, 
            Map<Integer,InetAddress>ifindextonetmaskmap)
    {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setSysObjectId(sysoid).setSysName(name).setType("A");
        final Map<Integer, SnmpInterfaceBuilder> ifindexsnmpbuildermap = new HashMap<Integer, SnmpInterfaceBuilder>();
        for (Integer ifIndex: ifindextoifnamemap.keySet()) {
            ifindexsnmpbuildermap.put(ifIndex, nb.addSnmpInterface(ifIndex).
                                      setIfType(6).
                                      setIfName(ifindextoifnamemap.get(ifIndex)).
                                      setIfAlias(getSuitableString(ifindextoifalias, ifIndex)).
                                      setIfSpeed(100000000).
                                      setNetMask(getMask(ifindextonetmaskmap,ifIndex)).
                                      setPhysAddr(getSuitableString(ifindextomacmap, ifIndex)).setIfDescr(getSuitableString(ifindextoifdescrmap,ifIndex)));
        }
        
        for (InetAddress ipaddr: ipinterfacemap.keySet()) { 
            String isSnmpPrimary="N";
            Integer ifIndex = ipinterfacemap.get(ipaddr);
            if (ipaddr.getHostAddress().equals(primaryip))
                isSnmpPrimary="P";
            if (ifIndex == null)
                nb.addInterface(ipaddr.getHostAddress()).setIsSnmpPrimary(isSnmpPrimary).setIsManaged("M");
            else {
                nb.addInterface(ipaddr.getHostAddress(), ifindexsnmpbuildermap.get(ifIndex).getSnmpInterface()).
                setIsSnmpPrimary(isSnmpPrimary).setIsManaged("M");            }
        }
            
        return nb.getCurrentNode();
    }
    
    private InetAddress getMask(
            Map<Integer, InetAddress> ifindextonetmaskmap, Integer ifIndex) {
        if (ifindextonetmaskmap.containsKey(ifIndex))
            return ifindextonetmaskmap.get(ifIndex);
        return null;
    }

    private String getSuitableString(Map<Integer,String> ifindextomacmap, Integer ifIndex) {
        String value = "";
        if (ifindextomacmap.containsKey(ifIndex))
            value = ifindextomacmap.get(ifIndex);
        return value;
    }
    
    
    OnmsNode getNodeWithoutSnmp(String name, String ipaddr) {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setType("A");
        nb.addInterface(ipaddr).setIsSnmpPrimary("N").setIsManaged("M");
        return nb.getCurrentNode();
    }
    
    void printCdpRow(CdpCacheTableEntry cdpCacheTableEntry) {
        LogUtils.debugf(this, "-----------------------------------------------------------");    
        LogUtils.debugf(this, "getCdpCacheIfIndex: "+cdpCacheTableEntry.getCdpCacheIfIndex());
        LogUtils.debugf(this, "getCdpCacheDeviceIndex: "+cdpCacheTableEntry.getCdpCacheDeviceIndex());
        LogUtils.debugf(this, "getCdpCacheAddressType: "+cdpCacheTableEntry.getCdpCacheAddressType());
        LogUtils.debugf(this, "getCdpCacheAddress: "+cdpCacheTableEntry.getCdpCacheAddress());
        LogUtils.debugf(this, "getCdpCacheIpv4Address: "+cdpCacheTableEntry.getCdpCacheIpv4Address().getHostName());
        LogUtils.debugf(this, "getCdpCacheVersion: "+cdpCacheTableEntry.getCdpCacheVersion());
        LogUtils.debugf(this, "getCdpCacheDeviceId: "+cdpCacheTableEntry.getCdpCacheDeviceId());
        LogUtils.debugf(this, "getCdpCacheDevicePort: "+cdpCacheTableEntry.getCdpCacheDevicePort());
        LogUtils.debugf(this, "-----------------------------------------------------------");
        LogUtils.debugf(this, "");        
        
    }

    void printLldpRemRow(Integer lldpRemLocalPortNum, String lldpRemSysname, 
            String lldpRemChassiid,Integer lldpRemChassisidSubtype,String lldpRemPortid, Integer lldpRemPortidSubtype) {
        LogUtils.debugf(this, "-----------------------------------------------------------");    
        LogUtils.debugf(this, "getLldpRemLocalPortNum: "+lldpRemLocalPortNum);
        LogUtils.debugf(this, "getLldpRemSysname: "+lldpRemSysname);
        LogUtils.debugf(this, "getLldpRemChassiid: "+lldpRemChassiid);
        LogUtils.debugf(this, "getLldpRemChassisidSubtype: "+lldpRemChassisidSubtype);
        LogUtils.debugf(this, "getLldpRemPortid: "+lldpRemPortid);
        LogUtils.debugf(this, "getLldpRemPortidSubtype: "+lldpRemPortidSubtype);
        LogUtils.debugf(this, "-----------------------------------------------------------");
        LogUtils.debugf(this, "");        
    }
    
    void printLldpLocRow(Integer lldpLocPortNum,
            Integer lldpLocPortidSubtype, String lldpLocPortid) {
        LogUtils.debugf(this, "-----------------------------------------------------------");    
        LogUtils.debugf(this, "getLldpLocPortNum: "+lldpLocPortNum);
        LogUtils.debugf(this, "getLldpLocPortid: "+lldpLocPortid);
        LogUtils.debugf(this, "getLldpRemPortidSubtype: "+lldpLocPortidSubtype);
        LogUtils.debugf(this, "-----------------------------------------------------------");
        LogUtils.debugf(this, "");
      
    }
    
    void printLink(DataLinkInterface datalinkinterface) {
        LogUtils.debugf(this, "----------------Link------------------");
        Integer nodeid = datalinkinterface.getNode().getId();
        LogUtils.debugf(this, "linkid: " + datalinkinterface.getId());
        LogUtils.debugf(this, "nodeid: " + nodeid);
        LogUtils.debugf(this, "nodelabel: " + m_nodeDao.get(nodeid).getLabel());       
        Integer ifIndex = datalinkinterface.getIfIndex();
        LogUtils.debugf(this, "ifindex: " + ifIndex);
        if (ifIndex > 0)
            LogUtils.debugf(this, "ifname: " + m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeid,ifIndex).getIfName());
        Integer nodeparent = datalinkinterface.getNodeParentId();
        LogUtils.debugf(this, "nodeparent: " + nodeparent);
        LogUtils.debugf(this, "parentnodelabel: " + m_nodeDao.get(nodeparent).getLabel());
        Integer parentifindex = datalinkinterface.getParentIfIndex();
        LogUtils.debugf(this, "parentifindex: " + parentifindex);        
        if (parentifindex > 0)
            LogUtils.debugf(this, "parentifname: " + m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeparent,parentifindex).getIfName());
        LogUtils.debugf(this, "--------------------------------------");
        LogUtils.debugf(this, "");

    }
    
    void checkLink(OnmsNode node, OnmsNode nodeparent, int ifindex, int parentifindex, DataLinkInterface datalinkinterface) {
        printLink(datalinkinterface);
        printNode(node);
        printNode(nodeparent);
        assertEquals(node.getId(),datalinkinterface.getNode().getId());
        assertEquals(ifindex,datalinkinterface.getIfIndex().intValue());
        assertEquals(nodeparent.getId(), datalinkinterface.getNodeParentId());
        assertEquals(parentifindex,datalinkinterface.getParentIfIndex().intValue());
    }

    private void printNode(OnmsNode node) {
        LogUtils.debugf(this, "----------------Node------------------");
        LogUtils.debugf(this, "nodeid: " + node.getId());
        LogUtils.debugf(this, "nodelabel: " + node.getLabel());
        LogUtils.debugf(this, "--------------------------------------");
        LogUtils.debugf(this, "");
        
    }
    
    int getStartPoint(List<DataLinkInterface> links) {
        int start = 0;
        for (final DataLinkInterface link:links) {
            if (start==0 || link.getId().intValue() < start)
                start = link.getId().intValue();                
        }
        return start;
    }
    
    void printipInterface(String nodeStringId,OnmsIpInterface ipinterface) {
    	LogUtils.debugf(this, nodeStringId+"_IP_IF_MAP.put(InetAddress.getByName(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
    }
    
    void printSnmpInterface(String nodeStringId,OnmsSnmpInterface snmpinterface) {
        if ( snmpinterface.getIfName() != null)
        	LogUtils.debugf(this, nodeStringId+"_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
            if (snmpinterface.getIfDescr() != null)
            	LogUtils.debugf(this, nodeStringId+"_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
            if (snmpinterface.getPhysAddr() != null)
            	LogUtils.debugf(this, nodeStringId+"_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
            if (snmpinterface.getIfAlias() != null)
            	LogUtils.debugf(this, nodeStringId+"_IF_IFALIAS_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfAlias()+"\");");            
            if (snmpinterface.getNetMask() != null && !snmpinterface.getNetMask().getHostAddress().equals("127.0.0.1"))
            	LogUtils.debugf(this, nodeStringId+"_IF_NETMASK_MAP.put("+snmpinterface.getIfIndex()+", InetAddress.getByName(\""+snmpinterface.getNetMask().getHostAddress()+"\"));");
        
    }

}
