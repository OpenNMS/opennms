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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.grpc.exporter.spog.SpogGrpcClient;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.plugin.grpc.proto.spog.AlarmUpdateList;
import org.opennms.plugin.grpc.proto.spog.EventUpdateList;
import org.opennms.plugin.grpc.proto.spog.NmsInventoryServiceSyncGrpc;
import org.opennms.plugin.grpc.proto.spog.NmsInventoryUpdateList;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
public class NmsInventoryServiceSyncIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final int PORT = 50051;
    private static final String TENANT_ID = "opennms-prime";
    private static final String HOST_NAME = "localhost:" + PORT;

    private Server server;
    private SpogGrpcClient client;

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

    private NmsInventoryServiceSyncImpl grpcServerService = null;
    private CompletableFuture<Boolean> serverResponseFuture = null;

    @Before
    public void setUp() throws Exception {
        eventdIpcMgr.setEventWriter(mockDatabase);
        // create data for node
        populateData();
        serverResponseFuture = new CompletableFuture<>();
        grpcServerService = new NmsInventoryServiceSyncImpl(serverResponseFuture);
        // configure server and client
        initializeAndStartServer();
        initializeAndStartClient();
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

    private void initializeAndStartServer() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(grpcServerService)
                .build()
                .start(); // Restart
    }

    private void initializeAndStartClient() throws SSLException {
        client = new SpogGrpcClient(HOST_NAME,
                null,
                false,
                new GrpcHeaderInterceptor(TENANT_ID)
        );
        client.start();
    }

    @Test
    public void testClientRecoveryAfterServerStopAndRestart() throws Exception {
        // Step 1: Wait for the channel to be ready and set up the initial inventory update
        waitForChannelToBeReady();
        final var node = nodeDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No node found"));

        final NmsInventoryUpdateList InventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.spog.Node.newBuilder()
                        .setId(node.getId())
                        .setForeignSource(node.getForeignSource())
                        .setForeignId(node.getForeignId())
                        .setLocation(node.getLocation().getLocationName())
                        .setLabel(node.getLabel())
                        .build())
                .build();
        // Ensure that the NMS Inventory Update Stream is ready
        if (client.getNmsInventoryUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getNmsInventoryUpdateStream() != null);
        }
        // Send the initial inventory update
        client.sendNmsInventoryUpdate(InventoryUpdateList);
        assertTrue("The initial response was not completed successfully.", serverResponseFuture.join());
        // Step 2: Stop the server to simulate a restart scenario
        if (server != null) {
            server.shutdownNow();
            // Wait for the server to fully shut down
            assertTrue("Server failed to shut down properly", server.awaitTermination(30, TimeUnit.SECONDS));
        }
        // Step 3: Restart the server (client remains the same, no restart for client)
        initializeAndStartServer();
        // Step 4: Wait for the client to automatically reconnect
        waitForChannelToBeReady();
        // Step 5: Send a recovery inventory update after server restart
        final NmsInventoryUpdateList recoveryInventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(false)
                .addNodes(org.opennms.plugin.grpc.proto.spog.Node.newBuilder()
                        .setId(node.getId())
                        .setForeignSource(node.getForeignSource())
                        .setForeignId(node.getForeignId())
                        .setLocation(node.getLocation().getLocationName())
                        .setLabel(node.getLabel())
                        .build())
                .build();
        // Ensure that the stream is available again after server recovery
        if (client.getNmsInventoryUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getNmsInventoryUpdateStream() != null);
        }
        // Send the recovery inventory update after server recovery
        client.sendNmsInventoryUpdate(recoveryInventoryUpdateList);
        assertTrue("The response after server recovery was not completed successfully.", serverResponseFuture.join());
        // Step 6: Assert that the client was able to reconnect and send the update after server restart
        System.out.println("Client successfully recovered and sent inventory update after server restart.");
    }

    @Test
    public void testRecoveryAfterClientAndServerRestart() throws Exception {
        // Step 1: Wait for the channel to be ready and set up the initial inventory update
        waitForChannelToBeReady();
        final var node = nodeDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No node found"));

        final NmsInventoryUpdateList initialInventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.spog.Node.newBuilder()
                        .setId(node.getId())
                        .setForeignSource(node.getForeignSource())
                        .setForeignId(node.getForeignId())
                        .setLocation(node.getLocation().getLocationName())
                        .setLabel(node.getLabel())
                        .build())
                .build();

        if (client.getNmsInventoryUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getNmsInventoryUpdateStream() != null);
        }

        client.sendNmsInventoryUpdate(initialInventoryUpdateList);

        assertTrue("The initial response was not completed successfully.", serverResponseFuture.join());

        // Step 2: Stop the server and client to simulate a restart
        if (server != null) {
            client.stop();
            server.shutdown();
            // Wait for the server to fully shutdown
            assertTrue("Server failed to shut down properly", server.awaitTermination(30, TimeUnit.SECONDS));
        }

        // Step 3: Restart the server and client
        initializeAndStartServer();
        initializeAndStartClient();

        // Step 4: Send another inventory update after server recovery
        waitForChannelToBeReady();

        final NmsInventoryUpdateList recoveryInventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(false)
                .addNodes(org.opennms.plugin.grpc.proto.spog.Node.newBuilder()
                        .setId(node.getId())
                        .setForeignSource(node.getForeignSource())
                        .setForeignId(node.getForeignId())
                        .setLocation(node.getLocation().getLocationName())
                        .setLabel(node.getLabel())
                        .build())
                .build();

        if (client.getNmsInventoryUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getNmsInventoryUpdateStream() != null);
        }

        client.sendNmsInventoryUpdate(recoveryInventoryUpdateList);
        assertTrue("The response after server recovery was not completed successfully.", serverResponseFuture.join());
        // Step 5: Assert that the client was able to reconnect and send the update after server restart
        System.out.println("Client successfully recovered and sent inventory update after server restart.");
    }

    private void waitForChannelToBeReady() {
        if (!(client.getChannelState().equals(ConnectivityState.READY))) {
            await()
                    .atMost(30, TimeUnit.SECONDS)
                    .until(() -> client.getChannelState() == ConnectivityState.READY);
        }
    }

    @Test
    public void testInventoryDataToBeSent() {

        waitForChannelToBeReady();

        final var node = nodeDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No node found"));

        Assert.assertNotNull(node);
        Assert.assertTrue("Node id should be exist", node.getId() > 0);
        Assert.assertNotNull("ForeignSource should not be null", node.getForeignSource());
        Assert.assertNotNull("ForeignId should not be null", node.getForeignId());
        Assert.assertNotNull("Location should not be null", node.getLocation().getLocationName());
        Assert.assertNotNull("Node Label should not be null", node.getLabel());

        final NmsInventoryUpdateList inventoryUpdateList = NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.spog.Node.newBuilder()
                        .setId(node.getId())
                        .setForeignSource(node.getForeignSource())
                        .setForeignId(node.getForeignId())
                        .setLocation(node.getLocation().getLocationName())
                        .setLabel(node.getLabel())
                        .build())
                .build();


        if (client.getNmsInventoryUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getNmsInventoryUpdateStream() != null);
        }

        client.sendNmsInventoryUpdate(inventoryUpdateList);
        assertTrue("The response was not completed successfully for inventory valid input.", serverResponseFuture.join());
    }

    @Test
    public void testAlarmDataToBeSent() {

        waitForChannelToBeReady();
        eventdIpcMgr.sendNow(MockEventUtil.createNodeDownEventBuilder("test", databasePopulator.getNode1()).getEvent());
        final OnmsAlarm alarm = nodeDownAlarmWithRelatedAlarm();
        alarmDao.save(alarm);

        final var alarmResponse = eventDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No alarm found"));

        Assert.assertNotNull(alarmResponse);
        Assert.assertTrue("Alarm id should be exist", alarmResponse.getId() > 0);
        Assert.assertTrue("Node id in alarm should be exist", alarmResponse.getNodeId() > 0);
        Assert.assertEquals(EventConstants.NODE_DOWN_EVENT_UEI, alarmResponse.getUei());

        final AlarmUpdateList alarmUpdateList = AlarmUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addAlarms(org.opennms.plugin.grpc.proto.spog.Alarm.newBuilder()
                        .setId(alarmResponse.getId())
                        .setUei(alarmResponse.getUei())
                        .setDescription(alarmResponse.getDescription())
                        .setNodeCriteria(org.opennms.plugin.grpc.proto.spog.NodeCriteria.newBuilder()
                                .setId(alarmResponse.getNodeId())
                                .setForeignId(alarmResponse.getNode().getForeignId())
                                .setForeignSource(alarmResponse.getNode().getForeignSource())
                                .build()))
                .build();

        if (client.getAlarmsUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getAlarmsUpdateStream() != null);
        }

        client.sendAlarmUpdate(alarmUpdateList);
        assertTrue("The response was not completed successfully for alarm valid input.", serverResponseFuture.join());
    }

    @Test
    public void testEventDataToBeSent() {

        waitForChannelToBeReady();
        eventdIpcMgr.sendNow(MockEventUtil.createNodeUpEventBuilder("test", databasePopulator.getNode1()).getEvent());
        final OnmsAlarm alarm = nodeUpAlarmWithRelatedAlarm();
        alarmDao.save(alarm);

        final var eventResponse = eventDao.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No event found"));

        Assert.assertNotNull(eventResponse);
        Assert.assertTrue("Event id should be exist", eventResponse.getId() > 0);
        Assert.assertTrue("Node id in event should be exist", eventResponse.getNodeId() > 0);
        Assert.assertEquals(EventConstants.NODE_UP_EVENT_UEI, eventResponse.getUei());

        final EventUpdateList eventUpdateList = EventUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .addEvent(org.opennms.plugin.grpc.proto.spog.Event.newBuilder()
                        .setId(eventResponse.getId())
                        .setDescription(eventResponse.getDescription())
                        .setLabel(eventResponse.getNodeLabel())
                        .setUei(eventResponse.getUei())
                        .build())
                .build();

        if (client.getEventUpdateStream() == null) {
            await()
                    .atMost(60, TimeUnit.SECONDS)
                    .until(() -> client.getEventUpdateStream() != null);
        }

        client.sendEventUpdate(eventUpdateList);
        assertTrue("The response was not completed successfully for event valid input.", serverResponseFuture.join());

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


    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        this.mockDatabase = database;
    }

    public class NmsInventoryServiceSyncImpl extends NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncImplBase {
        private final CompletableFuture<Boolean> responseFuture;

        public NmsInventoryServiceSyncImpl(CompletableFuture<Boolean> responseFuture) {
            this.responseFuture = responseFuture;
        }

        @Override
        public StreamObserver<NmsInventoryUpdateList> inventoryUpdate(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<NmsInventoryUpdateList>() {
                @Override
                public void onNext(NmsInventoryUpdateList value) {
                    // Process the received inventory update
                    System.out.println("Received inventory update: " + value);
                    responseFuture.complete(true);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error received from client: " + throwable.getMessage());
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) throwable;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            responseFuture.complete(false);
                        }
                    } else {
                        responseFuture.completeExceptionally(throwable);
                    }
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    System.out.println("Inventory update stream completed.");
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                    responseFuture.complete(true);
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
                    responseFuture.complete(true);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error received from client: " + throwable.getMessage());
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) throwable;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            responseFuture.complete(false);
                        }
                    } else {
                        responseFuture.completeExceptionally(throwable);
                    }
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    System.out.println("alarm update stream completed.");
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                    responseFuture.complete(true);
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
                    responseFuture.complete(true);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error received from client: " + throwable.getMessage());
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) throwable;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            responseFuture.complete(false);
                        }
                    } else {
                        responseFuture.completeExceptionally(throwable);
                    }
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    System.out.println("event update stream completed.");
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                    responseFuture.complete(true);
                }
            };
        }
    }
}