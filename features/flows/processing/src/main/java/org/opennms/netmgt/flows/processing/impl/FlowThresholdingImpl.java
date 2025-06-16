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

import static org.opennms.integration.api.v1.flows.Flow.Direction;

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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
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
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.processing.ProcessingOptions;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
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

public class FlowThresholdingImpl implements Closeable, ClassificationEngine.ClassificationRulesReloadedListener {
    private static final Logger LOG = LoggerFactory.getLogger(FlowThresholdingImpl.class);

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

    private final SessionUtils sessionUtils;

    public final long systemIdHash;

    private final ConcurrentMap<ExporterKey, Session> sessions = Maps.newConcurrentMap();

    private long stepSizeMs = 0;
    private volatile long idleTimeoutMs = 15 * 60 * 1000;

    private Timer timer;

    private FilterService filterService;

    private ClassificationEngine classificationEngine;

    private List<Rule> classificationRuleList;

    private ReentrantReadWriteLock classificationRuleListReadWriteLock = new ReentrantReadWriteLock();

    public FlowThresholdingImpl(final ThresholdingService thresholdingService,
                                final CollectionAgentFactory collectionAgentFactory,
                                final PersisterFactory persisterFactory,
                                final IpInterfaceDao ipInterfaceDao,
                                final DistPollerDao distPollerDao,
                                final SnmpInterfaceDao snmpInterfaceDao,
                                final FilterDao filterDao,
                                final SessionUtils sessionUtils,
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
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.filterService = Objects.requireNonNull(filterService);
        this.classificationRuleList = classificationRuleProvider.getRules();
        this.classificationEngine = Objects.requireNonNull(classificationEngine);
        this.classificationEngine.addClassificationRulesReloadedListener(this);
    }

