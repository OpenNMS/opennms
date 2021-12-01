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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.elastic.Direction;
import org.opennms.netmgt.flows.elastic.FlowDocument;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;

public class FlowThresholding {
    private static final Logger LOG = LoggerFactory.getLogger(FlowThresholding.class);

    public static final String SERVICE_NAME = "Flow-Threshold";
    public static final String RESOURCE_TYPE_NAME = "flowApp";

    private final static RrdRepository FLOW_APP_RRD_REPO = new RrdRepository();

    private final ThresholdingService thresholdingService;
    private final CollectionAgentFactory collectionAgentFactory;

    private final IpInterfaceDao ipInterfaceDao;

    private final Map<ApplicationKey, AtomicLong> applicationAccumulator;

    private final Cache<ExporterKey, Session> sessions;

    private long stepSizeMs = 0;

    private Timer timer;

    public FlowThresholding(final ThresholdingService thresholdingService,
                            final CollectionAgentFactory collectionAgentFactory,
                            final IpInterfaceDao ipInterfaceDao,
                            final CacheConfig sessionCacheConfig) {
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
        this.collectionAgentFactory = Objects.requireNonNull(collectionAgentFactory);

        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);

        //noinspection unchecked
        this.applicationAccumulator = Maps.newConcurrentMap();

        //noinspection unchecked
        this.sessions = new CacheBuilder<ExporterKey, Session>()
                .withConfig(sessionCacheConfig)
                .withCacheLoader(new CacheLoader<ExporterKey, Session>() {
                    @Override
                    public Session load(final ExporterKey key) throws Exception {
                        throw new IllegalStateException();
                    }
                })
                .build();
    }

    public long getStepSizeMs() {
        return this.stepSizeMs;
    }

    public void setStepSizeMs(final long stepSizeMs) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        this.stepSizeMs = stepSizeMs;

        if (this.stepSizeMs == 0) {
            return;
        }

        this.timer = new Timer(SERVICE_NAME + "-Timer", true);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runTimerTask();
            }
        }, stepSizeMs, stepSizeMs);
    }

    private void runTimerTask() {
        // Use one timestamp for the whole timer task run...
        final Date timerTaskDate = new Date();

        for (final Map.Entry<ApplicationKey, AtomicLong> entry : applicationAccumulator.entrySet()) {
            final ExporterKey exporterKey = entry.getKey().exporterKey;

            final OnmsIpInterface iface = ipInterfaceDao.get(exporterKey.interfaceId);

            final FlowThresholding.Session session;

            try {
                session = this.sessions.get(exporterKey, () -> {
                    final CollectionAgent collectionAgent = FlowThresholding.this.collectionAgentFactory.createCollectionAgent(iface);

                    final ThresholdingSession thresholdingSession = FlowThresholding.this.thresholdingService.createSession(iface.getNodeId(),
                            collectionAgent.getHostAddress(),
                            SERVICE_NAME,
                            FLOW_APP_RRD_REPO,
                            new ServiceParameters(Collections.emptyMap()));

                    return new Session(thresholdingSession,
                            collectionAgent);
                });
            } catch (ExecutionException e) {
                LOG.warn("Error creating cache entry for thresholding session", e);
                continue;
            }

            try {
                final NodeLevelResource nodeResource = new NodeLevelResource(iface.getNodeId());
                final DeferredGenericTypeResource appResource = new DeferredGenericTypeResource(nodeResource, RESOURCE_TYPE_NAME, entry.getKey().application);

                final CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(session.collectionAgent)
                        .withTimestamp(timerTaskDate)
                        .withCounter(appResource, entry.getKey().application, "bytes", entry.getValue().get());

                // TODO fooker: Set sequence number from flow to aid distributed thresholding

                session.thresholdingSession.accept(collectionSetBuilder.build());
            } catch (ThresholdInitializationException e) {
                LOG.warn("Error initializing thresholding session", e);
            }
        }
    }

    public void threshold(final List<FlowDocument> documents,
                          final FlowSource source) throws ExecutionException, ThresholdInitializationException {

        for (final var document : documents) {
            if (document.getNodeExporter() != null && !Strings.isNullOrEmpty(document.getApplication())) {
                final var exporterKey = new ExporterKey(document.getNodeExporter().getInterfaceId());

                final var applicationKey = new ApplicationKey(exporterKey,
                        document.getDirection() == Direction.INGRESS
                                ? document.getInputSnmp()
                                : document.getOutputSnmp(),
                        document.getApplication());

                this.applicationAccumulator.computeIfAbsent(applicationKey, k -> new AtomicLong(0)).addAndGet(document.getBytes());
            }
        }
    }

    private static class Session {
        public final ThresholdingSession thresholdingSession;
        public final CollectionAgent collectionAgent;

        private Session(final ThresholdingSession thresholdingSession,
                        final CollectionAgent collectionAgent) {
            this.thresholdingSession = Objects.requireNonNull(thresholdingSession);
            this.collectionAgent = Objects.requireNonNull(collectionAgent);
        }
    }
}
