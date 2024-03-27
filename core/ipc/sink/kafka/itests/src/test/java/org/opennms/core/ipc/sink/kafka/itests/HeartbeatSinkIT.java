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
package org.opennms.core.ipc.sink.kafka.itests;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.common.ThreadLockingMessageConsumer;
import org.opennms.core.ipc.sink.kafka.client.KafkaRemoteMessageDispatcherFactory;
import org.opennms.core.ipc.sink.kafka.itests.HeartbeatSinkPerfIT.HeartbeatGenerator;
import org.opennms.core.ipc.sink.kafka.itests.heartbeat.Heartbeat;
import org.opennms.core.ipc.sink.kafka.itests.heartbeat.HeartbeatModule;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-opennms-identity.xml"
})
@JUnitConfigurationEnvironment
public class HeartbeatSinkIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private MessageDispatcherFactory localMessageDispatcherFactory;

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    private KafkaRemoteMessageDispatcherFactory remoteMessageDispatcherFactory = new KafkaRemoteMessageDispatcherFactory();

    @Before
    public void setUp() throws Exception {
        Hashtable<String, Object> kafkaConfig = new Hashtable<String, Object>();
        kafkaConfig.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
        kafkaConfig.put("max.block.ms", 10000);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaSinkConstants.KAFKA_CONFIG_PID).getProperties())
            .thenReturn(kafkaConfig);
        remoteMessageDispatcherFactory.setConfigAdmin(configAdmin);
        remoteMessageDispatcherFactory.setTracerRegistry(new MockTracerRegistry());
        remoteMessageDispatcherFactory.setIdentity(new MinionIdentity() {
                    @Override
                    public String getId() {
                        return "0";
                    }
                    @Override
                    public String getLocation() {
                        return "some location";
                    }
                    @Override
                    public String getType() {
                        return SystemType.Minion.name();
                    }
                });
        remoteMessageDispatcherFactory.init();

        System.setProperty(String.format("%sbootstrap.servers", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                "earliest");
        consumerManager.afterPropertiesSet();
    }

    @Test(timeout=30000)
    public void canProduceAndConsumeMessages() throws Exception {
        HeartbeatModule module = new HeartbeatModule();

        AtomicInteger heartbeatCount = new AtomicInteger();
        final MessageConsumer<Heartbeat,Heartbeat> heartbeatConsumer = new MessageConsumer<Heartbeat,Heartbeat>() {
            @Override
            public SinkModule<Heartbeat,Heartbeat> getModule() {
                return module;
            }

            @Override
            public void handleMessage(final Heartbeat heartbeat) {
                heartbeatCount.incrementAndGet();
            }
        };

        try {
            consumerManager.registerConsumer(heartbeatConsumer);

            final SyncDispatcher<Heartbeat> localDispatcher = localMessageDispatcherFactory.createSyncDispatcher(module);
            localDispatcher.send(new Heartbeat());
            await().atMost(1, MINUTES).until(() -> heartbeatCount.get(), equalTo(1));

            final SyncDispatcher<Heartbeat> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

            dispatcher.send(new Heartbeat());
            await().atMost(1, MINUTES).until(() -> heartbeatCount.get(), equalTo(2));
        } finally {
            consumerManager.unregisterConsumer(heartbeatConsumer);
        }
    }

    @Test(timeout=60000)
    @org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
    public void canConsumeMessagesInParallel() throws Exception {
        final int NUM_CONSUMER_THREADS = 7;

        final HeartbeatModule parallelHeartbeatModule = new HeartbeatModule() {
            @Override
            public int getNumConsumerThreads() {
                return NUM_CONSUMER_THREADS;
            }
        };

        final ThreadLockingMessageConsumer<Heartbeat,Heartbeat> consumer = new ThreadLockingMessageConsumer<>(parallelHeartbeatModule);

        final CompletableFuture<Integer> future = consumer.waitForThreads(NUM_CONSUMER_THREADS);

        try {
            consumerManager.registerConsumer(consumer);

            final SyncDispatcher<Heartbeat> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

            final HeartbeatGenerator generator = new HeartbeatGenerator(dispatcher, 100.0);
            generator.start();

            // Wait until we have NUM_CONSUMER_THREADS locked
            future.get();

            // Take a snooze
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));

            // Verify that there aren't more than NUM_CONSUMER_THREADS waiting
            assertEquals(0, consumer.getNumExtraThreadsWaiting());

            generator.stop();
        } finally {
            consumerManager.unregisterConsumer(consumer);
        }
    }

    @Test(timeout = 60000)
    @org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
    public void testSinkMessagesBeingNotDropped() throws Exception {
        kafkaServer.stopKafkaServer();
        HeartbeatModule module = new HeartbeatModule();

        AtomicInteger heartbeatCount = new AtomicInteger();
        final MessageConsumer<Heartbeat, Heartbeat> heartbeatConsumer = new MessageConsumer<Heartbeat, Heartbeat>() {
            @Override
            public SinkModule<Heartbeat, Heartbeat> getModule() {
                return module;
            }

            @Override
            public void handleMessage(final Heartbeat heartbeat) {
                heartbeatCount.incrementAndGet();
            }
        };

        try {
            consumerManager.registerConsumer(heartbeatConsumer);

            final SyncDispatcher<Heartbeat> localDispatcher = localMessageDispatcherFactory.createSyncDispatcher(module);
            localDispatcher.send(new Heartbeat());
            await().atMost(30, SECONDS).until(() -> heartbeatCount.get(), equalTo(1));

            final SyncDispatcher<Heartbeat> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

            Executors.newSingleThreadExecutor().execute(() -> dispatcher.send(new Heartbeat()));
            Executors.newSingleThreadExecutor().execute(() -> dispatcher.send(new Heartbeat()));
            // This sleep is needed for testing the timeout for kafka producer.send();
            Thread.sleep(15000);
            kafkaServer.startKafkaServer();
            await().atMost(30, SECONDS).until(() -> heartbeatCount.get(), equalTo(3));
        } finally {
            consumerManager.unregisterConsumer(heartbeatConsumer);
        }

    }
}
