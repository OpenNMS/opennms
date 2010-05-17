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

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.SequenceTask;
import org.opennms.core.tasks.Task;
import org.opennms.core.tasks.TaskBuilder;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventForwarder;

public class NewSuspectScan implements RunInBatch {
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
    
    public Task createTask() {
        return m_taskCoordinator.createBatch().add(this).get();
    }
    
    public void run(BatchTask phase) {
        scanUndiscoveredNode(phase);
    }

    protected void scanUndiscoveredNode(BatchTask phase) {
        final OnmsNode node = m_provisionService.createUndiscoveredNode(m_ipAddress.getHostAddress());
        
        if (node != null) {
            
            TaskBuilder<SequenceTask> bldr = phase.getBuilder().createSequence();
            bldr.add(new IpInterfaceScan(node.getId(), m_ipAddress, null, m_provisionService));
            bldr.add(new NodeScan(node.getId(), null, null, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator));
            bldr.setParent(phase);
        }
    }

    protected void reparentNodes(BatchTask batch, Integer nodeId) {
        
    }
    


}