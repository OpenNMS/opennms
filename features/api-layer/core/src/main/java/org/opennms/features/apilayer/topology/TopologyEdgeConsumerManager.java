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
package org.opennms.features.apilayer.topology;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.EdgeMapper;
import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.model.TopologyEdge;
import org.opennms.integration.api.v1.topology.TopologyEdgeConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.TopologyVisitor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyEdgeConsumerManager extends InterfaceMapper<TopologyEdgeConsumer, OnmsTopologyConsumer> {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyEdgeConsumerManager.class);
    private final EdgeMapper edgeMapper;

    public TopologyEdgeConsumerManager(BundleContext bundleContext, EdgeMapper edgeMapper) {
        super(OnmsTopologyConsumer.class, bundleContext);
        this.edgeMapper = Objects.requireNonNull(edgeMapper);
    }

    @Override
    public OnmsTopologyConsumer map(TopologyEdgeConsumer ext) {
        return new OnmsTopologyConsumer() {
            @Override
            public String getName() {
                return ext.getClass().getName();
            }

            @Override
            public Set<OnmsTopologyProtocol> getProtocols() {
                if (ext.getProtocols() == null) {
                    LOG.debug("Protocols was null, returning empty set");
                    return Collections.emptySet();
                }

                LOG.trace("Returning mapped protocols from {}", ext.getProtocols());
                return ext.getProtocols()
                        .stream()
                        .map(ModelMappers::toOnmsTopologyProtocol)
                        .collect(Collectors.toSet());
            }

            @Override
            public void consume(OnmsTopologyMessage message) {
                message.getMessagebody().accept(new TopologyVisitor() {
                    @Override
                    public void visit(OnmsTopologyEdge edge) {
                        TopologyEdge topologyEdge = edgeMapper.toEdge(message.getProtocol(), edge);

                        switch (message.getMessagestatus()) {
                            case UPDATE:
                                LOG.trace("Mapped topology message {} to topology edge {} for add/update", message,
                                        topologyEdge);
                                ext.onEdgeAddedOrUpdated(topologyEdge);
                                break;
                            case DELETE:
                                LOG.trace("Mapped topology message {} to topology edge {} for delete", message,
                                        topologyEdge);
                                ext.onEdgeDeleted(topologyEdge);
                                break;
                            default:
                                LOG.warn("Unsupported message status of {}", message.getMessagestatus());
                        }
                    }
                });
            }
        };
    }
}
