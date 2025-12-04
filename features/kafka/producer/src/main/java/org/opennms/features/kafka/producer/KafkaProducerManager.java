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
package org.opennms.features.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.opennms.core.ipc.common.kafka.Utils;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Map;
import java.util.Dictionary;
import java.util.Properties;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaProducerManager {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerManager.class);

    public static final String GLOBAL_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client";
    public static final String EVENTS_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client.events";
    public static final String ALARMS_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client.alarms";
    public static final String METRICS_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client.metrics";
    public static final String NODES_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client.nodes";
    public static final String TOPOLOGY_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client.topology";
    public static final String ALARM_FEEDBACK_KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client.alarmFeedback";

    public enum MessageType {
        EVENT,
        ALARM,
        NODE,
        METRIC,
        TOPOLOGY_VERTEX,
        TOPOLOGY_EDGE,
        ALARM_FEEDBACK
    }

    private final ConfigurationAdmin configAdmin;
    private final Map<MessageType, Producer<byte[], byte[]>> messageTypeToProducerMap = new ConcurrentHashMap<>();
    private final Map<String, Producer<byte[], byte[]>> pidToProducerMap = new ConcurrentHashMap<>();

    public KafkaProducerManager(ConfigurationAdmin configAdmin) {
        this.configAdmin = Objects.requireNonNull(configAdmin);
    }

    public void init() {
        LOG.info("Initializing KafkaProducerManager");
        for (MessageType messageType : MessageType.values()) {
            try {
                getProducerForMessageType(messageType);
                LOG.debug("Successfully initialized producer for message type: {}", messageType);
            } catch (Exception e) {
                LOG.warn("Failed to initialize producer for message type: {}. It will be initialized lazily.", messageType, e);
            }
        }
    }

    public void destroy() {
        LOG.info("Destroying KafkaProducerManager");
        pidToProducerMap.values().forEach(producer -> {
            try {
                producer.close();
            } catch (Exception e) {
                LOG.warn("Error closing Kafka producer", e);
            }
        });
        pidToProducerMap.clear();
        messageTypeToProducerMap.clear();
    }

    public Producer<byte[], byte[]> getProducerForMessageType(MessageType messageType) {
        return messageTypeToProducerMap.computeIfAbsent(messageType, this::createProducerForMessageType);
    }

    private Producer<byte[], byte[]> createProducerForMessageType(MessageType messageType) {
        String pid = determinePidForMessageType(messageType);
        return getOrCreateProducerForPid(pid);
    }

    private String determinePidForMessageType(MessageType messageType) {
        switch (messageType) {
            case EVENT:
                return getEffectivePid(EVENTS_KAFKA_CLIENT_PID);
            case ALARM:
                return getEffectivePid(ALARMS_KAFKA_CLIENT_PID);
            case NODE:
                return getEffectivePid(NODES_KAFKA_CLIENT_PID);
            case METRIC:
                return getEffectivePid(METRICS_KAFKA_CLIENT_PID);
            case TOPOLOGY_VERTEX:
            case TOPOLOGY_EDGE:
                return getEffectivePid(TOPOLOGY_KAFKA_CLIENT_PID);
            case ALARM_FEEDBACK:
                return getEffectivePid(ALARM_FEEDBACK_KAFKA_CLIENT_PID);
            default:
                return GLOBAL_KAFKA_CLIENT_PID;
        }
    }

    private String getEffectivePid(String topicSpecificPid) {
        try {
            var config = configAdmin.getConfiguration(topicSpecificPid, null);
            if (config != null && config.getProperties() != null && !config.getProperties().isEmpty()) {
                Dictionary<String, Object> properties = config.getProperties();
                if (properties.get("bootstrap.servers") != null) {
                    LOG.debug("bootstrap.server found  for PID: {}", topicSpecificPid);
                    return topicSpecificPid;
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to check configuration for PID: {}", topicSpecificPid, e);
        }
        LOG.debug("Falling back to global configuration for PID: {}", GLOBAL_KAFKA_CLIENT_PID);
        return GLOBAL_KAFKA_CLIENT_PID;
    }

    private Producer<byte[], byte[]> getOrCreateProducerForPid(String pid) {
        return pidToProducerMap.computeIfAbsent(pid, this::initializeProducerForPid);
    }

    private Producer<byte[], byte[]> initializeProducerForPid(String pid) {
        try {
            final Properties producerConfig = getConfigurationForPid(pid);
            producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
            producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");


            LOG.info("Creating Kafka producer for PID: {} with bootstrap.servers: {}",
                    pid, producerConfig.getProperty("bootstrap.servers", "not configured"));

            return Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(producerConfig),
                    KafkaProducer.class.getClassLoader());

        } catch (Exception e) {
            LOG.error("Failed to create Kafka producer for PID: {}", pid, e);
            throw new RuntimeException("Failed to create Kafka producer for PID: " + pid, e);
        }
    }

    public Properties getConfigurationForMessageType(MessageType messageType) {
        String pid = determinePidForMessageType(messageType);
        return getConfigurationForPid(pid);
    }


    public Properties getConfigurationForPid(String pid) {
        try {
            final Properties config = new Properties();
            final Dictionary<String, Object> properties = configAdmin.getConfiguration(pid).getProperties();

            if (properties != null) {
                final Enumeration<String> keys = properties.keys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    Object value = properties.get(key);
                    if (value != null) {
                        config.put(key, value);
                    }
                }
            }
            return config;
        } catch (IOException e) {
            LOG.warn("Failed to load configuration for PID: {}, using empty properties", pid, e);
            return new Properties();
        }
    }

    public ConfigurationAdmin getConfigAdmin() {
        return configAdmin;
    }
}
