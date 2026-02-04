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
package org.opennms.features.kafka.producer.shell;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.features.kafka.producer.KafkaProducerManager;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Karaf shell command that displays the status of all Kafka producer topics.
 * Shows topics with their configuration PID and existence status.
 * Supports per-topic Kafka configurations (different Kafka clusters per topic).
 */
@Command(scope = "opennms", name = "kafka-producer-topics", description = "Show status of all Kafka producer topics.")
@Service
public class KafkaProducerTopics implements Action {

    private static final int DEFAULT_TIMEOUT = 5000;

    // Status indicators
    private static final String STATUS_OK = "[OK]";
    private static final String STATUS_MISSING = "[MISSING]";

    // Producer configuration PID
    private static final String PRODUCER_CONFIG_PID = "org.opennms.features.kafka.producer";

    // Topic property names (from blueprint-kafka-producer.xml)
    private static final String EVENT_TOPIC_PROP = "eventTopic";
    private static final String ALARM_TOPIC_PROP = "alarmTopic";
    private static final String NODE_TOPIC_PROP = "nodeTopic";
    private static final String METRIC_TOPIC_PROP = "metricTopic";
    private static final String TOPOLOGY_VERTEX_TOPIC_PROP = "topologyVertexTopic";
    private static final String TOPOLOGY_EDGE_TOPIC_PROP = "topologyEdgeTopic";
    private static final String ALARM_FEEDBACK_TOPIC_PROP = "alarmFeedbackTopic";

    // Default topic names
    private static final String DEFAULT_EVENT_TOPIC = "events";
    private static final String DEFAULT_ALARM_TOPIC = "alarms";
    private static final String DEFAULT_NODE_TOPIC = "nodes";
    private static final String DEFAULT_METRIC_TOPIC = "metrics";
    private static final String DEFAULT_TOPOLOGY_VERTEX_TOPIC = "vertices";
    private static final String DEFAULT_TOPOLOGY_EDGE_TOPIC = "edges";
    private static final String DEFAULT_ALARM_FEEDBACK_TOPIC = "alarmFeedback";

    @Reference
    private ConfigurationAdmin configAdmin;

    @Option(name = "-t", aliases = "--timeout", description = "Connection timeout for Kafka Server (ms)")
    private int timeout;

