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
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test that matches events forwarded to kafka by consuming from kafka
 * Also verifies event filtering and nodes.
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
public class KafkaEventForwarderIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final String KAFKA_PRODUCER_CLIENT_PID = "org.opennms.features.kafka.producer.client";
    private static final int NODE_ID_ONE = 1;

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

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

    private OpennmsKafkaProducer kafkaProducer;

    private KafkaMessageConsumerRunner kafkaConsumer;

    private MockDatabase m_database;

    @Before
    public void setup() throws IOException, InterruptedException {

        m_eventdIpcMgr.setEventWriter(m_database);
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("Default");
        m_locationDao.save(location);
        final OnmsNode node = new OnmsNode(location, "node1");
        node.setId(NODE_ID_ONE);
        m_nodeDao.save(node);

        Hashtable<String, Object> producerConfig = new Hashtable<String, Object>();
        producerConfig.put("group.id", "OpenNMS");
        producerConfig.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KAFKA_PRODUCER_CLIENT_PID).getProperties()).thenReturn(producerConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        kafkaConsumer = new KafkaMessageConsumerRunner(kafkaServer.getKafkaConnectString());
        executor.execute(kafkaConsumer);

        kafkaProducer = new OpennmsKafkaProducer(protobufMapper, nodeCache, configAdmin, m_eventdIpcMgr,
                alarmLifecycleListenerManager);

        kafkaProducer.setEventTopic("events");
        kafkaProducer.setEventFilter("getUei().equals(\"uei.opennms.org/internal/discovery/newSuspect\")");
        kafkaProducer.setNodeTopic("nodes");
        kafkaProducer.init();
        // 2 second delay for kafka producer to initialize
        Thread.sleep(2000);
    }

    @Test
    public void matchEventsAndNodesFromKafka() throws EventProxyException, IOException {

        EventBuilder builder = MockEventUtil.createNewSuspectEventBuilder("_test",
                EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "192.168.1.1");
        m_eventdIpcMgr.send(builder.getEvent());

        sendNodeUpEvent(NODE_ID_ONE);

        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS).untilAtomic(kafkaConsumer.getCount(),
                is(2));

        OpennmsModelProtos.Event newSuspectEvent = OpennmsModelProtos.Event
                .parseFrom(kafkaConsumer.getResult().get("events"));
        // This should only receive newSuspectEvent as filter disallows nodeUp event
        assertEquals(builder.getEvent().getUei(), newSuspectEvent.getUei());
        assertEquals(builder.getEvent().getSource(), newSuspectEvent.getSoure());

        OpennmsModelProtos.Node nodeData = OpennmsModelProtos.Node.parseFrom(kafkaConsumer.getResult().get("nodes"));
        assertEquals(nodeData.getId(), NODE_ID_ONE);

    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_database = database;
    }

    private void sendNodeUpEvent(long nodeId) throws EventProxyException {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "_test");
        Date currentTime = new Date();
        builder.setTime(currentTime);
        builder.setNodeid(nodeId);
        builder.setSeverity("Normal");
        m_eventdIpcMgr.send(builder.getEvent());
    }

    @After
    public void tearDown() throws Exception {
        kafkaConsumer.shutdown();
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
            consumer.subscribe(Arrays.asList("events", "alarms", "nodes"));
            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(100);
                for (ConsumerRecord<String, byte[]> record : records) {
                    if (record.topic().equals("events")) {
                        result.put("events", record.value());
                    }
                    if (record.topic().equals("nodes")) {
                        result.put("nodes", record.value());
                    }
                    count.incrementAndGet();
                }
            }
        }

        public String getKafkaServer() {
            return this.kafkaServer;
        }

        public AtomicInteger getCount() {
            return count;
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

}