    @Override
    public void classificationRulesReloaded(final List<Rule> classificationRuleList) {
        final Lock writeLock = classificationRuleListReadWriteLock.writeLock();
        writeLock.lock();
        try {
            this.classificationRuleList = classificationRuleList;
        } finally {
            writeLock.unlock();
        }

        LOG.debug("Classification rules reloaded. Marking sessions as dirty.");

        for (final Session session : this.sessions.values()) {
            session.updateApplicationList(getListOfApplicationsToPersist(session.exporterIpAddress));
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
                try {
                    runTimerTask();
                } catch (final Throwable ex) {
                    LOG.error("Thresholding timer bailed", ex);
                }
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
            if (session.lastUpdate != null && session.lastUpdate.isBefore(Instant.now().minus(this.idleTimeoutMs, ChronoUnit.MILLIS))) {
                idleSessions.add(exporterKey);
                continue;
            }

            final OnmsIpInterface iface = this.ipInterfaceDao.get(exporterKey.interfaceId);
            final NodeLevelResource nodeResource = new NodeLevelResource(iface.getNodeId());

            for (final Map.Entry<IndexKey, Map<String, AtomicLong>> indexEntry : session.indexKeyMap.entrySet()) {
                for (final Map.Entry<String, AtomicLong> applicationEntry : indexEntry.getValue().entrySet()) {
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

    private Set<String> getListOfApplicationsToPersist(final String exporterIpAddress) {
        final Lock readLock = classificationRuleListReadWriteLock.readLock();
        readLock.lock();
        try {
            return classificationRuleList.stream()
                    .filter(r -> r.getExporterFilter() == null || filterService.matches(exporterIpAddress, r.getExporterFilter()))
                    .map(r -> r.getName())
                    .collect(Collectors.toSet());
        } finally {
            readLock.unlock();
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

    public void threshold(final List<EnrichedFlow> documents,
                          final ProcessingOptions options) throws ExecutionException, ThresholdInitializationException {

        if (!(options.applicationThresholding || options.applicationDataCollection)) {
            return;
        }

        final var now = Instant.now();

        for (final var document : documents) {
            if (document.getExporterNodeInfo() != null && !Strings.isNullOrEmpty(document.getApplication())) {
                final var exporterKey = new ExporterKey(document.getExporterNodeInfo().getInterfaceId());

                final var session = this.sessions.computeIfAbsent(exporterKey, key ->
                        this.sessionUtils.withTransaction(() -> {
                            LOG.debug("Accepting session for exporterKey={}", exporterKey);

                            final OnmsIpInterface iface = FlowThresholdingImpl.this.ipInterfaceDao.get(exporterKey.interfaceId);

                            final CollectionAgent collectionAgent = FlowThresholdingImpl.this.collectionAgentFactory.createCollectionAgent(iface);

                            final ThresholdingSession thresholdingSession;
                            try {
                                thresholdingSession = FlowThresholdingImpl.this.thresholdingService.createSession(iface.getNodeId(),
                                        collectionAgent.getHostAddress(),
                                        SERVICE_NAME,
                                        new ServiceParameters(Collections.emptyMap()));
                            } catch (ThresholdInitializationException e) {
                                throw new RuntimeException("Error initializing thresholding session", e);
                            }

                            // Find the collection package for this exporter
                            PackageDefinition packageDefinition = null;
                            for (final PackageDefinition pkg : options.packages) {
                                if (pkg.getFilterRule() == null || FlowThresholdingImpl.this.filterDao.isValid(collectionAgent.getHostAddress(), pkg.getFilterRule())) {
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
                                    collectionAgent.getHostAddress(),
                                    getListOfApplicationsToPersist(collectionAgent.getHostAddress()));
                        }));

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
        private static final Logger LOG = LoggerFactory.getLogger(Session.class);

        public final Map<IndexKey, Map<String, AtomicLong>> indexKeyMap = Maps.newConcurrentMap();

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

        private ReentrantReadWriteLock applicationsReadWriteLock = new ReentrantReadWriteLock();

        private Set<String> applications;

        private Session(final ThresholdingSession thresholdingSession,
                        final CollectionAgent collectionAgent,
                        final long systemIdHash,
                        final boolean thresholding,
                        final boolean dataCollection,
                        final PackageDefinition packageDefinition,
                        final String exporterIpAddress,
                        final Set<String> applicationsToPersist) {
            this.sequenceNumber = new AtomicLong(systemIdHash | ThreadLocalRandom.current().nextInt());
            this.thresholdingSession = Objects.requireNonNull(thresholdingSession);
            this.collectionAgent = Objects.requireNonNull(collectionAgent);
            this.thresholding = thresholding;
            this.dataCollection = dataCollection;
            this.packageDefinition = packageDefinition;
            this.exporterIpAddress = exporterIpAddress;
            updateApplicationList(applicationsToPersist);
        }

        public void updateApplicationList(final Set<String> applications) {
            final Lock writeLock = applicationsReadWriteLock.writeLock();
            writeLock.lock();

            try {
                this.applications = applications;

                LOG.debug("Found {} matching applications for exporter {}", this.applications.size(), exporterIpAddress);

                for (final IndexKey indexKey : indexKeyMap.keySet()) {
                    final int beforeAdd = indexKeyMap.get(indexKey).size();

                    for (final String application : this.applications) {
                        indexKeyMap.get(indexKey).computeIfAbsent(application, a -> new AtomicLong(0));
                    }

                    final int afterAddBeforePurge = indexKeyMap.get(indexKey).size();
                    LOG.debug("Added {} applications for {}/{}/{}", afterAddBeforePurge - beforeAdd, exporterIpAddress, indexKey.iface, indexKey.direction);

                    indexKeyMap.get(indexKey).keySet().retainAll(applications);
                    final int afterPurge = indexKeyMap.get(indexKey).size();
                    LOG.debug("Removed {} applications for {}/{}/{}", afterAddBeforePurge - afterPurge, exporterIpAddress, indexKey.iface, indexKey.direction);
                }
            } finally {
                writeLock.unlock();
            }
        }

        private void addValue(final IndexKey indexKey, final String application, final long bytes) {
            if (!indexKeyMap.containsKey(indexKey)) {
                final Lock readLock = applicationsReadWriteLock.readLock();
                readLock.lock();
                try {
                    indexKeyMap.put(indexKey, applications.stream().collect(Collectors.toConcurrentMap(Function.identity(), e -> new AtomicLong(0))));
                } finally {
                    readLock.unlock();
                }
            }

            indexKeyMap.get(indexKey).get(application).addAndGet(bytes);
        }

        public void process(final Instant now, final EnrichedFlow document) {
            if (document.getInputSnmp() != null &&
                    document.getInputSnmp() != 0 &&
                    (document.getDirection() == Direction.INGRESS || document.getDirection() == Direction.UNKNOWN)) {
                final IndexKey indexKey = new IndexKey(document.getInputSnmp(), Direction.INGRESS);
                addValue(indexKey, document.getApplication(), document.getBytes());
            }

            if (document.getOutputSnmp() != null
                    && document.getOutputSnmp() != 0 &&
                    (document.getDirection() == Direction.EGRESS || document.getDirection() == Direction.UNKNOWN)) {
                final IndexKey indexKey = new IndexKey(document.getOutputSnmp(), Direction.EGRESS);
                addValue(indexKey, document.getApplication(), document.getBytes());
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
                    '}';
        }
    }

    @Override
    public void close() {
        this.classificationEngine.removeClassificationRulesReloadedListener(this);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        this.sessions.clear();
    }

    public static class IndexKey {
        public final int iface;
        public final Direction direction;

        public IndexKey(final int iface,
                        final Direction direction) {
            this.iface = iface;
            this.direction = direction;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IndexKey)) {
                return false;
            }
            final IndexKey that = (IndexKey) o;
            return Objects.equals(this.iface, that.iface) &&
                   Objects.equals(this.direction, that.direction);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.iface,
                                this.direction);
        }

        @Override
        public String toString() {
            return "IndexKey{" +
                   "iface=" + iface +
                   ", direction=" + direction +
                   '}';
        }
    }

    public static class ExporterKey {
        public final int interfaceId;

        public ExporterKey(final int interfaceId) {
            this.interfaceId = interfaceId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ExporterKey)) {
                return false;
            }
            final ExporterKey that = (ExporterKey) o;
            return Objects.equals(this.interfaceId, that.interfaceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.interfaceId);
        }

        @Override
        public String toString() {
            return "ExporterKey{" +
                    "interfaceId=" + interfaceId +
                    '}';
        }
    }
}
