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
package org.opennms.features.grpc.exporter;


import com.google.protobuf.Empty;
import io.grpc.ConnectivityState;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.grpc.exporter.spog.NmsInventoryGrpcClient;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAlias;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.plugin.grpc.proto.services.AlarmUpdateList;
import org.opennms.plugin.grpc.proto.services.EventUpdateList;
import org.opennms.plugin.grpc.proto.services.NmsInventoryUpdateList;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NmsInventoryServiceSyncIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final int PORT = 50051;
    private static final String TENANT_ID = "opennms-prime";
    private static final String HOST_NAME = "localhost";

    private Server server;
    private NmsInventoryGrpcClient client;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private HwEntityDao hwEntityDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private AlarmDao eventDao;

    @Autowired
    private MockEventIpcManager eventdIpcMgr;

    private int _id = 1;
    private MockDatabase mockDatabase;
    private final NmsInventoryServiceSyncImpl inventoryService = new NmsInventoryServiceSyncImpl();

    @Before
    public void setUp() throws Exception {

        eventdIpcMgr.setEventWriter(mockDatabase);

        // create data for node
        populateData();

        // configure server and client
        configureTestServerWithRealClient();
    }

    private void populateData() {

        databasePopulator.addExtension(new DatabasePopulator.Extension<HwEntityDao>() {

            @Override
            public DatabasePopulator.DaoSupport<HwEntityDao> getDaoSupport() {
                return new DatabasePopulator.DaoSupport<HwEntityDao>(HwEntityDao.class, hwEntityDao);
            }

            @Override
            public void onPopulate(DatabasePopulator populator, HwEntityDao dao) {
                OnmsNode node = databasePopulator.getNode1();
                OnmsHwEntity port = getHwEntityPort(node);
                dao.save(port);
                OnmsHwEntity container = getHwEntityContainer(node);
                container.addChildEntity(port);
                dao.save(container);
                OnmsHwEntity module = getHwEntityModule(node);
                module.addChildEntity(container);
                dao.save(module);
                OnmsHwEntity powerSupply = getHwEntityPowerSupply(node);
                dao.save(powerSupply);
                OnmsHwEntity chassis = getHwEntityChassis(node);
                chassis.addChildEntity(module);
                chassis.addChildEntity(powerSupply);
                dao.save(chassis);
                dao.flush();
            }

            @Override
            public void onShutdown(DatabasePopulator populator, HwEntityDao dao) {
                for (OnmsHwEntity entity : dao.findAll()) {
                    dao.delete(entity);
                }
            }
        });

        databasePopulator.populateDatabase();
    }

    private void configureTestServerWithRealClient() throws IOException, InterruptedException, ExecutionException {
        server = ServerBuilder.forPort(PORT)
                .addService(inventoryService)
                .build()
                .start(); // Restart the server

        client = new NmsInventoryGrpcClient(HOST_NAME,
                null,
                false,
                new GrpcHeaderInterceptor(TENANT_ID)
        );
        this.startIT();
    }

    @Test
    public void testClientRecoveryAfterServerRestart() throws Exception {
        // Step 1: Send initial inventory update
        CompletableFuture<Boolean> initialResponseFuture = new CompletableFuture<>();
        boolean isShutdown = false;

        final var list = nodeDao.findAll();
        NmsInventoryUpdateList initialInventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.services.Node.newBuilder()
                        .setId(list.get(0).getId())
                        .setForeignSource(list.get(0).getForeignSource())
                        .setForeignId(list.get(0).getForeignId())
                        .setLocation(list.get(0).getLocation().getLocationName())
                        .setLabel(list.get(0).getLabel())
                        .build())
                .build();

        this.sendInventoryUpdateIT(initialResponseFuture, initialInventoryUpdateList);
        assertTrue("The initial response was not completed successfully.", initialResponseFuture.join());

        // Step 2: Stop the server to simulate a restart
        if (server != null) {
            server.shutdown();
            // Wait for the server to fully shutdown
            isShutdown = server.awaitTermination(5, TimeUnit.SECONDS);
        }

        Assert.assertTrue("Server failed to be shutdown properly", isShutdown);

        // Step 3: Restart the server and client
        configureTestServerWithRealClient();

        // Step 4: Send another inventory update after server recovery
        CompletableFuture<Boolean> recoveryResponseFuture = new CompletableFuture<>();

        NmsInventoryUpdateList recoveryInventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.services.Node.newBuilder()
                        .setId(list.get(0).getId())
                        .setForeignSource(list.get(0).getForeignSource())
                        .setForeignId(list.get(0).getForeignId())
                        .setLocation(list.get(0).getLocation().getLocationName())
                        .setLabel(list.get(0).getLabel())
                        .build())
                .build();

        this.sendInventoryUpdateIT(recoveryResponseFuture, recoveryInventoryUpdateList);
        assertTrue("The response after server recovery was not completed successfully.", recoveryResponseFuture.join());
        // Step 5: Assert that the client was able to reconnect and send the update after server restart
        System.out.println("Client successfully recovered and sent inventory update after server restart.");
    }

    @Test
    public void testInventoryDataToBeSent() {

        CompletableFuture<Boolean> responseFuture = new CompletableFuture<>();

        final var node = nodeDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No node found"));

        Assert.assertNotNull(node);
        Assert.assertTrue("Node id should be exist", node.getId() > 0);
        Assert.assertNotNull("ForeignSource should not be null", node.getForeignSource());
        Assert.assertNotNull("ForeignId should not be null", node.getForeignId());
        Assert.assertNotNull("Location should not be null", node.getLocation().getLocationName());
        Assert.assertNotNull("Node Label should not be null", node.getLabel());

        NmsInventoryUpdateList inventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.services.Node.newBuilder()
                        .setId(node.getId())
                        .setForeignSource(node.getForeignSource())
                        .setForeignId(node.getForeignId())
                        .setLocation(node.getLocation().getLocationName())
                        .setLabel(node.getLabel())
                        .build())
                .build();


        // Send the inventory update to the server
        this.sendInventoryUpdateIT(responseFuture, inventoryUpdateList);
        assertTrue("The response was not completed successfully for inventory valid input.", responseFuture.join());

    }

    @Test
    public void testAlarmDataToBeSent() {

        CompletableFuture<Boolean> responseFuture = new CompletableFuture<>();

        eventdIpcMgr.sendNow(MockEventUtil.createNodeDownEventBuilder("test", databasePopulator.getNode1()).getEvent());
        final OnmsAlarm alarm = nodeDownAlarmWithRelatedAlarm();
        alarmDao.save(alarm);

        final var alarmResponse = eventDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No alarm found"));

        Assert.assertNotNull(alarmResponse);
        Assert.assertTrue("Alarm id should be exist", alarmResponse.getId() > 0);
        Assert.assertTrue("Node id in alarm should be exist", alarmResponse.getNodeId() > 0);
        Assert.assertEquals(EventConstants.NODE_DOWN_EVENT_UEI, alarmResponse.getUei());

        AlarmUpdateList alarmUpdateList = AlarmUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addAlarms(org.opennms.plugin.grpc.proto.services.Alarm.newBuilder()
                        .setId(alarmResponse.getId())
                        .setUei(alarmResponse.getUei())
                        .setDescription(alarmResponse.getDescription())
                        .setNodeCriteria(org.opennms.plugin.grpc.proto.services.NodeCriteria.newBuilder()
                                .setId(alarmResponse.getNodeId())
                                .setForeignId(alarmResponse.getNode().getForeignId())
                                .setForeignSource(alarmResponse.getNode().getForeignSource())
                                .build()))
                .build();

        this.sendAlarmUpdateIT(responseFuture, alarmUpdateList);
        assertTrue("The response was not completed successfully for alarm valid input.", responseFuture.join());
    }

    @Test
    public void testEventDataToBeSent() {
        CompletableFuture<Boolean> responseFuture = new CompletableFuture<>();
        eventdIpcMgr.sendNow(MockEventUtil.createNodeUpEventBuilder("test", databasePopulator.getNode1()).getEvent());
        final OnmsAlarm alarm = nodeUpAlarmWithRelatedAlarm();
        alarmDao.save(alarm);

        final var eventResponse = eventDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No event found"));

        Assert.assertNotNull(eventResponse);
        Assert.assertTrue("Event id should be exist", eventResponse.getId() > 0);
        Assert.assertTrue("Node id in event should be exist", eventResponse.getNodeId() > 0);
        Assert.assertEquals(EventConstants.NODE_UP_EVENT_UEI, eventResponse.getUei());

        EventUpdateList eventUpdateList = org.opennms.plugin.grpc.proto.services.EventUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .addEvent(org.opennms.plugin.grpc.proto.services.Event.newBuilder()
                        .setId(eventResponse.getId())
                        .setDescription(eventResponse.getDescription())
                        .setLabel(eventResponse.getNodeLabel())
                        .setUei(eventResponse.getUei())
                        .build())
                .build();

        this.sendEventUpdateIT(responseFuture, eventUpdateList);
        assertTrue("The response was not completed successfully for event valid input.", responseFuture.join());

    }

    public void startIT() throws InterruptedException, ExecutionException {
        client.startInProcessChannel();

        int maxWaitTime = 60;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            Future<Boolean> future = scheduler.submit(() -> {
                int waitTime = 0;
                while (waitTime < maxWaitTime) {
                    if (client.getChannelState() == ConnectivityState.READY) {
                        return true;
                    }
                    waitTime++;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for channel readiness.", e);
                    }
                }
                return false;
            });

            // Wait for the task to complete or time out
            boolean isReady = future.get(maxWaitTime, TimeUnit.SECONDS);

            // Check the result and throw exception if not ready
            if (!isReady) {
                throw new IllegalStateException("GRPC channel failed to connect within the expected time.");
            }

        } catch (TimeoutException e) {
            throw new IllegalStateException("GRPC channel failed to connect within the expected time.", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private OnmsAlarm nodeUpAlarmWithRelatedAlarm() {
        OnmsAlarm alarm = nodeUpAlarm();
        OnmsAlarm relatedAlarm = nodeUpAlarm();
        relatedAlarm.setId(_id++);
        alarm.addRelatedAlarm(relatedAlarm);
        return alarm;
    }

    private OnmsAlarm nodeUpAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(_id++);
        alarm.setUei(EventConstants.NODE_UP_EVENT_UEI);
        alarm.setNode(databasePopulator.getNode1());
        alarm.setCounter(1);
        alarm.setDescription("node up");
        alarm.setAlarmType(1);
        alarm.setLogMsg("node up");
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setReductionKey(String.format("%s:%d", EventConstants.NODE_UP_EVENT_UEI,
                databasePopulator.getNode1().getId()));
        return alarm;
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        this.mockDatabase = database;
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.shutdown();
        }
        eventdIpcMgr.reset();
        alarmDao.findAll().forEach(alarm -> alarmDao.delete(alarm));
        databasePopulator.resetDatabase();
        client.stop();
    }

    private OnmsAlarm nodeDownAlarmWithRelatedAlarm() {
        OnmsAlarm alarm = nodeDownAlarm();
        OnmsAlarm relatedAlarm = nodeDownAlarm();
        relatedAlarm.setId(_id++);
        alarm.addRelatedAlarm(relatedAlarm);
        return alarm;
    }

    private OnmsAlarm nodeDownAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(_id++);
        alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
        alarm.setNode(databasePopulator.getNode1());
        alarm.setCounter(1);
        alarm.setDescription("node down");
        alarm.setAlarmType(1);
        alarm.setLogMsg("node down");
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setReductionKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI,
                databasePopulator.getNode1().getId()));
        return alarm;
    }

    static OnmsHwEntity getHwEntityChassis(OnmsNode node) {
        final OnmsHwEntity chassis = new OnmsHwEntity();
        chassis.setNode(node);
        chassis.setEntPhysicalIndex(40);
        chassis.setEntPhysicalClass("chassis");
        chassis.setEntPhysicalDescr("ME-3400EG-2CS-A");
        chassis.setEntPhysicalFirmwareRev("12.2(60)EZ1");
        chassis.setEntPhysicalHardwareRev("V03");
        chassis.setEntPhysicalIsFRU(false);
        chassis.setEntPhysicalModelName("ME-3400EG-2CS-A");
        chassis.setEntPhysicalName("1");
        chassis.setEntPhysicalSerialNum("FOC1701V24Y");
        chassis.setEntPhysicalSoftwareRev("12.2(60)EZ1");
        chassis.setEntPhysicalVendorType(".1.3.6.1.4.1.9.12.3.1.3.760");
        return chassis;
    }

    static OnmsHwEntity getHwEntityPowerSupply(OnmsNode node) {
        final OnmsHwEntity powerSupply = new OnmsHwEntity();
        powerSupply.setNode(node);
        powerSupply.setEntPhysicalIndex(39);
        powerSupply.setEntPhysicalClass("powerSupply");
        powerSupply.setEntPhysicalDescr("ME-3400EG-2CS-A - Fan 0");
        powerSupply.setEntPhysicalIsFRU(false);
        powerSupply.setEntPhysicalModelName("ME-3400EG-2CS-A - Fan 0");
        powerSupply.setEntPhysicalVendorType(".1.3.6.1.4.1.9.12.3.1.7.81");
        return powerSupply;
    }

    static OnmsHwEntity getHwEntityModule(OnmsNode node) {
        final OnmsHwEntity module = new OnmsHwEntity();
        module.setNode(node);
        module.setEntPhysicalIndex(37);
        module.setEntPhysicalClass("module");
        module.setEntPhysicalDescr("ME-3400EG-2CS-A - Power Supply 0");
        module.setEntPhysicalIsFRU(false);
        module.setEntPhysicalModelName("ME-3400EG-2CS-A - Power Supply 0");
        module.setEntPhysicalSerialNum("LIT16490HHP");
        module.setEntPhysicalVendorType(".1.3.6.1.4.1.9.12.3.1.6.223");
        return module;
    }

    static OnmsHwEntity getHwEntityContainer(OnmsNode node) {
        OnmsHwEntity container = new OnmsHwEntity();
        container.setNode(node);
        container.setEntPhysicalIndex(36);
        container.setEntPhysicalClass("container");
        container.setEntPhysicalDescr("GigabitEthernet Container");
        container.setEntPhysicalIsFRU(false);
        container.setEntPhysicalName("GigabitEthernet0/4 Container");
        container.setEntPhysicalVendorType(".1.3.6.1.4.1.9.12.3.1.5.115");
        return container;
    }

    static OnmsHwEntity getHwEntityPort(OnmsNode node) {
        final OnmsHwEntity port = new OnmsHwEntity();
        port.setNode(node);
        port.setEntPhysicalIndex(35);
        port.setEntPhysicalAlias("10104");
        port.setEntPhysicalClass("port");
        port.setEntPhysicalDescr("1000BaseBX10-U SFP");
        port.setEntPhysicalHardwareRev("V01");
        port.setEntPhysicalIsFRU(true);
        port.setEntPhysicalModelName("GLC-BX-U");
        port.setEntPhysicalName("GigabitEthernet0/4");
        port.setEntPhysicalSerialNum("L03C2AC0179");
        port.setEntPhysicalVendorType(".1.3.6.1.4.1.9.12.3.1.10.253");
        OnmsHwEntityAlias onmsHwEntityAlias = new OnmsHwEntityAlias(0, ".1.3.6.1.2.1.2.2.1.1.10104");
        onmsHwEntityAlias.setHwEntity(port);
        port.setEntAliases(new TreeSet<>(Arrays.asList(onmsHwEntityAlias)));
        return port;
    }


    public class NmsInventoryServiceSyncImpl extends org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncImplBase {
        @Override
        public StreamObserver<NmsInventoryUpdateList> inventoryUpdate(final StreamObserver<Empty> responseObserver) {
            return new StreamObserver<NmsInventoryUpdateList>() {
                @Override
                public void onNext(NmsInventoryUpdateList value) {
                    // Process the received inventory update
                    System.out.println("Received inventory update: " + value);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error received from client: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    System.out.println("Inventory update stream completed.");
                    responseObserver.onNext(Empty.getDefaultInstance()); // Send acknowledgment
                    responseObserver.onCompleted(); // Close the stream
                }
            };
        }

        @Override
        public StreamObserver<AlarmUpdateList> alarmUpdate(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<AlarmUpdateList>() {
                @Override
                public void onNext(AlarmUpdateList alarmUpdateList) {
                    // Process the received alarm update
                    System.out.println("Received alarm update: " + alarmUpdateList);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error received from client: " + throwable.getMessage());
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    System.out.println("alarm update stream completed.");
                    responseObserver.onNext(Empty.getDefaultInstance()); // Send acknowledgment
                    responseObserver.onCompleted(); // Close the stream
                }
            };
        }

        @Override
        public StreamObserver<EventUpdateList> eventUpdate(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<EventUpdateList>() {
                @Override
                public void onNext(EventUpdateList eventUpdateList) {
                    // Process the received event update
                    System.out.println("Received event update: " + eventUpdateList);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error received from client: " + throwable.getMessage());
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    System.out.println("event update stream completed.");
                    responseObserver.onNext(Empty.getDefaultInstance()); // Send acknowledgment
                    responseObserver.onCompleted(); // Close the stream
                }
            };
        }
    }

    private synchronized void sendInventoryUpdateIT(CompletableFuture<Boolean> responseFuture, NmsInventoryUpdateList inventoryUpdateList) {
        if (client.getChannelState().equals(ConnectivityState.READY)) {
            org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncStub nmsSyncStub = org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.newStub(client.getChannel());
            // Create a StreamObserver for sending inventory updates
            StreamObserver<NmsInventoryUpdateList> requestObserver = nmsSyncStub.inventoryUpdate(new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {
                    // Acknowledgment from server
                    System.out.println("Received acknowledgment from server.");
                    responseFuture.complete(true); // Mark future as complete
                }

                @Override
                public void onError(Throwable t) {
                    // Complete exceptionally if there's an error
                    if (t instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) t;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            responseFuture.complete(false);
                        }
                    } else {
                        responseFuture.completeExceptionally(t);
                    }
                    System.err.println("Error from server: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // Completion of the stream
                    System.out.println("Inventory update stream completed.");
                }
            });

            // Send the inventory update to the server
            requestObserver.onNext(inventoryUpdateList);

            // Complete the stream (this will trigger the acknowledgment)
            requestObserver.onCompleted();
        } else {
            System.out.println("Channel is not ready for communication.");
        }
    }

    private synchronized void sendAlarmUpdateIT(CompletableFuture<Boolean> responseFuture, org.opennms.plugin.grpc.proto.services.AlarmUpdateList alarmUpdateList) {

        if (client.getChannelState().equals(ConnectivityState.READY)) {
            org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncStub nmsSyncStub = org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.newStub(client.getChannel());
            // Create a StreamObserver for sending alarm updates
            StreamObserver<AlarmUpdateList> requestObserver = nmsSyncStub.alarmUpdate(new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {
                    // Acknowledgment from server
                    System.out.println("Received acknowledgment from server.");
                    responseFuture.complete(true); // Mark future as complete
                }

                @Override
                public void onError(Throwable t) {
                    // Complete exceptionally if there's an error
                    if (t instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) t;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            responseFuture.complete(false);
                        }
                    } else {
                        responseFuture.completeExceptionally(t);
                    }
                    System.err.println("Error from server: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // Completion of the stream
                    System.out.println("Alarm update stream completed.");
                }
            });

            // Send the alarm update to the server
            requestObserver.onNext(alarmUpdateList);

            // Complete the stream (this will trigger the acknowledgment)
            requestObserver.onCompleted();
        } else {
            System.out.println("Channel is not ready for communication.");
        }

    }

    private synchronized void sendEventUpdateIT(CompletableFuture<Boolean> responseFuture, org.opennms.plugin.grpc.proto.services.EventUpdateList eventUpdateList) {

        if (client.getChannelState().equals(ConnectivityState.READY)) {
            org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncStub nmsSyncStub = org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.newStub(client.getChannel());
            // Create a StreamObserver for sending event updates
            StreamObserver<EventUpdateList> requestObserver = nmsSyncStub.eventUpdate(new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {
                    // Acknowledgment from server
                    System.out.println("Received acknowledgment from server.");
                    responseFuture.complete(true); // Mark future as complete
                }

                @Override
                public void onError(Throwable t) {
                    // Complete exceptionally if there's an error
                    if (t instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) t;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            responseFuture.complete(false);
                        }
                    } else {
                        responseFuture.completeExceptionally(t);
                    }
                    System.err.println("Error from server: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // Completion of the stream
                    System.out.println("Event update stream completed.");
                }
            });

            // Send the event update to the server
            requestObserver.onNext(eventUpdateList);

            // Complete the stream (this will trigger the acknowledgment)
            requestObserver.onCompleted();
        } else {
            System.out.println("Channel is not ready for communication.");
        }
    }

}