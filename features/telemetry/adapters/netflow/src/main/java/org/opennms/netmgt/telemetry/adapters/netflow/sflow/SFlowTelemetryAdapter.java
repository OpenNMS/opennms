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

package org.opennms.netmgt.telemetry.adapters.netflow.sflow;

import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.first;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getString;

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
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.adapters.collection.AbstractPersistingAdapter;
import org.opennms.netmgt.telemetry.adapters.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.adapters.collection.ScriptedCollectionSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFlowTelemetryAdapter extends AbstractPersistingAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SFlowTelemetryAdapter.class);

    private InterfaceToNodeCache interfaceToNodeCache;

    private CollectionAgentFactory collectionAgentFactory;

    public SFlowTelemetryAdapter() {

    }

    @Override
    public Stream<CollectionSetWithAgent> handleMessage(final TelemetryMessage message,
                                                        final TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        LOG.trace("Parsing packet: {}", message);
        final BsonDocument document = new RawBsonDocument(message.getByteArray()).getDocument("data");
        if (document == null) {
            return Stream.empty();
        }

        final String address = first(
                getString(document, "agent_address", "ipv6"),
                getString(document, "agent_address", "ipv4"))
                .orElseThrow(() -> new IllegalStateException("Incomplete document"));

        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            LOG.warn("Failed to resolve agent address: {}", address);
            return Stream.empty();
        }

        final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
        if (!nodeId.isPresent()) {
            LOG.warn("Unable to find node and interface for agent address: {}", address);
            return Stream.empty();
        }
        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        final CollectionAgent agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
        return document.getArray("samples").stream()
                .map(BsonValue::asDocument)
                .flatMap(sampleDocument -> {
                    if ("0:2".equals(sampleDocument.get("format").asString().getValue()) ||
                        "0:4".equals(sampleDocument.get("format").asString().getValue())) {
                        // Handle only (expanded) counter samples
                        try {
                            final CollectionSet collectionSet = builder.build(agent, sampleDocument.get("data").asDocument());
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
