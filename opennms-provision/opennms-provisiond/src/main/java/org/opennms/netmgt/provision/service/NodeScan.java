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

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.lifecycle.Phase;

public class NodeScan implements Runnable {
    private String m_foreignSource;
    private String m_foreignId;
    private ProvisionService m_provisionService;
    private LifeCycleRepository m_lifeCycleRepository;
    private List<Object> m_providers;
    
    private OnmsNode m_node;

    public NodeScan(String foreignSource, String foreignId, ProvisionService provisionService, LifeCycleRepository lifeCycleRepository, List<Object> providers) {
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
    
    public OnmsNode getNode() {
        return m_node;
    }


    public void run() {
        try {
            doNodeScan();
            System.err.println(String.format("Finished Scanning Node %s / %s", m_foreignSource, m_foreignId));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    ScheduledFuture<?> schedule(ScheduledExecutorService executor, NodeScanSchedule schedule) {
        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(this, schedule.getInitialDelay().getMillis(), schedule.getScanInterval().getMillis(), TimeUnit.MILLISECONDS);
        System.err.println(String.format("SCHEDULE: Created schedule for node %d : %s", schedule.getNodeId(), future));
        return future;
    }

    private void doNodeScan() throws InterruptedException, ExecutionException {
        LifeCycleInstance doNodeScan = m_lifeCycleRepository.createLifeCycleInstance("nodeScan", m_providers.toArray());
        doNodeScan.setAttribute("nodeScan", this);
        
        doNodeScan.trigger();
        
        doNodeScan.waitFor();
    }

    public void doLoadNode(Phase loadNode) {
        m_node = m_provisionService.getRequisitionedNode(getForeignSource(), getForeignId());
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

    private AgentScan createAgentScan(InetAddress agentAddress, String agentType) {
        return new AgentScan(m_node, agentAddress, agentType);
    }
    
    
    /**
     * AgentScan
     *
     * @author brozow
     */
    public class AgentScan {

        private OnmsNode m_node;
        private InetAddress m_agentAddress;
        private String m_agentType;
        private Integer m_nodeId;
        private Date m_scanStamp;
        private boolean m_aborted = false;
        
        public AgentScan(OnmsNode node, InetAddress agentAddress, String agentType) {
            m_node = node;
            m_agentAddress = agentAddress;
            m_agentType = agentType;
        }
        
        public OnmsNode getNode() {
            return m_node;
        }
        
        public String getForeignSource() {
            return m_node.getForeignSource();
        }
        
        public String getForeignId() {
            return m_node.getForeignId();
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
            m_nodeId = m_provisionService.updateNodeAttributes(getNode()).getId();
        }

        public Integer getNodeId() {
            return m_nodeId;
        }

        public void doUpdateIPInterface(Phase currentPhase, OnmsIpInterface iface) {
            System.out.println("Saving OnmsIpInterface "+iface);
            m_provisionService.updateIpInterfaceAttributes(getNodeId(), iface);
        }

        public void triggerIPInterfaceScan(Phase currentPhase, InetAddress ipAddress) {
            currentPhase.createNestedLifeCycle("ipInterfaceScan")
                .setAttribute("ipInterfaceScan", createIpInterfaceScan(m_nodeId, ipAddress))
                .setAttribute("foreignSource", getForeignSource())
                .setAttribute("nodeId", getNodeId())
                .setAttribute("ipAddress", ipAddress)
                .trigger();
        }

        private IpInterfaceScan createIpInterfaceScan(Integer nodeId, InetAddress ipAddress) {
            return new IpInterfaceScan(nodeId, ipAddress);
        }

        public void setScanStamp(Date scanStamp) {
            m_scanStamp = scanStamp;
        }

        public Date getScanStamp() {
            return m_scanStamp;
        }

        public void setNode(OnmsNode node) {
            m_node = node;
        }

        public boolean isAborted() {
            return m_aborted;
        }

        public void abort() {
            m_aborted = true;
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
        
    }

}