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

import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.lifecycle.Phase;

public class NodeScan implements Runnable {
    private Integer m_nodeId;
    private String m_foreignSource;
    private String m_foreignId;
    private ProvisionService m_provisionService;
    private EventForwarder m_eventForwarder;
    private DefaultTaskCoordinator m_taskCoordinator;
    private LifeCycleRepository m_lifeCycleRepository;
    private CoreScanActivities m_scanActivities;

    
    //NOTE TO SELF: This is referenced from the AgentScan inner class
    private boolean m_aborted = false;
    
    private OnmsNode m_node;

    public NodeScan(Integer nodeId, String foreignSource, String foreignId, ProvisionService provisionService, EventForwarder eventForwarder, DefaultTaskCoordinator taskCoordinator, LifeCycleRepository lifeCycleRepository, CoreScanActivities scanActivities) {
        m_nodeId = nodeId;
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_taskCoordinator = taskCoordinator;
        m_lifeCycleRepository = lifeCycleRepository;
        m_scanActivities = scanActivities;
    }
    
    public String getForeignSource() {
        return m_foreignSource;
    }
    
    public String getForeignId() {
        return m_foreignId;
    }
    
    public Integer getNodeId() {
        return m_nodeId;
    }
    
    public OnmsNode getNode() {
        return m_node;
    }

    public boolean isAborted() {
        return m_aborted;
    }
    
    public void abort(String reason) {
        m_aborted = true;
        
        log().info(String.format("Aborting Scan of node %d for the following reason: %s", m_nodeId, reason));
        
        EventBuilder bldr = new EventBuilder(EventConstants.PROVISION_SCAN_ABORTED_UEI, "Provisiond");
        if (m_nodeId != null) {
            bldr.setNodeid(m_nodeId);
        }
        bldr.addParam(EventConstants.PARM_FOREIGN_SOURCE, m_foreignSource);
        bldr.addParam(EventConstants.PARM_FOREIGN_ID, m_foreignId);
        bldr.addParam(EventConstants.PARM_REASON, reason);
        
        m_eventForwarder.sendNow(bldr.getEvent());
        
    }

    public void run() {
        try {
            log().info(String.format("Scanning node (%s/%s)", m_foreignSource, m_foreignId));

            // loadNode
            // detectAgents
            // scanCompleted

            

            LifeCycleInstance doNodeScan = m_lifeCycleRepository.createLifeCycleInstance("nodeScan", m_scanActivities);
            doNodeScan.setAttribute("nodeScan", this);
            doNodeScan.trigger();
            doNodeScan.waitFor();
            
            log().debug(String.format("Finished scanning node (%s/%s)", m_foreignSource, m_foreignId));
        } catch (InterruptedException e) {
            log().warn("The node scan was interrupted", e);
        } catch (ExecutionException e) {
            log().warn(String.format("An error occurred while scanning node (%s/%s)", m_foreignSource, m_foreignId), e);
        }
    }
    
    ScheduledFuture<?> schedule(ScheduledExecutorService executor, NodeScanSchedule schedule) {
        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(this, schedule.getInitialDelay().getMillis(), schedule.getScanInterval().getMillis(), TimeUnit.MILLISECONDS);
        return future;
    }

    public void doLoadNode(BatchTask loadNode) {
        m_node = m_provisionService.getRequisitionedNode(getForeignSource(), getForeignId());
        if (m_node == null) {
            abort(String.format("Unable to get requisitioned node (%s/%s): aborted", m_foreignSource, m_foreignId));
        }
    }

    public void doAgentScan(final Phase currentPhase, InetAddress agentAddress, String agentType) {
        
        final AgentScan agentScan = createAgentScan(agentAddress, agentType);
        
        currentPhase.addSequence(
                new Runnable () {
                    public void run() {
                        m_scanActivities.collectNodeInfo(currentPhase, agentScan);
                    }
                },
                new Runnable() {
                    public void run() {
                        m_scanActivities.persistNodeInfo(currentPhase, agentScan);
                    }
                },
                new Runnable() {
                    public void run() {
                        m_scanActivities.detectPhysicalInterfaces(currentPhase, agentScan);
                    }
                },
                new Runnable() {
                    public void run() {
                        m_scanActivities.detectIpInterfaces(currentPhase, agentScan);
                    }
                },
                new Runnable() {
                    public void run() {
                        m_scanActivities.deleteObsoleteResources(currentPhase, agentScan);
                    }
                },
                new Runnable() {
                    public void run() {
                        m_scanActivities.agentScanCompleted(currentPhase, agentScan);
                    }
                }
        );
    }

