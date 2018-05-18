/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.nxos;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.adapters.collection.AbstractScriptPersistingAdapter;
import org.opennms.netmgt.telemetry.adapters.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.adapters.collection.ScriptedCollectionSetBuilder;
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis;
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis.Telemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Iterables;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class NxosGpbAdapter extends AbstractScriptPersistingAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NxosGpbAdapter.class);

    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();

    static {
        TelemetryBis.registerAllExtensions(s_registry);
    }

    @Autowired
    private CollectionAgentFactory collectionAgentFactory;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionOperations transactionTemplate;

    @Override
    public Stream<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog) {
        final Telemetry msg;
        try {
            msg = tryParsingTelemetryMessage(message.getByteArray());
        } catch (InvalidProtocolBufferException e) {
            LOG.warn("Invalid packet: {}", e);
            return Stream.empty();
        }

        CollectionAgent agent = null;
        try {
            LOG.debug(" Telemetry message content : {} ", msg);
            final InetAddress inetAddress = InetAddress.getByName(msg.getNodeIdStr());
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert system id to address: {}", msg.getNodeIdStr());
        }

        if (agent == null) {
            // We were unable to build the agent by resolving the systemId, try finding a node with a matching label
            agent = transactionTemplate.execute(new TransactionCallback<CollectionAgent>() {
                @Override
                public CollectionAgent doInTransaction(TransactionStatus status) {
                    OnmsNode node = Iterables.getFirst(nodeDao.findByLabelForLocation(msg.getNodeIdStr(), messageLog.getLocation()), null);
                    if (node == null) {
                        // If there is no matching label , Try matching with foreignId
                        node = Iterables.getFirst(nodeDao.findByForeignIdForLocation(msg.getNodeIdStr(), messageLog.getLocation()), null);
                    }
                    if (node != null) {
                        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
                        return collectionAgentFactory.createCollectionAgent(primaryInterface);
                    }
                    return null;
                }
            });
        }

        if (agent == null) {
            LOG.warn("Unable to find node and interface for system id: {}", msg.getNodeIdStr());
            return Stream.empty();
        }

        final ScriptedCollectionSetBuilder builder = scriptedCollectionSetBuilders.get();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        try {
            final CollectionSet collectionSet = builder.build(agent, msg);
            return Stream.of(new CollectionSetWithAgent(agent, collectionSet));

        } catch (final ScriptException e) {
            LOG.warn("Error while running script: {}: {}", getScript(), e);
            return Stream.empty();
        }
    }

    private Telemetry tryParsingTelemetryMessage(byte[] bs) throws InvalidProtocolBufferException {

        try {
            return TelemetryBis.Telemetry.parseFrom(bs, s_registry);
        } catch (InvalidProtocolBufferException e) {
            // Attempt with offset 6
            ByteBuffer buf = ByteBuffer.wrap(bs, 6, bs.length - 6);
            return TelemetryBis.Telemetry.parseFrom(buf, s_registry);
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
