/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.InetAddressUtils.addr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.net.InetAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import org.opennms.netmgt.config.SnmpAgentConfigFactory;
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
import org.opennms.netmgt.snmp.TableTracker;
import org.springframework.util.Assert;

/**
 * <p>NodeScan class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NodeScan implements RunInBatch {
    private static final Logger LOG = LoggerFactory.getLogger(NodeScan.class);

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
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.SnmpAgentConfigFactory} object.
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
        
        LOG.info("Aborting Scan of node {} for the following reason: {}", m_nodeId, reason);
        
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
    @Override
    public void run(final BatchTask parent) {
        LOG.info("Scanning node {}/{}/{}", m_nodeId, m_foreignSource, m_foreignId);

        parent.getBuilder().addSequence(
                new RunInBatch() {
                    @Override
                    public void run(final BatchTask phase) {
                        loadNode(phase);
                    }
                },
                new RunInBatch() {
                    @Override
                    public void run(final BatchTask phase) {
                        detectAgents(phase);
                    }
                },
                new RunInBatch() {
                    @Override
                    public void run(final BatchTask phase) {
                        handleAgentUndetected(phase);
                    }
                },
                new RunInBatch() {
                    @Override
                    public void run(final BatchTask phase) {
                        scanCompleted(phase);
                    }
                }
        );
        
        
    }
    

    ScheduledFuture<?> schedule(ScheduledExecutorService executor, NodeScanSchedule schedule) {
        
    	final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    
                	final Task t = createTask();
                    t.schedule();
                    t.waitFor();
                    
                    LOG.info("Finished scanning node {}/{}/{}", getNodeId(), getForeignSource(), getForeignId());
                } catch (final InterruptedException e) {
                    LOG.warn("The node scan for node {}/{}/{} was interrupted", getNodeId(), getForeignSource(), getForeignId(), e);
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException e) {
                    LOG.warn("An error occurred while scanning node {}/{}/{}", getNodeId(), getForeignSource(), getForeignId(), e);
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
                    loadNode.add(new IpInterfaceScan(getNodeId(), iface.getIpAddress(), getForeignSource(), getProvisionService()));
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

        public AgentScan(final Integer nodeId, final OnmsNode node, final InetAddress agentAddress, final String agentType) {
            super(nodeId, node);
            m_agentAddress = agentAddress;
            m_agentType = agentType;
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
            
        @Override
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
                bldr.setInterface(getAgentAddress());
                getEventForwarder().sendNow(bldr.getEvent());
            }
        }

        void deleteObsoleteResources() {
            if (!isAborted()) {
                getProvisionService().updateNodeScanStamp(getNodeId(), getScanStamp());
                getProvisionService().deleteObsoleteInterfaces(getNodeId(), getScanStamp());
                LOG.debug("Finished deleteObsoleteResources for {}", this);
            }
        }

        public SnmpAgentConfigFactory getAgentConfigFactory() {
            return m_agentConfigFactory;
        }

        public void detectIpAddressTable(final BatchTask currentPhase) {
        	final OnmsNode node = getNode();

            LOG.debug("Attempting to scan the IPAddress table for node {}", node);

			// mark all provisioned interfaces as 'in need of scanning' so we can mark them
            // as scanned during ipAddrTable processing
            final Set<InetAddress> provisionedIps = new HashSet<InetAddress>();
            if (getForeignSource() != null) {
                for(final OnmsIpInterface provisioned : node.getIpInterfaces()) {
                    provisionedIps.add(provisioned.getIpAddress());
                }
            }

            final IPAddressTableTracker ipAddressTracker = new IPAddressTableTracker() {
            	@Override
            	public void processIPAddressRow(final IPAddressRow row) {
            		final String ipAddress = row.getIpAddress();
					LOG.info("Processing IPAddress table row with ipAddr {}", ipAddress);

					final InetAddress address = addr(ipAddress);

					// skip if it's any number of unusual/local address types
					if (address == null) return;
					if (address.isAnyLocalAddress()) {
						LOG.debug("{}.isAnyLocalAddress() == true, Skipping.", ipAddress);
						return;
					}
					if (address.isLinkLocalAddress()) {
						LOG.debug("{}.isLinkLocalAddress() == true, Skipping.", ipAddress);
						return;
					}
					if (address.isLoopbackAddress()) {
						LOG.debug("{}.isLoopbackAddress() == true, Skipping.", ipAddress);
						return;
					}
					if (address.isMulticastAddress()) {
						LOG.debug("{}.isMulticastAddress() == true, Skipping.", ipAddress);
						return;
					}

                    // mark any provisioned interface as scanned
                    provisionedIps.remove(ipAddress);

                    OnmsIpInterface iface = row.createInterfaceFromRow();

                    if (iface != null) {
                        iface.setIpLastCapsdPoll(getScanStamp());
                        iface.setIsManaged("M");

                        final List<IpInterfacePolicy> policies = getProvisionService().getIpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
                        for(final IpInterfacePolicy policy : policies) {
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

            walkTable(currentPhase, provisionedIps, ipAddressTracker);
        }
        
        public void detectIpInterfaceTable(final BatchTask currentPhase) {
        	final OnmsNode node = getNode();

            LOG.debug("Attempting to scan the IPInterface table for node {}", node);

			// mark all provisioned interfaces as 'in need of scanning' so we can mark them
            // as scanned during ipAddrTable processing
            final Set<InetAddress> provisionedIps = new HashSet<InetAddress>();
            if (getForeignSource() != null) {
                for(final OnmsIpInterface provisioned : node.getIpInterfaces()) {
                    provisionedIps.add(provisioned.getIpAddress());
                }
            }

            final IPInterfaceTableTracker ipIfTracker = new IPInterfaceTableTracker() {
            	@Override
            	public void processIPInterfaceRow(final IPInterfaceRow row) {
            		final String ipAddress = row.getIpAddress();
            		LOG.info("Processing IPInterface table row with ipAddr {} for node {}/{}/{}", ipAddress, node.getId(), node.getForeignSource(), node.getForeignId());

					final InetAddress address = addr(ipAddress);

					// skip if it's any number of unusual/local address types
					if (address == null) return;
					if (address.isAnyLocalAddress()) {
						LOG.debug("{}.isAnyLocalAddress() == true, Skipping.", ipAddress);
						return;
					}
					if (address.isLinkLocalAddress()) {
						LOG.debug("{}.isLinkLocalAddress() == true, Skipping.", ipAddress);
						return;
					}
					if (address.isLoopbackAddress()) {
						LOG.debug("{}.isLoopbackAddress() == true, Skipping.", ipAddress);
						return;
					}
					if (address.isMulticastAddress()) {
						LOG.debug("{}.isMulticastAddress() == true, Skipping.", ipAddress);
						return;
					}

                    // mark any provisioned interface as scanned
                    provisionedIps.remove(ipAddress);

                    // save the interface
                    OnmsIpInterface iface = row.createInterfaceFromRow();
                    
                    if (iface != null) {
	                    iface.setIpLastCapsdPoll(getScanStamp());
	
	                    // add call to the ip interface is managed policies
	                    iface.setIsManaged("M");
	
	                    final List<IpInterfacePolicy> policies = getProvisionService().getIpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
	                    for(final IpInterfacePolicy policy : policies) {
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

            walkTable(currentPhase, provisionedIps, ipIfTracker);
        }

		private void walkTable(final BatchTask currentPhase, final Set<InetAddress> provisionedIps, final TableTracker tracker) {
            final OnmsNode node = getNode();
			LOG.info("detecting IP interfaces for node {}/{}/{} using table tracker {}", node.getId(), node.getForeignSource(), node.getForeignId(), tracker);

			if (isAborted()) {
				LOG.debug("'{}' is marked as aborted; skipping scan of table {}", currentPhase, tracker);
			} else {
	            Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");
	
	        	final SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(getAgentAddress());
	
				final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "IP address tables", tracker);
				walker.start();
	      
				try {
				    walker.waitFor();
	      
				    if (walker.timedOut()) {
				        abort("Aborting node scan : Agent timed out while scanning the IP address tables");
				    }
				    else if (walker.failed()) {
				        abort("Aborting node scan : Agent failed while scanning the IP address tables : " + walker.getErrorMessage());
				    } else {
	      
				        // After processing the SNMP provided interfaces then we need to scan any that 
				        // were provisioned but missing from the ip table
				        for(final InetAddress ipAddr : provisionedIps) {
				            final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddr);
				            
				            if (iface != null) {
					            iface.setIpLastCapsdPoll(getScanStamp());
					            iface.setIsManaged("M");
		      
					            currentPhase.add(ipUpdater(currentPhase, iface), "write");
				            }
				        }
	      
				        LOG.debug("Finished phase {}", currentPhase);
	      
				    }
				} catch (final InterruptedException e) {
				    abort("Aborting node scan : Scan thread failed while waiting for the IP address tables");
				}
			}
		}
        
        public void detectPhysicalInterfaces(final BatchTask currentPhase) {
            if (isAborted()) { return; }
            final SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(getAgentAddress());
            Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");
            
            final PhysInterfaceTableTracker physIfTracker = new PhysInterfaceTableTracker() {
                @Override
                public void processPhysicalInterfaceRow(PhysicalInterfaceRow row) {
                	LOG.info("Processing ifTable row for ifIndex {} on node {}/{}/{}", row.getIfIndex(), getNodeId(), getForeignSource(), getForeignId());
                	OnmsSnmpInterface snmpIface = row.createInterfaceFromRow();
                    snmpIface.setLastCapsdPoll(getScanStamp());
                    
                    final List<SnmpInterfacePolicy> policies = getProvisionService().getSnmpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
                    for(final SnmpInterfacePolicy policy : policies) {
                        if (snmpIface != null) {
                            snmpIface = policy.apply(snmpIface);
                        }
                    }
                    
                    if (snmpIface != null) {
                        final OnmsSnmpInterface snmpIfaceResult = snmpIface;
        
                        // add call to the SNMP interface collection enable policies
        
                        final Runnable r = new Runnable() {
                            @Override
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
                    abort("Aborting node scan : Agent timed out while scanning the interfaces table");
                }
                else if (walker.failed()) {
                    abort("Aborting node scan : Agent failed while scanning the interfaces table: " + walker.getErrorMessage());
                }
                else {
                    LOG.debug("Finished phase {}", currentPhase);
                }
            } catch (final InterruptedException e) {
                abort("Aborting node scan : Scan thread interrupted while waiting for interfaces table");
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run(final ContainerTask<?> parent) {
            parent.getBuilder().addSequence(
                    new NodeInfoScan(getNode(),getAgentAddress(), getForeignSource(), this, getAgentConfigFactory(), getProvisionService(), getNodeId()),
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                            detectPhysicalInterfaces(phase);
                        }
                    },
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                        	detectIpAddressTable(phase);
                        }
                    },
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                            detectIpInterfaceTable(phase);
                        }
                    },
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                            deleteObsoleteResources();
                        }
                    },
                    new RunInBatch() {
                        @Override
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
            LOG.debug("Finished phase {}", phase);
        }

        @Override
        public void run(final ContainerTask<?> parent) {
            parent.getBuilder().addSequence(
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                            applyNodePolicies(phase);
                        }
                    },
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                            stampProvisionedInterfaces(phase);
                        }
                    },
                    new RunInBatch() {
                        @Override
                        public void run(final BatchTask phase) {
                            deleteObsoleteResources(phase);
                        }
                    },
                    new RunInBatch() {
                        @Override
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

        @Override
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
                currentPhase.add(new IpInterfaceScan(getNodeId(), iface.getIpAddress(), getForeignSource(), getProvisionService()));
            }
        }

        protected Runnable ipUpdater(final BatchTask currentPhase, final OnmsIpInterface iface) {
            Runnable r = new Runnable() {
                @Override
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
    @Override
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
                LOG.debug("Found primary interface and SNMP service for node {}/{}/{}", node.getId(), node.getForeignSource(), node.getForeignId());
                onAgentFound(currentPhase, primaryIface);
            } else {
                LOG.debug("Failed to locate primary interface and SNMP service for node {}/{}/{}", node.getId(), node.getForeignSource(), node.getForeignId());
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
        currentPhase.add(createAgentScan(primaryIface.getIpAddress(), "SNMP"));
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
