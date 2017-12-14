/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.kafka.heartbeat.Heartbeat;
import org.opennms.core.ipc.sink.kafka.heartbeat.HeartbeatModule;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.util.concurrent.RateLimiter;

/**
 * Used to help profile the sink producer and consumer
 * against Kafka.
 *
 * By default, we only run a quick test to validate the setup.
 *
 * A longer test, against which you can attach a profiler is available
 * but disabled by default.
 * 
 * @author ranger
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml"
})
@JUnitConfigurationEnvironment
public class HeartbeatSinkPerfIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    private KafkaRemoteMessageDispatcherFactory messageDispatcherFactory = new KafkaRemoteMessageDispatcherFactory();

    private List<HeartbeatGenerator> generators = new ArrayList<>();
    private final MetricRegistry metrics = new MetricRegistry();
    private final Meter receivedMeter = metrics.meter("receivedMeter");
    private final Meter sentMeter = metrics.meter("sent");
    private final Timer sendTimer = metrics.timer("send");

    // Tuneables
    private static final int NUM_CONSUMER_THREADS = 2;
    private static final int NUM_GENERATORS = 2;
    private static final double RATE_PER_GENERATOR = 1000.0;

    @Before
    public void setUp() throws Exception {
        Hashtable<String, Object> kafkaConfig = new Hashtable<String, Object>();
        kafkaConfig.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaSinkConstants.KAFKA_CONFIG_PID).getProperties())
            .thenReturn(kafkaConfig);
        messageDispatcherFactory.setConfigAdmin(configAdmin);
        messageDispatcherFactory.init();

        System.setProperty(String.format("%sbootstrap.servers", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                "earliest");
        consumerManager.afterPropertiesSet();
    }

    public void configureGenerators() throws Exception {
        System.err.println("Starting Heartbeat generators.");

        // Start the consumer
        final HeartbeatModule parallelHeartbeatModule = new HeartbeatModule() {
            @Override
            public int getNumConsumerThreads() {
                return NUM_CONSUMER_THREADS;
            }
        };
        final HeartbeatConsumer consumer = new HeartbeatConsumer(parallelHeartbeatModule, receivedMeter);
        consumerManager.registerConsumer(consumer);

        // Start the dispatcher
        final SyncDispatcher<Heartbeat> dispatcher = messageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

        // Fire up the generators
        generators = new ArrayList<>(NUM_GENERATORS);
        for (int k = 0; k < NUM_GENERATORS; k++) {
            final HeartbeatGenerator generator = new HeartbeatGenerator(dispatcher, RATE_PER_GENERATOR, sentMeter, sendTimer);
            generators.add(generator);
            generator.start();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (generators != null) {
            for (HeartbeatGenerator generator : generators) {
                generator.stop();
            }
            generators.clear();
        }
        consumerManager.unregisterAllConsumers();
    }

    @Test(timeout=30000)
    public void quickRun() throws Exception {
        configureGenerators();
        await().until(() -> Long.valueOf(receivedMeter.getCount()), greaterThan(100L)); 
    }

    @Ignore
    public void longRun() throws Exception {
        // Here we enable console logging of the metrics we gather
        // To see these, you'll want to turn down the logging
        // You can do this by setting the following system property
        // on the JVM when running the tests:
        // -Dorg.opennms.core.test.mockLogger.defaultLogLevel=WARN

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        try {
            reporter.start(15, TimeUnit.SECONDS);
            Thread.sleep(5 * 60 * 1000);
        } finally {
            reporter.stop();
        }
    }

    public static class HeartbeatConsumer implements MessageConsumer<Heartbeat,Heartbeat> {

        private final HeartbeatModule heartbeatModule;
        private final Meter receivedMeter;

        public HeartbeatConsumer(HeartbeatModule heartbeatModule, Meter receivedMeter) {
            this.heartbeatModule = heartbeatModule;
            this.receivedMeter = receivedMeter;
        }

        @Override
        public SinkModule<Heartbeat,Heartbeat> getModule() {
            return heartbeatModule;
        }

        @Override
        public void handleMessage(Heartbeat message) {
            receivedMeter.mark();
        }
    }

    public static class HeartbeatGenerator {
        Thread thread;

        final SyncDispatcher<Heartbeat> dispatcher;
        final double rate;
        final AtomicBoolean stopped = new AtomicBoolean(false);
        private final Meter sentMeter;
        private final Timer sendTimer;

        public HeartbeatGenerator(SyncDispatcher<Heartbeat> dispatcher, double rate) {
            this.dispatcher = dispatcher;
            this.rate = rate;
            MetricRegistry metrics = new MetricRegistry();
            this.sentMeter = metrics.meter("sent");
            this.sendTimer = metrics.timer("send");
        }

        public HeartbeatGenerator(SyncDispatcher<Heartbeat> dispatcher, double rate, Meter sentMeter, Timer sendTimer) {
            this.dispatcher = dispatcher;
            this.rate = rate;
            this.sentMeter = sentMeter;
            this.sendTimer = sendTimer;
        }

        public synchronized void start() {
            stopped.set(false);
            final RateLimiter rateLimiter = RateLimiter.create(rate);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                   
                    while(!stopped.get()) {
                        rateLimiter.acquire();
                        try (Context ctx = sendTimer.time()) {
                            dispatcher.send(new Heartbeat());
                            sentMeter.mark();
                        }
                    }
                }
            });
            thread.start();
        }

        public synchronized void stop() throws InterruptedException {
            stopped.set(true);
            if (thread != null) {
                thread.join();
                thread = null;
            }
        }
    }
}
