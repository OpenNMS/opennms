/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
import static org.opennms.core.utils.LogUtils.tracef;
import static org.opennms.core.utils.LogUtils.warnf;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.NeedsContainer;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.util.Assert;

/**
 * <p>NodeScan class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NodeScan implements RunInBatch {

    private Integer m_nodeId;
    private String m_foreignSource;
    private String m_foreignId;
    private Date m_scanStamp;
    private ProvisionService m_provisionService;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    private DefaultTaskCoordinator m_taskCoordinator;

    //NOTE TO SELF: This is referenced from the AgentScan inner class
    private boolean m_aborted = false;
    
    private OnmsNode m_node;
    private boolean m_agentFound = false;

    /**
     * <p>Constructor for NodeScan.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param agentConfigFactory a {@link org.opennms.netmgt.dao.SnmpAgentConfigFactory} object.
     * @param taskCoordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     */
    public NodeScan(final Integer nodeId, final String foreignSource, final String foreignId, final ProvisionService provisionService, final EventForwarder eventForwarder, final SnmpAgentConfigFactory agentConfigFactory, final DefaultTaskCoordinator taskCoordinator) {
        m_nodeId = nodeId;
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_scanStamp = new Date();
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_agentConfigFactory = agentConfigFactory;
        m_taskCoordinator = taskCoordinator;

    }
    
    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }
    
    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        return m_foreignId;
    }
    
    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId() {
        return m_nodeId;
    }
    
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsNode getNode() {
        return m_node;
    }
    
    /**
     * @param agentFound the agentFound to set
     */
    private void setAgentFound(final boolean agentFound) {
        m_agentFound = agentFound;
    }

    /**
     * @return the agentFound
     */
    private boolean isAgentFound() {
        return m_agentFound;
    }

    /**
     * <p>getScanStamp</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getScanStamp() {
        return m_scanStamp;
    }



    /**
     * <p>getProvisionService</p>
     *
     * @return the provisionService
     */
    public ProvisionService getProvisionService() {
        return m_provisionService;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return the eventForwarder
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    
    /**
     * <p>getTaskCoordinator</p>
     *
     * @return a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     */
    public DefaultTaskCoordinator getTaskCoordinator() {
        return m_taskCoordinator;
    }

    /**
     * <p>isAborted</p>
     *
     * @return a boolean.
     */
    public boolean isAborted() {
        return m_aborted;
    }
    
    /**
     * <p>abort</p>
     *
     * @param reason a {@link java.lang.String} object.
     */
    public void abort(final String reason) {
        m_aborted = true;
        
        infof(this, "Aborting Scan of node %d for the following reason: %s", m_nodeId, reason);
        
        final EventBuilder bldr = new EventBuilder(EventConstants.PROVISION_SCAN_ABORTED_UEI, "Provisiond");
        if (m_nodeId != null) {
            bldr.setNodeid(m_nodeId);
        }
        bldr.addParam(EventConstants.PARM_FOREIGN_SOURCE, m_foreignSource);
        bldr.addParam(EventConstants.PARM_FOREIGN_ID, m_foreignId);
        bldr.addParam(EventConstants.PARM_REASON, reason);
        
        m_eventForwarder.sendNow(bldr.getEvent());
        
    }

    Task createTask() {
        return getTaskCoordinator().createBatch().add(NodeScan.this).get();
    }
    
    /** {@inheritDoc} */
    public void run(final BatchTask parent) {
        infof(this, "Scanning node %d/%s/%s", m_nodeId, m_foreignSource, m_foreignId);

        parent.getBuilder().addSequence(
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        loadNode(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        detectAgents(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        handleAgentUndetected(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        scanCompleted(phase);
                    }
                }
        );
        
        
    }
    

    ScheduledFuture<?> schedule(ScheduledExecutorService executor, NodeScanSchedule schedule) {
        
    	final Runnable r = new Runnable() {
            public void run() {
                try {
                    
                	final Task t = createTask();
                    t.schedule();
                    t.waitFor();
                    
                    debugf(NodeScan.this, "Finished scanning node %d/%s/%s", getNodeId(), getForeignSource(), getForeignId());
                } catch (final InterruptedException e) {
                    warnf(NodeScan.this, e, "The node scan for node %d/%s/%s was interrupted", getNodeId(), getForeignSource(), getForeignId());
                } catch (final ExecutionException e) {
                    warnf(NodeScan.this, e, "An error occurred while scanning node %d/%s/%s", getNodeId(), getForeignSource(), getForeignId());
                }
            }
        };
        
        final ScheduledFuture<?> future = executor.scheduleWithFixedDelay(r, schedule.getInitialDelay().getMillis(), schedule.getScanInterval().getMillis(), TimeUnit.MILLISECONDS);
        return future;
    }

    /**
     * <p>loadNode</p>
     *
     * @param loadNode a {@link org.opennms.core.tasks.BatchTask} object.
     */
    public void loadNode(final BatchTask loadNode) {
        if (getForeignSource() != null) {
            m_node = m_provisionService.getRequisitionedNode(getForeignSource(), getForeignId());
            if (m_node == null) {
                abort(String.format("Unable to get requisitioned node (%s/%s): aborted", m_foreignSource, m_foreignId));
            } else {
                for(final OnmsIpInterface iface : m_node.getIpInterfaces()) {
                    loadNode.add(new IpInterfaceScan(getNodeId(), iface.getInetAddress(), getForeignSource(), getProvisionService()));
                }
            }
        } else {
            m_node = m_provisionService.getNode(m_nodeId);
        }

    }

    /**
     * <p>createAgentScan</p>
     *
     * @param agentAddress a {@link java.net.InetAddress} object.
     * @param agentType a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.NodeScan.AgentScan} object.
     */
    public AgentScan createAgentScan(final InetAddress agentAddress, final String agentType) {
        return new AgentScan(getNodeId(), getNode(), agentAddress, agentType);
    }
    
    NoAgentScan createNoAgentScan() {
        return new NoAgentScan(getNodeId(), getNode());
    }
 
    /**
     * AgentScan
     *
     * @author brozow
     */
    public class AgentScan extends BaseAgentScan implements NeedsContainer, ScanProgress {

        private InetAddress m_agentAddress;
        private String m_agentType;
        private Map<Integer,String> m_nodeMap;

        public AgentScan(final Integer nodeId, final OnmsNode node, final InetAddress agentAddress, final String agentType) {
            super(nodeId, node);
            m_agentAddress = agentAddress;
            m_agentType = agentType;
            m_nodeMap = new HashMap<Integer,String>();
        }
        
        public InetAddress getAgentAddress() {
            return m_agentAddress;
        }
        
        public String getAgentType() {
            return m_agentType;
        }

        public void setNode(final OnmsNode node) {
            m_node = node;
        }
            
        public String toString() {
            return new ToStringBuilder(this)
                .append("address", m_agentAddress)
                .append("type", m_agentType)
                .toString();
        }
        
        public EventForwarder getEventForwarder() {
            return m_eventForwarder;
        }

        void completed() {
            if (!isAborted()) {
            	final EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond");
                bldr.setNodeid(getNodeId());
                bldr.setInterface(getAgentAddress().getHostAddress());
                getEventForwarder().sendNow(bldr.getEvent());
            }
        }

        void deleteObsoleteResources() {
            if (!isAborted()) {
                getProvisionService().updateNodeScanStamp(getNodeId(), getScanStamp());
                getProvisionService().deleteObsoleteInterfaces(getNodeId(), getScanStamp());
                debugf(this, "Finished deleteObsoleteResources for %s", this);
            }
        }

        public SnmpAgentConfigFactory getAgentConfigFactory() {
            return m_agentConfigFactory;
        }

        public void detectIpInterfaces(final BatchTask currentPhase) {
			infof(this, "detecting IP interfaces for node %d/%s/%s", getNode().getId(), getNode().getForeignSource(), getNode().getForeignId());
            if (!isAborted()) { 
                SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(getAgentAddress());
                Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");

                // mark all provisioned interfaces as 'in need of scanning' so we can mark them
                // as scanned during ipAddrTable processing
                final Set<String> provisionedIps = new HashSet<String>();
                if (getForeignSource() != null) {
                    for(final OnmsIpInterface provisioned : getNode().getIpInterfaces()) {
                        provisionedIps.add(provisioned.getIpAddress());
                    }
                }


                final IPInterfaceTableTracker ipIfTracker = new IPInterfaceTableTracker() {
                    @Override
                    public void processIPInterfaceRow(final IPInterfaceRow row) {
                		infof(this, "Processing IPInterface table row with ipAddr %s for node %d/%s/%s", row.getIpAddress(), getNode().getId(), getNode().getForeignSource(), getNode().getForeignId());
                        if (!row.getIpAddress().startsWith("127.0.0")) {
                        	if (row.getIpAddress() != null)
                        		storeIfIndexIpAddress(row.getIfIndex(), row.getIpAddress());

                            // mark any provisioned interface as scanned
                            provisionedIps.remove(row.getIpAddress());

                            // save the interface
                            OnmsIpInterface iface = row.createInterfaceFromRow();
                            iface.setIpLastCapsdPoll(getScanStamp());

                            // add call to the ip interface is managed policies
                            iface.setIsManaged("M");

                            List<IpInterfacePolicy> policies = getProvisionService().getIpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
        
                            for(IpInterfacePolicy policy : policies) {
                                if (iface != null) {
                                    iface = policy.apply(iface);
                                }
                            }
        
                            if (iface != null) {
                                currentPhase.add(ipUpdater(currentPhase, iface), "write");
                            }
        
                        }
                    }
                };
        
                SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ipAddrTable", ipIfTracker);
                walker.start();
        
                try {
                    walker.waitFor();
        
                    if (walker.timedOut()) {
                        abort("Aborting node scan : Agent timedout while scanning the ipAddrTable");
                    }
                    else if (walker.failed()) {
                        abort("Aborting node scan : Agent failed while scanning the ipAddrTable : " + walker.getErrorMessage());
                    }
                    else {
        
        
                        // After processing the snmp provided interfaces then we need to scan any that 
                        // were provisioned but missing from the ip table
                        for(final String ipAddr : provisionedIps) {
                            final OnmsIpInterface iface = getNode().getIpInterfaceByIpAddress(ipAddr);
                            iface.setIpLastCapsdPoll(getScanStamp());
                            iface.setIsManaged("M");
        
                            currentPhase.add(ipUpdater(currentPhase, iface), "write");
        
                        }
        
                        debugf(this, "Finished phase %s", currentPhase);
        
                    }
                } catch (final InterruptedException e) {
                    abort("Aborting node scan : Scan thread failed while waiting for ipAddrTable");
                }
        
            }
        }
        
    	public void storeIfIndexIpAddress(final Integer ifIndex,
    			final String ipAddress) {
    		debugf(this, "storeIfIndexIpAddress ifIndex %s" , ifIndex);
    		debugf(this, "storeIfIndexIpAddress ipAddr %s" , ipAddress);
    		m_nodeMap.put(ifIndex, ipAddress);
    		
    	}

    	public String getIpAddress(final Integer ifIndex) {
    		tracef(this, "getIpAddress ifIndex %s", ifIndex);
    		return m_nodeMap.get(ifIndex);
    	}
    	
    	public void cleanIfIndexIpAddressMap(final Integer nodeId) {
    		if (m_nodeMap != null)
    			m_nodeMap.remove(nodeId);
    	}

        public void detectPhysicalInterfaces(final BatchTask currentPhase) {
            if (isAborted()) { return; }
            final SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(getAgentAddress());
            Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");
            
            final PhysInterfaceTableTracker physIfTracker = new PhysInterfaceTableTracker() {
                @Override
                public void processPhysicalInterfaceRow(PhysicalInterfaceRow row) {
                	infof(this, "Processing ifTable row for ifIndex %s on node %d/%s/%s", row.getIfIndex(), getNodeId(), getForeignSource(), getForeignId());
                    OnmsSnmpInterface snmpIface = row.createInterfaceFromRow();
                    final String ipAddress = getIpAddress(row.getIfIndex());
                    if (ipAddress != null ) {
                    	snmpIface.setIpAddress(ipAddress);
                    }
                    snmpIface.setLastCapsdPoll(getScanStamp());
                    
                    final List<SnmpInterfacePolicy> policies = getProvisionService().getSnmpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
                    for(final SnmpInterfacePolicy policy : policies) {
                        if (snmpIface != null) {
                            snmpIface = policy.apply(snmpIface);
                        }
                    }
                    
                    if (snmpIface != null) {
                        final OnmsSnmpInterface snmpIfaceResult = snmpIface;
        
                        // add call to the snmp interface collection enable policies
        
                        final Runnable r = new Runnable() {
                            public void run() {
                                getProvisionService().updateSnmpInterfaceAttributes(getNodeId(), snmpIfaceResult);
                            }
                        };
                        currentPhase.add(r, "write");
                    }
                }
            };
            
            final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", physIfTracker);
            walker.start();
            
            try {
                walker.waitFor();
        
                if (walker.timedOut()) {
                    abort("Aborting node scan : Agent timedout while scanning the interfaces table");
                }
                else if (walker.failed()) {
                    abort("Aborting node scan : Agent failed while scanning the interfaces table: " + walker.getErrorMessage());
                }
                else {
                    debugf(this, "Finished phase %s", currentPhase);
                }
            } catch (final InterruptedException e) {
                abort("Aborting node scan : Scan thread interrupted while waiting for interfaces table");
            }
        }

        public void run(final ContainerTask<?> parent) {
            parent.getBuilder().addSequence(
                    new NodeInfoScan(getNode(),getAgentAddress(), getForeignSource(), this, getAgentConfigFactory(), getProvisionService(), getNodeId()),
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            detectIpInterfaces(phase);
                        }
                    },
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            detectPhysicalInterfaces(phase);
                        }
                    },
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            deleteObsoleteResources();
                        }
                    },
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            completed();
                        }
                    }
            );
        }
    }
    
    public class NoAgentScan extends BaseAgentScan implements NeedsContainer {
        

        private NoAgentScan(final Integer nodeId, final OnmsNode node) {
            super(nodeId, node);
        }
        
        private void setNode(final OnmsNode node) {
            m_node = node;
        }
           
        private void applyNodePolicies(final BatchTask phase) {
        	final List<NodePolicy> nodePolicies = getProvisionService().getNodePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
            
        	OnmsNode node = getNode();
        	for(final NodePolicy policy : nodePolicies) {
                if (node != null) {
                    node = policy.apply(node);
                }
            }
            
            if (node == null) {
            	abort("Aborted scan of node due to configured policy");
            } else {
                setNode(node);
            }
            
        }
        
        void stampProvisionedInterfaces(final BatchTask phase) {
            if (!isAborted()) { 
            
                for(final OnmsIpInterface iface : getNode().getIpInterfaces()) {
                    iface.setIpLastCapsdPoll(getScanStamp());
                    phase.add(ipUpdater(phase, iface), "write");
            
                }
            
            }
        }

        void deleteObsoleteResources(final BatchTask phase) {
            getProvisionService().updateNodeScanStamp(getNodeId(), getScanStamp());
            getProvisionService().deleteObsoleteInterfaces(getNodeId(), getScanStamp());
        }
        
        private void doPersistNodeInfo(final BatchTask phase) {
            if (!isAborted()) {
                getProvisionService().updateNodeAttributes(getNode());
            }
            debugf(this, "Finished phase %s", phase);
        }

        public void run(final ContainerTask<?> parent) {
            parent.getBuilder().addSequence(
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            applyNodePolicies(phase);
                        }
                    },
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            stampProvisionedInterfaces(phase);
                        }
                    },
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            deleteObsoleteResources(phase);
                        }
                    },
                    new RunInBatch() {
                        public void run(final BatchTask phase) {
                            doPersistNodeInfo(phase);
                        }
                    }
            );
        }
        
    }
    
    public class BaseAgentScan {

        private OnmsNode m_node;
        private Integer m_nodeId;
        
        private BaseAgentScan(final Integer nodeId, final OnmsNode node) {
            m_nodeId = nodeId;
            m_node = node;
        }

        public Date getScanStamp() {
            return m_scanStamp;
        }

        public OnmsNode getNode() {
            return m_node;
        }
        
        public Integer getNodeId() {
            return m_nodeId;
        }

        public boolean isAborted() {
            return NodeScan.this.isAborted();
        }

        public void abort(final String reason) {
            NodeScan.this.abort(reason);
        }

        public String getForeignSource() {
            return getNode().getForeignSource();
        }

        public String getForeignId() {
            return getNode().getForeignId();
        }
        
        public ProvisionService getProvisionService() {
            return m_provisionService;
        }

        public String toString() {
            return new ToStringBuilder(this)
                .append("foreign source", getForeignSource())
                .append("foreign id", getForeignId())
                .append("node id", m_nodeId)
                .append("scan stamp", m_scanStamp)
                .toString();
        }

        void updateIpInterface(final BatchTask currentPhase, final OnmsIpInterface iface) {
            getProvisionService().updateIpInterfaceAttributes(getNodeId(), iface);
            if (iface.isManaged()) {
                currentPhase.add(new IpInterfaceScan(getNodeId(), iface.getInetAddress(), getForeignSource(), getProvisionService()));
            }
        }

        protected Runnable ipUpdater(final BatchTask currentPhase, final OnmsIpInterface iface) {
            Runnable r = new Runnable() {
                public void run() {
                    updateIpInterface(currentPhase, iface);
                }
            };
            return r;
        }
        
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append("foreign source", m_foreignSource)
            .append("foreign id", m_foreignId)
            .append("node id", m_nodeId)
            .append("aborted", m_aborted)
            .append("provision service", m_provisionService)
            .toString();
    }


    /**
     * <p>detectAgents</p>
     *
     * @param currentPhase a {@link org.opennms.core.tasks.BatchTask} object.
     */
    public void detectAgents(final BatchTask currentPhase) {
        
        if (!isAborted()) {
        	final OnmsNode node = getNode();
        	final OnmsIpInterface primaryIface = m_provisionService.getPrimaryInterfaceForNode(node);
            if (primaryIface != null && primaryIface.getMonitoredServiceByServiceType("SNMP") != null) {
                debugf(this, "Found primary interface and SNMP service for node %d/%s/%s", node.getId(), node.getForeignSource(), node.getForeignId());
                onAgentFound(currentPhase, primaryIface);
            } else {
                debugf(this, "Failed to locate primary interface and SNMP service for node %d/%s/%s", node.getId(), node.getForeignSource(), node.getForeignId());
            }
        }
    }

    /**
     * <p>handleAgentUndetected</p>
     *
     * @param currentPhase a {@link org.opennms.core.tasks.BatchTask} object.
     */
    public void handleAgentUndetected(final BatchTask currentPhase) {
        
        if (!isAgentFound()) {
            currentPhase.add(createNoAgentScan());
        }
        
    }

    private void onAgentFound(final ContainerTask<?> currentPhase, final OnmsIpInterface primaryIface) {
        // Make AgentScan a NeedContainer class and have that call run
        currentPhase.add(createAgentScan(primaryIface.getInetAddress(), "SNMP"));
        setAgentFound(true);
    }

    /**
     * <p>scanCompleted</p>
     *
     * @param currentPhase a {@link org.opennms.core.tasks.BatchTask} object.
     */
    public void scanCompleted(final BatchTask currentPhase) {
        if (!isAborted()) {
        	final EventBuilder bldr = new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond");
            bldr.setNodeid(getNodeId());
            bldr.addParam(EventConstants.PARM_FOREIGN_SOURCE, getForeignSource());
            bldr.addParam(EventConstants.PARM_FOREIGN_ID, getForeignId());
            getEventForwarder().sendNow(bldr.getEvent());
        }
        
    }

}
