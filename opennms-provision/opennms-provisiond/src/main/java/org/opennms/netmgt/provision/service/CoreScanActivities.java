/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import java.net.InetAddress;

import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;
import org.opennms.netmgt.provision.service.snmp.IfTable;
import org.opennms.netmgt.provision.service.snmp.IfXTable;
import org.opennms.netmgt.provision.service.snmp.IpAddrTable;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * CoreImportActivities
 *
 * @author brozow
 */
@ActivityProvider
public class CoreScanActivities {
    
    @Autowired
    private ProvisionService m_provisionService;
    
    @Autowired
    private SnmpAgentConfigFactory m_agentConfigFactory;
    

   /*
     * load the node from the database (or maybe the requistion)
     * 
     * walk the snmp interface table
     *   - add non-existent snmp interfaces to the database
     *   - update snmp interfaces that have changed
     *   - delete snmp interfaces that no longer exist
     * walk the ip interface table 
     *   - add non-existent ip interfaces to the database 
     *   - associate the ipinterface with the corresponding snmp interface
     *   - update ipInterfaces that have changed
     *   - delete ipInterfaces that no longer exist
     *   
     * for each ipinterface - detect services 
     *    - add serivces that have yet been detected/provisioned on the interface
     *    
     *    
     *  nodeScan.collectNodeInfo
     *  nodeScan.persistNodeInfo
     *  
     *  nodeScan.detectPhysicalInterfaces
     *  nodeScan.persistPhysicalInterfaces
     *  
     *  nodeScan.detectIpInterfaces
     *  nodeScan.persistIpInterfaces
     *  
     *  serviceDetect.detectIfService
     *  serviceDetect.persistIfService
     *  
     */


    @Activity( lifecycle = "nodeScan", phase = "collectNodeInfo" )
    public OnmsNode collectNodeInfo(@Attribute("foreignSource") String foreignSource, @Attribute("foreignId") String foreignId) throws InterruptedException {
        OnmsNode node = m_provisionService.getRequisitionedNode(foreignSource, foreignId);
        Assert.notNull(node, "node is null");
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        Assert.notNull(primaryInterface, "primaryInterface is null");
        InetAddress agentAddress = primaryInterface.getInetAddress();
        
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(agentAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        SystemGroup systemGroup = new SystemGroup(agentAddress);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "systemGroup", systemGroup);
        walker.start();
        
        walker.waitFor();
        
        systemGroup.updateSnmpDataForNode(node);
        
        return node;
        
    }

    @Activity( lifecycle = "nodeScan", phase = "persistNodeInfo", schedulingHint="write")
    public void persistNodeInfo(OnmsNode node) {
        m_provisionService.updateNodeAttributes(node);
    }

    @Activity( lifecycle = "nodeScan", phase = "detectPhysicalInterfaces" )
    public void detectPhysicalInterfaces(OnmsNode node) throws InterruptedException {
        InetAddress agentAddress = node.getPrimaryInterface().getInetAddress();
        
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(agentAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        IfTable ifTable = new IfTable(agentAddress);
        IfXTable ifXTable = new IfXTable(agentAddress);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", ifTable, ifXTable);
        walker.start();
        
        walker.waitFor();
        
        ifTable.updateSnmpInterfaceData(node);
        ifXTable.updateSnmpInterfaceData(node);
        System.err.println("detectPhysicalInterfaces");
    }

    @Activity( lifecycle = "nodeScan", phase = "persistPhysicalInterfaces", schedulingHint="write" )
    public void persistPhysicalInterfaces(OnmsNode node) {
        m_provisionService.updateNode(node, false, true);
    }

    @Activity( lifecycle = "nodeScan", phase = "detectIpInterfaces" )
    public void detectIpInterfaces(OnmsNode node) throws InterruptedException {
        InetAddress agentAddress = node.getPrimaryInterface().getInetAddress();
        
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(agentAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        IpAddrTable ipAddrTable = new IpAddrTable(agentAddress);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ipAddrTable", ipAddrTable);
        walker.start();
        
        walker.waitFor();
        
        ipAddrTable.updateIpInterfaceData(node);
    }

    @Activity( lifecycle = "nodeScan", phase = "persistIpInterfaces", schedulingHint="write" )
    public void persistIpInterfaces(OnmsNode node) {
        m_provisionService.updateNode(node, false, true);
    }

    
}
