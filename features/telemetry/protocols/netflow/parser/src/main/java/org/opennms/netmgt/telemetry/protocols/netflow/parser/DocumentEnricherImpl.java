/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import com.google.protobuf.UInt64Value;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceAndMetaInfoToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache.Entry;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Locality;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class DocumentEnricherImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentEnricherImpl.class);

    private final InterfaceAndMetaInfoToNodeCache interfaceAndMetaInfoToNodeCache;

    private final SessionUtils sessionUtils;

    private final long clockSkewCorrectionThreshold;

    private final DocumentMangler mangler;

    public DocumentEnricherImpl(final InterfaceAndMetaInfoToNodeCache interfaceAndMetaInfoToNodeCache,
                                final SessionUtils sessionUtils,
                                final long clockSkewCorrectionThreshold,
                                final DocumentMangler mangler) {
        this.interfaceAndMetaInfoToNodeCache = Objects.requireNonNull(interfaceAndMetaInfoToNodeCache);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);


        this.clockSkewCorrectionThreshold = clockSkewCorrectionThreshold;

        this.mangler = Objects.requireNonNull(mangler);
    }

    public FlowMessage.Builder enrich(final FlowMessage.Builder flow, final FlowSource source) {
        if (flow == null) {
            LOG.info("Nothing to enrich.");
            return null;
        }
        return (FlowMessage.Builder) sessionUtils.withTransaction(() -> {
            final FlowMessage.Builder document = this.mangler.mangle(flow);
            if (document == null) {
                return Stream.empty();
            }

            // Metadata from message
            document.setHost(source.getSourceAddress());
            document.setLocation(source.getLocation());

            // Node data
            getNodeInfoFromCache(source.getLocation(), source.getSourceAddress(), source.getContextKey(), flow.getNodeIdentifier()).ifPresent(document::setExporterNodeInfo);
            if (flow.getDstAddress() != null) {
                getNodeInfoFromCache(source.getLocation(), flow.getDstAddress(), null, null).ifPresent(document::setSrcNodeInfo);
            }
            if (flow.getSrcAddress() != null) {
                getNodeInfoFromCache(source.getLocation(), flow.getSrcAddress(), null, null).ifPresent(document::setDstNodeInfo);
            }

            // Locality
            if (flow.getSrcAddress() != null) {
                document.setSrcLocality(isPrivateAddress(flow.getSrcAddress()) ? Locality.PRIVATE : Locality.PUBLIC);
            }
            if (flow.getDstAddress() != null) {
                document.setDstLocality(isPrivateAddress(flow.getDstAddress()) ? Locality.PRIVATE : Locality.PUBLIC);
            }

            if (Locality.PUBLIC.equals(document.getDstLocality()) || Locality.PUBLIC.equals(document.getSrcLocality())) {
                document.setFlowLocality(Locality.PUBLIC);
            } else if (Locality.PRIVATE.equals(document.getDstLocality()) || Locality.PRIVATE.equals(document.getSrcLocality())) {
                document.setFlowLocality(Locality.PRIVATE);
            }

            // Fix skewed clock
            // If received time and export time differ too much, correct all timestamps by the difference
            if (this.clockSkewCorrectionThreshold > 0) {
                // Since the enrichment is already move to parser side, the receivedAt is no difference than system timestamp
                var timestamp = Instant.ofEpochMilli(flow.getTimestamp());
                final var skew = Duration.between(Instant.now(), timestamp);
                if (skew.abs().toMillis() >= this.clockSkewCorrectionThreshold) {
                    // The applied correction is the negative skew
                    document.setClockCorrection(skew.negated().toMillis());

                    // Fix the skew on all timestamps of the flow
                    document.setTimestamp(timestamp.minus(skew).toEpochMilli());
                    document.setFirstSwitched(UInt64Value.of(timestamp.minus(skew).toEpochMilli()));
                    document.setDeltaSwitched(UInt64Value.of(timestamp.minus(skew).toEpochMilli()));
                    document.setLastSwitched(UInt64Value.of(timestamp.minus(skew).toEpochMilli()));
                }
            }

            return document;
        });
    }

    private static boolean isPrivateAddress(String ipAddress) {
        final InetAddress inetAddress = InetAddressUtils.addr(ipAddress);
        return inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
    }

    private Optional<NodeInfo> getNodeInfoFromCache(final String location, final String ipAddress, final ContextKey contextKey, final String value) {
        Optional<NodeInfo> nodeDocument = Optional.empty();
        var entry = this.interfaceAndMetaInfoToNodeCache.getFirst(location, contextKey.getContext(), contextKey.getKey(), value);
        if (entry.isEmpty()) {
            entry = this.interfaceAndMetaInfoToNodeCache.getFirst(location, InetAddressUtils.addr(ipAddress));
        }
        if (entry.isPresent()) {
            return Optional.of(toNodeInfo(entry.get()));
        }
        return nodeDocument;
    }

    private NodeInfo toNodeInfo(Entry entry) {
        return NodeInfo.newBuilder()
                .setNodeId(entry.nodeId)
                .setInterfaceId(entry.interfaceId)
                .setForeignId(entry.foreignId)
                .setForeignSource(entry.foreignSource)
                .addAllCategories(entry.categories).build();
    }
}
