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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaAlarmDataView {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAlarmDataView.class);

    private static final String ALARM_STORE_NAME = "alarm_store";
    private static final String KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client";

    private final ConfigurationAdmin configAdmin;
    private String alarmTopic;
    private KafkaStreams streams;
    private KTable<String, byte[]> alarmStreamTable;

    public KafkaAlarmDataView(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void init() throws IOException {

        final Properties streamProperties = new Properties();

        final Dictionary<String, Object> properties = configAdmin.getConfiguration(KAFKA_CLIENT_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                streamProperties.put(key, properties.get(key));
            }
        }
        streamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-datasync");

        final StreamsBuilder builder = new StreamsBuilder();

        alarmStreamTable = builder.table(alarmTopic, Consumed.with(Serdes.String(), Serdes.ByteArray()),
                Materialized.as(ALARM_STORE_NAME));
        final Topology topology = builder.build();

        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            streams = new KafkaStreams(topology, streamProperties);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

        streams.setUncaughtExceptionHandler(
                (t, e) -> LOG.error(String.format("Stream error on thread: %s", t.getName()), e));
        streams.start();

    }

    public void setAlarmTopic(String alarmTopic) {
        this.alarmTopic = alarmTopic;
    }

    public void destroy() {
        if (streams != null) {
            streams.close();
        }
    }

    public KTable<String, byte[]> getAlarmStreamTable() {
        return alarmStreamTable;
    }

}
