/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.topology;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.EdgeMapper;
import org.opennms.features.apilayer.utils.InterfaceMapper;
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
