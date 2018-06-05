/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.features.kafka.producer.datasync.KafkaAlarmDataSync;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Verifies events/alarms/nodes forwarded to Kafka.
 *
 * @author cgorantla
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath:/applicationContext-test-kafka-producer.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
public class KafkaForwarderIT implements TemporaryDatabaseAware<MockDatabase> {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaForwarderIT.class);

    private static final String EVENT_TOPIC_NAME = "events";
    private static final String ALARM_TOPIC_NAME = "test-alarms";
    private static final String NODE_TOPIC_NAME = "test-nodes";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer(tempFolder);

    @Autowired
    private MockEventIpcManager eventdIpcMgr;

    @Autowired
    private ProtobufMapper protobufMapper;

    @Autowired
    private NodeCache nodeCache;

    @Autowired
    private AlarmLifecycleListenerManager alarmLifecycleListenerManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    private MockDatabase mockDatabase;

    private OpennmsKafkaProducer kafkaProducer;

    private KafkaAlarmDataSync kafkaAlarmaDataStore;

    private ExecutorService executor;

    private KafkaMessageConsumerRunner kafkaConsumer;

    @Before
    public void setUp() throws IOException {
        File data = tempFolder.newFolder("data");
        eventdIpcMgr.setEventWriter(mockDatabase);

        databasePopulator.populateDatabase();

        Hashtable<String, Object> producerConfig = new Hashtable<>();
        producerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaForwarderIT.class.getCanonicalName());
        producerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        Hashtable<String, Object> streamsConfig = new Hashtable<>();
        streamsConfig.put(StreamsConfig.STATE_DIR_CONFIG, data.getAbsolutePath());
        streamsConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfig.put(StreamsConfig.METADATA_MAX_AGE_CONFIG, 1000);
        when(configAdmin.getConfiguration(OpennmsKafkaProducer.KAFKA_CLIENT_PID).getProperties()).thenReturn(producerConfig);
        when(configAdmin.getConfiguration(KafkaAlarmDataSync.KAFKA_STREAMS_PID).getProperties()).thenReturn(streamsConfig);

        kafkaProducer = new OpennmsKafkaProducer(protobufMapper, nodeCache, configAdmin, eventdIpcMgr,
                alarmLifecycleListenerManager);
        kafkaProducer.setEventTopic(EVENT_TOPIC_NAME);
        // Don't forward newSuspect events
        kafkaProducer.setEventFilter("!getUei().equals(\"" + EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI + "\")");
        kafkaProducer.setAlarmTopic(ALARM_TOPIC_NAME);
        // No alarm filtering
        kafkaProducer.setAlarmFilter(null);
        kafkaProducer.setNodeTopic(NODE_TOPIC_NAME);
        kafkaProducer.init();

        kafkaAlarmaDataStore = new KafkaAlarmDataSync(configAdmin, kafkaProducer, alarmDao, protobufMapper,
                transactionTemplate);
        kafkaAlarmaDataStore.setAlarmTopic(ALARM_TOPIC_NAME);
        kafkaAlarmaDataStore.setAlarmSyncIntervalMs(1000);
        kafkaAlarmaDataStore.init();
    }

    @Test
    public void canProducerAndConsumeMessages() throws Exception {
        // Send a node down event (should be forwarded)
        eventdIpcMgr.sendNow(MockEventUtil.createNodeDownEventBuilder("test", databasePopulator.getNode1()).getEvent());
        // Create and trigger the associated alarm
        final String alarmReductionKey = String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, databasePopulator.getNode1().getId());
        final OnmsAlarm alarm = new OnmsAlarm();
        {
            alarm.setId(1);
            alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
            alarm.setNode(databasePopulator.getNode1());
            alarm.setCounter(1);
            alarm.setDescription("node down");
            alarm.setAlarmType(1);
            alarm.setLogMsg("node down");
            alarm.setSeverity(OnmsSeverity.NORMAL);
            alarm.setReductionKey(alarmReductionKey);
            alarmDao.save(alarm);
            kafkaProducer.handleNewOrUpdatedAlarm(alarm);
        }

        // Send a unrelated newSuspect event (should not be forwarded)
        eventdIpcMgr.sendNow(MockEventUtil.createNewSuspectEventBuilder("test",
                EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "192.168.1.1")
                .getEvent());

        // Send a node up (should be forwarded)
        eventdIpcMgr.sendNow(MockEventUtil.createNodeUpEventBuilder("test", databasePopulator.getNode2()).getEvent());

        if (!kafkaProducer.getEventForwardedLatch().await(1, TimeUnit.MINUTES)) {
            throw new Exception("No events were successfully forwarded in time!");
        }
        if (!kafkaProducer.getNodeForwardedLatch().await(1, TimeUnit.MINUTES)) {
            throw new Exception("No nodes were successfully forwarded in time!");
        }
        if (!kafkaProducer.getAlarmForwardedLatch().await(1, TimeUnit.MINUTES)) {
            throw new Exception("No alarm were successfully forwarded in time!");
        }

        // Fire up the consumer
        executor = Executors.newSingleThreadExecutor();
        kafkaConsumer = new KafkaMessageConsumerRunner(kafkaServer.getKafkaConnectString());
        executor.execute(kafkaConsumer);

        // Wait for the events to be consumed
        await().atMost(1, TimeUnit.MINUTES).until(this::getUeisForConsumedEvents, hasItems(
                EventConstants.NODE_DOWN_EVENT_UEI, EventConstants.NODE_UP_EVENT_UEI));
        assertThat(getUeisForConsumedEvents(), not(hasItem(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)));
        // Wait for the nodes to be consumed
        await().atMost(1, TimeUnit.MINUTES).until(this::getIdsForConsumedNodes, hasItems(
                databasePopulator.getNode1().getId(), databasePopulator.getNode2().getId()));
        // Wait for the alarms to be consumed
        await().atMost(1, TimeUnit.MINUTES).until(() -> kafkaConsumer.getAlarmByReductionKey(alarmReductionKey), not(nullValue()));

        // Events, nodes and alarms were forwarded and consumed!

        // Verify the consumed alarm object
        assertThat(kafkaConsumer.getAlarmByReductionKey(alarmReductionKey).getDescription(), equalTo("node down"));

        // Now delete the alarm directly in the database
        alarmDao.delete(alarm);

        // Wait until the synchronization process kicks off and delete the alarm
        await().atMost(2, TimeUnit.MINUTES).until(() ->
                kafkaConsumer.getAlarmByReductionKey(alarmReductionKey), nullValue());

    }

    private Set<String> getUeisForConsumedEvents() {
        return kafkaConsumer.getEvents().stream()
                .map(OpennmsModelProtos.Event::getUei)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getIdsForConsumedNodes() {
        return kafkaConsumer.getNodes().stream()
                .filter(Objects::nonNull)
                .map(n -> (int)n.getId())
                .collect(Collectors.toSet());
    }

    private Set<String> getReductionKeysForConsumedAlarms() {
        return kafkaConsumer.getAlarms().stream()
                .filter(Objects::nonNull)
                .map(OpennmsModelProtos.Alarm::getReductionKey)
                .collect(Collectors.toSet());
    }

    private static class KafkaMessageConsumerRunner implements Runnable {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private KafkaConsumer<String, byte[]> consumer;
        private String kafkaConnectString;
        private List<OpennmsModelProtos.Event> events = new ArrayList<>();
        private List<OpennmsModelProtos.Node> nodes = new ArrayList<>();
        private List<OpennmsModelProtos.Alarm> alarms = new ArrayList<>();
        private Map<String, OpennmsModelProtos.Alarm> alarmsByReductionKey = new LinkedHashMap<>();
        private AtomicInteger numRecordsConsumed = new AtomicInteger(0);

        public KafkaMessageConsumerRunner(String kafkaConnectString) {
            this.kafkaConnectString = Objects.requireNonNull(kafkaConnectString);
        }

        @Override
        public void run() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectString);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaMessageConsumerRunner.class.getCanonicalName() + "-" + UUID.randomUUID().toString());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.TRUE.toString());
            props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(EVENT_TOPIC_NAME, NODE_TOPIC_NAME, ALARM_TOPIC_NAME));

            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(1000);
                for (ConsumerRecord<String, byte[]> record : records) {
                    try {
                        switch (record.topic()) {
                            case EVENT_TOPIC_NAME:
                                events.add(OpennmsModelProtos.Event.parseFrom(record.value()));
                                break;
                            case NODE_TOPIC_NAME:
                                nodes.add(record.value() != null ?
                                        OpennmsModelProtos.Node.parseFrom(record.value()) : null);
                                break;
                            case ALARM_TOPIC_NAME:
                                final OpennmsModelProtos.Alarm alarm = record.value() != null ?
                                        OpennmsModelProtos.Alarm.parseFrom(record.value()) : null;
                                alarms.add(alarm);
                                alarmsByReductionKey.put(record.key(), alarm);
                                break;
                        }
                        numRecordsConsumed.incrementAndGet();
                    } catch (Exception e) {
                        LOG.error("Failed to parse record: {}",  record, e);
                    }
                }
            }
            consumer.close(1, TimeUnit.MINUTES);
        }

        public AtomicInteger getNumRecordsConsumed() {
            return numRecordsConsumed;
        }

        public List<OpennmsModelProtos.Event> getEvents() {
            return events;
        }

        public List<OpennmsModelProtos.Node> getNodes() {
            return nodes;
        }

        public List<OpennmsModelProtos.Alarm> getAlarms() {
            return alarms;
        }

        public Map<String, OpennmsModelProtos.Alarm> getAlarmsByReductionKey() {
            return alarmsByReductionKey;
        }

        public OpennmsModelProtos.Alarm getAlarmByReductionKey(String reductionKey) {
            return alarmsByReductionKey.get(reductionKey);
        }

        public void shutdown() {
            closed.set(true);
        }
    }

    @After
    public void tearDown() {
        if (kafkaConsumer != null) {
            kafkaConsumer.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
        databasePopulator.resetDatabase();
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        mockDatabase = database;
    }
}
