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

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.mina.core.future.IoFutureListener;
import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.Callback;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.service.NodeScan.IpInterfaceScan;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.ActivityProvider;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
    public void detectAgents(BatchTask currentPhase, NodeScan nodeScan) {
        
        boolean foundAgent = false;

        if (!nodeScan.isAborted()) {
            OnmsIpInterface primaryIface = nodeScan.getNode().getPrimaryInterface();
            if (primaryIface != null && primaryIface.getMonitoredServiceByServiceType("SNMP") != null) {
                // Make AgentScan a NeedContainer class and have that call run
                nodeScan.createAgentScan(primaryIface.getInetAddress(), "SNMP").run(currentPhase);
                foundAgent = true;
            }
            
            if (!foundAgent) {
                currentPhase.add(nodeScan.createNoAgentScan());
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

    @Activity( lifecycle = "ipInterfaceScan", phase = "detectServices" )
    public void detectServices(BatchTask currentPhase, IpInterfaceScan ifaceScan) {
        
        Collection<ServiceDetector> detectors = m_provisionService.getDetectorsForForeignSource(ifaceScan.getForeignSource());
        
        Integer nodeId = ifaceScan.getNodeId();
        InetAddress ipAddress = ifaceScan.getAddress();
        
        debugf(this, "detectServices for %d : %s: found %d detectors", nodeId, ipAddress.getHostAddress(), detectors.size());
        
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
                Object[] args = { detector.getServiceName(), ipAddress.getHostAddress(), serviceDetected };
                infof(this, "Attempted to detect service %s on address %s: %s", args);
                if (serviceDetected) {
                    m_provisionService.addMonitoredService(nodeId, ipAddress.getHostAddress(), detector.getServiceName());
                }
            }
            public void handleException(Throwable t) {
                Object[] args = { detector.getServiceName(), ipAddress.getHostAddress() };
                infof(this, t, "Exception occurred trying to detect service %s on address %s", args);
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
                    Object[] args = { detector.getServiceName(), ipAddress.getHostAddress() };
                    infof(this, "Attemping to detect service %s on address %s", args);
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
                Object[] args = { m_detector.getServiceName(), m_ipAddress.getHostAddress() };
                infof(this, "Attemping to detect service %s on address %s", args);
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

}
