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
package org.opennms.features.kafka.producer.datasync;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
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
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.features.kafka.producer.AlarmEqualityChecker;
import org.opennms.features.kafka.producer.KafkaProducerManager;
import org.opennms.features.kafka.producer.OpennmsKafkaProducer;
import org.opennms.features.kafka.producer.ProtobufMapper;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.alarmd.api.AlarmCallbackStateTracker;
import org.opennms.netmgt.model.OnmsAlarm;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

public class KafkaAlarmDataSync implements AlarmDataStore, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAlarmDataSync.class);

    private static final String ALARM_STORE_NAME = "alarm_store";
    public static final String KAFKA_STREAMS_PID = "org.opennms.features.kafka.producer.streams";
    private final KafkaProducerManager kafkaProducerManager;
    private final OpennmsKafkaProducer kafkaProducer;
    private final ProtobufMapper protobufMapper;
    private final AtomicBoolean closed = new AtomicBoolean(true);

    private String alarmTopic;
    private boolean alarmSync;
    private boolean startWithCleanState = false;

    private KafkaStreams streams;
    private ScheduledExecutorService scheduler;
    private KTable<String, byte[]> alarmBytesKtable;
    private KTable<String, OpennmsModelProtos.Alarm> alarmKtable;

    private final AlarmEqualityChecker alarmEqualityChecker =
            AlarmEqualityChecker.with(AlarmEqualityChecker.Exclusions::defaultExclusions);
    private boolean suppressIncrementalAlarms;

    public KafkaAlarmDataSync(KafkaProducerManager kafkaProducerManager, OpennmsKafkaProducer kafkaProducer, ProtobufMapper protobufMapper) {
        this.kafkaProducerManager = Objects.requireNonNull(kafkaProducerManager);
        this.kafkaProducer = Objects.requireNonNull(kafkaProducer);
        this.protobufMapper = Objects.requireNonNull(protobufMapper);
    }

    /**
     * This method initializes the stream client, but doesn't actually start it until
     * an alarm is forwarded by the producer.
     *
     * @throws IOException when an error occurs in loading/parsing the Kafka client/stream configuration
     */
    public void init() throws IOException {
        if (!isEnabled()) {
            LOG.info("Alarm synchronization disabled. Skipping initialization.");
            return;
        }

        if (!kafkaProducerManager.hasConfigurationForMessageType(KafkaProducerManager.MessageType.ALARM)) {
            LOG.warn("No Kafka configuration found for alarms. Alarm synchronization will not be initialized.");
            return;
        }

        final Properties streamProperties = loadStreamsProperties();
        if (streamProperties == null || !streamProperties.containsKey("bootstrap.servers")) {
            LOG.warn("No bootstrap.servers configured for alarm synchronization. Skipping initialization.");
            return;
        }

        final StreamsBuilder builder = new StreamsBuilder();
        final GlobalKTable<String, byte[]> alarmBytesKtable = builder.globalTable(alarmTopic, Consumed.with(Serdes.String(), Serdes.ByteArray()),
                Materialized.as(ALARM_STORE_NAME));

        final Topology topology = builder.build();

        // Use the class-loader for the KStream class, since the kafka-client bundle
        // does not import the required classes from the kafka-streams bundle
        streams = Utils.runWithGivenClassLoader(() -> new KafkaStreams(topology, streamProperties), KStream.class.getClassLoader());

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
            if (startWithCleanState) {
                LOG.info("Performing stream state cleanup.");
                streams.cleanUp();
            }
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
    }

    public void destroy() {
        closed.set(true);
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(2, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                LOG.warn("Failed to shut down the alarm data sync scheduler.", e);
            }
        }
        if (streams != null) {
            streams.close(Duration.ofMinutes(2));
        }
    }

    @Override
    public synchronized AlarmSyncResults handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        if (!isReady()) {
            LOG.debug("Alarm store is not ready yet. Skipping synchronization.");
            return null;
        }

        LOG.debug("Performing alarm synchronization with ktable.");
        final AlarmSyncResults results;
        try {
            // Retrieve the map of alarms by reduction key from the ktable
            final Map<String, OpennmsModelProtos.Alarm> alarmsInKtableByReductionKey = getAlarms();

            final Set<String> reductionKeysInKtable = alarmsInKtableByReductionKey.keySet();

            // Use the given alarms and apply the filter (if any) to these
            // This represents the set of alarms that should be in the ktable at the given timestamp
            final List<OnmsAlarm> alarmsInDb = alarms.stream()
                    .filter(kafkaProducer::shouldForwardAlarm)
                    .collect(Collectors.toList());

            final Map<String, OnmsAlarm> alarmsInDbByReductionKey = alarmsInDb.stream()
                    .collect(Collectors.toMap(OnmsAlarm::getReductionKey, a -> a));
            final Set<String> reductionKeysInDb = alarmsInDbByReductionKey.keySet();

            // Grab a reference to the state tracker
            final AlarmCallbackStateTracker stateTracker = kafkaProducer.getAlarmCallbackStateTracker();

            // Push deletes for keys that are in the ktable, but not in the database
            final Set<String> reductionKeysNotInDb = Sets.difference(reductionKeysInKtable, reductionKeysInDb).stream()
                    // Only remove it if the alarm we have dates before the snapshot
                    .filter(reductionKey -> !stateTracker.wasAlarmWithReductionKeyUpdated(reductionKey))
                    .collect(Collectors.toSet());
            reductionKeysNotInDb.forEach(rkey -> kafkaProducer.handleDeletedAlarm((int)alarmsInKtableByReductionKey.get(rkey).getId(), rkey));

            // Push new entries for keys that are in the database, but not in the ktable
            final Set<String> reductionKeysNotInKtable = Sets.difference(reductionKeysInDb, reductionKeysInKtable).stream()
                    // Unless we've deleted the alarm after the snapshot time
                    .filter(reductionKey -> !stateTracker.wasAlarmWithReductionKeyDeleted(reductionKey))
                    .collect(Collectors.toSet());
            reductionKeysNotInKtable.forEach(rkey -> kafkaProducer.handleNewOrUpdatedAlarm(alarmsInDbByReductionKey.get(rkey)));

            // Handle Updates
            final Set<String> reductionKeysUpdated = new LinkedHashSet<>();
            final Set<String> commonReductionKeys = Sets.intersection(reductionKeysInKtable, reductionKeysInDb);
            commonReductionKeys.forEach(rkey -> {
                // Don't bother updating the alarm if the one we we have is more recent than the snapshot
                if (stateTracker.wasAlarmWithReductionKeyUpdated(rkey)) {
                    return;
                }

                final OnmsAlarm dbAlarm = alarmsInDbByReductionKey.get(rkey);
                final OpennmsModelProtos.Alarm.Builder mappedDbAlarm = protobufMapper.toAlarm(dbAlarm);
                final OpennmsModelProtos.Alarm alarmFromKtable = alarmsInKtableByReductionKey.get(rkey);
                final OpennmsModelProtos.Alarm.Builder alarmBuilderFromKtable =
                        alarmsInKtableByReductionKey.get(rkey).toBuilder();

                if ((suppressIncrementalAlarms && !alarmEqualityChecker.equalsExcludingOnBoth(mappedDbAlarm,
                        alarmBuilderFromKtable)) || (!suppressIncrementalAlarms && !Objects.equals(mappedDbAlarm.build(),
                        alarmFromKtable))) {
                    kafkaProducer.handleNewOrUpdatedAlarm(dbAlarm);
                    reductionKeysUpdated.add(rkey);
                }
            });

            results = new AlarmSyncResults(alarmsInKtableByReductionKey, alarmsInDb, alarmsInDbByReductionKey,
                    reductionKeysNotInKtable, reductionKeysNotInDb, reductionKeysUpdated);
        } catch (Exception e) {
            LOG.error("An error occurred while performing alarm synchronization with the ktable. Will try again on next callback.", e);
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done performing alarm synchronization with the ktable for {} alarms. Executed {} updates.",
                    results.getAlarmsInDb().size(),
                    results.getReductionKeysAdded().size()
                            + results.getReductionKeysDeleted().size()
                            + results.getReductionKeysUpdated().size());
            LOG.debug("Reduction keys added to ktable: {}", results.getReductionKeysAdded());
            LOG.debug("Reduction keys deleted from the ktable: {}", results.getReductionKeysDeleted());
            LOG.debug("Reduction keys updated in the ktable: {}", results.getReductionKeysUpdated());
        }

        return results;
    }



    private Properties loadStreamsProperties() throws IOException {
        final Properties streamsProperties = new Properties();
        // Default values
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-datasync");
        Path kafkaDir = Paths.get(System.getProperty("karaf.data"), "kafka");
        streamsProperties.put(StreamsConfig.STATE_DIR_CONFIG, kafkaDir.toString());
        // Copy common properties from client configuration, which should save the user from having to configure
        // properties for the stream client 99% of time
        Properties clientProperties = kafkaProducerManager.getConfigurationForMessageType(KafkaProducerManager.MessageType.ALARM);

        if (clientProperties == null || clientProperties.isEmpty()) {
            LOG.warn("No Kafka configuration found for alarms. Cannot load streams properties.");
            return null;
        }


        copyPropertyIfSet(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientProperties, streamsProperties);
        copyPropertyIfSet(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, clientProperties, streamsProperties);
        copyPropertyIfSet(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, clientProperties, streamsProperties);
        copyPropertyIfSet(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clientProperties, streamsProperties);

        // Now add all of the stream properties, overriding any of the properties inherited from the producer config
        final Dictionary<String, Object> properties = getConfigurationAdmin().getConfiguration(KAFKA_STREAMS_PID).getProperties();
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

        if (!streamsProperties.containsKey("bootstrap.servers")) {
            LOG.warn("No bootstrap.servers configured in alarm synchronization properties.");
            return null;
        }
        return streamsProperties;
    }

    private ConfigurationAdmin getConfigurationAdmin() {
        return kafkaProducerManager.getConfigAdmin();
    }

    private static void copyPropIfNonNull(String propName, Dictionary<String, Object> sourceMap, Properties targetMap) {
        Object propValue = sourceMap.get(propName);
        if (propValue != null) {
            targetMap.put(propName, propValue);
        }
    }

    private void copyPropertyIfSet(String propertyName, Properties source, Properties target) {
        String value = source.getProperty(propertyName);
        if (value != null && !value.trim().isEmpty()) {
            target.put(propertyName, value);
        }
    }


    public void setAlarmTopic(String alarmTopic) {
        this.alarmTopic = alarmTopic;
    }

    public void setAlarmSync(boolean alarmSync) {
        this.alarmSync = alarmSync;
    }

    @Override
    public void setStartWithCleanState(boolean startWithCleanState) {
        this.startWithCleanState = startWithCleanState;
    }

    private ReadOnlyKeyValueStore<String, byte[]> getAlarmTableNow() throws InvalidStateStoreException {
        return streams.store(StoreQueryParameters.fromNameAndType(ALARM_STORE_NAME, QueryableStoreTypes.keyValueStore()));
    }

    @Override
    public boolean isEnabled() {
        return kafkaProducer.isForwardingAlarms() && alarmSync;
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

    public void setSuppressIncrementalAlarms(boolean suppressIncrementalAlarms) {
        this.suppressIncrementalAlarms = suppressIncrementalAlarms;
    }
}
