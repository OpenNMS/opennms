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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.FasterFilesystemForeignSourceRepository;
import org.opennms.netmgt.provision.persist.FusedForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
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



    private FusedForeignSourceRepository createForeignSourceRepository() throws IOException {
        FusedForeignSourceRepository foreignSourceRepository = new FusedForeignSourceRepository();
        FasterFilesystemForeignSourceRepository deployed = new FasterFilesystemForeignSourceRepository();
        deployed.setForeignSourcePath(tempFolder.newFolder("foreign-sources").getPath());
        deployed.setRequisitionPath(tempFolder.newFolder("imports").getPath());
        FasterFilesystemForeignSourceRepository pending = new FasterFilesystemForeignSourceRepository();
        pending.setForeignSourcePath(tempFolder.newFolder("foreign-sources", "pending").getPath());
        pending.setRequisitionPath(tempFolder.newFolder("imports", "pending").getPath());
        foreignSourceRepository.setDeployedForeignSourceRepository(deployed);
        foreignSourceRepository.setPendingForeignSourceRepository(pending);
        return foreignSourceRepository;
    }

    private EventProxy createReloadCountingEventProxy(final AtomicInteger reloadCount) throws Exception {
        EventProxy eventProxy = Mockito.mock(EventProxy.class);
        Mockito.doAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            if (EventConstants.RELOAD_IMPORT_UEI.equals(event.getUei())) {
                reloadCount.incrementAndGet();
            }
            return null;
        }).when(eventProxy).send(Mockito.any(Event.class));
        return eventProxy;
    }

    private HeartbeatConsumer createConsumer(final NodeDao nodeDaoToUse, final EventProxy eventProxy) throws IOException {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.hasEventListener(Mockito.anyString())).thenReturn(true);

        HeartbeatConsumer heartbeatConsumer = new HeartbeatConsumer();
        heartbeatConsumer.setMinionDao(minionDao);
        heartbeatConsumer.setEventProxy(eventProxy);
        heartbeatConsumer.setDeployedForeignSourceRepository(createForeignSourceRepository());
        heartbeatConsumer.setEventSubscriptionService(eventSubscriptionService);
        heartbeatConsumer.setNodeDao(nodeDaoToUse);
        return heartbeatConsumer;
    }

    @Test
    public void testRequisitionRebuiltWhenUnreadable() throws Exception {
        final AtomicInteger reloadCount = new AtomicInteger();
        // The fleet is still provisioned in the database, only the requisition is unreadable
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        Mockito.when(mockNodeDao.findByForeignIdForLocation(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
        Mockito.when(mockNodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.singletonMap("existing-minion", 1));

        minionDao.save(new OnmsMinion("existing-minion", "existing-location", "up", new Date()));

        HeartbeatConsumer heartbeatConsumer = createConsumer(mockNodeDao, createReloadCountingEventProxy(reloadCount));

        // A customized foreign source definition that must survive the rebuild
        final ForeignSource customForeignSource = new ForeignSource("Minions");
        customForeignSource.addDetector(new PluginConfig("ICMP", "org.opennms.netmgt.provision.detector.icmp.IcmpDetector"));
        heartbeatConsumer.getDeployedForeignSourceRepository().save(customForeignSource);

        // An existing requisition that fails validation (duplicate foreign-ids) and therefore loads as null
        final File minionsXml = new File(tempFolder.getRoot(), "imports/Minions.xml");
        final String invalidRequisition = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\""
                + " date-stamp=\"2026-01-01T00:00:00.000-05:00\" foreign-source=\"Minions\">"
                + "<node location=\"loc\" foreign-id=\"dup\" node-label=\"a\"/>"
                + "<node location=\"loc\" foreign-id=\"dup\" node-label=\"b\"/>"
                + "</model-import>";
        Files.write(minionsXml.toPath(), invalidRequisition.getBytes(StandardCharsets.UTF_8));

        MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
        minionIdentityDTO.setId(UUID.randomUUID().toString());
        minionIdentityDTO.setLocation(UUID.randomUUID().toString());
        heartbeatConsumer.handleMessage(minionIdentityDTO);

        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 1L);

        final String content = new String(Files.readAllBytes(minionsXml.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue("the provisioned node must be restored", content.contains("foreign-id=\"existing-minion\""));
        Assert.assertTrue("the heartbeating minion must be added", content.contains("foreign-id=\"" + minionIdentityDTO.getId() + "\""));
        Assert.assertFalse("the invalid entries must be gone", content.contains("foreign-id=\"dup\""));
        Assert.assertEquals("a single reload import should be triggered", 1, reloadCount.get());

        final ForeignSource rebuiltForeignSource = heartbeatConsumer.getDeployedForeignSourceRepository().getForeignSource("Minions");
        Assert.assertNotNull("the custom detector must survive the rebuild", rebuiltForeignSource.getDetector("ICMP"));
        Assert.assertNotNull("the default detector must be added", rebuiltForeignSource.getDetector("SNMP"));
    }

    @Test
    public void testStaleRequisitionRestoresMissingProvisionedNodes() throws Exception {
        final AtomicInteger reloadCount = new AtomicInteger();
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        Mockito.when(mockNodeDao.findByForeignIdForLocation(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
        Mockito.when(mockNodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.singletonMap("existing-minion", 1));

        minionDao.save(new OnmsMinion("existing-minion", "existing-location", "up", new Date()));

        HeartbeatConsumer heartbeatConsumer = createConsumer(mockNodeDao, createReloadCountingEventProxy(reloadCount));

        // A valid requisition that lost the provisioned node (stale copy)
        final File minionsXml = new File(tempFolder.getRoot(), "imports/Minions.xml");
        final String staleRequisition = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\""
                + " date-stamp=\"2026-01-01T00:00:00.000-05:00\" foreign-source=\"Minions\"/>";
        Files.write(minionsXml.toPath(), staleRequisition.getBytes(StandardCharsets.UTF_8));

        MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
        minionIdentityDTO.setId(UUID.randomUUID().toString());
        minionIdentityDTO.setLocation(UUID.randomUUID().toString());
        heartbeatConsumer.handleMessage(minionIdentityDTO);

        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 1L);

        final String content = new String(Files.readAllBytes(minionsXml.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue("the provisioned node must be restored", content.contains("foreign-id=\"existing-minion\""));
        Assert.assertTrue("the heartbeating minion must be added", content.contains("foreign-id=\"" + minionIdentityDTO.getId() + "\""));
        Assert.assertEquals("a single reload import should be triggered", 1, reloadCount.get());
    }

    @Test
    public void testReloadRetriedWhenNodeMissingFromDatabase() throws Exception {
        final AtomicInteger reloadCount = new AtomicInteger();
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        Mockito.when(mockNodeDao.findByForeignIdForLocation(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
        Mockito.when(mockNodeDao.getForeignIdToNodeIdMap(Mockito.anyString())).thenReturn(Collections.emptyMap());

        final String minionId = UUID.randomUUID().toString();
        final String location = UUID.randomUUID().toString();
        minionDao.save(new OnmsMinion(minionId, location, "up", new Date()));

        HeartbeatConsumer heartbeatConsumer = createConsumer(mockNodeDao, createReloadCountingEventProxy(reloadCount));

        // The requisition already contains the minion, but its node never made it into the database
        // (e.g. the import triggered by an earlier heartbeat failed)
        final File minionsXml = new File(tempFolder.getRoot(), "imports/Minions.xml");
        final String requisition = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\""
                + " date-stamp=\"2026-01-01T00:00:00.000-05:00\" foreign-source=\"Minions\">"
                + "<node location=\"" + location + "\" foreign-id=\"" + minionId + "\" node-label=\"" + minionId + "\">"
                + "<interface ip-addr=\"127.0.0.1\" snmp-primary=\"P\">"
                + "<monitored-service service-name=\"Minion-Heartbeat\"/>"
                + "<monitored-service service-name=\"Minion-RPC\"/>"
                + "<monitored-service service-name=\"JMX-Minion\"/>"
                + "</interface></node></model-import>";
        Files.write(minionsXml.toPath(), requisition.getBytes(StandardCharsets.UTF_8));

        MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
        minionIdentityDTO.setId(minionId);
        minionIdentityDTO.setLocation(location);

        heartbeatConsumer.handleMessage(minionIdentityDTO);
        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 1L);
        Assert.assertEquals("the import should be re-triggered", 1, reloadCount.get());

        heartbeatConsumer.handleMessage(minionIdentityDTO);
        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 2L);
        Assert.assertEquals("the retry should be throttled", 1, reloadCount.get());

        heartbeatConsumer.setReloadRetryInterval(0);
        heartbeatConsumer.handleMessage(minionIdentityDTO);
        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 3L);
        Assert.assertEquals("the import should be re-triggered after the cooldown", 2, reloadCount.get());
    }

    @Test
    public void testReloadRetriedImmediatelyAfterFailedSend() throws Exception {
        final AtomicInteger reloadCount = new AtomicInteger();
        final AtomicBoolean failFirstSend = new AtomicBoolean(true);
        EventProxy eventProxy = Mockito.mock(EventProxy.class);
        Mockito.doAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            if (EventConstants.RELOAD_IMPORT_UEI.equals(event.getUei())) {
                if (failFirstSend.compareAndSet(true, false)) {
                    throw new EventProxyException("simulated event subsystem failure");
                }
                reloadCount.incrementAndGet();
            }
            return null;
        }).when(eventProxy).send(Mockito.any(Event.class));

        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        Mockito.when(mockNodeDao.findByForeignIdForLocation(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());
        Mockito.when(mockNodeDao.getForeignIdToNodeIdMap(Mockito.anyString())).thenReturn(Collections.emptyMap());

        final String minionId = UUID.randomUUID().toString();
        final String location = UUID.randomUUID().toString();
        minionDao.save(new OnmsMinion(minionId, location, "up", new Date()));

        HeartbeatConsumer heartbeatConsumer = createConsumer(mockNodeDao, eventProxy);

        final File minionsXml = new File(tempFolder.getRoot(), "imports/Minions.xml");
        final String requisition = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\""
                + " date-stamp=\"2026-01-01T00:00:00.000-05:00\" foreign-source=\"Minions\">"
                + "<node location=\"" + location + "\" foreign-id=\"" + minionId + "\" node-label=\"" + minionId + "\">"
                + "<interface ip-addr=\"127.0.0.1\" snmp-primary=\"P\">"
                + "<monitored-service service-name=\"Minion-Heartbeat\"/>"
                + "<monitored-service service-name=\"Minion-RPC\"/>"
                + "<monitored-service service-name=\"JMX-Minion\"/>"
                + "</interface></node></model-import>";
        Files.write(minionsXml.toPath(), requisition.getBytes(StandardCharsets.UTF_8));

        MinionIdentityDTO minionIdentityDTO = new MinionIdentityDTO();
        minionIdentityDTO.setId(minionId);
        minionIdentityDTO.setLocation(location);

        // The first re-fire attempt fails to send; no cooldown must be recorded for it
        heartbeatConsumer.handleMessage(minionIdentityDTO);
        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 1L);
        Assert.assertEquals("the failed send must not count as a reload", 0, reloadCount.get());

        heartbeatConsumer.handleMessage(minionIdentityDTO);
        await().atMost(10, TimeUnit.SECONDS).until(() -> heartbeatConsumer.getExecutor().getCompletedTaskCount() == 2L);
        Assert.assertEquals("the reload must be retried immediately after a failed send", 1, reloadCount.get());
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


}
