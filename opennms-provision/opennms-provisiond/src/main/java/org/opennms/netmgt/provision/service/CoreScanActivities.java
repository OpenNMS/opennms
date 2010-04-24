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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.IoFutureListener;
import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.Callback;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.service.NodeScan.AgentScan;
import org.opennms.netmgt.provision.service.NodeScan.BaseAgentScan;
import org.opennms.netmgt.provision.service.NodeScan.IpInterfaceScan;
import org.opennms.netmgt.provision.service.NodeScan.NoAgentScan;
import org.opennms.netmgt.provision.service.lifecycle.Phase;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("standard")
    private EventForwarder m_eventForwarder;
    
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
    public void loadNode(BatchTask currentPhase, NodeScan nodeScan) {
        nodeScan.doLoadNode(currentPhase);
    }

    @Activity( lifecycle = "nodeScan", phase = "detectAgents" )
    public void detectAgents(Phase currentPhase, NodeScan nodeScan) {
        
        boolean foundAgent = false;

        if (!nodeScan.isAborted()) {
            OnmsIpInterface primaryIface = nodeScan.getNode().getPrimaryInterface();
            if (primaryIface != null && primaryIface.getMonitoredServiceByServiceType("SNMP") != null) {
                nodeScan.doAgentScan(currentPhase, primaryIface.getInetAddress(), "SNMP");
                foundAgent = true;
            }
            
            if (!foundAgent) {
                nodeScan.doNoAgentScan(currentPhase);
            }
        }
    }
    
    @Activity( lifecycle = "nodeScan", phase = "scanCompleted" )
    public void scanCompleted(BatchTask currentPhase, NodeScan nodeScan) {
        if (!nodeScan.isAborted()) {
            EventBuilder bldr = new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond");
            bldr.setNodeid(nodeScan.getNodeId());
            bldr.addParam(EventConstants.PARM_FOREIGN_SOURCE, nodeScan.getForeignSource());
            bldr.addParam(EventConstants.PARM_FOREIGN_ID, nodeScan.getForeignId());
            m_eventForwarder.sendNow(bldr.getEvent());
        }
    }

    @Activity( lifecycle = "agentScan", phase = "collectNodeInfo" )
    public void collectNodeInfo(BatchTask currentPhase, AgentScan agentScan) throws InterruptedException {
        
        Date scanStamp = new Date();
        agentScan.setScanStamp(scanStamp);
        
        InetAddress primaryAddress = agentScan.getAgentAddress();
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(primaryAddress);
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        SystemGroup systemGroup = new SystemGroup(primaryAddress);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "systemGroup", systemGroup);
        walker.start();
        
        walker.waitFor();
        
        if (walker.timedOut()) {
            agentScan.abort("Aborting node scan : Agent timedout while scanning the system table");
        }
        else if (walker.failed()) {
            agentScan.abort("Aborting node scan : Agent failed while scanning the system table: " + walker.getErrorMessage());
        } else {
        
            systemGroup.updateSnmpDataForNode(agentScan.getNode());

            List<NodePolicy> nodePolicies = m_provisionService.getNodePoliciesForForeignSource(agentScan.getForeignSource());

            OnmsNode node = agentScan.getNode();
            for(NodePolicy policy : nodePolicies) {
                if (node != null) {
                    node = policy.apply(node);
                }
            }

            if (node == null) {
                agentScan.abort("Aborted scan of node due to configured policy");
            } else {
                agentScan.setNode(node);
            }
        
        }
    }

    @Activity( lifecycle = "agentScan", phase = "persistNodeInfo", schedulingHint="write")
    public void persistNodeInfo(BatchTask currentPhase, AgentScan agentScan) {
        agentScan.doPersistNodeInfo();
    }

    @Activity( lifecycle = "agentScan", phase = "detectPhysicalInterfaces" )
    public void detectPhysicalInterfaces(final BatchTask currentPhase, final AgentScan agentScan) throws InterruptedException {
        if (agentScan.isAborted()) { return; }
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(agentScan.getAgentAddress());
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");
        
        final PhysInterfaceTableTracker physIfTracker = new PhysInterfaceTableTracker() {
            @Override
            public void processPhysicalInterfaceRow(PhysicalInterfaceRow row) {
                System.out.println("Processing row for ifIndex "+row.getIfIndex());
                OnmsSnmpInterface snmpIface = row.createInterfaceFromRow();
                snmpIface.setLastCapsdPoll(agentScan.getScanStamp());
                
                List<SnmpInterfacePolicy> policies = m_provisionService.getSnmpInterfacePoliciesForForeignSource(agentScan.getForeignSource());
                for(SnmpInterfacePolicy policy : policies) {
                    if (snmpIface != null) {
                        snmpIface = policy.apply(snmpIface);
                    }
                }
                
                if (snmpIface != null) {
                    final OnmsSnmpInterface snmpIfaceResult = snmpIface;

                    // add call to the snmp interface collection enable policies

                    Runnable r = new Runnable() {
                        public void run() {
                            System.out.println("Saving OnmsSnmpInterface "+snmpIfaceResult);
                            m_provisionService.updateSnmpInterfaceAttributes(
                                                                             agentScan.getNodeId(),
                                                                             snmpIfaceResult);
                        }
                    };
                    currentPhase.add(r, "write");
                }
            }
        };
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", physIfTracker);
        walker.start();
        walker.waitFor();

        if (walker.timedOut()) {
            agentScan.abort("Aborting node scan : Agent timedout while scanning the interfaces table");
        }
        else if (walker.failed()) {
            agentScan.abort("Aborting node scan : Agent failed while scanning the interfaces table: " + walker.getErrorMessage());
        }
        else {
            debug("Finished phase " + currentPhase);
        }
    }

    @Activity( lifecycle = "agentScan", phase = "detectIpInterfaces" )
    public void detectIpInterfaces(final Phase currentPhase, final AgentScan agentScan) throws InterruptedException {
        if (agentScan.isAborted()) { return; }
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(agentScan.getAgentAddress());
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory was not injected");

        // mark all provisioned interfaces as 'in need of scanning' so we can mark them
        // as scanned during ipAddrTable processing
        final Set<String> provisionedIps = new HashSet<String>();
        for(OnmsIpInterface provisioned : agentScan.getNode().getIpInterfaces()) {
            provisionedIps.add(provisioned.getIpAddress());
        }
        
        
        final IPInterfaceTableTracker ipIfTracker = new IPInterfaceTableTracker() {
            @Override
            public void processIPInterfaceRow(IPInterfaceRow row) {
                System.out.println("Processing row with ipAddr "+row.getIpAddress());
                if (!row.getIpAddress().startsWith("127.0.0")) {
                    
                    // mark any provisioned interface as scanned
                    provisionedIps.remove(row.getIpAddress());
                    
                    // save the interface
                    OnmsIpInterface iface = row.createInterfaceFromRow();
                    iface.setIpLastCapsdPoll(agentScan.getScanStamp());
                    
                    // add call to the ip interface is managed policies
                    iface.setIsManaged("M");
                    
                    List<IpInterfacePolicy> policies = m_provisionService.getIpInterfacePoliciesForForeignSource(agentScan.getForeignSource());
                    
                    for(IpInterfacePolicy policy : policies) {
                        if (iface != null) {
                            iface = policy.apply(iface);
                        }
                    }
                    
                    if (iface != null) {
                        currentPhase.add(ipUpdater(currentPhase, agentScan, iface), "write");
                    }
                    
                }
            }
        };
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ipAddrTable", ipIfTracker);
        walker.start();
        walker.waitFor();
        
        if (walker.timedOut()) {
            agentScan.abort("Aborting node scan : Agent timedout while scanning the ipAddrTable");
        }
        else if (walker.failed()) {
            agentScan.abort("Aborting node scan : Agent failed while scanning the ipAddrTable : " + walker.getErrorMessage());
        }
        else {


            // After processing the snmp provided interfaces then we need to scan any that 
            // were provisioned but missing from the ip table
            for(String ipAddr : provisionedIps) {
                OnmsIpInterface iface = agentScan.getNode().getIpInterfaceByIpAddress(ipAddr);
                iface.setIpLastCapsdPoll(agentScan.getScanStamp());
                iface.setIsManaged("M");

                currentPhase.add(ipUpdater(currentPhase, agentScan, iface), "write");

            }

            debug("Finished phase " + currentPhase);

        }
    }
    
    @Activity( lifecycle = "agentScan", phase = "deleteObsoleteResources", schedulingHint="write")
    public void deleteObsoleteResources(BatchTask currentPhase, AgentScan agentScan) {
        if (agentScan.isAborted()) { return; }

        m_provisionService.updateNodeScanStamp(agentScan.getNodeId(), agentScan.getScanStamp());
        
        m_provisionService.deleteObsoleteInterfaces(agentScan.getNodeId(), agentScan.getScanStamp());
        
        debug("Finished phase " + currentPhase);
    }
    
    @Activity( lifecycle = "agentScan", phase = "agentScanCompleted", schedulingHint="write")
    public void agentScanCompleted(BatchTask currentPhase, AgentScan agentScan) {
        if (!agentScan.isAborted()) {
            EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond");
            bldr.setNodeid(agentScan.getNodeId());
            bldr.setInterface(agentScan.getAgentAddress().getHostAddress());
            m_eventForwarder.sendNow(bldr.getEvent());
        }
        
    }
    
    @Activity( lifecycle = "noAgent", phase = "stampProvisionedInterfaces", schedulingHint="write")
    public void stampProvisionedInterfaces(Phase currentPhase, NoAgentScan scan) {
        if (scan.isAborted()) { return; }
        
        scan.setScanStamp(new Date());
        
        for(OnmsIpInterface iface : scan.getNode().getIpInterfaces()) {
            iface.setIpLastCapsdPoll(scan.getScanStamp());
            
            currentPhase.add(ipUpdater(currentPhase, scan, iface), "write");
            
        }
        
    }
    
    @Activity( lifecycle = "noAgent", phase = "deleteUnprovisionedInterfaces", schedulingHint="write")
    public void deleteObsoleteResources(BatchTask currentPhase, NoAgentScan scan) {

        m_provisionService.updateNodeScanStamp(scan.getNodeId(), scan.getScanStamp());
        
        m_provisionService.deleteObsoleteInterfaces(scan.getNodeId(), scan.getScanStamp());
        
        debug("Finished phase " + currentPhase);
    }
    
    
    
    @Activity( lifecycle = "ipInterfaceScan", phase = "detectServices" )
    public void detectServices(BatchTask currentPhase, IpInterfaceScan ifaceScan) {
        
        Collection<ServiceDetector> detectors = m_provisionService.getDetectorsForForeignSource(ifaceScan.getForeignSource());
        
        Integer nodeId = ifaceScan.getNodeId();
        InetAddress ipAddress = ifaceScan.getAddress();

        debug("detectServices for %d : %s: found %d detectors", nodeId, ipAddress.getHostAddress(), detectors.size());
        for(ServiceDetector detector : detectors) {
            addServiceDetectorTask(currentPhase, detector, nodeId, ipAddress);
        }
        
    }

    private void addServiceDetectorTask(BatchTask currentPhase, ServiceDetector detector, Integer nodeId, InetAddress ipAddress) {
        if (detector instanceof SyncServiceDetector) {
            addSyncServiceDetectorTask(currentPhase, nodeId, ipAddress, (SyncServiceDetector)detector);
        } else {
            addAsyncServiceDetectorTask(currentPhase, nodeId, ipAddress, (AsyncServiceDetector)detector);
        }
    }

    private void addAsyncServiceDetectorTask(BatchTask currentPhase, Integer nodeId, InetAddress ipAddress, AsyncServiceDetector detector) {
        currentPhase.add(runDetector(ipAddress, detector), persistService(nodeId, ipAddress, detector));
    }
    
    private void addSyncServiceDetectorTask(BatchTask currentPhase, final Integer nodeId, final InetAddress ipAddress, final SyncServiceDetector detector) {
        currentPhase.add(runDetector(ipAddress, detector, persistService(nodeId, ipAddress, detector)));
    }
        
    private Callback<Boolean> persistService(final Integer nodeId, final InetAddress ipAddress, final ServiceDetector detector) {
        return new Callback<Boolean>() {
            public void complete(Boolean serviceDetected) {
                info("Attempted to detect service %s on address %s: %s", detector.getServiceName(), ipAddress.getHostAddress(), serviceDetected);
                if (serviceDetected) {
                    m_provisionService.addMonitoredService(nodeId, ipAddress.getHostAddress(), detector.getServiceName());
                }
            }
            public void handleException(Throwable t) {
                info(t, "Exception occurred trying to detect service %s on address %s", detector.getServiceName(), ipAddress.getHostAddress());
            }
        };
    }
    
    private Async<Boolean> runDetector(InetAddress ipAddress, AsyncServiceDetector detector) {
        return new AsyncDetectorRunner(ipAddress, detector);
    }
    
    private Runnable runDetector(final InetAddress ipAddress, final SyncServiceDetector detector, final Callback<Boolean> cb) {
        return new Runnable() {
            public void run() {
                try {
                    info("Attemping to detect service %s on address %s", detector.getServiceName(), ipAddress.getHostAddress());
                    cb.complete(detector.isServiceDetected(ipAddress, new NullDetectorMonitor()));
                } catch (Throwable t) {
                    cb.handleException(t);
                }finally{
                    detector.dispose();
                }
            }
            @Override
            public String toString() {
                return String.format("Run detector %s on address %s", detector.getServiceName(), ipAddress.getHostAddress());
            }

        };
    }
        
    private class AsyncDetectorRunner implements Async<Boolean> {
        
        private final AsyncServiceDetector m_detector;
        private final InetAddress m_ipAddress;
        
        public AsyncDetectorRunner(InetAddress address, AsyncServiceDetector detector) {
            m_detector = detector;
            m_ipAddress = address;
        }

        public void submit(Callback<Boolean> cb) {
            try {
                info("Attemping to detect service %s on address %s", m_detector.getServiceName(), m_ipAddress.getHostAddress());
                DetectFuture future = m_detector.isServiceDetected(m_ipAddress, new NullDetectorMonitor());
                future.addListener(listener(cb));
            } catch (Throwable e) {
                cb.handleException(e);
            }
        }
        
        @Override
        public String toString() {
            return String.format("Run detector %s on address %s", m_detector.getServiceName(), m_ipAddress.getHostAddress());
        }

        private IoFutureListener<DetectFuture> listener(final Callback<Boolean> cb) {
            return new IoFutureListener<DetectFuture>() {
                public void operationComplete(DetectFuture future) {
                    try {
                        if (future.getException() != null) {
                            cb.handleException(future.getException());
                        } else {
                            cb.complete(future.isServiceDetected());
                        }
                    } finally{
                       m_detector.dispose();
                    }
                }
            };
        }
        
    }
    
    private Runnable ipUpdater(final Phase currentPhase,
            final BaseAgentScan agentScan, final OnmsIpInterface iface) {
        Runnable r = new Runnable() {
            public void run() {
                agentScan.doUpdateIPInterface(currentPhase, iface);
                if (iface.isManaged()) {
                    agentScan.triggerIPInterfaceScan(currentPhase, iface.getInetAddress());
                }
            }
        };
        return r;
    }
    
    

    
    @SuppressWarnings("unused")
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
    private void info(Throwable t, String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args), t);
        }
    }
    private void info(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args));
        }
    }
    @SuppressWarnings("unused")
    private void error(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        log.error(String.format(format, args));
    }

}
