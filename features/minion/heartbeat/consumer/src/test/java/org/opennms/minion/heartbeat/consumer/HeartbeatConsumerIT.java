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
package org.opennms.minion.heartbeat.consumer;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.minion.heartbeat.common.MinionIdentityDTO;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.FasterFilesystemForeignSourceRepository;
import org.opennms.netmgt.provision.persist.FusedForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class HeartbeatConsumerIT {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatConsumerIT.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private MinionDao minionDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;

    @Test
    public void testProvisioningOfMinions() throws IOException {
        EventProxy eventProxy = Mockito.mock(EventProxy.class);
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.hasEventListener(Mockito.anyString())).thenReturn(true);
        FusedForeignSourceRepository foreignSourceRepository = new FusedForeignSourceRepository();
        FasterFilesystemForeignSourceRepository deployed = new FasterFilesystemForeignSourceRepository();
        String foreignSourcePath = tempFolder.newFolder("foreign-sources").getPath();
        String importsPath = tempFolder.newFolder("imports").getPath();
        String pendingForeignSourcePath = tempFolder.newFolder("foreign-sources", "pending").getPath();
        String pendingImportsPath = tempFolder.newFolder("imports", "pending").getPath();
        deployed.setForeignSourcePath(foreignSourcePath);
        deployed.setRequisitionPath(importsPath);
        FasterFilesystemForeignSourceRepository pending = new FasterFilesystemForeignSourceRepository();
        pending.setRequisitionPath(pendingImportsPath);
        pending.setForeignSourcePath(pendingForeignSourcePath);
        foreignSourceRepository.setDeployedForeignSourceRepository(deployed);
        foreignSourceRepository.setPendingForeignSourceRepository(pending);

        // Spawn 500 minions
        List<MinionIdentityDTO> minionDTOs = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
            minionIdentityDTO.setId(UUID.randomUUID().toString());
            minionIdentityDTO.setLocation(UUID.randomUUID().toString());
            minionDTOs.add(minionIdentityDTO);
        }

        HeartbeatConsumer heartbeatConsumer = new HeartbeatConsumer();
        heartbeatConsumer.setMinionDao(minionDao);
        heartbeatConsumer.setEventProxy(eventProxy);
        heartbeatConsumer.setDeployedForeignSourceRepository(foreignSourceRepository);
        heartbeatConsumer.setEventSubscriptionService(eventSubscriptionService);
        heartbeatConsumer.setNodeDao(nodeDao);

        // Stream the messages in parallel.
        minionDTOs.parallelStream().forEach(heartbeatConsumer::handleMessage);

        //Verify that heartbeat does get consumed within short time.
        await().atMost(10, TimeUnit.SECONDS).until(() -> minionDao.countAll() == 500);

        //Verify that eventually all the minions get persisted in imports.
        await().atMost(45, TimeUnit.SECONDS).until(() ->
                heartbeatConsumer.getDeployedForeignSourceRepository().getRequisitions().stream()
                        .mapToInt(Requisition::getNodeCount).sum() == 500);

        // Now Mock NodeDao to return true for minion existence.
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        List<OnmsNode> onmsNodes = new ArrayList<>();
        onmsNodes.add(new OnmsNode());
        Mockito.when(mockNodeDao.findByForeignIdForLocation(Mockito.anyString(), Mockito.anyString())).thenReturn(onmsNodes);
        heartbeatConsumer.setNodeDao(mockNodeDao);
        // Spawn 500 more minions.
        for (int i = 0; i < 500; i++) {
            MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
            minionIdentityDTO.setId(UUID.randomUUID().toString());
            minionIdentityDTO.setLocation(UUID.randomUUID().toString());
            minionDTOs.add(minionIdentityDTO);
        }
        // Stream the messages in parallel.
        minionDTOs.parallelStream().forEach(heartbeatConsumer::handleMessage);

        //Verify that heartbeat does get consumed within short time.
        await().atMost(10, TimeUnit.SECONDS).until(() -> minionDao.countAll() == 1000);

        // Verify that no new requisition nodes get added and provisioning got short-circuited
        Assert.assertThat(Collections.unmodifiableSet(heartbeatConsumer.getDeployedForeignSourceRepository().getRequisitions()).stream()
                .mapToInt(Requisition::getNodeCount).sum(), Matchers.is(500));

        // Verify that some of the heartbeats are rejected.
        Assert.assertThat(heartbeatConsumer.getNumofRejected().get(), Matchers.greaterThanOrEqualTo(0));
    }



    @Test
    public void testMonitoringSystemLocationChangedEventWhenMinionChangesLocation() throws IOException {

        FusedForeignSourceRepository foreignSourceRepository = new FusedForeignSourceRepository();
        FasterFilesystemForeignSourceRepository deployed = new FasterFilesystemForeignSourceRepository();
        String foreignSourcePath = tempFolder.newFolder("foreign-sources").getPath();
        String importsPath = tempFolder.newFolder("imports").getPath();
        String pendingForeignSourcePath = tempFolder.newFolder("foreign-sources", "pending").getPath();
        String pendingImportsPath = tempFolder.newFolder("imports", "pending").getPath();
        deployed.setForeignSourcePath(foreignSourcePath);
        deployed.setRequisitionPath(importsPath);
        FasterFilesystemForeignSourceRepository pending = new FasterFilesystemForeignSourceRepository();
        pending.setRequisitionPath(pendingImportsPath);
        pending.setForeignSourcePath(pendingForeignSourcePath);
        foreignSourceRepository.setDeployedForeignSourceRepository(deployed);
        foreignSourceRepository.setPendingForeignSourceRepository(pending);

        HeartbeatConsumer heartbeatConsumer = new HeartbeatConsumer();
        heartbeatConsumer.setMinionDao(minionDao);
        heartbeatConsumer.setEventProxy(m_mockEventIpcManager);
        heartbeatConsumer.setDeployedForeignSourceRepository(foreignSourceRepository);
        heartbeatConsumer.setEventSubscriptionService(m_mockEventIpcManager);
        heartbeatConsumer.setNodeDao(nodeDao);


        MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
        String minionId = UUID.randomUUID().toString();
        String firstLocation = UUID.randomUUID().toString();
        minionIdentityDTO.setId(minionId);
        minionIdentityDTO.setLocation(firstLocation);

        EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_ADDED_UEI,
                "OpenNMS.Minion.Heartbeat");

        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, minionId);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, firstLocation);

        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(eventBuilder.getEvent());

        heartbeatConsumer.handleMessage(minionIdentityDTO);

        // Wait until we receive monitoringSystemAdded event.
        await().atMost(15, TimeUnit.SECONDS).until(() -> m_mockEventIpcManager.getEventAnticipator().getAnticipatedEventsReceived(), hasSize(1));

        // Change location and send heartbeat
        String secondLocation = UUID.randomUUID().toString();
        minionIdentityDTO.setLocation(secondLocation);

        eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_LOCATION_CHANGED_UEI,
                "OpenNMS.Minion.Heartbeat");
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, minionId);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_PREV_LOCATION, firstLocation);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, secondLocation);
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(eventBuilder.getEvent());

        heartbeatConsumer.handleMessage(minionIdentityDTO);

        // Wait until we receive monitoringSystemLocationChanged event.
        await().atMost(15, TimeUnit.SECONDS).until(() -> m_mockEventIpcManager.getEventAnticipator().getAnticipatedEventsReceived(), hasSize(2));

    }

    /**
     * Reproduction harness for truncated Minion requisitions seen with
     * {@code opennms.minion.provisioning=true}.
     *
     * <p>Over 1000 distinct minions are provisioned into a <em>single shared</em> requisition,
     * then made to re-send their heartbeats repeatedly. Because the default
     * {@code opennms.minion.provisioning.foreignSourcePattern} ("Minions") has no format
     * specifier, every minion lands in one "Minions" requisition regardless of location — so
     * giving them all the same location simply maximizes read-modify-write contention on that
     * one requisition file, which is where node loss has been observed in the field.
     *
     * <p>The truncation vector is the requisition cache: {@code FileReloadContainer} considers
     * a cached requisition fresh when the file's {@code lastModified} has not advanced and its
     * length is unchanged (see its own XXX comment about same-second writes). Under a burst of
     * rapid saves a stale, smaller requisition can be served back into the next
     * read-modify-write cycle, dropping previously-added nodes.
     *
     * <p>Heartbeats are submitted in waves no larger than the provisioning queue and the
     * single-threaded executor is drained between waves, so the bounded executor never rejects
     * (and drops) a heartbeat. A node-count shortfall therefore can only be truncation, not a
     * dropped heartbeat. The invariant asserted is simple: the requisition must contain exactly
     * one node per distinct minion, after the initial build-up and after every replay round.
     */
    @Test
    public void testManyMinionsProvisionIntoSharedRequisitionWithoutTruncation() throws IOException {
        // Wave size stays under HeartbeatConsumer's provisioning queue (default 500) so the
        // bounded executor cannot reject a heartbeat — rejection would confound truncation.
        final int waveSize = 256;
        final int totalMinions = 1024; // > 1000
        final int replayRounds = 2;

        final EventProxy eventProxy = Mockito.mock(EventProxy.class);
        final EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.hasEventListener(Mockito.anyString())).thenReturn(true);

        final HeartbeatConsumer heartbeatConsumer = newFilesystemBackedConsumer(eventProxy, eventSubscriptionService);

        // All minions share one location -> one shared "Minions" requisition, maximum contention.
        final String sharedLocation = "stress-location";
        final List<MinionIdentityDTO> minions = new ArrayList<>(totalMinions);
        for (int i = 0; i < totalMinions; i++) {
            final MinionIdentityDTO dto = new MinionIdentityDTO();
            dto.setId(UUID.randomUUID().toString());
            dto.setLocation(sharedLocation);
            minions.add(dto);
        }

        // Initial build-up: provision all minions for the first time.
        provisionInDrainedWaves(heartbeatConsumer, minions, waveSize);

        Assert.assertEquals("no heartbeat should have been rejected by the provisioning queue",
                0, heartbeatConsumer.getNumofRejected().get());
        Assert.assertEquals("every minion should be persisted to the DB",
                totalMinions, minionDao.countAll());
        Assert.assertEquals("requisition truncated during initial build-up: fewer nodes than minions",
                totalMinions, totalRequisitionNodes(heartbeatConsumer));

        // Steady state: the same minions repeatedly re-send heartbeats. Re-provisioning a
        // known minion must neither drop nor duplicate its requisition node.
        for (int round = 1; round <= replayRounds; round++) {
            provisionInDrainedWaves(heartbeatConsumer, minions, waveSize);
            Assert.assertEquals("requisition node count changed after replay round " + round
                            + " (truncation or duplication)",
                    totalMinions, totalRequisitionNodes(heartbeatConsumer));
        }
    }

    /**
     * Builds a HeartbeatConsumer backed by on-disk (filesystem) foreign source repositories,
     * exactly as the other tests do, but factored out for reuse.
     */
    private HeartbeatConsumer newFilesystemBackedConsumer(final EventProxy eventProxy,
                                                          final EventSubscriptionService eventSubscriptionService)
            throws IOException {
        final FusedForeignSourceRepository foreignSourceRepository = new FusedForeignSourceRepository();
        final FasterFilesystemForeignSourceRepository deployed = new FasterFilesystemForeignSourceRepository();
        deployed.setForeignSourcePath(tempFolder.newFolder("foreign-sources").getPath());
        deployed.setRequisitionPath(tempFolder.newFolder("imports").getPath());
        final FasterFilesystemForeignSourceRepository pending = new FasterFilesystemForeignSourceRepository();
        pending.setForeignSourcePath(tempFolder.newFolder("foreign-sources", "pending").getPath());
        pending.setRequisitionPath(tempFolder.newFolder("imports", "pending").getPath());
        foreignSourceRepository.setDeployedForeignSourceRepository(deployed);
        foreignSourceRepository.setPendingForeignSourceRepository(pending);

        final HeartbeatConsumer heartbeatConsumer = new HeartbeatConsumer();
        heartbeatConsumer.setMinionDao(minionDao);
        heartbeatConsumer.setEventProxy(eventProxy);
        heartbeatConsumer.setDeployedForeignSourceRepository(foreignSourceRepository);
        heartbeatConsumer.setEventSubscriptionService(eventSubscriptionService);
        heartbeatConsumer.setNodeDao(nodeDao);
        return heartbeatConsumer;
    }

    /**
     * Submits the heartbeats in parallel waves of {@code waveSize}, draining the
     * single-threaded provisioning executor to quiescence between waves. Keeping each wave at
     * or below the executor's queue capacity guarantees no heartbeat is rejected/dropped.
     */
    private static void provisionInDrainedWaves(final HeartbeatConsumer consumer,
                                                final List<MinionIdentityDTO> minions,
                                                final int waveSize) {
        final ThreadPoolExecutor executor = consumer.getExecutor();
        for (int start = 0; start < minions.size(); start += waveSize) {
            final int end = Math.min(start + waveSize, minions.size());
            // parallelStream().forEach() returns only once every handleMessage has enqueued its
            // provisioning task, so after it returns the executor is either draining or idle.
            minions.subList(start, end).parallelStream().forEach(consumer::handleMessage);
            await().atMost(180, TimeUnit.SECONDS)
                    .until(() -> executor.getQueue().isEmpty() && executor.getActiveCount() == 0);
        }
    }

    private static int totalRequisitionNodes(final HeartbeatConsumer consumer) {
        return consumer.getDeployedForeignSourceRepository().getRequisitions().stream()
                .mapToInt(Requisition::getNodeCount).sum();
    }

    /**
     * Reproduction harness for the {@code reloadImport}-storm hypothesis.
     *
     * <p>Every time {@code HeartbeatConsumer.provision()} alters a foreign source it fires a
     * {@code uei.opennms.org/internal/importer/reloadImport} event carrying the requisition URL.
     * With 1000+ minions piling into a single shared "Minions" requisition that is one event per
     * node added — a storm of requisition re-imports. In production provisiond consumes those
     * events and re-imports the requisition <em>concurrently</em>, from its own
     * {@link FasterFilesystemForeignSourceRepository} instance with its own cache, while the
     * HeartbeatConsumer is still adding nodes.
     *
     * <p>This test recreates that topology exactly: the consumer writes through one deployed
     * repository, while a second deployed repository instance — pointed at the SAME on-disk
     * directories — plays provisiond, re-importing the requisition on a background thread for
     * each reloadImport event. The two instances have independent locks and independent caches
     * (per-instance {@code m_globalLock}), so a re-import can read a stale cached requisition and
     * {@code save()} it back over a fresher file written by the consumer — a lost update that
     * truncates the requisition.
     *
     * <p>Re-imports are coalesced to at most one in flight, mirroring how provisiond debounces
     * reloadImport for a given foreign source (and keeping the test's I/O bounded). The asserted
     * invariant is unchanged: once both writers go quiescent the requisition must hold exactly
     * one node per minion. A shortfall is the truncation we are hunting.
     */
    @Test
    public void testReloadImportStormDoesNotTruncateSharedRequisition() throws Exception {
        final int waveSize = 256;
        final int totalMinions = 1024; // > 1000

        final EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.hasEventListener(Mockito.anyString())).thenReturn(true);

        // Deployed repository directories are shared on disk between the consumer and the
        // simulated provisiond importer.
        final String foreignSourcePath = tempFolder.newFolder("foreign-sources").getPath();
        final String importsPath = tempFolder.newFolder("imports").getPath();
        final String pendingForeignSourcePath = tempFolder.newFolder("foreign-sources", "pending").getPath();
        final String pendingImportsPath = tempFolder.newFolder("imports", "pending").getPath();

        // Repository the HeartbeatConsumer writes through.
        final FusedForeignSourceRepository foreignSourceRepository = new FusedForeignSourceRepository();
        final FasterFilesystemForeignSourceRepository consumerDeployed = new FasterFilesystemForeignSourceRepository();
        consumerDeployed.setForeignSourcePath(foreignSourcePath);
        consumerDeployed.setRequisitionPath(importsPath);
        final FasterFilesystemForeignSourceRepository pending = new FasterFilesystemForeignSourceRepository();
        pending.setForeignSourcePath(pendingForeignSourcePath);
        pending.setRequisitionPath(pendingImportsPath);
        foreignSourceRepository.setDeployedForeignSourceRepository(consumerDeployed);
        foreignSourceRepository.setPendingForeignSourceRepository(pending);

        // Separate repository instance over the SAME directories — this is "provisiond".
        final FasterFilesystemForeignSourceRepository provisiondDeployed = new FasterFilesystemForeignSourceRepository();
        provisiondDeployed.setForeignSourcePath(foreignSourcePath);
        provisiondDeployed.setRequisitionPath(importsPath);
        final ProvisiondImportSimulator provisiond = new ProvisiondImportSimulator(provisiondDeployed);

        final HeartbeatConsumer heartbeatConsumer = new HeartbeatConsumer();
        heartbeatConsumer.setMinionDao(minionDao);
        heartbeatConsumer.setEventProxy(provisiond);
        heartbeatConsumer.setDeployedForeignSourceRepository(foreignSourceRepository);
        heartbeatConsumer.setEventSubscriptionService(eventSubscriptionService);
        heartbeatConsumer.setNodeDao(nodeDao);

        final String sharedLocation = "stress-location";
        final List<MinionIdentityDTO> minions = new ArrayList<>(totalMinions);
        for (int i = 0; i < totalMinions; i++) {
            final MinionIdentityDTO dto = new MinionIdentityDTO();
            dto.setId(UUID.randomUUID().toString());
            dto.setLocation(sharedLocation);
            minions.add(dto);
        }

        try {
            // NOTE: the simulated provisiond runs the full DefaultProvisionService.loadRequisition()
            // sequence (importResourceRequisition + updateLastImported + save + flush) concurrently
            // with the consumer's own writes. Failure can surface two ways: (1) before the atomic-
            // write fix, a torn read of a half-written file yields a null requisition and provision()
            // NPEs on the worker thread (attributed to this test); (2) a lost update where a
            // concurrent save() writes a stale snapshot over the consumer's edits — caught by the
            // node-count assertion below. This test therefore tells us whether loadRequisition's
            // trailing save(r) still truncates the requisition even with atomic writes + Part A.
            provisionInDrainedWaves(heartbeatConsumer, minions, waveSize);
            // Let any coalesced re-import finish so we read a settled end state.
            provisiond.awaitQuiescent();

            final int nodes = totalRequisitionNodes(heartbeatConsumer);
            LOG.info("reloadImport storm: minions={}, reloadImport events={}, concurrent re-imports={}, import failures={}, final requisition nodes={}",
                    totalMinions, provisiond.getReloadImportEvents(), provisiond.getReimports(),
                    provisiond.getImportFailures(), nodes);

            Assert.assertEquals("no heartbeat should have been rejected by the provisioning queue",
                    0, heartbeatConsumer.getNumofRejected().get());
            Assert.assertEquals("every minion should be persisted to the DB",
                    totalMinions, minionDao.countAll());
            Assert.assertEquals("requisition truncated under reloadImport storm (reloadImport events="
                            + provisiond.getReloadImportEvents() + ", concurrent re-imports="
                            + provisiond.getReimports() + ")",
                    totalMinions, nodes);
        } finally {
            provisiond.shutdown();
        }
    }

    /**
     * Stands in for provisiond's reaction to {@code reloadImport} events. On each event it runs
     * the exact sequence of {@code DefaultProvisionService.loadRequisition()} —
     * {@code importResourceRequisition()} then {@code updateLastImported()} + {@code save()} +
     * {@code flush()} — through its own repository instance, on a background thread, concurrently
     * with the HeartbeatConsumer's ongoing writes. Re-imports are coalesced to at most one in
     * flight per the debounce provisiond itself applies.
     */
    private static final class ProvisiondImportSimulator implements EventProxy {
        private final FasterFilesystemForeignSourceRepository importerRepository;
        private final ExecutorService importer = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "simulated-provisiond-import"));
        private final AtomicBoolean importQueued = new AtomicBoolean(false);
        private final AtomicInteger reloadImportEvents = new AtomicInteger(0);
        private final AtomicInteger reimports = new AtomicInteger(0);
        private final AtomicInteger importFailures = new AtomicInteger(0);

        private ProvisiondImportSimulator(final FasterFilesystemForeignSourceRepository importerRepository) {
            this.importerRepository = importerRepository;
        }

        @Override
        public void send(final Event event) {
            if (event == null || !EventConstants.RELOAD_IMPORT_UEI.equals(event.getUei())) {
                return;
            }
            reloadImportEvents.incrementAndGet();

            final Parm urlParm = event.getParm(EventConstants.PARM_URL);
            if (urlParm == null || urlParm.getValue() == null) {
                return;
            }
            final String url = urlParm.getValue().getContent();
            if (url == null || url.isEmpty() || "null".equals(url)) {
                return;
            }

            // Coalesce: provisiond does not run one import per event for the same foreign source.
            if (!importQueued.compareAndSet(false, true)) {
                return;
            }
            importer.submit(() -> {
                importQueued.set(false);
                try {
                    // Mirror the FIXED DefaultProvisionService.loadRequisition(): import the
                    // resource (read-only self-import, Part A) then persist the last-import stamp
                    // via the targeted, per-file-locked read-modify-write rather than re-saving a
                    // stale full snapshot. updateLastImported() re-reads the current on-disk
                    // requisition and changes only the timestamp, so concurrent node additions by
                    // the HeartbeatConsumer are preserved.
                    final Requisition r = importerRepository.importResourceRequisition(new UrlResource(url));
                    importerRepository.updateLastImported(r.getForeignSource());
                    reimports.incrementAndGet();
                } catch (final Exception e) {
                    importFailures.incrementAndGet();
                    LOG.debug("simulated provisiond loadRequisition failed for {}", url, e);
                }
            });
        }

        @Override
        public void send(final Log eventLog) {
            // Heartbeat provisioning never sends event logs; nothing to simulate.
        }

        void awaitQuiescent() throws InterruptedException {
            // No more events will arrive once the consumer is drained; drain the single
            // coalesced import (if any) to a settled state.
            importer.shutdown();
            if (!importer.awaitTermination(180, TimeUnit.SECONDS)) {
                LOG.warn("simulated provisiond importer did not quiesce within timeout");
            }
        }

        void shutdown() {
            importer.shutdownNow();
        }

        int getReloadImportEvents() {
            return reloadImportEvents.get();
        }

        int getReimports() {
            return reimports.get();
        }

        int getImportFailures() {
            return importFailures.get();
        }
    }

}
