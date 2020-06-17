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

package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.script.ScriptException;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractScriptedCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;

import telemetry.OpenConfigTelemetryProto;

public class OpenConfigAdapter extends AbstractScriptedCollectionAdapter {

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    private NodeDao nodeDao;

    private TransactionOperations transactionTemplate;

    public OpenConfigAdapter(AdapterDefinition adapterConfig, MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog) {
        OpenConfigTelemetryProto.OpenConfigData openConfigData = null;
        try {
            openConfigData = OpenConfigTelemetryProto.OpenConfigData.parseFrom(message.getByteArray());
        } catch (InvalidProtocolBufferException e) {
            LOG.warn("Invalid packet: {}", e);
            return Stream.empty();
        }
        String systemId = openConfigData.getSystemId();
        Optional<OpenConfigTelemetryProto.KeyValue> ifName =
                openConfigData.getKvList().stream().filter(keyValue -> keyValue.getKey().contains("name")).findFirst();
        CollectionAgent agent = null;
        try {
            // Attempt to resolve the systemId to an InetAddress
            final InetAddress inetAddress = InetAddress.getByName(systemId);
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the
                // nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert system id to address: {}", systemId);
        }

        if (agent == null) {
            // We were unable to build the agent by resolving the systemId,
            // try finding
            // a node with a matching label
            agent = transactionTemplate.execute(new TransactionCallback<CollectionAgent>() {
                @Override
                public CollectionAgent doInTransaction(TransactionStatus status) {
                    final OnmsNode node = Iterables.getFirst(nodeDao.findByLabel(systemId), null);
                    if (node != null) {
                        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
                        return collectionAgentFactory.createCollectionAgent(primaryInterface);
                    }
                    return null;
                }
            });
        }

        if (agent == null) {
            LOG.warn("Unable to find node and interface for system id: {}", systemId);
            return Stream.empty();
        }

        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        try {
            final CollectionSet collectionSet = builder.build(agent, openConfigData, openConfigData.getTimestamp());
            return Stream.of(new CollectionSetWithAgent(agent, collectionSet));

        } catch (final ScriptException e) {
            LOG.warn("Error while running script: {}: {}", getScript(), e);
            return Stream.empty();
        }
    }

    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }
}
