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

package org.opennms.netmgt.telemetry.protocols.carbon.adapter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import javax.script.ScriptException;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractPersistingAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

public class CarbonAdapter extends AbstractPersistingAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CarbonAdapter.class);

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    public CarbonAdapter() {
    }

    @Override
    public Stream<CollectionSetWithAgent> handleMessage(final TelemetryMessageLogEntry message,
                                                        final TelemetryMessageLog messageLog) {

        final Iterator<String> line = Splitter.on(' ')
                .trimResults()
                .limit(3)
                .split(new String(message.getByteArray())).iterator();

        final String metric = line.next();
        final double value = Double.parseDouble(line.next());
        final long timestamp = Long.parseLong(line.next());

        final CarbonMessage carbonMessage = new CarbonMessage(metric,
                value,
                timestamp != -1
                        ? Instant.ofEpochSecond(timestamp)
                        : Instant.ofEpochMilli(message.getTimestamp()));

        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(messageLog.getSourceAddress());
        } catch (UnknownHostException e) {
            LOG.warn("Failed to resolve agent address: {}", messageLog.getSourceAddress());
            return Stream.empty();
        }

        final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);

        final CollectionAgent agent;
        if (nodeId.isPresent()) {
            agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);

        } else {
            LOG.warn("Unable to find node and interface for agent address: {}", messageLog.getSourceAddress());
            return Stream.empty();
        }

        final ScriptedCollectionSetBuilder builder = getCollectionBuilder();
        if (builder == null) {
            LOG.error("Error compiling script '{}'. See logs for details.", this.getScript());
            return Stream.empty();
        }

        try {
            final CollectionSet collectionSet = builder.build(agent, carbonMessage);
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
}
