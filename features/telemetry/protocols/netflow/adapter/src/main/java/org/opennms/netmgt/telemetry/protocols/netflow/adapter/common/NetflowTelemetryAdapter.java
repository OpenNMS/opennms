/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.telemetry.protocols.netflow.adapter.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptException;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractScriptedCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Value;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class NetflowTelemetryAdapter extends AbstractScriptedCollectionAdapter {
    private InterfaceToNodeCache interfaceToNodeCache;
    private CollectionAgentFactory collectionAgentFactory;

    protected NetflowTelemetryAdapter(final AdapterDefinition adapterConfig, final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(final TelemetryMessageLogEntry message, final TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        LOG.trace("Parsing packet: {}", message);
        FlowMessage flowMessage;
        try {
            flowMessage = FlowMessage.parseFrom(message.getByteArray());
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Unable to parse message from proto", e);
            return Stream.empty();
        }

        final String address = messageLog.getSourceAddress();

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

        final Map<String, Object> data = flowMessage.getRawMessageList().stream().collect(HashMap::new, (m, v)->m.put(v.getName(), mapToJavaTypes(v)), HashMap::putAll);
        try {
            return Stream.of(new CollectionSetWithAgent(agent, builder.build(agent, data, message.getTimestamp())));
        } catch (final ScriptException e) {
            LOG.error("Error while running script: {}", e.getMessage());
            return Stream.empty();
        }
    }

    static Object mapToJavaTypes(Value value) {
        switch (value.getOneofValueCase()) {
            case BOOLEAN:
                return value.getBoolean().getBool().getValue();
            case FLOAT:
                return value.getFloat().getDouble().getValue();
            case DATETIME:
                return value.getDatetime().getUint64().getValue();
            case IPV4ADDRESS:
                return value.getIpv4Address().getString().getValue();
            case IPV6ADDRESS:
                return value.getIpv6Address().getString().getValue();
            case MACADDRESS:
                return value.getMacaddress().getString().getValue();
            case LIST:
                return value.getList().getListList().stream()
                        .map(org.opennms.netmgt.telemetry.protocols.netflow.transport.List::getValueList)
                        .map(list -> list.stream()
                                .map(NetflowTelemetryAdapter::mapToJavaTypes)
                                .collect(Collectors.toList()));
            case SIGNED:
                return value.getSigned().getInt64().getValue();
            case UNSIGNED:
                return value.getUnsigned().getUint64().getValue();
            case STRING:
                return value.getString().getString().getValue();
            case OCTETARRAY:
                return value.getOctetarray().getBytes().getValue().toByteArray();
            case UNDECLARED:
                return value.getOctetarray().getBytes().getValue().toByteArray();
            case NULL:
            case ONEOFVALUE_NOT_SET:
            default:
                return null;
        }

    }

    public void setCollectionAgentFactory(final CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(final InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }
}
