/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.graphite.adapter;

import com.codahale.metrics.MetricRegistry;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.script.ScriptException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.SnmpCollectionSet;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractScriptedCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;

public class GraphiteAdapter extends AbstractScriptedCollectionAdapter {
    private CollectionAgentFactory collectionAgentFactory;
    private InterfaceToNodeCache interfaceToNodeCache;
    private NodeDao nodeDao;

    public GraphiteAdapter(final AdapterDefinition adapterConfig, final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(final TelemetryMessageLogEntry message, final TelemetryMessageLog messageLog) {
        final String messageText = new String(message.getByteArray());
        LOG.trace("plaintext message: {}", messageText);
        final String[] lines = messageText.split("\n");

        CollectionAgent agent = null;
        try {
            final InetAddress inetAddress = InetAddressUtils.addr(messageLog.getSourceAddress());
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (final RuntimeException e) {
            LOG.warn("Unable to determine source address from message log.", e);
            return Stream.empty();
        }

        if (agent == null) {
            LOG.warn("Unable to determine collection agent from location={} and address={}", messageLog.getLocation(), messageLog.getSourceAddress());
            return Stream.empty();
        }

        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        final List<CollectionSetWithAgent> collectionSets = new ArrayList<>();

        for (final String line : lines) {
            final String[] entry = line.split(" ", 3);
            if (entry.length != 3) {
                LOG.warn("Unparseable graphite plaintext message: {}", line);
            } else {
                try {
                    final GraphiteMetric metric = new GraphiteMetric(entry[0], entry[1], Long.valueOf(entry[2], 10));

                    // Pass in this agentList. If script adds a CollectionAgent to this list, we use it instead of the
                    // one created above. Can be used to change the node that this CollectionSet is associated with
                    // Also pass in a CollectionAgentFactory and NodeDao which may be used by the script to
                    // create a modified CollectionAgent
                    var agentList = new ArrayList<CollectionAgent>();
                    var props = new HashMap<String, Object>();
                    props.put("agentList", agentList);
                    props.put("collectionAgentFactory", collectionAgentFactory);
                    props.put("nodeDao", nodeDao);

                    final CollectionSet collectionSet = builder.build(agent, metric, metric.getTimestamp(), props);

                    CollectionAgent agentToUse = agentList.isEmpty() ? agent : agentList.get(0);

                    if (!agentList.isEmpty()) {
                        LOG.trace("Graphite: node modified by script, nodeId now: {}", agentToUse.getNodeId());

                        // Need to use underlying object type instead of CollectionSet as
                        // CollectionSet does not allow modification of the CollectionAgent
                        if (collectionSet instanceof SnmpCollectionSet &&
                            agentToUse instanceof SnmpCollectionAgent) {
                            ((SnmpCollectionSet) collectionSet).setCollectionAgent((SnmpCollectionAgent) agentToUse);
                        } else if (collectionSet instanceof CollectionSetDTO) {
                            CollectionAgentDTO agentDto = new CollectionAgentDTO(agentToUse);
                            ((CollectionSetDTO) collectionSet).setCollectionAgent(agentDto);
                        }
                    }

                    collectionSets.add(new CollectionSetWithAgent(agentToUse, collectionSet));
                } catch (final NumberFormatException | ScriptException e) {
                    LOG.warn("Dropping metric, unable to create collection set: {}", Arrays.asList(entry), e);
                }
            }
        }

        return collectionSets.stream();
    }

    public void setCollectionAgentFactory(final CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(final InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }
}
