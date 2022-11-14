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

package org.opennms.minion.heartbeat.consumer;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
