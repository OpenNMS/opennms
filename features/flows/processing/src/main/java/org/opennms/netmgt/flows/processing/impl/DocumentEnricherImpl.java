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
package org.opennms.netmgt.flows.processing.impl;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentEnricherImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentEnricherImpl.class);

    private final SessionUtils sessionUtils;

    private final ClassificationEngine classificationEngine;

    private final NodeInfoCache nodeInfoCache;

    private final long clockSkewCorrectionThreshold;

    private final DocumentMangler mangler;

    public DocumentEnricherImpl(final SessionUtils sessionUtils,
                                final ClassificationEngine classificationEngine,
                                final long clockSkewCorrectionThreshold,
                                final DocumentMangler mangler,
                                final NodeInfoCache nodeInfoCache) {
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);
        this.nodeInfoCache = Objects.requireNonNull(nodeInfoCache);
        this.clockSkewCorrectionThreshold = clockSkewCorrectionThreshold;
        this.mangler = Objects.requireNonNull(mangler);
    }

    public List<EnrichedFlow> enrich(final Collection<Flow> flows, final FlowSource source) {
        if (flows.isEmpty()) {
            LOG.info("Nothing to enrich.");
            return Collections.emptyList();
        }

        return sessionUtils.withTransaction(() -> flows.stream().flatMap(flow -> {
            final EnrichedFlow document = this.mangler.mangle(EnrichedFlow.from(flow));
            if (document == null) {
                return Stream.empty();
            }

            // Metadata from message
            document.setHost(source.getSourceAddress());
            document.setLocation(source.getLocation());

            // Node data
            nodeInfoCache.getNodeInfoFromCache(source.getLocation(), source.getSourceAddress(), source.getContextKey(), flow.getNodeIdentifier()).ifPresent(document::setExporterNodeInfo);
            if (flow.getDstAddr() != null) {
                nodeInfoCache.getNodeInfoFromCache(source.getLocation(), flow.getDstAddr(), null, null).ifPresent(document::setSrcNodeInfo);
            }
            if (flow.getSrcAddr() != null) {
                nodeInfoCache.getNodeInfoFromCache(source.getLocation(), flow.getSrcAddr(), null, null).ifPresent(document::setDstNodeInfo);
            }

            // Locality
            if (flow.getSrcAddr() != null) {
                document.setSrcLocality(isPrivateAddress(flow.getSrcAddr()) ? EnrichedFlow.Locality.PRIVATE : EnrichedFlow.Locality.PUBLIC);
            }
            if (flow.getDstAddr() != null) {
                document.setDstLocality(isPrivateAddress(flow.getDstAddr()) ? EnrichedFlow.Locality.PRIVATE : EnrichedFlow.Locality.PUBLIC);
            }

            if (EnrichedFlow.Locality.PUBLIC.equals(document.getDstLocality()) || EnrichedFlow.Locality.PUBLIC.equals(document.getSrcLocality())) {
                document.setFlowLocality(EnrichedFlow.Locality.PUBLIC);
            } else if (EnrichedFlow.Locality.PRIVATE.equals(document.getDstLocality()) || EnrichedFlow.Locality.PRIVATE.equals(document.getSrcLocality())) {
                document.setFlowLocality(EnrichedFlow.Locality.PRIVATE);
            }

            final ClassificationRequest classificationRequest = createClassificationRequest(document);

            // Check whether classification is possible
            if (classificationRequest.isClassifiable()) {
                // Apply Application mapping
                document.setApplication(classificationEngine.classify(classificationRequest));
            }

            // Fix skewed clock
            // If received time and export time differ to much, correct all timestamps by the difference
            if (this.clockSkewCorrectionThreshold > 0) {
                final var skew = Duration.between(flow.getReceivedAt(), flow.getTimestamp());
                if (skew.abs().toMillis() >= this.clockSkewCorrectionThreshold) {
                    // The applied correction is the negative skew
                    document.setClockCorrection(skew.negated());

                    // Fix the skew on all timestamps of the flow
                    document.setTimestamp(flow.getTimestamp().minus(skew));
                    document.setFirstSwitched(flow.getFirstSwitched().minus(skew));
                    document.setDeltaSwitched(flow.getDeltaSwitched().minus(skew));
                    document.setLastSwitched(flow.getLastSwitched().minus(skew));
                }
            }

            return Stream.of(document);
        }).collect(Collectors.toList()));
    }

    private static boolean isPrivateAddress(String ipAddress) {
        final InetAddress inetAddress = InetAddressUtils.addr(ipAddress);
        return inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
    }

    // Key class, which is used to cache NodeInfo for a given node metadata.

    public static ClassificationRequest createClassificationRequest(EnrichedFlow document) {
        final ClassificationRequest request = new ClassificationRequest();
        request.setProtocol(Protocols.getProtocol(document.getProtocol()));
        request.setLocation(document.getLocation());
        request.setExporterAddress(document.getHost());
        request.setDstAddress(document.getDstAddr());
        request.setDstPort(document.getDstPort());
        request.setSrcAddress(document.getSrcAddr());
        request.setSrcPort(document.getSrcPort());

        return request;
    }
}
