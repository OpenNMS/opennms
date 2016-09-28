/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.Task;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ForceRescanScan class.</p>
 *
 * @author agalue
 * @version $Id: $
 */
public class ForceRescanScan implements Scan {
    private static final Logger LOG = LoggerFactory.getLogger(ForceRescanScan.class);
    private Integer m_nodeId;
    private ProvisionService m_provisionService;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    private TaskCoordinator m_taskCoordinator;

    /**
     * <p>Constructor for NewSuspectScan.</p>
     *
     * @param m_nodeId a {@link java.land.Integer} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     * @param taskCoordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     */
    public ForceRescanScan(final Integer nodeId, final ProvisionService provisionService, final EventForwarder eventForwarder, final SnmpAgentConfigFactory agentConfigFactory, final TaskCoordinator taskCoordinator) {
        m_nodeId = nodeId;
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_agentConfigFactory = agentConfigFactory;
        m_taskCoordinator = taskCoordinator;
    }

    @Override
    public Task createTask() {
        return m_taskCoordinator.createBatch().add(this).get();
    }

    /** {@inheritDoc} */
    @Override
    public void run(final BatchTask phase) {
        scanExistingNode(phase);
    }

    /**
     * <p>scanExistingNode</p>
     *
     * @param phase a {@link org.opennms.core.tasks.BatchTask} object.
     */
    protected void scanExistingNode(final BatchTask phase) {
        LOG.info("Attempting to re-scan node with Id {}", m_nodeId);
        final OnmsNode node = m_provisionService.getNode(m_nodeId);
        if (node != null) {
            OnmsIpInterface iface = m_provisionService.getPrimaryInterfaceForNode(node);
            if (iface == null) { // NMS-6380, a discovered node added with wrong SNMP settings doesn't have a primary interface yet.
                iface = node.getIpInterfaces().isEmpty() ? null : node.getIpInterfaces().iterator().next();
            } else {
                LOG.info("The node with ID OP does not have a primary interface", m_nodeId);
            }
            if (iface == null) {
                LOG.info("The node with ID {} does not have any IP addresses", m_nodeId);
            } else {
                phase.getBuilder().addSequence(
                    new NodeInfoScan(node, iface.getIpAddress(), node.getForeignSource(), node.getLocation(), createScanProgress(), m_agentConfigFactory, m_provisionService, node.getId()),
                    new IpInterfaceScan(node.getId(), iface.getIpAddress(), node.getForeignSource(), node.getLocation(), m_provisionService),
                    new NodeScan(node.getId(), node.getForeignSource(), node.getForeignId(), node.getLocation(), m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator)
                );
            }
        } else {
            LOG.info("Can't find node with ID {}", m_nodeId);
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
