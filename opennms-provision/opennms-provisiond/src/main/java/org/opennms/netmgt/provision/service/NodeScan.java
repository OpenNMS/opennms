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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.lifecycle.Phase;

public class NodeScan implements Runnable {
    private Integer m_nodeId;
    private String m_foreignSource;
    private String m_foreignId;
    private ProvisionService m_provisionService;
    private LifeCycleRepository m_lifeCycleRepository;
    private List<Object> m_providers;
    
    //NOTE TO SELF: This is referenced from the AgentScan inner class
    private boolean m_aborted = false;
    
    private OnmsNode m_node;

    public NodeScan(Integer nodeId, String foreignSource, String foreignId, ProvisionService provisionService, LifeCycleRepository lifeCycleRepository, List<Object> providers) {
        m_nodeId = nodeId;
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_provisionService = provisionService;
        m_lifeCycleRepository = lifeCycleRepository;
        m_providers = providers;
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

    public void run() {
        try {
            doNodeScan();
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

    private void doNodeScan() throws InterruptedException, ExecutionException {
        log().info(String.format("Scanning node (%s/%s)", m_foreignSource, m_foreignId));

        LifeCycleInstance doNodeScan = m_lifeCycleRepository.createLifeCycleInstance("nodeScan", m_providers.toArray());
        doNodeScan.setAttribute("nodeScan", this);
        doNodeScan.trigger();
        doNodeScan.waitFor();

        log().debug(String.format("Finished scanning node (%s/%s)", m_foreignSource, m_foreignId));
    }

    public void doLoadNode(Phase loadNode) {
        m_node = m_provisionService.getRequisitionedNode(getForeignSource(), getForeignId());
        if (m_node == null) {
            log().warn(String.format("Unable to get requisitioned node (%s/%s): aborted", m_foreignSource, m_foreignId));
            m_aborted = true;
        }
    }

    public void doAgentScan(Phase detectAgents, InetAddress agentAddress, String agentType) {
        detectAgents.createNestedLifeCycle("agentScan")
            .setAttribute("agentScan", createAgentScan(agentAddress, agentType))
            .setAttribute("agentType", agentType)
            .setAttribute("node", getNode())
            .setAttribute("foreignSource", getForeignSource())
            .setAttribute("foreignId", getForeignId())
            .setAttribute("primaryAddress", agentAddress)
            .trigger();
    }

    public void doNoAgentScan(Phase detectAgents) {
        // we could not find an agent so do a noAgent lifecycle
        detectAgents.createNestedLifeCycle("noAgent")
            .setAttribute("noAgentScan",createNoAgentScan())
            .trigger();
    }


    private BaseAgentScan createAgentScan(InetAddress agentAddress, String agentType) {
        return new AgentScan(m_nodeId, m_node, agentAddress, agentType);
    }
    
    
    private BaseAgentScan createNoAgentScan() {
        return new NoAgentScan(m_nodeId, m_node);
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
            return m_aborted;
        }

        public void abort() {
            m_aborted = true;
        }

        public String getForeignSource() {
            return m_node.getForeignSource();
        }

        public String getForeignId() {
            return m_node.getForeignId();
        }

        public void doUpdateIPInterface(Phase currentPhase, OnmsIpInterface iface) {
            m_provisionService.updateIpInterfaceAttributes(getNodeId(), iface);
        }

        public void triggerIPInterfaceScan(Phase currentPhase, InetAddress ipAddress) {
            currentPhase.createNestedLifeCycle("ipInterfaceScan")
                .setAttribute("ipInterfaceScan", createIpInterfaceScan(getNodeId(), ipAddress))
                .setAttribute("foreignSource", getForeignSource())
                .setAttribute("nodeId", getNodeId())
                .setAttribute("ipAddress", ipAddress)
                .trigger();
        }

        private IpInterfaceScan createIpInterfaceScan(Integer nodeId, InetAddress ipAddress) {
            return new IpInterfaceScan(nodeId, ipAddress);
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
            .append("providers", m_providers)
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
            .append(m_providers)
            .append(m_provisionService)
            .append(m_lifeCycleRepository)
            .toHashCode();
    }
}