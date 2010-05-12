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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.Callback;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.NeedsContainer;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.SequenceTask;
import org.opennms.core.tasks.Task;
import org.opennms.core.tasks.TaskBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.util.Assert;

public class NewSuspectScan implements Runnable {
    private InetAddress m_ipAddress;
    private ProvisionService m_provisionService;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    private DefaultTaskCoordinator m_taskCoordinator;

    public NewSuspectScan(InetAddress ipAddress, ProvisionService provisionService, EventForwarder eventForwarder, SnmpAgentConfigFactory agentConfigFactory, DefaultTaskCoordinator taskCoordinator) {
        m_ipAddress = ipAddress;
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_agentConfigFactory = agentConfigFactory;
        m_taskCoordinator = taskCoordinator;
    }
    
    public void run() {
        try {
            infof(this, "Examining New Suspect (%s)", m_ipAddress);

            BatchTask sequence = m_taskCoordinator.createBatch().add(
                    new RunInBatch() {
                        public void run(BatchTask phase) {
                            scanUndiscoveredNode(phase);
                        }
                    }
            ).get();
            
            sequence.schedule();
            sequence.waitFor();
            
            debugf(this, "Finished examining New Suspect (%s)", m_ipAddress);
        } catch (InterruptedException e) {
            warnf(this, e, "The node scan was interrupted");
        } catch (ExecutionException e) {
            warnf(this, e, "An error occurred while examing New Suspect (%s)", m_ipAddress);
        }
    }

    protected void scanUndiscoveredNode(BatchTask phase) {
        final OnmsNode node = m_provisionService.createUndiscoveredNode(m_ipAddress.getHostAddress());
        
        if (node != null) {
            
            TaskBuilder<SequenceTask> bldr = phase.getBuilder().createSequence();
            bldr.add(new IpInterfaceScan(node.getId(), m_ipAddress, null, m_provisionService));
            bldr.add(new NodeScan(node.getId(), null, null, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator));
            bldr.add(new RunInBatch() {
                public void run(BatchTask batch) {
                    reparentNodes(batch, node.getId());
                }
            });
            bldr.setParent(phase);
        }
    }

    protected void reparentNodes(BatchTask batch, Integer nodeId) {
        
    }
    


}