    public void doNoAgentScan(final BatchTask phase) {
        
        final NoAgentScan noAgentScan = new NoAgentScan(m_nodeId, m_node);
        
        phase.addSequence(
                new Runnable() {
                    public void run() {
                        m_scanActivities.stampProvisionedInterfaces(phase, noAgentScan);
                    }
                },
                new Runnable() {
                    public void run() {
                        m_scanActivities.deleteObsoleteResources(phase, noAgentScan);
                    }
                }
        );

    }


    private AgentScan createAgentScan(InetAddress agentAddress, String agentType) {
        return new AgentScan(m_nodeId, m_node, agentAddress, agentType);
    }
    
    
    private Category log() {
        return ThreadCategory.getInstance(NodeScan.class);
    }

    /**
     * AgentScan
     *
     * @author brozow
     */
    public class AgentScan extends BaseAgentScan {

        private InetAddress m_agentAddress;
        private String m_agentType;
        
        public AgentScan(Integer nodeId, OnmsNode node, InetAddress agentAddress, String agentType) {
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

        public void doPersistNodeInfo() {
            if (isAborted()) {
                return;
            }
            m_provisionService.updateNodeAttributes(getNode());
        }
        
        public void setNode(OnmsNode node) {
            m_node = node;
        }
            
        public String toString() {
            return new ToStringBuilder(this)
                .append("address", m_agentAddress)
                .append("type", m_agentType)
                .toString();
        }
        
        public int hashCode() {
            return new HashCodeBuilder()
                .append(m_agentAddress)
                .append(m_agentType)
                .toHashCode();
        }
    }
    
    public class NoAgentScan extends BaseAgentScan {
        

        private NoAgentScan(Integer nodeId, OnmsNode node) {
            super(nodeId, node);
        }
        
    }
    
    public class BaseAgentScan {

        private Date m_scanStamp;
        private OnmsNode m_node;
        private Integer m_nodeId;
        
        private BaseAgentScan(Integer nodeId, OnmsNode node) {
            m_nodeId = nodeId;
            m_node = node;
        }

        public void setScanStamp(Date scanStamp) {
            m_scanStamp = scanStamp;
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

        public void abort(String reason) {
            NodeScan.this.abort(reason);
        }

        public String getForeignSource() {
            return m_node.getForeignSource();
        }

        public String getForeignId() {
            return m_node.getForeignId();
        }

        public void doUpdateIPInterface(BatchTask currentPhase, OnmsIpInterface iface) {
            m_provisionService.updateIpInterfaceAttributes(getNodeId(), iface);
        }

        public void triggerIPInterfaceScan(final BatchTask currentPhase, final InetAddress ipAddress) {
            currentPhase.add(new Runnable() {
                public void run() {
                    m_scanActivities.detectServices(currentPhase, new IpInterfaceScan(getNodeId(), ipAddress));
                }
            });
        }

        public String toString() {
            return new ToStringBuilder(this)
                .append("foreign source", getForeignSource())
                .append("foreign id", getForeignId())
                .append("node id", m_nodeId)
                .append("scan stamp", m_scanStamp)
                .toString();
        }
        
        public int hashCode() {
            return new HashCodeBuilder()
                .append(m_nodeId)
                .append(m_scanStamp)
                .toHashCode();
        }
        
    }
    
    public class IpInterfaceScan {
        
        private InetAddress m_address;
        private Integer m_nodeId;

        public IpInterfaceScan(Integer nodeId, InetAddress address) {
            m_nodeId = nodeId;
            m_address = address;
        }

        public String getForeignSource() {
            return m_foreignSource;
        }

        public Integer getNodeId() {
            return m_nodeId;
        }
        
        public InetAddress getAddress() {
            return m_address;
        }
        
        public String toString() {
            return new ToStringBuilder(this)
                .append("address", m_address)
                .append("foreign source", m_foreignSource)
                .append("node ID", m_nodeId)
                .toString();
        }
        
        public int hashCode() {
            return new HashCodeBuilder()
                .append(m_address)
                .append(m_foreignSource)
                .append(m_nodeId)
                .toHashCode();
        }
        
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("foreign source", m_foreignSource)
            .append("foreign id", m_foreignId)
            .append("node id", m_nodeId)
            .append("aborted", m_aborted)
            .append("provision service", m_provisionService)
            .append("lifecycle repository", m_lifeCycleRepository)
            .toString();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(m_foreignSource)
            .append(m_foreignId)
            .append(m_nodeId)
            .append(m_aborted)
            .append(m_provisionService)
            .append(m_lifeCycleRepository)
            .toHashCode();
    }
}