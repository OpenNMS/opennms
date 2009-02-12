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
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.IoFutureListener;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.provision.service.tasks.DefaultTaskCoordinator;
import org.opennms.netmgt.provision.service.tasks.Task;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
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
    
    @Autowired
    private DefaultTaskCoordinator m_taskCoordinator;
    
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
    public void detectAgents(Phase currentPhase, OnmsNode node) {
        // someday I'll change this to use agentDetectors
        OnmsIpInterface primaryIface = node.getPrimaryInterface();
        if (primaryIface.getMonitoredServiceByServiceType("SNMP") != null) {
            currentPhase.createNestedLifeCycle("agentScan")
                .setAttribute("agentType", "SNMP")
                .setAttribute("node", node)
                .setAttribute("foreignSource", node.getForeignSource())
                .setAttribute("foreignId", node.getForeignId())
                .setAttribute("primaryAddress", primaryIface.getInetAddress())
                .trigger();
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

    @Activity( lifecycle = "agentScan", phase = "detectIpInterfaces" )
    public void detectIpInterfaces(@Attribute("foreignSource") final String foreignSource, final Integer nodeId, final InetAddress primaryAddress, final Phase currentPhase) throws InterruptedException {
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
                            
                            currentPhase.createNestedLifeCycle("ipInterfaceScan")
                                .setAttribute("foreignSource", foreignSource)
                                .setAttribute("nodeId", nodeId)
                                .setAttribute("ipAddress", iface.getInetAddress())
                                .trigger();

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

    @Activity( lifecycle = "ipInterfaceScan", phase = "detectServices" )
    public void detectServices(@Attribute("foreignSource") final String foreignSource, final Integer nodeId, final InetAddress ipAddress, final Phase currentPhase) throws InterruptedException {
        
        Collection<ServiceDetector> detectors = m_provisionService.getDetectorsForForeignSource(foreignSource);
        
        System.err.println(String.format("detectServices for %d : %s: found %d detectors", nodeId, ipAddress, detectors.size()));
        for(ServiceDetector detector : detectors) {
            currentPhase.add(createServiceDetectorTask(detector, nodeId, ipAddress));
        }
        
    }

    private Task createServiceDetectorTask(ServiceDetector detector, Integer nodeId, InetAddress ipAddress) {
        if (detector instanceof SyncServiceDetector) {
            return createSyncServiceDetectorTask((SyncServiceDetector)detector, nodeId, ipAddress);
        } else {
            return createAsyncServiceDetectorTask((AsyncServiceDetector)detector, nodeId, ipAddress);
        }
    }

    private Task createSyncServiceDetectorTask(final SyncServiceDetector detector, final Integer nodeId, final InetAddress ipAddress) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    info("Attemping to detect service %s on address %s", detector.getServiceName(), ipAddress);
                    boolean serviceDetected = detector.isServiceDetected(ipAddress, new NullDetectorMonitor());
                    info("Attempted to detect service %s on address %s: %s", detector.getServiceName(), ipAddress, serviceDetected);
                    if (serviceDetected) {
                        m_provisionService.addMonitoredService(nodeId, ipAddress.getHostAddress(), detector.getServiceName());
                    }
                } catch(Throwable t) {
                    error(t, "Unhandle exception/error while detecto service %s on address %s", detector.getServiceName(), ipAddress);
                }
            }
        };
        
        return m_taskCoordinator.createTask(r, "scan");
    }
    
    private abstract class AsyncServiceDetectorTask extends Task implements IoFutureListener<DetectFuture> {
        
        private final AsyncServiceDetector m_detector;
        private final InetAddress m_address;

        public AsyncServiceDetectorTask(DefaultTaskCoordinator coordinator, AsyncServiceDetector detector, InetAddress address) {
            super(coordinator);
            m_detector = detector;
            m_address = address;
        }

        @Override
        protected void doSubmit() {
            try {
                info("Attemping to detect service %s on address %s", m_detector.getServiceName(), m_address);
                DetectFuture future = m_detector.isServiceDetected(m_address, new NullDetectorMonitor());
                future.addListener(this);
            } catch (Exception e) {
                error(e, "Unexpected exception detecting service %s for interface %s", m_detector.getServiceName(), m_address);
                markTaskAsCompleted();
            }
        }

        public void operationComplete(DetectFuture future) {
            try {
                boolean serviceDetected = future.isServiceDetected();
                info("Attempted to detect service %s on address %s: %s", m_detector.getServiceName(), m_address, serviceDetected);
                if (serviceDetected) {
                    serviceDetected();
                }
            } finally {
                markTaskAsCompleted();
            }
        }

        protected abstract void serviceDetected();
        
    }

    private Task createAsyncServiceDetectorTask(final AsyncServiceDetector detector, final Integer nodeId, final InetAddress ipAddress) {
        return new AsyncServiceDetectorTask(m_taskCoordinator, detector, ipAddress) {

            @Override
            protected void serviceDetected() {
                m_provisionService.addMonitoredService(nodeId, ipAddress.getHostAddress(), detector.getServiceName());
            }
            
        };
    }
    
    
    private void error(Throwable t, String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        log.error(String.format(format, args), t);
    }

    private void debug(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        };
    }
    private void info(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args));
        }
    }
    private void error(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        log.error(String.format(format, args));
    }

    
}
