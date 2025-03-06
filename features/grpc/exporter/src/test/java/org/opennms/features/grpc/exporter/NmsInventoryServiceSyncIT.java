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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@RunWith(OpenNMSJUnit4ClassRunner.class)
public class NmsInventoryServiceSyncIT {

    private static final int PORT = 8080;

    private Server server;
    private org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncImplBase serviceImpl;
    private org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncStub asyncStub;
    private ManagedChannel channel;

    private static final Logger LOG = LoggerFactory.getLogger(NmsInventoryServiceSyncIT.class);

    @Before
    public void setUp() throws Exception {

        serviceImpl = mock(org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.NmsInventoryServiceSyncImplBase.class);
        server = ServerBuilder.forPort(PORT)
                .addService(serviceImpl)
                .build()
                .start();
        channel = ManagedChannelBuilder.forAddress("localhost", PORT)
                .usePlaintext()
                .build();

        asyncStub = org.opennms.plugin.grpc.proto.services.NmsInventoryServiceSyncGrpc.newStub(channel);
    }

    @Test
    public void testInventoryUpdate() {

        org.opennms.plugin.grpc.proto.services.NmsInventoryUpdateList request = org.opennms.plugin.grpc.proto.services.NmsInventoryUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addNodes(org.opennms.plugin.grpc.proto.services.Node.newBuilder()
                        .setId(12345)
                        .setForeignSource("source")
                        .setForeignId("foreignId")
                        .setLocation("Test Location")
                        .setLabel("Test Node")
                        .setCreateTime(System.currentTimeMillis())
                        .build())
                .build();

        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            StreamObserver<Empty> responseObserver = invocation.getArgument(0);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            latch.countDown();
            return null;
        }).when(serviceImpl).inventoryUpdate(any());

        StreamObserver<org.opennms.plugin.grpc.proto.services.NmsInventoryUpdateList> requestObserver = asyncStub.inventoryUpdate(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                System.out.println("Received response");
            }

            @Override
            public void onError(Throwable t) {
                LOG.error(t.getMessage());
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed");
            }
        });

        requestObserver.onNext(request);
        requestObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Thread was interrupted during latch wait", e);
            throw new RuntimeException(e);
        }

        verify(serviceImpl, times(1)).inventoryUpdate(any());
    }

      @Test
    public void testAlarmUpdate() {

        org.opennms.plugin.grpc.proto.services.AlarmUpdateList request = org.opennms.plugin.grpc.proto.services.AlarmUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addAlarms(org.opennms.plugin.grpc.proto.services.Alarm.newBuilder()
                        .setId(1001)
                        .setUei("uei_test")
                        .setSeverity(org.opennms.plugin.grpc.proto.services.Severity.WARNING.getNumber())
                        .setDescription("Test Alarm")
                        .build())
                .build();

          CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            StreamObserver<Empty> responseObserver = invocation.getArgument(0);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            latch.countDown();
            return null;
        }).when(serviceImpl).alarmUpdate(any());

        StreamObserver<org.opennms.plugin.grpc.proto.services.AlarmUpdateList> responseObserver = asyncStub.alarmUpdate(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty empty) {
                System.out.println("Received response");
            }

            @Override
            public void onError(Throwable t) {
                LOG.error(t.getMessage());
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed");
            }
        });

          responseObserver.onNext(request);
          responseObserver.onCompleted();

          try {
              latch.await();
          } catch (InterruptedException e) {
              LOG.error("Thread was interrupted during latch wait", e);
              throw new RuntimeException(e);
          }

        verify(serviceImpl, times(1)).alarmUpdate(any());
    }

    @Test
    public void testEventUpdate() {
        org.opennms.plugin.grpc.proto.services.EventUpdateList request = org.opennms.plugin.grpc.proto.services.EventUpdateList.newBuilder()
                .setInstanceId("instance_1")
                .setInstanceName("TestInstance")
                .setSnapshot(true)
                .addEvent(org.opennms.plugin.grpc.proto.services.Event.newBuilder()
                        .setId(12345)
                        .setUei("uei_test_event")
                        .setTime(System.currentTimeMillis())
                        .setSource("TestSource")
                        .addParameter(org.opennms.plugin.grpc.proto.services.EventParameter.newBuilder()
                                .setName("param1")
                                .setValue("value1")
                                .setType("String")
                                .build())
                        .setDescription("Test Event")
                        .setLogMessage("Test Event Log Message")
                        .setSeverity(org.opennms.plugin.grpc.proto.services.Severity.NORMAL)
                        .setIpAddress("192.168.1.1")
                        .setDistPoller("TestPoller")
                        .setNodeId(12345)
                        .setLabel("Test Event Label")
                        .build())
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            StreamObserver<Empty> responseObserver = invocation.getArgument(0);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            latch.countDown();
            return null;
        }).when(serviceImpl).eventUpdate(any());

        StreamObserver<org.opennms.plugin.grpc.proto.services.EventUpdateList> requestObserver = asyncStub.eventUpdate(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                System.out.println("Received response");
            }

            @Override
            public void onError(Throwable t) {
                LOG.error(t.getMessage());
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed");
            }
        });

        requestObserver.onNext(request);
        requestObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Thread was interrupted during latch wait", e);
            throw new RuntimeException(e);
        }
        verify(serviceImpl, times(1)).eventUpdate(any());
    }


    @After
    public void tearDown() {
        if (server != null) {
            server.shutdown();
        }
        if (channel != null) {
            channel.shutdownNow();
        }
    }
}

