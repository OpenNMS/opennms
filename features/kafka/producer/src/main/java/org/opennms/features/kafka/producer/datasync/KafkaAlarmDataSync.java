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

package org.opennms.features.kafka.producer.datasync;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.opennms.features.kafka.producer.OpennmsKafkaProducer;
import org.opennms.features.kafka.producer.ProtobufMapper;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

public class KafkaAlarmDataSync implements AlarmDataStore, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAlarmDataSync.class);

    private static final String ALARM_STORE_NAME = "alarm_store";
    public static final String KAFKA_STREAMS_PID = "org.opennms.features.kafka.producer.streams";

    private final ConfigurationAdmin configAdmin;
    private final OpennmsKafkaProducer kafkaProducer;
    private final TransactionOperations transactionOperations;
    private final AlarmDao alarmDao;
    private final ProtobufMapper protobufMapper;
    private final AtomicBoolean closed = new AtomicBoolean(true);

    private String alarmTopic;
    private long alarmSyncIntervalMs;

    private KafkaStreams streams;
    private ScheduledExecutorService scheduler;
    private KTable<String, byte[]> alarmBytesKtable;
    private KTable<String, OpennmsModelProtos.Alarm> alarmKtable;

    public KafkaAlarmDataSync(ConfigurationAdmin configAdmin, OpennmsKafkaProducer kafkaProducer, AlarmDao alarmDao,
            ProtobufMapper protobufMapper, TransactionOperations transactionOperations) {
        this.configAdmin = Objects.requireNonNull(configAdmin);
        this.kafkaProducer = Objects.requireNonNull(kafkaProducer);
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.protobufMapper = Objects.requireNonNull(protobufMapper);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    /**
     * This method initializes the stream client, but doesn't actually start it until
     * an alarm is forwarded by the producer.
     *
     * @throws IOException when an error occurs in loading/parsing the Kafka client/stream configuration
     */
    public void init() throws IOException {
        if (!kafkaProducer.isForwardingAlarms() || alarmSyncIntervalMs <= 0) {
            LOG.info("Alarm synchronization disabled.");
            return;
        }

        final Properties streamProperties = loadStreamsProperties();
        final StreamsBuilder builder = new StreamsBuilder();
        final GlobalKTable<String, byte[]> alarmBytesKtable = builder.globalTable(alarmTopic, Consumed.with(Serdes.String(), Serdes.ByteArray()),
                Materialized.as(ALARM_STORE_NAME));

        final Topology topology = builder.build();
        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Use the class-loader for the KStream class, since the kafka-client bundle
            // does not import the required classes from the kafka-streams bundle
            Thread.currentThread().setContextClassLoader(KStream.class.getClassLoader());
            streams = new KafkaStreams(topology, streamProperties);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
        streams.setUncaughtExceptionHandler((t, e) -> LOG.error(
                String.format("Stream error on thread: %s", t.getName()), e));

        // Defer startup to another thread
        scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                .setNameFormat("kafka-producer-alarm-datasync-%d")
                .build()
        );
        closed.set(false);
        scheduler.execute(this);
    }

    @Override
    public void run() {
        try {
            if (kafkaProducer.getAlarmForwardedLatch().await(2, TimeUnit.MINUTES)) {
                LOG.debug("Triggered: An alarm was successfully forwarded to the topic.");
            } else {
                LOG.debug("Triggered: Timeout reached before an alarm was successfully forwarded to the topic.");
            }
        } catch (InterruptedException e) {
            LOG.info("Interrupted while waiting for alarm to be forwarded. Synchronization will not be performed.");
            return;
        }

        try {
            LOG.info("Starting alarm datasync stream.");
            streams.start();
            LOG.info("Starting alarm datasync started.");
        } catch (StreamsException | IllegalStateException e) {
            LOG.error("Failed to start alarm datasync stream. Synchronization will not be performed.", e);
        }

        LOG.info("Waiting for alarm data store to be ready.");
        while (!closed.get()) {
            if (isReady()) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.info("Interrupted while waiting for store to be ready. Synchronization will not be performed.");
                return;
            }
        }
        LOG.info("Alarm data store is ready!");

        LOG.info("Scheduling periodic alarm synchronization every {}ms", alarmSyncIntervalMs);
        // Schedule sync after initial delay of 1 minute or the sync interval, whichever is shorter
        scheduler.scheduleWithFixedDelay(this::doSynchronizeAlarmsWithDb, Math.min(TimeUnit.MINUTES.toMillis(1), alarmSyncIntervalMs),
                alarmSyncIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        closed.set(true);
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (streams != null) {
            streams.close(2, TimeUnit.MINUTES);
        }
    }

    private void doSynchronizeAlarmsWithDb() {
        LOG.debug("Performing alarm synchronization with ktable.");
        try {
            final AlarmSyncResults results = synchronizeAlarmsWithDb();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Done performing alarm synchronization with the ktable. Executed {} updates.",
                        results.getReductionKeysAdded().size()
                                + results.getReductionKeysDeleted().size()
                                + results.getReductionKeysUpdated().size());
                LOG.debug("Reduction keys added to ktable: {}", results.getReductionKeysAdded());
                LOG.debug("Reduction keys deleted from the ktable: {}", results.getReductionKeysDeleted());
                LOG.debug("Reduction keys updated in the ktable: {}", results.getReductionKeysAdded());
            }
        } catch (Exception e) {
            LOG.error("An error occurred while performing alarm synchronization with the ktable. Will try again after {} ms.",
                    alarmSyncIntervalMs, e);
        }
    }

    @Override
    public synchronized AlarmSyncResults synchronizeAlarmsWithDb() {
        // Retrieve the map of alarms by reduction key from the ktable
        final Map<String, OpennmsModelProtos.Alarm> alarmsInKtableByReductionKey = getAlarms();

        // Perform the synchronization in a single transaction context
        return transactionOperations.execute(status -> {
            final Set<String> reductionKeysInKtable = alarmsInKtableByReductionKey.keySet();

            // Retrieve all of the alarms from the database and apply the filter (if any) to these
            final List<OnmsAlarm> alarmsInDb = alarmDao.findAll().stream()
                    .filter(kafkaProducer::shouldForwardAlarm)
                    .collect(Collectors.toList());

            final Map<String, OnmsAlarm> alarmsInDbByReductionKey = alarmsInDb.stream()
                    .collect(Collectors.toMap(OnmsAlarm::getReductionKey, a -> a));
            final Set<String> reductionKeysInDb = alarmsInDbByReductionKey.keySet();

            // Push deletes for keys that are in the ktable, but not in the database
            final Set<String> reductionKeysNotInDb = Sets.difference(reductionKeysInKtable, reductionKeysInDb);
            reductionKeysNotInDb.forEach(kafkaProducer::handleDeletedAlarm);

            // Push new entries for keys that are in the database, but not in the ktable
            final Set<String> reductionKeysNotInKtable = Sets.difference(reductionKeysInDb, reductionKeysInKtable);
            reductionKeysNotInKtable.forEach(rkey -> kafkaProducer.handleNewOrUpdatedAlarm(alarmsInDbByReductionKey.get(rkey)));

            // Handle Updates
            final Set<String> reductionKeysUpdated = new LinkedHashSet<>();
            final Set<String> commonReductionKeys = Sets.intersection(reductionKeysInKtable, reductionKeysInDb);
            commonReductionKeys.forEach(rkey -> {
                final OnmsAlarm dbAlarm = alarmsInDbByReductionKey.get(rkey);
                final OpennmsModelProtos.Alarm mappedDbAlarm = protobufMapper.toAlarm(dbAlarm).build();
                final OpennmsModelProtos.Alarm alarmFromKtable = alarmsInKtableByReductionKey.get(rkey);
                if (!Objects.equals(mappedDbAlarm, alarmFromKtable)) {
                    kafkaProducer.handleNewOrUpdatedAlarm(dbAlarm);
                    reductionKeysUpdated.add(rkey);
                }
            });
            return new AlarmSyncResults(alarmsInKtableByReductionKey, alarmsInDb, alarmsInDbByReductionKey,
                    reductionKeysNotInKtable, reductionKeysNotInDb, reductionKeysUpdated);
        });
    }

    private Properties loadStreamsProperties() throws IOException {
        final Properties streamsProperties = new Properties();
        // Default values
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-datasync");
        Path kafkaDir = Paths.get(System.getProperty("karaf.data"), "kafka");
        streamsProperties.put(StreamsConfig.STATE_DIR_CONFIG, kafkaDir.toString());
        // Copy kafka server info from client properties
        final Dictionary<String, Object> clientProperties = configAdmin.getConfiguration(OpennmsKafkaProducer.KAFKA_CLIENT_PID).getProperties();
        if (clientProperties != null) {
            streamsProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, clientProperties.get(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
        }
        // Add all of the stream properties, overriding the bootstrap servers if set
        final Dictionary<String, Object> properties = configAdmin.getConfiguration(KAFKA_STREAMS_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                streamsProperties.put(key, properties.get(key));
            }
        }
        // Override the deserializers unconditionally
        streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
        return streamsProperties;
    }

    public void setAlarmTopic(String alarmTopic) {
        this.alarmTopic = alarmTopic;
    }

    public void setAlarmSyncIntervalMs(long intervalMs) {
        alarmSyncIntervalMs = intervalMs;
    }

    private ReadOnlyKeyValueStore<String, byte[]> getAlarmTableNow() throws InvalidStateStoreException {
        return streams.store(ALARM_STORE_NAME, QueryableStoreTypes.keyValueStore());
    }

    @Override
    public boolean isEnabled() {
        return !kafkaProducer.isForwardingAlarms() || alarmSyncIntervalMs <= 0;
    }

    @Override
    public boolean isReady() {
        try {
            getAlarmTableNow();
            return true;
        } catch (InvalidStateStoreException ignored) {
            // Store is not yet ready for querying
            return false;
        }
    }

    @Override
    public Map<String, OpennmsModelProtos.Alarm> getAlarms() {
        final Map<String, OpennmsModelProtos.Alarm> alarmsByReductionKey = new LinkedHashMap<>();
        getAlarmTableNow().all().forEachRemaining(kv -> {
            try {
                alarmsByReductionKey.put(kv.key, kv.value != null ? OpennmsModelProtos.Alarm.parseFrom(kv.value) : null);
            } catch (InvalidProtocolBufferException e) {
                LOG.error("Failed to parse alarm for bytes at reduction key '{}'. Alarm will be empty in map.", kv.key);
                alarmsByReductionKey.put(kv.key, null);
            }
        });
        return alarmsByReductionKey;
    }

    @Override
    public OpennmsModelProtos.Alarm getAlarm(String reductionKey) {
        final byte[] alarmBytes = getAlarmTableNow().get(reductionKey);
        try {
            return OpennmsModelProtos.Alarm.parseFrom(alarmBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to parse alarm for bytes at reduction key " + reductionKey, e);
        }
    }

}
