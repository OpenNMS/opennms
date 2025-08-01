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
package org.opennms.netmgt.telemetry.protocols.sflow.adapter;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.first;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.script.ScriptException;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractScriptedCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class SFlowTelemetryAdapter extends AbstractScriptedCollectionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SFlowTelemetryAdapter.class);

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    public SFlowTelemetryAdapter(final AdapterDefinition adapterConfig,
                                 final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(final TelemetryMessageLogEntry message,
                                                                  final TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        LOG.trace("Parsing packet: {}", message);
        final BsonDocument document = new RawBsonDocument(message.getByteArray()).getDocument("data");
        if (document == null) {
            return Stream.empty();
        }

        final String address = first(
                getString(document, "agent_address", "ipv6", "address"),
                getString(document, "agent_address", "ipv4", "address"))
                .orElseThrow(() -> new IllegalStateException("Incomplete document"));

        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            LOG.warn("Failed to resolve agent address: {}", address);
            return Stream.empty();
        }

        final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);

        final CollectionAgent agent;
        if (nodeId.isPresent()) {
            agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);

        } else {
            LOG.warn("Unable to find node and interface for agent address: {}", address);
            return Stream.empty();
        }
        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        return document.getArray("samples").stream()
                .map(BsonValue::asDocument)
                .flatMap(sampleDocument -> {
                    if ("0:2".equals(sampleDocument.get("format").asString().getValue()) ||
                        "0:4".equals(sampleDocument.get("format").asString().getValue())) {
                        // Handle only (expanded) counter samples
                        try {
                            Long timestamp = null;
                            if (sampleDocument.containsKey("time")) {
                                timestamp = sampleDocument.getInt64("time").getValue();
                            }
                            final CollectionSet collectionSet = builder.build(agent,
                                    sampleDocument.get("data").asDocument(), timestamp);
                            return Stream.of(new CollectionSetWithAgent(agent, collectionSet));
                        } catch (final ScriptException e) {
                            LOG.error("Error while running script: {}", e.getMessage());
                            return Stream.empty();
                        }
                    } else {
                        return Stream.empty();
                    }
                });
    }

    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }
}
