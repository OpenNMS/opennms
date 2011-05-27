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
import static org.opennms.core.utils.LogUtils.warnf;

import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.updates.NodeUpdate;

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
	private NodeUpdate m_nodeUpdate;
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
    
    public void setNode(final OnmsNode node) {
    	m_node = node;
    }
    
    public NodeUpdate getNodeUpdate() {
    	return m_nodeUpdate;
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
    
    public SnmpAgentConfigFactory getAgentConfigFactory() {
    	return m_agentConfigFactory;
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
            m_nodeUpdate = m_provisionService.getRequisitionedNodeUpdate(getForeignSource(), getForeignId());
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
     * @return a {@link org.opennms.netmgt.provision.service.AgentScan} object.
     */
    public AgentScan createAgentScan(final InetAddress agentAddress, final String agentType) {
        final AgentScan agentScan = new AgentScan(getNodeId(), getNode(), getNodeUpdate(), agentAddress, agentType, getProvisionService(), this);
        agentScan.setScanStamp(getScanStamp());
		return agentScan;
    }
    
    NoAgentScan createNoAgentScan() {
        return new NoAgentScan(getNodeId(), getNode(), getNodeUpdate(), getProvisionService(), this);
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
