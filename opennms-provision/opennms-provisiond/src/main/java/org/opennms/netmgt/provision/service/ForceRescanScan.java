/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.service;

import static org.opennms.netmgt.provision.service.ProvisionService.ABORT;
import static org.opennms.netmgt.provision.service.ProvisionService.ERROR;
import static org.opennms.netmgt.provision.service.ProvisionService.LOCATION;
import static org.opennms.netmgt.provision.service.ProvisionService.NODE_ID;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionOverallMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;

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
    private Span m_span;
    private ProvisionMonitor monitor;
    private ProvisionOverallMonitor overallMonitor;

    /**
     * <p>Constructor for NewSuspectScan.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     * @param taskCoordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     */
    public ForceRescanScan(final Integer nodeId, final ProvisionService provisionService, final EventForwarder eventForwarder, final SnmpAgentConfigFactory agentConfigFactory, final TaskCoordinator taskCoordinator, final ProvisionMonitor monitor, final ProvisionOverallMonitor overallMonitor) {
        m_nodeId = nodeId;
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_agentConfigFactory = agentConfigFactory;
        m_taskCoordinator = taskCoordinator;
        this.monitor = monitor;
        this.overallMonitor = overallMonitor;
    }

    @Override
    public Task createTask() {
        return m_taskCoordinator.createBatch().add(this).get();
    }

    /** {@inheritDoc} */
    @Override
    public void run(final BatchTask phase) {
        m_span = m_provisionService.buildAndStartSpan("ForceScan", null);
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
            m_span.setTag(NODE_ID, node.getId());
            m_span.setTag(LOCATION, node.getLocation().getLocationName());
            OnmsIpInterface iface = m_provisionService.getPrimaryInterfaceForNode(node);
            if (iface == null) { // NMS-6380, a discovered node added with wrong SNMP settings doesn't have a primary interface yet.
                iface = node.getIpInterfaces().isEmpty() ? null : node.getIpInterfaces().iterator().next();
                if (iface != null) {
                    LOG.info("The node with ID {} does not have a primary interface", m_nodeId);
                }
            }
            if (iface == null) {
                LOG.info("The node with ID {} does not have any IP addresses", m_nodeId);
            } else {
                phase.getBuilder().addSequence(
                    new NodeInfoScan(node, iface.getIpAddress(), node.getForeignSource(), node.getLocation(), createScanProgress(), m_agentConfigFactory, m_provisionService, node.getId(), m_span),
                    new IpInterfaceScan(node.getId(), iface.getIpAddress(), node.getForeignSource(), node.getLocation(), m_provisionService, m_span),
                    new NodeScan(node.getId(), node.getForeignSource(), node.getForeignId(), node.getLocation(), m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator, m_span, monitor, overallMonitor),
                        new RunInBatch() {
                            @Override
                            public void run(BatchTask batch) {
                                LOG.info("Done re-scanning node with Id {}", m_nodeId);
                                m_span.finish();
                            }
                        });

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
                m_span.setTag(ERROR, true);
                m_span.setTag(ABORT, true);
                m_span.log(message);
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