    @Override
    public Object execute() throws Exception {
        System.out.println("\nKafka Producer Topics Status");
        System.out.println("============================\n");

        // Get producer topic configuration (optional - uses defaults if not present)
        Properties producerConfig = getProducerConfig();

        // Build map of topic property -> topic name (use defaults if config not present)
        Map<String, String> topicNames = new LinkedHashMap<>();
        topicNames.put(EVENT_TOPIC_PROP, getTopicName(producerConfig, EVENT_TOPIC_PROP, DEFAULT_EVENT_TOPIC));
        topicNames.put(ALARM_TOPIC_PROP, getTopicName(producerConfig, ALARM_TOPIC_PROP, DEFAULT_ALARM_TOPIC));
        topicNames.put(NODE_TOPIC_PROP, getTopicName(producerConfig, NODE_TOPIC_PROP, DEFAULT_NODE_TOPIC));
        topicNames.put(METRIC_TOPIC_PROP, getTopicName(producerConfig, METRIC_TOPIC_PROP, DEFAULT_METRIC_TOPIC));
        topicNames.put(TOPOLOGY_VERTEX_TOPIC_PROP, getTopicName(producerConfig, TOPOLOGY_VERTEX_TOPIC_PROP, DEFAULT_TOPOLOGY_VERTEX_TOPIC));
        topicNames.put(TOPOLOGY_EDGE_TOPIC_PROP, getTopicName(producerConfig, TOPOLOGY_EDGE_TOPIC_PROP, DEFAULT_TOPOLOGY_EDGE_TOPIC));
        topicNames.put(ALARM_FEEDBACK_TOPIC_PROP, getTopicName(producerConfig, ALARM_FEEDBACK_TOPIC_PROP, DEFAULT_ALARM_FEEDBACK_TOPIC));

        // Map topic property to its Kafka client PID
        Map<String, String> topicToPid = new LinkedHashMap<>();
        topicToPid.put(EVENT_TOPIC_PROP, getEffectivePid(KafkaProducerManager.EVENTS_KAFKA_CLIENT_PID));
        topicToPid.put(ALARM_TOPIC_PROP, getEffectivePid(KafkaProducerManager.ALARMS_KAFKA_CLIENT_PID));
        topicToPid.put(NODE_TOPIC_PROP, getEffectivePid(KafkaProducerManager.NODES_KAFKA_CLIENT_PID));
        topicToPid.put(METRIC_TOPIC_PROP, getEffectivePid(KafkaProducerManager.METRICS_KAFKA_CLIENT_PID));
        topicToPid.put(TOPOLOGY_VERTEX_TOPIC_PROP, getEffectivePid(KafkaProducerManager.TOPOLOGY_KAFKA_CLIENT_PID));
        topicToPid.put(TOPOLOGY_EDGE_TOPIC_PROP, getEffectivePid(KafkaProducerManager.TOPOLOGY_KAFKA_CLIENT_PID));
        topicToPid.put(ALARM_FEEDBACK_TOPIC_PROP, getEffectivePid(KafkaProducerManager.ALARM_FEEDBACK_KAFKA_CLIENT_PID));

        // Get unique PIDs and check connectivity for each
        Map<String, Set<String>> pidToTopics = new HashMap<>();
        Map<String, Boolean> pidConnectivity = new HashMap<>();

        for (String pid : topicToPid.values()) {
            if (pid != null && !pidToTopics.containsKey(pid)) {
                Properties kafkaConfig = getKafkaClientConfig(pid);
                if (kafkaConfig != null && kafkaConfig.containsKey(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG)) {
                    try {
                        Set<String> existingTopics = Utils.getTopics(kafkaConfig);
                        pidToTopics.put(pid, existingTopics);
                        pidConnectivity.put(pid, true);
                    } catch (Exception e) {
                        pidToTopics.put(pid, Set.of());
                        pidConnectivity.put(pid, false);
                    }
                }
            }
        }

        // Print Kafka connectivity status
        System.out.println("Kafka Connectivity (per configuration):");
        if (pidConnectivity.isEmpty()) {
            System.out.printf("  %-60s %s%n", "No Kafka client configuration found", STATUS_MISSING);
            return null;
        }
        for (Map.Entry<String, Boolean> entry : pidConnectivity.entrySet()) {
            String pid = entry.getKey();
            boolean connected = entry.getValue();
            String displayPid = pid.equals(KafkaProducerManager.GLOBAL_KAFKA_CLIENT_PID) ? pid + " (global)" : pid;
            System.out.printf("  %-60s %s%n", displayPid, connected ? STATUS_OK : STATUS_MISSING);
        }
        System.out.println();

        // Print producer topics status
        System.out.println("Producer Topics:");
        int existCount = 0;
        int totalCount = 0;

        for (Map.Entry<String, String> entry : topicNames.entrySet()) {
            String topicProp = entry.getKey();
            String topicName = entry.getValue();
            String pid = topicToPid.get(topicProp);

            Set<String> existingTopics = pid != null ? pidToTopics.getOrDefault(pid, Set.of()) : Set.of();
            boolean exists = existingTopics.contains(topicName);
            String status = exists ? STATUS_OK : STATUS_MISSING;

            System.out.printf("  %-50s %s%n", topicName, status);

            if (exists) {
                existCount++;
            }
            totalCount++;
        }

        // Summary
        System.out.println();
        System.out.printf("Summary: %d of %d topics exist%n", existCount, totalCount);

        return null;
    }

    private Properties getProducerConfig() {
        try {
            Configuration config = configAdmin.getConfiguration(PRODUCER_CONFIG_PID, null);
            if (config != null && config.getProperties() != null) {
                Properties props = new Properties();
                Dictionary<String, Object> dict = config.getProperties();
                var keys = dict.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    Object value = dict.get(key);
                    if (value != null) {
                        props.setProperty(key, value.toString());
                    }
                }
                return props;
            }
        } catch (IOException e) {
            // Configuration not found
        }
        return null;
    }

    private Properties getKafkaClientConfig(String pid) {
        try {
            Configuration config = configAdmin.getConfiguration(pid, null);
            if (config != null && config.getProperties() != null) {
                Properties props = new Properties();
                Dictionary<String, Object> dict = config.getProperties();
                var keys = dict.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    Object value = dict.get(key);
                    if (value != null) {
                        props.setProperty(key, value.toString());
                    }
                }

                // Set timeout
                int effectiveTimeout = timeout > 0 ? timeout : DEFAULT_TIMEOUT;
                props.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(effectiveTimeout));

                return props;
            }
        } catch (IOException e) {
            // Configuration not found
        }
        return null;
    }

    private String getEffectivePid(String topicSpecificPid) {
        // Check if topic-specific config exists with bootstrap.servers
        Properties topicConfig = getKafkaClientConfig(topicSpecificPid);
        if (topicConfig != null && topicConfig.containsKey(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG)) {
            return topicSpecificPid;
        }

        // Fall back to global config
        Properties globalConfig = getKafkaClientConfig(KafkaProducerManager.GLOBAL_KAFKA_CLIENT_PID);
        if (globalConfig != null && globalConfig.containsKey(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG)) {
            return KafkaProducerManager.GLOBAL_KAFKA_CLIENT_PID;
        }

        return null;
    }

    private String getTopicName(Properties config, String property, String defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        return config.getProperty(property, defaultValue);
    }
}
