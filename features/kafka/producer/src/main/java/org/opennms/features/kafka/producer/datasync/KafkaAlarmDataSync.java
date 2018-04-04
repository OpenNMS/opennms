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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.processor.FailOnInvalidTimestamp;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaAlarmDataSync implements AlarmDataStore {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAlarmDataSync.class);

    private static final String ALARM_STORE_NAME = "alarm_store";
    private static final String KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client";
    private static final String KAFKA_STREAMS_PID = "org.opennms.features.kafka.producer.streams";

    private final ConfigurationAdmin configAdmin;
    private String alarmTopic;
    private KafkaStreams streams;
    private long alarmStoreQueryTimeout = TimeUnit.MINUTES.toMillis(1);

    public KafkaAlarmDataSync(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void init() throws IOException {

        final Properties streamProperties = loadStreamsProperties();

        final StreamsBuilder builder = new StreamsBuilder();
        builder.table(alarmTopic, Consumed.with(Serdes.String(), Serdes.ByteArray()),
                Materialized.as(ALARM_STORE_NAME));
        final Topology topology = builder.build();

        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Use the class-loader for the current class, since the kafka-client bundle
            // does not import the required classes from the kafka-streams bundle
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            streams = new KafkaStreams(topology, streamProperties);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

        streams.setUncaughtExceptionHandler(
                (t, e) -> LOG.error(String.format("Stream error on thread: %s", t.getName()), e));
        try {
            streams.start();
        } catch (StreamsException | IllegalStateException e) {
            LOG.error(" Alarm datasync stream is not started, {}", e);
        }

    }

    public void destroy() {
        if (streams != null) {
            streams.close();
        }
    }

    private ReadOnlyKeyValueStore<String, byte[]> waitUntilStoreIsQueryable() throws InterruptedException {
        final long timeBeforeQuery = System.currentTimeMillis();
        while (true) {
            try {
                return streams.store(ALARM_STORE_NAME, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException ignored) {
                // store not yet ready for querying
                final long now = System.currentTimeMillis();
                if (now - timeBeforeQuery >= alarmStoreQueryTimeout) {
                    return null;
                }
                Thread.sleep(100);
            }
        }
    }

    @Override
    public CompletableFuture<ReadOnlyKeyValueStore<String, byte[]>> getAlarmDataStore() {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return waitUntilStoreIsQueryable();
            } catch (InterruptedException e) {
                LOG.error(" Exception while querying on Alarm store", e);
            }
            return null;
        });
    }

    private Properties loadStreamsProperties() throws IOException {

        final Properties streamsProperties = new Properties();
        // Copy kafka server info from client properties
        final Dictionary<String, Object> clientProperties = configAdmin.getConfiguration(KAFKA_CLIENT_PID)
                .getProperties();
        if (clientProperties != null) {
            streamsProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, clientProperties.get(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        final Dictionary<String, Object> properties = configAdmin.getConfiguration(KAFKA_STREAMS_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                streamsProperties.put(key, properties.get(key));
            }
        }
        streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-datasync");
        streamsProperties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndFailExceptionHandler.class);
        streamsProperties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FailOnInvalidTimestamp.class);
        if (streamsProperties.get(StreamsConfig.STATE_DIR_CONFIG) == null) {
            Path kafkaDir = Paths.get(System.getProperty("karaf.data"), "kafka");
            streamsProperties.put(StreamsConfig.STATE_DIR_CONFIG, kafkaDir.toString());
        }
        return streamsProperties;
    }

    public void setAlarmTopic(String alarmTopic) {
        this.alarmTopic = alarmTopic;
    }

    public void setAlarmStoreQueryTimeout(long alarmStoreQueryTimeout) {
        this.alarmStoreQueryTimeout = alarmStoreQueryTimeout;
    }

}
