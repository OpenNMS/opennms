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
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.provision.service.tasks.Task;
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
     *  
     *
     *  nodeScan:
     *  precond: foreignSource, foreignId of req'd node
     *  postcond: node
     *  - loadNode
     *  
     *  precond: node with primary address
     *  postcond: agents detected and agentScan lifeCycle for each agent started
     *            configuration for agents in lifecycle
     *  - detectAgents
     *    - agentScan for each agent
     *    
     *  precond: agent scans complete
     *  
     *  -update lastCapsdPoll
     *  
     *  
     *  agentScan:
     *  
     *  collectNodeInfo
     *  - this needs set the scan stamp
     *  
     *  persistNodeInfo
     *  
     *  scanForPhysicalInterfaces
     *  - for each found save the interface and set the scan stamp
     *    - make sure we update 
     *  
     *  scanForIpInterfaces
     *  - for each found save the interface and set the scan stmp
     *    - include the association with the physical interface
     *  - start the ipInterfaceScan for the interface
     *  
     *  
     *  ipInterfaceScan
     *  - scan for each service configured in the detector
     *  
     *  
     */
    
    @Activity( lifecycle = "nodeScan", phase = "loadNode" ) 
    public OnmsNode loadNode(@Attribute("foreignSource") String foreignSource, @Attribute("foreignId") String foreignId) {
        return m_provisionService.getRequisitionedNode(foreignSource, foreignId);
    }
    
    @Activity( lifecycle = "nodeScan", phase = "detectAgents" )
    public void detectAgents(LifeCycleInstance lifeCycle, Phase currentPhase, OnmsNode node) {
        // someday I'll change this to use agentDetectors
        OnmsIpInterface primaryIface = node.getPrimaryInterface();
        if (primaryIface.getMonitoredServiceByServiceType("SNMP") != null) {
            LifeCycleInstance nested = lifeCycle.createNestedLifeCycle(currentPhase, "agentScan");
            nested.setAttribute("agentType", "SNMP");
            nested.setAttribute("node", node);
            nested.setAttribute("primaryAddress", primaryIface.getInetAddress());
            currentPhase.add((Task)nested);
        }
        
    }
    
    @Activity( lifecycle = "nodeScan", phase = "deleteObsoleteResources", schedulingHint="write")
    public void deleteObsoleteResources() {
        System.err.println("nodeScan.deleteObsoletResources");
    }
    
    @Activity( lifecycle = "agentScan", phase = "collectNodeInfo" )
    public void collectNodeInfo(OnmsNode node, InetAddress primaryAddress) throws InterruptedException {

        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(primaryAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        SystemGroup systemGroup = new SystemGroup(primaryAddress);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "systemGroup", systemGroup);
        walker.start();
        
        walker.waitFor();
        
        systemGroup.updateSnmpDataForNode(node);
        
    }

    @Activity( lifecycle = "agentScan", phase = "persistNodeInfo", schedulingHint="write")
    @Attribute("nodeId")
    public Integer persistNodeInfo(OnmsNode node) {
        return m_provisionService.updateNodeAttributes(node).getId();
    }
    
    @Activity( lifecycle = "agentScan", phase = "detectPhysicalInterfaces" )
    public void detectPhysicalInterfaces(final Integer nodeId, final InetAddress primaryAddress, final Phase currentPhase) throws InterruptedException {
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(primaryAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        final PhysInterfaceTableTracker physIfTracker = new PhysInterfaceTableTracker() {
            @Override
            public void processPhysicalInterfaceRow(PhysicalInterfaceRow row) {
                System.out.println("Processing row for ifIndex "+row.getIfIndex());
                final OnmsSnmpInterface snmpIface = row.createInterfaceFromRow();
                Runnable r = new Runnable() {
                    public void run() {
                        System.out.println("Saving OnmsSnmpInterface "+snmpIface);
                        m_provisionService.updateSnmpInterfaceAttributes(
                                              nodeId,
                                              snmpIface);
                    }
                };
                currentPhase.add(r, "write");
            }
        };
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", physIfTracker);
        walker.start();
        walker.waitFor();
        
        System.err.println("detectPhysicalInterfaces");
    }

    @Activity( lifecycle = "agentScan", phase = "persistPhysicalInterfaces", schedulingHint="write" )
    public void persistPhysicalInterfaces(OnmsNode node) {
        System.err.println("persistIpInterfaces");
    }

    @Activity( lifecycle = "agentScan", phase = "detectIpInterfaces" )
    public void detectIpInterfaces(final Integer nodeId, final InetAddress primaryAddress, final Phase currentPhase) throws InterruptedException {
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(primaryAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        final IPInterfaceTableTracker ipIfTracker = new IPInterfaceTableTracker() {
            @Override
            public void processIPInterfaceRow(IPInterfaceRow row) {
                System.out.println("Processing row with ipAddr "+row.getIpAddress());
                if (!row.getIpAddress().startsWith("127.0.0")) {
                    final OnmsIpInterface iface = row.createInterfaceFromRow();
                    Runnable r = new Runnable() {
                        public void run() {
                            System.out.println("Saving OnmsIpInterface "+iface);
                            m_provisionService.updateIpInterfaceAttributes(nodeId, iface);
                        }
                    };
                    currentPhase.add(r, "write");
                }
            }
        };
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ipAddrTable", ipIfTracker);
        walker.start();
        walker.waitFor();
        
        System.err.println("detectIpInterfaces");
    }

    @Activity( lifecycle = "agentScan", phase = "persistIpInterfaces", schedulingHint="write" )
    public void persistIpInterfaces(OnmsNode node) {
        System.err.println("persistIpInterfaces");
    }

    
}
