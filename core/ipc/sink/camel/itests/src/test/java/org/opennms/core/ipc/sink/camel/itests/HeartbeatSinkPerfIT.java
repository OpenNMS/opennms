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
package org.opennms.core.ipc.sink.camel.itests;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.camel.itests.heartbeat.Heartbeat;
import org.opennms.core.ipc.sink.camel.itests.heartbeat.HeartbeatModule;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.util.concurrent.RateLimiter;

/**
 * Used to help profile the sink producer and consumer
 * against an ActiveMQ broker.
 *
 * By default, we only run a quick test to validate the setup.
 *
 * A longer test, against which you can attach a profiler is available
 * but disabled by default.
 * 
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-server.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-client.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-opennms-identity.xml"
})
@JUnitConfigurationEnvironment
@org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
public class HeartbeatSinkPerfIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private MessageConsumerManager consumerManager;

    private List<HeartbeatGenerator> generators = new ArrayList<>();
    private final MetricRegistry metrics = new MetricRegistry();
    private final Meter receivedMeter = metrics.meter("receivedMeter");
    private final Meter sentMeter = metrics.meter("sent");
    private final Timer sendTimer = metrics.timer("send");

    // Tuneables
    private static final int NUM_CONSUMER_THREADS = 2;
    private static final int NUM_GENERATORS = 2;
    private static final double RATE_PER_GENERATOR = 1000.0;

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MinionIdentity() {
                    @Override
                    public String getId() {
                        return "0";
                    }
                    @Override
                    public String getLocation() {
                        return REMOTE_LOCATION_NAME;
                    }
                    @Override
                    public String getType() {
                        return SystemType.Minion.name();
                    }
                }, new Properties()));

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(queuingservice, props));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-ipc-client.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        final HeartbeatModule parallelHeartbeatModule = new HeartbeatModule() {
            @Override
            public int getNumConsumerThreads() {
                return NUM_CONSUMER_THREADS;
            }
        };

        final HeartbeatConsumer consumer = new HeartbeatConsumer(parallelHeartbeatModule, receivedMeter);
        consumerManager.registerConsumer(consumer);

        final MessageDispatcherFactory remoteMessageDispatcherFactory = context.getRegistry().lookupByNameAndType("camelRemoteMessageDispatcherFactory", MessageDispatcherFactory.class);
        final SyncDispatcher<Heartbeat> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

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
        for (HeartbeatGenerator generator : generators) {
            generator.stop();
        }
        generators.clear();
        super.tearDown();
    }

    @Test(timeout=60000)
    public void quickRun() throws Exception {
        await().atMost(60, TimeUnit.SECONDS).until(() -> Long.valueOf(receivedMeter.getCount()), greaterThan(100L)); 
    }

    @Ignore("enable manually to test long running logging")
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

    public static class HeartbeatConsumer implements MessageConsumer<Heartbeat, Heartbeat> {

        private final HeartbeatModule heartbeatModule;
        private final Meter receivedMeter;

        public HeartbeatConsumer(HeartbeatModule heartbeatModule, Meter receivedMeter) {
            this.heartbeatModule = heartbeatModule;
            this.receivedMeter = receivedMeter;
        }

        @Override
        public SinkModule<Heartbeat, Heartbeat> getModule() {
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
