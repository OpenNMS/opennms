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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.kafka.client.KafkaRemoteMessageDispatcherFactory;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.ipc.sink.model.SinkMessage;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Grows a topic's partition count while a multi-chunk Sink message is being sent and verifies that all chunks
 * land on the partition chosen for the first chunk and that the message is dispatched exactly once.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-opennms-identity.xml"
})
@JUnitConfigurationEnvironment
public class KafkaChunkPartitionPinningIT {

    private static final String TEST_UEI = "uei/test/kafka/partitionPinning";
    private static final long NODE_ID = 2345;
    private static final int CHUNK_SIZE = 1000;
    private static final int PAYLOAD_SIZE = 3_000_000;
    private static final int INITIAL_PARTITIONS = 1;
    private static final int GROWN_PARTITIONS = 4;
    private static final int CHUNKS_BEFORE_GROWTH = 50;

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    private final KafkaRemoteMessageDispatcherFactory remoteMessageDispatcherFactory = new KafkaRemoteMessageDispatcherFactory();

    private Properties adminConfig;

    @Before
    public void setUp() throws Exception {
        adminConfig = new Properties();
        adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());

        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
        // Refresh metadata quickly so the producer notices the new partitions while the message is in flight.
        kafkaConfig.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 100);
        kafkaConfig.put(KafkaSinkConstants.MAX_BUFFER_SIZE_PROPERTY, CHUNK_SIZE);
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
        assertThat(remoteMessageDispatcherFactory.getMaxBufferSize(), is(CHUNK_SIZE));

        System.setProperty(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX + ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        System.setProperty(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        System.setProperty(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX + ConsumerConfig.METADATA_MAX_AGE_CONFIG, "100");

        consumerManager.afterPropertiesSet();
    }

    @After
    public void destroy() {
        remoteMessageDispatcherFactory.destroy();
    }

    @Test
    public void chunksStayOnOnePartitionWhenTopicGrowsMidSend() throws Exception {
        final String routingKey = routingKeyThatMovesWhenTopicGrows();
        final EventsMockModule module = new EventsMockModule() {
            @Override
            public Optional<String> getRoutingKey(final Event message) {
                return Optional.of(routingKey);
            }
        };
        final String topic = remoteMessageDispatcherFactory.getModuleMetadata(module);
        createTopic(topic, INITIAL_PARTITIONS);

        final List<Event> outputEvents = Collections.synchronizedList(new ArrayList<>());
        final MessageConsumer<Event, Event> eventConsumer = new MessageConsumer<Event, Event>() {
            @Override
            public SinkModule<Event, Event> getModule() {
                return EventsMockModule.INSTANCE;
            }

            @Override
            public void handleMessage(Event event) {
                outputEvents.add(event);
            }
        };

        final Event event = buildLargeEvent();
        try {
            consumerManager.registerConsumer(eventConsumer);
            final SyncDispatcher<Event> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(module);

            // Grow the topic as soon as the first chunks are on the log, while the send loop is still running.
            final CompletableFuture<Long> topicGrownAt = CompletableFuture.supplyAsync(() -> {
                await().atMost(30, SECONDS).until(() -> endOffset(topic, 0), greaterThan((long) CHUNKS_BEFORE_GROWTH));
                growTopic(topic, GROWN_PARTITIONS);
                return System.nanoTime();
            });

            dispatcher.send(event);
            final long sendFinishedAt = System.nanoTime();

            // The test only proves something if the topic really grew before the last chunk went out.
            assertThat(topicGrownAt.get(30, SECONDS), lessThan(sendFinishedAt));
            assertThat(partitionCount(topic), is(GROWN_PARTITIONS));

            // Every chunk exactly once, all on the same partition.
            final Map<Integer, Integer> partitionByChunk = new HashMap<>();
            final List<ConsumerRecord<String, byte[]>> records = readAllRecords(topic, GROWN_PARTITIONS);
            int totalChunks = 0;
            for (ConsumerRecord<String, byte[]> record : records) {
                final SinkMessage chunk = SinkMessage.parseFrom(record.value());
                totalChunks = chunk.getTotalChunks();
                final Integer previous = partitionByChunk.put(chunk.getCurrentChunkNumber(), record.partition());
                assertThat("chunk " + chunk.getCurrentChunkNumber() + " was written twice", previous, is((Integer) null));
            }
            assertThat(totalChunks, greaterThan(1));
            assertThat(records.size(), is(totalChunks));
            assertThat(partitionByChunk.keySet().size(), is(totalChunks));
            final Set<Integer> partitionsUsed = new HashSet<>(partitionByChunk.values());
            assertThat("chunks were spread over partitions " + partitionsUsed, partitionsUsed.size(), is(1));

            // Dispatched once, intact, and no second copy shows up later.
            await().atMost(1, MINUTES).until(outputEvents::size, equalTo(1));
            await().pollDelay(5, SECONDS).atMost(10, SECONDS).until(outputEvents::size, equalTo(1));
            assertThat(outputEvents.get(0).getUei(), is(TEST_UEI));
            assertThat(outputEvents.get(0).getDescr(), is(event.getDescr()));
        } finally {
            consumerManager.unregisterConsumer(eventConsumer);
        }
    }

    /**
     * With one partition every key maps to partition 0. Pick a key that the default partitioner would move to a
     * different partition once the topic has {@link #GROWN_PARTITIONS} partitions, so that an unpinned producer
     * would demonstrably split the message.
     */
    private static String routingKeyThatMovesWhenTopicGrows() {
        for (int i = 0; ; i++) {
            final String key = "pin-" + i;
            final int hash = Utils.toPositive(Utils.murmur2(key.getBytes(StandardCharsets.UTF_8)));
            if (hash % GROWN_PARTITIONS != 0) {
                return key;
            }
        }
    }

    private void createTopic(String topic, int partitions) throws Exception {
        try (AdminClient admin = AdminClient.create(adminConfig)) {
            admin.createTopics(Collections.singleton(new NewTopic(topic, partitions, (short) 1))).all().get(30, SECONDS);
        }
    }

    private void growTopic(String topic, int partitions) {
        try (AdminClient admin = AdminClient.create(adminConfig)) {
            admin.createPartitions(Collections.singletonMap(topic, NewPartitions.increaseTo(partitions))).all().get(30, SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to grow topic " + topic, e);
        }
    }

    private int partitionCount(String topic) throws Exception {
        try (AdminClient admin = AdminClient.create(adminConfig)) {
            return admin.describeTopics(Collections.singleton(topic)).allTopicNames().get(30, SECONDS)
                    .get(topic).partitions().size();
        }
    }

    private long endOffset(String topic, int partition) {
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(rawConsumerConfig())) {
            final TopicPartition topicPartition = new TopicPartition(topic, partition);
            return consumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
        }
    }

    private List<ConsumerRecord<String, byte[]>> readAllRecords(String topic, int partitions) {
        final List<ConsumerRecord<String, byte[]>> records = new ArrayList<>();
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(rawConsumerConfig())) {
            final List<TopicPartition> topicPartitions = new ArrayList<>();
            for (int partition = 0; partition < partitions; partition++) {
                topicPartitions.add(new TopicPartition(topic, partition));
            }
            consumer.assign(topicPartitions);
            consumer.seekToBeginning(topicPartitions);
            final Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
            await().atMost(1, MINUTES).until(() -> {
                final ConsumerRecords<String, byte[]> polled = consumer.poll(Duration.ofMillis(500));
                polled.forEach(records::add);
                return topicPartitions.stream().allMatch(tp -> consumer.position(tp) >= endOffsets.get(tp));
            });
        }
        return records;
    }

    private Properties rawConsumerConfig() {
        final Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return config;
    }

    private Event buildLargeEvent() throws Exception {
        final EventBuilder eventBuilder = new EventBuilder(TEST_UEI, "kafka-test");
        eventBuilder.setInterface(InetAddress.getLocalHost());
        eventBuilder.setHost(InetAddressUtils.getLocalHostName());
        eventBuilder.setNodeid(NODE_ID);
        eventBuilder.setDescription(RandomStringUtils.random(PAYLOAD_SIZE, true, true));
        return eventBuilder.getEvent();
    }
}
