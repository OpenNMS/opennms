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
package org.opennms.netmgt.telemetry.protocols.nxos.adapter;

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
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractScriptedCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis;
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Iterables;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class NxosGpbAdapter extends AbstractScriptedCollectionAdapter {

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

    public NxosGpbAdapter(final AdapterDefinition adapterConfig,
                          final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog) {
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

        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        try {
            final CollectionSet collectionSet = builder.build(agent, msg, msg.getMsgTimestamp());
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
