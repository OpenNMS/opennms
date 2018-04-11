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
import static org.hamcrest.Matchers.containsString;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
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
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.mock.MockEventUtil;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Test that matches events/alarms forwarded to kafka by consuming from kafka
 * Also verifies event filtering and data from alarm store.
 *
 * @author cgorantla
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath:/applicationContext-test-kafka-producer.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
public class KafkaForwarderIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final String KAFKA_PRODUCER_CLIENT_PID = "org.opennms.features.kafka.producer.client";
    private static final String KAFKA_STREAMS_PID = "org.opennms.features.kafka.producer.streams";
    private static final int ID_ONE = 1;
    private static final int ID_TWO = 2;
    private static final String ALARM_TRIGGER = "uei.opennms.org/alarms/trigger";

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private MockEventIpcManager m_eventdIpcMgr;

    @Autowired
    private ProtobufMapper protobufMapper;

    @Autowired
    private NodeCache nodeCache;

    @Autowired
    private AlarmLifecycleListenerManager alarmLifecycleListenerManager;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AlarmDao m_alarmDao;

    private OpennmsKafkaProducer kafkaProducer;

    private KafkaMessageConsumerRunner kafkaConsumerRunner;

    private MockDatabase m_database;

    private KafkaAlarmDataSync kafkaAlarmaDataStore;

    @Before
    public void setup() throws IOException, InterruptedException {

        File data = tempFolder.newFolder("data");
        m_eventdIpcMgr.setEventWriter(m_database);
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("Default");
        m_locationDao.save(location);
        final OnmsNode node = new OnmsNode(location, "node1");
        node.setId(ID_ONE);
        m_nodeDao.save(node);

        Hashtable<String, Object> producerConfig = new Hashtable<String, Object>();
        producerConfig.put("group.id", "OpenNMS");
        producerConfig.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        Hashtable<String, Object> streamsConfig = new Hashtable<String, Object>();
        streamsConfig.put("state.dir", data.getAbsolutePath());
        when(configAdmin.getConfiguration(KAFKA_PRODUCER_CLIENT_PID).getProperties()).thenReturn(producerConfig);
        when(configAdmin.getConfiguration(KAFKA_STREAMS_PID).getProperties()).thenReturn(streamsConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        kafkaConsumerRunner = new KafkaMessageConsumerRunner(kafkaServer.getKafkaConnectString());
        executor.execute(kafkaConsumerRunner);

        kafkaProducer = new OpennmsKafkaProducer(protobufMapper, nodeCache, configAdmin, m_eventdIpcMgr,
                alarmLifecycleListenerManager);

        kafkaProducer.setEventTopic("events");
        kafkaProducer.setEventFilter("getUei().equals(\"uei.opennms.org/internal/discovery/newSuspect\")");
        kafkaProducer.setNodeTopic("nodes");
        kafkaProducer.setAlarmTopic("alarms");

        kafkaProducer.init();

        kafkaAlarmaDataStore = new KafkaAlarmDataSync(configAdmin, kafkaProducer, m_alarmDao, protobufMapper,
                transactionTemplate);
        kafkaAlarmaDataStore.setAlarmTopic("alarms");
        kafkaAlarmaDataStore.init();

        waitUntilTopicsAreInitialized();

        // Send alarm data initially so that sync takes care of modified data
        sendAlarmDataToKafka();
        modifyAlarmsInDB();

    }

    @Test
    public void matchEventsAndNodesFromKafka() throws EventProxyException, IOException {
        kafkaConsumerRunner.resetCount();
        EventBuilder builder = MockEventUtil.createNewSuspectEventBuilder("_test",
                EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "192.168.1.1");
        m_eventdIpcMgr.send(builder.getEvent());

        sendNodeUpEvent(ID_ONE);

        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .untilAtomic(kafkaConsumerRunner.getCount(), greaterThan(1));

        OpennmsModelProtos.Event newSuspectEvent = OpennmsModelProtos.Event
                .parseFrom(kafkaConsumerRunner.getResult().get("events"));
        // This should only receive newSuspectEvent as filter disallows nodeUp
        // event
        assertEquals(builder.getEvent().getUei(), newSuspectEvent.getUei());
        assertEquals(builder.getEvent().getSource(), newSuspectEvent.getSource());

    }

    @Test
    public void matchAlarmsFromKafka() throws InvalidProtocolBufferException, InterruptedException, ExecutionException {

        // Alarm data was sent already.
        await().atMost(3, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS)
                .until(() -> verifyUpdatedAlarmsOnDataStore(), containsString("Updated_Alarm2"));

    }

    private String verifyUpdatedAlarmsOnDataStore()
            throws InterruptedException, ExecutionException, InvalidProtocolBufferException {

        try {
            CompletableFuture<ReadOnlyKeyValueStore<String, byte[]>> future = kafkaAlarmaDataStore.getAlarmDataStore();
            ReadOnlyKeyValueStore<String, byte[]> alarmStore = future.get(5, TimeUnit.SECONDS);
            String key = String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, ID_TWO);
            // Wait till datastore gets updated with alarm trigger which was
            // modified in DB but not sent to producer
            if (alarmStore != null && alarmStore.get(key) != null && alarmStore.get(ALARM_TRIGGER) != null) {
                List<String> keys = new ArrayList<>();
                alarmStore.all().forEachRemaining(alarmData -> keys.add(alarmData.key));
                // Verify that this event got deleted
                assertFalse(keys.contains(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, ID_ONE)));
                return OpennmsModelProtos.Alarm.parseFrom(alarmStore.get(key)).toString();
            }
        } catch (TimeoutException e) {
            // pass
        }
        return null;

    }

    private void sendAlarmDataToKafka() {

        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("Default");
        m_locationDao.save(location);
        {
            final OnmsNode node = new OnmsNode(location, "node2");
            node.setId(ID_ONE);
            m_nodeDao.save(node);
            OnmsAlarm alarm = new OnmsAlarm();
            alarm.setId(1);
            alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
            alarm.setNode(m_nodeDao.get(ID_ONE));
            alarm.setCounter(1);
            alarm.setDescription("Alarm1");
            alarm.setAlarmType(1);
            alarm.setLogMsg("test-log");
            alarm.setSeverity(OnmsSeverity.NORMAL);
            alarm.setReductionKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, ID_ONE));
            m_alarmDao.save(alarm);
            kafkaProducer.handleNewOrUpdatedAlarm(alarm);
        }
        {
            final OnmsNode node = new OnmsNode(location, "node2");
            node.setId(ID_TWO);
            m_nodeDao.save(node);
            OnmsAlarm alarm = new OnmsAlarm();
            alarm.setId(ID_TWO);
            alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
            alarm.setNode(m_nodeDao.get(ID_TWO));
            alarm.setCounter(1);
            alarm.setDescription("Alarm2");
            alarm.setAlarmType(1);
            alarm.setLogMsg("test-log");
            alarm.setSeverity(OnmsSeverity.NORMAL);
            alarm.setReductionKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, 2));
            m_alarmDao.save(alarm);
            kafkaProducer.handleNewOrUpdatedAlarm(alarm);
        }

    }

    private void modifyAlarmsInDB() {
        m_alarmDao.delete(ID_ONE);
        OnmsAlarm retrievedAlarm = m_alarmDao.get(ID_TWO);
        retrievedAlarm.setDescription("Updated_Alarm2");
        retrievedAlarm.setCounter(ID_TWO);
        m_alarmDao.save(retrievedAlarm);
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(3);
        alarm.setUei(ALARM_TRIGGER);
        alarm.setCounter(1);
        alarm.setDescription("Alarm-Trigger");
        alarm.setAlarmType(1);
        alarm.setLogMsg("test-log");
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setReductionKey(ALARM_TRIGGER);
        m_alarmDao.save(alarm);
    }

    private void sendNodeUpEvent(long nodeId) throws EventProxyException {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "_test");
        Date currentTime = new Date();
        builder.setTime(currentTime);
        builder.setNodeid(nodeId);
        builder.setSeverity("Normal");
        m_eventdIpcMgr.send(builder.getEvent());
    }

    private void waitUntilTopicsAreInitialized() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
        props.put("group.id", "OpenNMS");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
        await().atMost(45, SECONDS).pollDelay(10, SECONDS).pollInterval(5, SECONDS)
                .until(() -> kafkaConsumer.listTopics().keySet().contains("events"));
        kafkaConsumer.close();
    }

    private class KafkaMessageConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private KafkaConsumer<String, byte[]> consumer;
        private String kafkaServer;
        private Map<String, byte[]> result = new HashMap<>();
        private AtomicInteger count = new AtomicInteger(0);

        public KafkaMessageConsumerRunner(String bootstrapServer) {
            this.kafkaServer = bootstrapServer;
        }

        @Override
        public void run() {
            Properties props = new Properties();
            props.put("bootstrap.servers", getKafkaServer());
            props.put("group.id", "OpenNMS");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("events", "nodes", "alarms"));

            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(100);
                for (ConsumerRecord<String, byte[]> record : records) {
                    if (record.topic().equals("events")) {
                        result.put("events", record.value());
                    }
                    if (record.topic().equals("nodes")) {
                        result.put("nodes", record.value());
                    }
                    if (record.topic().equals("alarms")) {
                        result.put("alarms", record.value());
                    }

                    count.incrementAndGet();
                }
            }
        }

        public String getKafkaServer() {
            return this.kafkaServer;
        }

        public AtomicInteger getCount() {
            return this.count;
        }

        public void resetCount() {
            this.count.set(0);
        }

        public Map<String, byte[]> getResult() {
            return result;
        }

        public void shutdown() {
            closed.set(true);
            if (consumer != null) {
                consumer.wakeup();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        kafkaConsumerRunner.shutdown();
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_database = database;
    }

}
