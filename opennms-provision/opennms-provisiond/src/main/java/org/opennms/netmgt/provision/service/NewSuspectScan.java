/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.InetAddress;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;

import org.opennms.netmgt.config.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventForwarder;

/**
 * <p>NewSuspectScan class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NewSuspectScan implements RunInBatch {
    private static final Logger LOG = LoggerFactory.getLogger(NewSuspectScan.class);
    private InetAddress m_ipAddress;
    private ProvisionService m_provisionService;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    private DefaultTaskCoordinator m_taskCoordinator;

    /**
     * <p>Constructor for NewSuspectScan.</p>
     *
     * @param ipAddress a {@link java.net.InetAddress} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.SnmpAgentConfigFactory} object.
     * @param taskCoordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     */
    public NewSuspectScan(final InetAddress ipAddress, final ProvisionService provisionService, final EventForwarder eventForwarder, final SnmpAgentConfigFactory agentConfigFactory, final DefaultTaskCoordinator taskCoordinator) {
        m_ipAddress = ipAddress;
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_agentConfigFactory = agentConfigFactory;
        m_taskCoordinator = taskCoordinator;
    }
    
    /**
     * <p>createTask</p>
     *
     * @return a {@link org.opennms.core.tasks.Task} object.
     */
    public Task createTask() {
        return m_taskCoordinator.createBatch().add(this).get();
    }
    
    /** {@inheritDoc} */
    @Override
    public void run(final BatchTask phase) {
        scanUndiscoveredNode(phase);
    }

    /**
     * <p>scanUndiscoveredNode</p>
     *
     * @param phase a {@link org.opennms.core.tasks.BatchTask} object.
     */
    protected void scanUndiscoveredNode(final BatchTask phase) {
    	final String addrString = str(m_ipAddress);
		LOG.info("Attempting to scan new suspect address {}", addrString);
        final OnmsNode node = m_provisionService.createUndiscoveredNode(addrString);
        
        if (node != null) {

            phase.getBuilder().addSequence(
                    new NodeInfoScan(node, m_ipAddress, null, createScanProgress(), m_agentConfigFactory, m_provisionService, null),
                    new IpInterfaceScan(node.getId(), m_ipAddress, null, m_provisionService),
                    new NodeScan(node.getId(), null, null, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator)
            );

        }
    }

    private ScanProgress createScanProgress() {
        return new ScanProgress() {
            private boolean m_aborted = false;
            @Override
            public void abort(final String message) {
                m_aborted = true;
                LOG.info(message);
            }

            @Override
            public boolean isAborted() {
                return m_aborted;
            }};
    }

    /**
     * <p>reparentNodes</p>
     *
     * @param batch a {@link org.opennms.core.tasks.BatchTask} object.
     * @param nodeId a {@link java.lang.Integer} object.
     */
    protected void reparentNodes(final BatchTask batch, final Integer nodeId) {
        LOG.debug("reparenting node ID {} not supported", nodeId);
    }
    


}
