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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.common.ThreadLockingMessageConsumer;
import org.opennms.core.ipc.sink.kafka.client.KafkaRemoteMessageDispatcherFactory;
import org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.eventd.sink.EventsModule;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.util.concurrent.RateLimiter;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml" })
@JUnitConfigurationEnvironment
public class EventSinkConsumerIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private MessageDispatcherFactory localMessageDispatcherFactory;

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    @Autowired
    private EventdConfig m_config;

    private KafkaRemoteMessageDispatcherFactory remoteMessageDispatcherFactory = new KafkaRemoteMessageDispatcherFactory();

    @Before
    public void setUp() throws Exception {
        Hashtable<String, Object> kafkaConfig = new Hashtable<String, Object>();
        kafkaConfig.put("bootstrap.servers",
                        kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class,
                                              RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants.KAFKA_CONFIG_PID).getProperties()).thenReturn(kafkaConfig);
        remoteMessageDispatcherFactory.setConfigAdmin(configAdmin);
        remoteMessageDispatcherFactory.init();

        System.setProperty(String.format("%sbootstrap.servers",
                                         org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                           kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset",
                                         KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                           "earliest");
        consumerManager.afterPropertiesSet();
    }

    @Test(timeout = 30000)
    public void canProduceAndConsumeMessages() throws Exception {

        EventsModule eventsModule = new EventsModule(m_config);
        AtomicInteger eventsCount = new AtomicInteger();

        final MessageConsumer<Event, Log> eventMessageConsumer = new MessageConsumer<Event, Log>() {

            @Override
            public void handleMessage(Log messageLog) {
                eventsCount.incrementAndGet();
            }

            @Override
            public SinkModule<Event, Log> getModule() {
                return eventsModule;
            }
        };

        try {
            consumerManager.registerConsumer(eventMessageConsumer);

            final SyncDispatcher<Event> localDispatcher = localMessageDispatcherFactory.createSyncDispatcher(eventsModule);
            localDispatcher.send(new Event());
            await().atMost(1, MINUTES).until(() -> eventsCount.get(),
                                             equalTo(1));

            final SyncDispatcher<Event> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(new EventsModule(m_config));

            dispatcher.send(new Event());
            await().atMost(1, MINUTES).until(() -> eventsCount.get(),
                                             equalTo(2));
        } finally {
            consumerManager.unregisterConsumer(eventMessageConsumer);
        }

    }

    @Test(timeout = 60000)
    @Ignore("flapping")
    public void canConsumeMessagesInParallel() throws Exception {
        final int NUM_CONSUMER_THREADS = 7;

        final EventsModule parallelEventsModule = new EventsModule(m_config) {
            @Override
            public int getNumConsumerThreads() {
                return NUM_CONSUMER_THREADS;
            }
        };

        final ThreadLockingMessageConsumer<Event, Log> consumer = new ThreadLockingMessageConsumer<>(parallelEventsModule);

        final CompletableFuture<Integer> future = consumer.waitForThreads(NUM_CONSUMER_THREADS);

        try {
            consumerManager.registerConsumer(consumer);

            final SyncDispatcher<Event> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(new EventsModule(m_config));

            final EventGenerator generator = new EventGenerator(dispatcher,
                                                                        100.0);
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

    public static class EventGenerator {
        Thread thread;

        final SyncDispatcher<Event> dispatcher;
        final double rate;
        final AtomicBoolean stopped = new AtomicBoolean(false);
        private final Meter sentMeter;
        private final Timer sendTimer;

        public EventGenerator(SyncDispatcher<Event> dispatcher, double rate) {
            this.dispatcher = dispatcher;
            this.rate = rate;
            MetricRegistry metrics = new MetricRegistry();
            this.sentMeter = metrics.meter("sent");
            this.sendTimer = metrics.timer("send");
        }

        public EventGenerator(SyncDispatcher<Event> dispatcher, double rate,
                Meter sentMeter, Timer sendTimer) {
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

                    while (!stopped.get()) {
                        rateLimiter.acquire();
                        try (Context ctx = sendTimer.time()) {
                            dispatcher.send(new Event());
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
