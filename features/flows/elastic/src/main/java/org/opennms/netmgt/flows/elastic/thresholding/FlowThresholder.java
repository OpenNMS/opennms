/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic.thresholding;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.elastic.Direction;
import org.opennms.netmgt.flows.elastic.FlowDocument;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;

public class FlowThresholder {
    private static final Logger LOG = LoggerFactory.getLogger(FlowThresholder.class);

    private final static RrdRepository FLOW_APP_RRD_REPO = new RrdRepository();

    private final ThresholdingService thresholdingService;
    private final CollectionAgentFactory collectionAgentFactory;

    private final Cache<ApplicationKey, AtomicLong> applicationAccumulator;

    private final Cache<NodeInterfaceKey, ThresholdingSession> thresholdingSessions;

    public FlowThresholder(final ThresholdingService thresholdingService,
                           final CollectionAgentFactory collectionAgentFactory,
                           final CacheConfig aggregationCacheConfig,
                           final CacheConfig sessionCacheConfig) {
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
        this.collectionAgentFactory = Objects.requireNonNull(collectionAgentFactory);

        this.applicationAccumulator = new CacheBuilder<NodeInterfaceKey, AtomicLong>()
                .withConfig(aggregationCacheConfig)
                .withCacheLoader(new CacheLoader<NodeInterfaceKey, AtomicLong>() {
                    @Override
                    public AtomicLong load(final NodeInterfaceKey nik) throws Exception {
                        return new AtomicLong(0L);
                    }
                }).build();

        this.thresholdingSessions = new CacheBuilder<NodeInterfaceKey, ThresholdingSession>()
                .withConfig(sessionCacheConfig)
                .withCacheLoader(new CacheLoader<NodeInterfaceKey, ThresholdingSession>() {
                    @Override
                    public ThresholdingSession load(final NodeInterfaceKey nik) throws Exception {
                        return FlowThresholder.this.thresholdingService.createSession(nik.nodeId,
                                                                                      nik.ifaceAddr,
                                                                                      "flow-application",
                                                                                      FLOW_APP_RRD_REPO,
                                                                                      new ServiceParameters(Collections.emptyMap()));
                    }
                }).build();
    }

    public void threshold(final FlowDocument document,
                          final FlowSource source) throws ExecutionException, ThresholdInitializationException {
        if (document.getNodeExporter() != null && !Strings.isNullOrEmpty(document.getApplication())) {
            final var nodeId = document.getNodeExporter().getNodeId();

            final var accumulator = this.applicationAccumulator.get(new ApplicationKey(nodeId,
                                       document.getDirection() == Direction.INGRESS
                                       ? document.getInputSnmp()
                                       : document.getOutputSnmp(),
                                       document.getApplication()));

            final var counter = accumulator.addAndGet(document.getBytes());

            final var thresholdingSession = this.thresholdingSessions.get(new NodeInterfaceKey(nodeId,
                                                                                               document.getDirection() == Direction.INGRESS
                                                                                               ? document.getSrcAddr()
                                                                                               : document.getDstAddr()));

            final var collectionAgent = this.collectionAgentFactory.createCollectionAgent(
                    Integer.toString(nodeId),
                    InetAddressUtils.addr(source.getSourceAddress()));

            final var nodeResource = new NodeLevelResource(nodeId);
            final var appResource = new DeferredGenericTypeResource(nodeResource, "flow-application", document.getApplication());

            final var collectionSetBuilder = new CollectionSetBuilder(collectionAgent)
                    .withCounter(appResource, "flow-application", "bytes", counter);

            thresholdingSession.accept(collectionSetBuilder.build());
        }
    }
}
