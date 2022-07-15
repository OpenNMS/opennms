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

import java.io.Closeable;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.flows.api.ProcessingOptions;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.elastic.Direction;
import org.opennms.netmgt.flows.elastic.FlowDocument;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FlowThresholding implements Closeable, ClassificationEngine.ClassificationRulesReloadedListener {
    private static final Logger LOG = LoggerFactory.getLogger(FlowThresholding.class);

    public static final String NAME = "flowThreshold";
    public static final String SERVICE_NAME = "Flow-Threshold";
    public static final String RESOURCE_TYPE_NAME = "flowApp";
    public static final String RESOURCE_GROUP = "application";

    private final ThresholdingService thresholdingService;
    private final CollectionAgentFactory collectionAgentFactory;
    private final PersisterFactory persisterFactory;

    private final IpInterfaceDao ipInterfaceDao;

    private final SnmpInterfaceDao snmpInterfaceDao;

    private final FilterDao filterDao;

    public final long systemIdHash;

    private final ConcurrentMap<ExporterKey, Session> sessions = Maps.newConcurrentMap();

    private long stepSizeMs = 0;
    private volatile long idleTimeoutMs = 15 * 60 * 1000;

    private Timer timer;

    private FilterService filterService;

    private ClassificationRuleProvider classificationRuleProvider;

    private List<Rule> classificationRuleList;

    private ClassificationEngine classificationEngine;

    public FlowThresholding(final ThresholdingService thresholdingService,
                            final CollectionAgentFactory collectionAgentFactory,
                            final PersisterFactory persisterFactory,
                            final IpInterfaceDao ipInterfaceDao,
                            final DistPollerDao distPollerDao,
                            final SnmpInterfaceDao snmpInterfaceDao,
                            final FilterDao filterDao,
                            final FilterService filterService,
                            final ClassificationRuleProvider classificationRuleProvider,
                            final ClassificationEngine classificationEngine) {
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
        this.collectionAgentFactory = Objects.requireNonNull(collectionAgentFactory);
        this.persisterFactory = Objects.requireNonNull(persisterFactory);
        this.systemIdHash = (long) distPollerDao.whoami().getId().hashCode() << 32;
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.filterDao = Objects.requireNonNull(filterDao);
        this.filterService = Objects.requireNonNull(filterService);
        this.classificationRuleProvider = Objects.requireNonNull(classificationRuleProvider);
        this.classificationRuleList = this.classificationRuleProvider.getRules();
        this.classificationEngine = Objects.requireNonNull(classificationEngine);
        this.classificationEngine.addClassificationRulesReloadedListener(this);
    }

    @Override
    public void classificationRulesReloaded() {
        synchronized (this.classificationRuleList) {
            this.classificationRuleList = this.classificationRuleProvider.getRules();
            LOG.debug("Classification rules reloaded. Marking sessions as dirty.");
            for (final Session session : getSessions()) {
                session.dirty = true;
            }
        }
    }

    public long getStepSizeMs() {
        return this.stepSizeMs;
    }

    public void setStepSizeMs(final long stepSizeMs) {
        if (timer != null) {
            timer.cancel();
            timer = null;
            LOG.debug("Timer task stopped.");
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
        }, this.stepSizeMs, this.stepSizeMs);
        LOG.debug("Timer task re-scheduled (stepSizeMs={}ms, idleTimeoutMs={}ms).", this.stepSizeMs, this.idleTimeoutMs);
    }

    public long getIdleTimeoutMs() {
        return this.idleTimeoutMs;
    }

    public void setIdleTimeoutMs(final long idleTimeoutMs) {
        this.idleTimeoutMs = idleTimeoutMs;
    }

    public void runTimerTask() {
        // Use one timestamp for the whole timer task run...
        final Date timerTaskDate = new Date();

        final List<ExporterKey> idleSessions = Lists.newArrayList();

        LOG.debug("Running Timer task for {} session(s)...", this.sessions.entrySet().size());

        for (final Map.Entry<ExporterKey, Session> entry : this.sessions.entrySet()) {
            final var exporterKey = entry.getKey();
            final var session = entry.getValue();

            LOG.debug("Processing session={} for exporterKey={}...", session, exporterKey);

            // Check whether session is idle and mark it for removal
            if (session.lastUpdate.isBefore(Instant.now().minus(this.idleTimeoutMs, ChronoUnit.MILLIS))) {
                idleSessions.add(exporterKey);
                continue;
            }

            if (session.dirty) {
                final Set<String> applicationsToPersist;

                synchronized (classificationRuleList) {
                    applicationsToPersist = classificationRuleList.stream()
                            .filter(r -> r.getExporterFilter() == null || filterService.matches(session.exporterIpAddress, r.getExporterFilter()))
                            .map(r -> r.getName())
                            .collect(Collectors.toUnmodifiableSet());
                }

                LOG.debug("Found {} matching applications in {} rules for exporter {}" , applicationsToPersist.size(), classificationRuleList.size(), session.exporterIpAddress);

                synchronized (entry.getValue().applications) {
                    for(final IndexKey indexKey : entry.getValue().applications.keySet()) {
                        final int beforeAdd = entry.getValue().applications.get(indexKey).size();

                        for (final String application : applicationsToPersist) {
                            entry.getValue().applications.get(indexKey).computeIfAbsent(application, a -> new AtomicLong(0));
                        }
                        final int afterAddBeforePurge = entry.getValue().applications.get(indexKey).size();
                        LOG.debug("Added {} applications for {}/{}/{}", afterAddBeforePurge - beforeAdd, session.exporterIpAddress, indexKey.iface, indexKey.direction);

                        entry.getValue().applications.get(indexKey).entrySet().removeIf(e -> !applicationsToPersist.contains(e.getKey()));
                        final int afterPurge = entry.getValue().applications.get(indexKey).size();
                        LOG.debug("Removed {} applications for {}/{}/{}", afterAddBeforePurge - afterPurge, session.exporterIpAddress, indexKey.iface, indexKey.direction);
                    }
                }
                session.dirty = false;
            }


            final OnmsIpInterface iface = this.ipInterfaceDao.get(exporterKey.interfaceId);
            final NodeLevelResource nodeResource = new NodeLevelResource(iface.getNodeId());

            for (final Map.Entry<IndexKey, Map<String, AtomicLong>> indexEntry : session.applications.entrySet()) {
                for(final Map.Entry<String, AtomicLong> applicationEntry : indexEntry.getValue().entrySet()) {
                    try {
                        final String ifName = getIfNameForNodeIdAndIfIndex(session.collectionAgent.getNodeId(), indexEntry.getKey().iface);

                        final DeferredGenericTypeResource appResource = new DeferredGenericTypeResource(nodeResource,
                                RESOURCE_TYPE_NAME,
                                String.format("%s:%s",
                                        ifName,
                                        applicationEntry.getKey()));

                        final var collectionSet = new CollectionSetBuilder(session.collectionAgent)
                                .withTimestamp(timerTaskDate)
                                .withSequenceNumber(session.sequenceNumber.getAndIncrement())
                                .withCounter(appResource,
                                        RESOURCE_GROUP,
                                        indexEntry.getKey().direction == Direction.INGRESS
                                                ? "bytesIn"
                                                : "bytesOut",
                                        applicationEntry.getValue().get())
                                .withStringAttribute(appResource,
                                        RESOURCE_GROUP,
                                        "application",
                                        applicationEntry.getKey())
                                .withStringAttribute(appResource,
                                        RESOURCE_GROUP,
                                        "ifName",
                                        ifName)
                                .build();

                        if (session.thresholding) {
                            LOG.trace("Checking thresholds for collection-set value={}, ifName={}, application={}, ds={}",
                                    applicationEntry.getValue().get(),
                                    ifName,
                                    applicationEntry.getKey(),
                                    indexEntry.getKey().direction == Direction.INGRESS ? "bytesIn" : "bytesOut");

                            session.thresholdingSession.accept(collectionSet);
                        }

                        if (session.dataCollection) {
                            LOG.trace("Persisting data for collection-set value={}, ifName={}, application={}, ds={}",
                                    applicationEntry.getValue().get(),
                                    ifName,
                                    applicationEntry.getKey(),
                                    indexEntry.getKey().direction == Direction.INGRESS ? "bytesIn" : "bytesOut");

                            final var repository = new RrdRepository();
                            repository.setStep(session.packageDefinition.getRrd().getStep());
                            repository.setHeartBeat(repository.getStep() * 2);
                            repository.setRraList(session.packageDefinition.getRrd().getRras());
                            repository.setRrdBaseDir(new File(session.packageDefinition.getRrd().getBaseDir()));

                            collectionSet.visit(this.persisterFactory.createPersister(new ServiceParameters(Collections.emptyMap()),
                                    repository,
                                    false,
                                    false,
                                    true));

                        }
                    } catch (ThresholdInitializationException e) {
                        LOG.warn("Error initializing thresholding session", e);
                    }
                }
            }
        }

        // Cleanup idle sessions
        for (ExporterKey exporterKey : idleSessions) {
            LOG.debug("Dropping session for exporterKey={}", exporterKey);
            this.sessions.remove(exporterKey);
        }
    }

    private String getIfNameForNodeIdAndIfIndex(final int nodeId, final int ifIndex) {
        final OnmsSnmpInterface snmpInterface = snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex);

        if (snmpInterface != null && !Strings.isNullOrEmpty(snmpInterface.getIfName())) {
            return snmpInterface.getIfName();
        } else {
            return Integer.toString(ifIndex);
        }
    }

    public void threshold(final List<FlowDocument> documents,
                          final ProcessingOptions options) throws ExecutionException, ThresholdInitializationException {

        if (!(options.applicationThresholding || options.applicationDataCollection)) {
            return;
        }

        final var now = Instant.now();

        for (final var document : documents) {
            if (document.getNodeExporter() != null && !Strings.isNullOrEmpty(document.getApplication())) {
                final var exporterKey = new ExporterKey(document.getNodeExporter().getInterfaceId());

                final var session = this.sessions.computeIfAbsent(exporterKey, key -> {
                    LOG.debug("Accepting session for exporterKey={}", exporterKey);

                    final OnmsIpInterface iface = this.ipInterfaceDao.get(exporterKey.interfaceId);

                    final CollectionAgent collectionAgent = FlowThresholding.this.collectionAgentFactory.createCollectionAgent(iface);

                    final ThresholdingSession thresholdingSession;
                    try {
                        thresholdingSession = FlowThresholding.this.thresholdingService.createSession(iface.getNodeId(),
                                                                                                      collectionAgent.getHostAddress(),
                                                                                                      SERVICE_NAME,
                                                                                                      new ServiceParameters(Collections.emptyMap()));
                    } catch (ThresholdInitializationException e) {
                        throw new RuntimeException("Error initializing thresholding session", e);
                    }

                    // Find the collection package for this exporter
                    PackageDefinition packageDefinition = null;
                    for (final PackageDefinition pkg : options.packages) {
                        if (pkg.getFilterRule() == null || FlowThresholding.this.filterDao.isValid(collectionAgent.getHostAddress(), pkg.getFilterRule())) {
                            packageDefinition = pkg;
                            break;
                        }
                    }

                    return new Session(thresholdingSession,
                                       collectionAgent,
                                       systemIdHash,
                                       options.applicationThresholding,
                                       options.applicationDataCollection,
                                       packageDefinition,
                                       collectionAgent.getHostAddress());
                });

                session.process(now, document);
            }
        }
    }

    public Set<ExporterKey> getExporterKeys() {
        return Collections.unmodifiableSet(this.sessions.keySet());
    }

    public Collection<Session> getSessions() {
        return Collections.unmodifiableCollection(this.sessions.values());
    }

    public static class Session {
        public final Map<IndexKey, Map<String, AtomicLong>> applications = Maps.newConcurrentMap();

        public final boolean thresholding;
        public final boolean dataCollection;

        public final PackageDefinition packageDefinition;

        public final ThresholdingSession thresholdingSession;
        public final CollectionAgent collectionAgent;

        // The last time this session sees any incoming flow.
        // This is not synchronized as reference updates are always atomic.
        // See https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.7
        private volatile Instant lastUpdate = null;

        private final AtomicLong sequenceNumber;

        private final String exporterIpAddress;

        private boolean dirty = true;

        private Session(final ThresholdingSession thresholdingSession,
                        final CollectionAgent collectionAgent,
                        final long systemIdHash,
                        final boolean thresholding,
                        final boolean dataCollection,
                        final PackageDefinition packageDefinition,
                        final String exporterIpAddress) {
            this.sequenceNumber = new AtomicLong(systemIdHash | ThreadLocalRandom.current().nextInt());

            this.thresholdingSession = Objects.requireNonNull(thresholdingSession);
            this.collectionAgent = Objects.requireNonNull(collectionAgent);

            this.thresholding = thresholding;
            this.dataCollection = dataCollection;

            this.packageDefinition =  packageDefinition;
            this.exporterIpAddress = exporterIpAddress;
        }

        public void process(final Instant now, final FlowDocument document) {
            if (document.getInputSnmp() != null &&
                document.getInputSnmp() != 0 &&
                (document.getDirection() == Direction.INGRESS || document.getDirection() == Direction.UNKNOWN)) {
                final IndexKey indexKey = new IndexKey(document.getInputSnmp(), Direction.INGRESS);
                synchronized (this.applications) {
                    this.applications.computeIfAbsent(indexKey, k -> new TreeMap<>()).computeIfAbsent(document.getApplication(), a -> new AtomicLong(0)).addAndGet(document.getBytes());
                }
            }

            if (document.getOutputSnmp() != null
                && document.getOutputSnmp() != 0 &&
                (document.getDirection() == Direction.EGRESS || document.getDirection() == Direction.UNKNOWN)) {
                final IndexKey indexKey = new IndexKey(document.getOutputSnmp(), Direction.EGRESS);
                synchronized (this.applications) {
                    this.applications.computeIfAbsent(indexKey, k -> new TreeMap<>()).computeIfAbsent(document.getApplication(), a -> new AtomicLong(0)).addAndGet(document.getBytes());
                }
            }

            // Mark session as updated
            this.lastUpdate = now;
        }

        public Instant getLastUpdate() {
            return this.lastUpdate;
        }

        @Override
        public String toString() {
            return "Session{" +
                    "thresholding=" + thresholding +
                    ", dataCollection=" + dataCollection +
                    ", lastUpdate=" + lastUpdate +
                    ", sequenceNumber=" + sequenceNumber +
                    ", exporterIpAddress=" + exporterIpAddress +
                    ", dirty=" + dirty +
                    '}';
        }
    }

    @Override
    public void close() {
        this.classificationEngine.removeClassificationRulesReloadedListener(this);
        this.sessions.clear();
        this.timer.cancel();
    }
}
