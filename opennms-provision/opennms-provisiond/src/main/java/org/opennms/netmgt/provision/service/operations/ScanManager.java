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
// Modifications:
//
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8
package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.provision.service.snmp.IfSnmpCollector;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;

public class ScanManager {
    
    private IfSnmpCollector m_collector;

    ScanManager(InetAddress address) {
        m_collector = new IfSnmpCollector(address);
    }
    
    public IfSnmpCollector getCollector() {
        return m_collector;
    }

    public void updateSnmpDataForResource(ScanResource sr) {
        if (getCollector() != null && getCollector().hasSystemGroup()) {
            sr.setAttribute("sysContact", getSystemGroup().getSysContact());
            sr.setAttribute("sysDescription", getSystemGroup().getSysDescr());
            sr.setAttribute("sysLocation", getSystemGroup().getSysLocation());
            sr.setAttribute("sysObjectId", getSystemGroup().getSysObjectID());
        }
    }

    /**
     * @return
     */
    private SystemGroup getSystemGroup() {
        return getCollector().getSystemGroup();
    }

    void resolveIpHostname(OnmsIpInterface ipIf) {
    	ipIf.setIpHostName(ipIf.getIpAddress());
    //
    //     DON'T DO THIS SINCE DNS DOESN'T RELIABLY AVOID HANGING
    //
    //    	log().info("Resolving Hostname for "+ipIf.getIpAddress());
    //		try {
    //			InetAddress addr = InetAddress.getByName(ipIf.getIpAddress());
    //			ipIf.setIpHostName(addr.getHostName());
    //		} catch (Exception e) {
    //			if (ipIf.getIpHostName() == null)
    //				ipIf.setIpHostName(ipIf.getIpAddress());
    //		}
    }

    Integer getIfType(int ifIndex) {
        int ifType = getCollector().getIfType(ifIndex);
    	return (ifType == -1 ? null : new Integer(ifType));
    }

    String getNetMask(int ifIndex) {
        InetAddress addr = getCollector().getIpAddrTable().getNetMask(ifIndex);
        return (addr == null ? null : addr.getHostAddress());
    }

    Integer getAdminStatus(int ifIndex) {
        int adminStatus = getCollector().getAdminStatus(ifIndex);
    	return (adminStatus == -1 ? null : new Integer(adminStatus));
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    void updateSnmpDataforInterface(OnmsIpInterface ipIf) {
    
        OnmsNode node = ipIf.getNode();
        if (getCollector() == null || !getCollector().hasIpAddrTable() || !getCollector().hasIfTable()) return;
    
    	String ipAddr = ipIf.getIpAddress();
    	log().debug("Creating SNMP info for interface "+ipAddr);
    
    	InetAddress inetAddr = ipIf.getInetAddress();
    
    	int ifIndex = getCollector().getIfIndex(inetAddr);
    	if (ifIndex == -1) return;
    
        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf = node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf == null) {
            // if not then create one
            snmpIf = new OnmsSnmpInterface(ipAddr, new Integer(ifIndex), node);
            snmpIf.setIfAlias(getCollector().getIfAlias(ifIndex));
            snmpIf.setIfName(getCollector().getIfName(ifIndex));
            snmpIf.setIfType(getIfType(ifIndex));
            snmpIf.setNetMask(getNetMask(ifIndex));
            snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
            snmpIf.setIfDescr(getCollector().getIfDescr(ifIndex));
            snmpIf.setIfSpeed(getCollector().getIfSpeed(ifIndex));
            snmpIf.setPhysAddr(getCollector().getPhysAddr(ifIndex));
        }
        
        if (ipIf.getIsSnmpPrimary() == CollectionType.PRIMARY) {
            // make sure the snmpIf has the ipAddr of the primary interface
            snmpIf.setIpAddress(ipAddr);
        }
    	
    	ipIf.setSnmpInterface(snmpIf);
    
    	//FIXME: Improve OpenNMS to provide these values
    	// ifOperStatus
    }

    boolean isSnmpDataForInterfacesUpToDate() {
        return getCollector() != null && getCollector().hasIfTable() && getCollector().hasIpAddrTable();
    }

    boolean isSnmpDataForNodeUpToDate() {
        return getCollector() != null && getCollector().hasSystemGroup();
    }

    void updateSnmpData(OnmsNode node) {
        if (getCollector() != null) 
        	getCollector().run();
        
        ScanResource sr = new ScanResource("SNMP");
        sr.setNode(node);
        updateSnmpDataForResource(sr);
        
        for (OnmsIpInterface ipIf : node.getIpInterfaces()) {
            resolveIpHostname(ipIf);
            updateSnmpDataforInterface(ipIf);
        }
    }

}
