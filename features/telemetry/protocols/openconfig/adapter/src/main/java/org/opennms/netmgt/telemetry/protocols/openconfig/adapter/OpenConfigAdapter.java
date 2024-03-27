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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.jti.Telemetry;
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

import javax.script.ScriptException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Stream;

public class OpenConfigAdapter extends AbstractScriptedCollectionAdapter {

    private final String DEFAULT_MODE = "gnmi";
    private final String JTI_MODE = "jti";

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    private NodeDao nodeDao;

    private TransactionOperations transactionTemplate;

    private String mode;

    public OpenConfigAdapter(AdapterDefinition adapterConfig, MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog) {

        try {
            // Default mode is gnmi
            if (JTI_MODE.equalsIgnoreCase(mode)) {
                Telemetry.OpenConfigData openConfigData = Telemetry.OpenConfigData.parseFrom(message.getByteArray());
                String systemId = openConfigData.getSystemId();
                CollectionAgent agent = getCollectionAgent(messageLog, systemId);
                return buildCollectionSet(agent, openConfigData, openConfigData.getTimestamp());
            } else {
                Gnmi.SubscribeResponse subscribeResponse = Gnmi.SubscribeResponse.parseFrom(message.getByteArray());
                Gnmi.Notification notification = subscribeResponse.getUpdate();
                long timeStamp = notification.getTimestamp();
                CollectionAgent agent = getCollectionAgent(messageLog, null);
                return buildCollectionSet(agent, subscribeResponse, timeStamp);

            }
        } catch (InvalidProtocolBufferException e) {
            LOG.warn("Invalid packet: ", e);
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


    private CollectionAgent getCollectionAgent(TelemetryMessageLog messageLog, String systemId) {
        CollectionAgent agent = null;
        try {
            // Match source address to the interface.
            final InetAddress inetAddress = InetAddress.getByName(messageLog.getSourceAddress());
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the
                // nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert resolve from source address: {}", messageLog.getSourceAddress());
        }

        if (agent == null && !Strings.isNullOrEmpty(systemId)) {
            // We were unable to build the agent from source address,
            // try finding a node with a matching label
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
        return agent;
    }

    private Stream<CollectionSetWithAgent> buildCollectionSet(CollectionAgent collectionAgent, Object response, long timeStamp) {
        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }
        try {
            final CollectionSet collectionSet = builder.build(collectionAgent, response, timeStamp);
            return Stream.of(new CollectionSetWithAgent(collectionAgent, collectionSet));
        } catch (final ScriptException e) {
            LOG.warn("Error while running script: {}: {}", getScript(), e);
            return Stream.empty();
        }
    }
}
