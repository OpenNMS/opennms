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
import org.opennms.features.grpc.exporter.spog.AlarmExporter;
import org.opennms.features.grpc.exporter.spog.EventsExporter;
import org.opennms.features.grpc.exporter.spog.InventoryExporter;
import org.opennms.features.grpc.exporter.spog.SpogGrpcClient;
import org.opennms.features.grpc.exporter.spog.SpogInventoryService;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.plugin.grpc.proto.spog.AlarmUpdateList;
import org.opennms.plugin.grpc.proto.spog.EventUpdateList;
import org.opennms.plugin.grpc.proto.spog.NmsInventoryServiceSyncGrpc;
import org.opennms.plugin.grpc.proto.spog.NmsInventoryUpdateList;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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
public class SpogInventoryServiceSyncIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final Logger LOG = LoggerFactory.getLogger(SpogInventoryServiceSyncIT.class);
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
    private MockEventIpcManager eventdIpcMgr;

    private int _id = 1;
    private long nodeId = 0;
    private MockDatabase mockDatabase;

    private NmsInventoryServiceSyncImpl grpcServerService = null;
    private CompletableFuture<NmsInventoryUpdateList> inventoryResponseFuture = null;
    private CompletableFuture<AlarmUpdateList> alarmResponseFuture = null;
    private CompletableFuture<EventUpdateList> eventResponseFuture = null;

    private AlarmExporter alarmExporter;
    private EventsExporter eventsExporter;
    private InventoryExporter inventoryExporter;
    private RuntimeInfo runtimeInfo;
    private EventSubscriptionService eventSubscriptionService;
    private SpogInventoryService spogInventoryService;


    @Before
    public void setUp() throws Exception {
        eventdIpcMgr.setEventWriter(mockDatabase);
        // create data for node
        populateData();
        inventoryResponseFuture = new CompletableFuture<>();
        alarmResponseFuture = new CompletableFuture<>();
        eventResponseFuture = new CompletableFuture<>();
        grpcServerService = new NmsInventoryServiceSyncImpl(inventoryResponseFuture, alarmResponseFuture, eventResponseFuture);
        // configure server and client
        initializeAndStartServer();
        initializeAndStartClient();

        runtimeInfo = mock(RuntimeInfo.class);
        this.eventSubscriptionService = mock(EventSubscriptionService.class);
    }

    private void populateData() {
        this.databasePopulator.populateDatabase();
    }

    private void initializeAndStartServer() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(grpcServerService)
                .build()
                .start();
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
        // Step 1: Set up the initial inventory update
        spogInventoryService = new SpogInventoryService(nodeDao, runtimeInfo, client, new MockSessionUtils(), 0, true);
        inventoryExporter = new InventoryExporter(eventSubscriptionService, nodeDao, spogInventoryService);

        final var nodeEvent = MockEventUtil.createNodeUpEventBuilder("test", databasePopulator.getNode1()).getEvent();
        eventdIpcMgr.sendNow(nodeEvent);
        this.nodeId = nodeEvent.getNodeid();
        AtomicReference<Boolean> inventoryFlag = new AtomicReference<>(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        inventoryExporter.onEvent(ImmutableMapper.fromMutableEvent(nodeEvent));
                        inventoryFlag.set(inventoryResponseFuture.get(10, TimeUnit.SECONDS).getNodesCount() > 0);
                        return inventoryFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        assertTrue("The initial response was not completed successfully.", inventoryFlag.get());
        validateInventoryReceivedData();
        // Step 2: Stop the server to simulate a restart scenario
        if (server != null) {
            server.shutdownNow();
            // Wait for the server to fully shut down
            assertTrue("Server failed to shut down properly", server.awaitTermination(30, TimeUnit.SECONDS));
        }
        // Step 3: Restart the server (client remains the same, no restart for client)
        initializeAndStartServer();
        // Step 4: reset and await
        inventoryFlag.set(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        inventoryExporter.onEvent(ImmutableMapper.fromMutableEvent(nodeEvent));
                        inventoryFlag.set(inventoryResponseFuture.get(10, TimeUnit.SECONDS).getNodesCount() > 0);
                        return inventoryFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        // Step 5: Assert that the client was able to reconnect and send the update after server restart
        assertTrue("The response after server recovery was not completed successfully.", inventoryFlag.get());
        validateInventoryReceivedData();
    }

    @Test
    public void testRecoveryAfterClientAndServerRestart() throws Exception {
        // Step 1: Set up the initial inventory update
        spogInventoryService = new SpogInventoryService(nodeDao, runtimeInfo, client, new MockSessionUtils(), 0, true);
        inventoryExporter = new InventoryExporter(eventSubscriptionService, nodeDao, spogInventoryService);

        final var nodeEvent = MockEventUtil.createNodeUpEventBuilder("test", databasePopulator.getNode2()).getEvent();
        eventdIpcMgr.sendNow(nodeEvent);
        this.nodeId = nodeEvent.getNodeid();

        AtomicReference<Boolean> inventoryFlag = new AtomicReference<>(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        inventoryExporter.onEvent(ImmutableMapper.fromMutableEvent(nodeEvent));
                        inventoryFlag.set(inventoryResponseFuture.get(10, TimeUnit.SECONDS).getNodesCount() > 0);
                        return inventoryFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        assertTrue("The initial response was not completed successfully.", inventoryFlag.get());
        validateInventoryReceivedData();

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
        // Step 4: reset and await
        inventoryFlag.set(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        inventoryExporter.onEvent(ImmutableMapper.fromMutableEvent(nodeEvent));
                        inventoryFlag.set(inventoryResponseFuture.get(10, TimeUnit.SECONDS).getNodesCount() > 0);
                        return inventoryFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        // Step 5: Assert that the client was able to reconnect and send the update after server restart
        assertTrue("The response after server recovery was not completed successfully.", inventoryFlag.get());
        validateInventoryReceivedData();

    }

    @Test
    public void testInventoryDataToBeSent() throws Exception {
        spogInventoryService = new SpogInventoryService(nodeDao, runtimeInfo, client, new MockSessionUtils(), 0, true);
        inventoryExporter = new InventoryExporter(eventSubscriptionService, nodeDao, spogInventoryService);

        final var nodeEvent = MockEventUtil.createNodeUpEventBuilder("testInventoryDataToBeSent", databasePopulator.getNode3()).getEvent();
        eventdIpcMgr.sendNow(nodeEvent);
        this.nodeId = nodeEvent.getNodeid();
        AtomicReference<Boolean> inventoryFlag = new AtomicReference<>(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        inventoryExporter.onEvent(ImmutableMapper.fromMutableEvent(nodeEvent));
                        inventoryFlag.set(inventoryResponseFuture.get(10, TimeUnit.SECONDS).getNodesCount() > 0);
                        return inventoryFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        assertTrue("The response was not completed successfully for inventory valid input.", inventoryFlag.get());
        validateInventoryReceivedData();
    }

    @Test
    public void testInventoryDataWithoutEventToBeSent() throws Exception {
        spogInventoryService = new SpogInventoryService(nodeDao, runtimeInfo, client, new MockSessionUtils(), 10, true);
        spogInventoryService.start();
        var node = nodeDao.findAll().stream().findAny()
                .orElseThrow(() -> new NoSuchElementException("No node found"));
        this.nodeId = node.getId();
        AtomicReference<Boolean> inventoryFlag = new AtomicReference<>(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        inventoryFlag.set(inventoryResponseFuture.get(10, TimeUnit.SECONDS).getNodesCount() > 0);
                        return inventoryFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        spogInventoryService.stop();
                        return false;
                    }
                });
        spogInventoryService.stop();
        assertTrue("The response was not completed successfully for inventory valid input.", inventoryFlag.get());
        validateInventoryReceivedData();
    }

    public void validateInventoryReceivedData() throws Exception {
        final var nodeData = inventoryResponseFuture.get().getNodesList().stream().filter(obj -> obj.getId() == nodeId).findAny()
                .orElseThrow(() -> new NoSuchElementException("No node found"));
        Assert.assertNotNull(nodeData);
        Assert.assertEquals(nodeId, nodeData.getId());
        Assert.assertTrue("Node id should be exist", nodeData.getId() > 0);
        Assert.assertNotNull("ForeignSource should not be null", nodeData.getForeignSource());
        Assert.assertNotNull("ForeignId should not be null", nodeData.getForeignId());
        Assert.assertNotNull("Location should not be null", nodeData.getLocation());
        Assert.assertNotNull("Node Label should not be null", nodeData.getLabel());
    }

    @Test
    public void testAlarmDataToBeSent() throws Exception {
        alarmExporter = new AlarmExporter(runtimeInfo, client, true);
        eventdIpcMgr.sendNow(MockEventUtil.createNodeDownEventBuilder("test", databasePopulator.getNode1()).getEvent());
        final OnmsAlarm alarm = nodeDownAlarmWithRelatedAlarm();
        alarmDao.save(alarm);
        AtomicReference<Boolean> alarmFlag = new AtomicReference<>(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        alarmExporter.handleAlarmSnapshot(alarmDao.findAll());
                        alarmFlag.set(alarmResponseFuture.get(10, TimeUnit.SECONDS).getAlarmsCount() > 0);
                        return alarmFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        assertTrue("The response was not completed successfully for alarm valid input.", alarmFlag.get());
        Assert.assertEquals(1, alarmResponseFuture.get().getAlarmsCount());
        final var alarmData = alarmResponseFuture.get().getAlarmsList().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No alarm found"));
        Assert.assertNotNull(alarmData);
        Assert.assertTrue("Alarm id should be exist", alarmData.getId() > 0);
        Assert.assertTrue("Node id in alarm should be exist", alarmData.getNodeCriteria().getId() > 0);
        Assert.assertEquals(EventConstants.NODE_DOWN_EVENT_UEI, alarmData.getUei());
    }

    @Test
    public void testEventDataToBeSent() throws Exception {
        eventsExporter = new EventsExporter(eventSubscriptionService, runtimeInfo, client, true);
        final var event = MockEventUtil.createNodeUpEventBuilder("test", databasePopulator.getNode1()).getEvent();
        eventdIpcMgr.sendNow(event);
        final OnmsAlarm alarm = nodeUpAlarmWithRelatedAlarm();
        alarmDao.save(alarm);
        AtomicReference<Boolean> eventFlag = new AtomicReference<>(false);
        await().pollInterval(2000, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        eventsExporter.onEvent(ImmutableMapper.fromMutableEvent(event));
                        eventFlag.set(eventResponseFuture.get(10, TimeUnit.SECONDS).getEventCount() > 0);
                        return eventFlag.get();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                });
        assertTrue("The response was not completed successfully for event valid input.", eventFlag.get());
        Assert.assertEquals(1, eventResponseFuture.get().getEventCount());
        final var eventData = eventResponseFuture.get().getEventList().stream().findAny()
                .orElseThrow(() -> new NoSuchElementException("No event found"));
        Assert.assertNotNull(eventData);
        Assert.assertTrue("Event id should be exist", eventData.getId() > 0);
        Assert.assertTrue("Node id in event should be exist", eventData.getNodeId() > 0);
        Assert.assertEquals(EventConstants.NODE_UP_EVENT_UEI, eventData.getUei());

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
        this.nodeId = 0;
        alarmDao.findAll().forEach(alarm -> alarmDao.delete(alarm));
        databasePopulator.resetDatabase();
        client.stop();
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        this.mockDatabase = database;
    }

    public class NmsInventoryServiceSyncImpl extends NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncImplBase {

        private final CompletableFuture<NmsInventoryUpdateList> inventoryResponseFuture;
        private final CompletableFuture<AlarmUpdateList> alarmResponseFuture;
        private final CompletableFuture<EventUpdateList> eventResponseFuture;

        public NmsInventoryServiceSyncImpl(CompletableFuture<NmsInventoryUpdateList> inventoryResponseFuture, CompletableFuture<AlarmUpdateList> alarmResponseFuture, CompletableFuture<EventUpdateList> eventResponseFuture) {
            this.inventoryResponseFuture = inventoryResponseFuture;
            this.alarmResponseFuture = alarmResponseFuture;
            this.eventResponseFuture = eventResponseFuture;
        }

        @Override
        public StreamObserver<NmsInventoryUpdateList> inventoryUpdate(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<NmsInventoryUpdateList>() {
                @Override
                public void onNext(NmsInventoryUpdateList value) {
                    // Process the received inventory update
                    inventoryResponseFuture.complete(value);
                }

                @Override
                public void onError(Throwable throwable) {
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) throwable;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            eventResponseFuture.completeExceptionally(new IllegalArgumentException("Invalid inventory input", exception));
                        }
                    } else {
                        inventoryResponseFuture.completeExceptionally(throwable);
                    }
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                    inventoryResponseFuture.complete(null);
                }
            };
        }

        @Override
        public StreamObserver<AlarmUpdateList> alarmUpdate(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<AlarmUpdateList>() {
                @Override
                public void onNext(AlarmUpdateList alarmUpdateList) {
                    // Process the received alarm update
                    alarmResponseFuture.complete(alarmUpdateList);
                }

                @Override
                public void onError(Throwable throwable) {
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) throwable;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            eventResponseFuture.completeExceptionally(new IllegalArgumentException("Invalid alarm input", exception));
                        }
                    } else {
                        alarmResponseFuture.completeExceptionally(throwable);
                    }
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                    alarmResponseFuture.complete(null);
                }
            };
        }

        @Override
        public StreamObserver<EventUpdateList> eventUpdate(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<EventUpdateList>() {
                @Override
                public void onNext(EventUpdateList eventUpdateList) {
                    // Process the received event update
                    eventResponseFuture.complete(eventUpdateList);
                }

                @Override
                public void onError(Throwable throwable) {
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException exception = (StatusRuntimeException) throwable;
                        if (exception.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                            eventResponseFuture.completeExceptionally(new IllegalArgumentException("Invalid event input", exception));
                        }
                    } else {
                        eventResponseFuture.completeExceptionally(throwable);
                    }
                }

                @Override
                public void onCompleted() {
                    // Send acknowledgment once the stream is completed
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                    eventResponseFuture.complete(null);
                }
            };
        }
    }
}