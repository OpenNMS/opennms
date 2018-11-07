/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.itests;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.common.ThreadLockingMessageConsumer;
import org.opennms.core.ipc.sink.kafka.itests.HeartbeatSinkPerfIT.HeartbeatGenerator;
import org.opennms.core.ipc.sink.kafka.client.KafkaRemoteMessageDispatcherFactory;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.itests.heartbeat.Heartbeat;
import org.opennms.core.ipc.sink.kafka.itests.heartbeat.HeartbeatModule;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml"
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
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaSinkConstants.KAFKA_CONFIG_PID).getProperties())
            .thenReturn(kafkaConfig);
        remoteMessageDispatcherFactory.setConfigAdmin(configAdmin);
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
}
