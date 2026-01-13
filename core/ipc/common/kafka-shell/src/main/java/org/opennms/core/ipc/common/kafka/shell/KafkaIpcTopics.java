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
package org.opennms.core.ipc.common.kafka.shell;

import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME;
import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.KAFKA_TOPIC_PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.common.kafka.KafkaRpcConstants;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.common.base.Strings;

/**
 * Karaf shell command that displays the status of all Kafka IPC topics.
 * Shows topics grouped by category (RPC, Sink, Twin) with their existence status.
 * On Core, queries MonitoringLocationDao to show expected topics for each location.
 * On Minion, shows only the own location's topics.
 */
@Command(scope = "opennms", name = "kafka-ipc-topics", description = "Show status of all Kafka IPC topics.")
@Service
public class KafkaIpcTopics implements Action {

    private static final int DEFAULT_TIMEOUT = 5000;

    // Status indicators
    private static final String STATUS_OK = "[OK]";
    private static final String STATUS_MISSING = "[MISSING]";           // Required topic missing (problem)
    private static final String STATUS_NOT_CONFIGURED = "[NOT CONFIGURED]"; // Optional topic not present (normal)

    // Required sink module (must exist)
    private static final String REQUIRED_SINK_MODULE = "Heartbeat";  // HeartbeatModule.MODULE_ID

    // Optional sink modules (only needed when feature is enabled)
    // Module IDs match their respective SinkModule implementations
    private static final List<String> OPTIONAL_SINK_MODULES = List.of(
            "Events",      // EventSinkModule.MODULE_ID (features/events/sink/dispatcher)
            "Syslog",      // SyslogSinkModule.MODULE_ID (features/events/syslog)
            "Trap",        // TrapSinkModule.getId() (features/events/traps)
            "DeviceConfig" // DeviceConfigSinkModuleImpl.MODULE_ID (features/device-config/sink/module)
    );

    // Telemetry module ID prefix
    private static final String TELEMETRY_MODULE_PREFIX = "Telemetry-";

    // Known telemetry queue names (from telemetryd-configuration.xml)
    private static final List<String> KNOWN_TELEMETRY_QUEUES = List.of(
            "Netflow-5", "Netflow-9", "IPFIX", "SFlow",
            "JTI", "NXOS", "BMP", "OpenConfig", "Graphite"
    );

    @Reference
    private Identity identity;

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference(optional = true)
    private MonitoringLocationDao monitoringLocationDao;

    @Option(name = "-t", aliases = "--timeout", description = "Connection timeout for Kafka Server (ms)")
    private int timeout;

    @Override
    public Object execute() throws Exception {
        Properties kafkaConfig = Utils.getKafkaConfig(identity, configAdmin, KafkaRpcConstants.RPC_TOPIC_PREFIX);

        if (kafkaConfig.isEmpty() || kafkaConfig.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG) == null) {
            System.out.println("Kafka not configured (bootstrap.servers not set)");
            return null;
        }

        if (timeout <= 0) {
            String requestTimeoutMsConfig = kafkaConfig.getProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG);
            if (!Strings.isNullOrEmpty(requestTimeoutMsConfig)) {
                timeout = Integer.parseInt(requestTimeoutMsConfig);
            } else {
                timeout = DEFAULT_TIMEOUT;
            }
        }
        kafkaConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, timeout);

        System.out.println("\nKafka IPC Topics Status");
        System.out.println("=======================\n");

        // Kafka connectivity check
        System.out.println("Kafka Connectivity:");
        Set<String> existingTopics;
        try {
            existingTopics = Utils.getTopics(kafkaConfig);
            System.out.printf("  %-60s %s%n", "Connecting to Kafka", STATUS_OK);
        } catch (Exception e) {
            System.out.printf("  %-60s %s%n", "Connecting to Kafka", STATUS_MISSING);
            System.out.println("  Error: " + e.getMessage());
            return null;
        }
        System.out.println();

        String instanceId = SystemInfoUtils.getInstanceId();

        // Get locations
        List<String> locations = getLocations();

        int totalMissing = 0;

        // RPC Topics
        totalMissing += printRpcTopics(existingTopics, instanceId, locations);

        // Sink Topics
        totalMissing += printSinkTopics(existingTopics, instanceId);

        // Twin Topics
        totalMissing += printTwinTopics(existingTopics, instanceId, locations);

        // Summary
        System.out.println();
        if (totalMissing > 0) {
            System.out.println("Summary: " + totalMissing + " required topic(s) missing");
        } else {
            System.out.println("Summary: All required topics exist");
        }

        return null;
    }

    private List<String> getLocations() {
        List<String> locations = new ArrayList<>();

        if (monitoringLocationDao != null) {
            // On Core: query all locations from database
            try {
                List<OnmsMonitoringLocation> allLocations = monitoringLocationDao.findAll();
                locations = allLocations.stream()
                        .map(OnmsMonitoringLocation::getLocationName)
                        .sorted()
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.out.println("Warning: Could not query locations: " + e.getMessage());
            }
        }

        // On Minion or if DAO query failed: use own location
        if (locations.isEmpty()) {
            locations.add(identity.getLocation());
        }

        return locations;
    }

    private int printRpcTopics(Set<String> existingTopics, String instanceId, List<String> locations) {
        int missingCount = 0;

        // Global RPC response topic (required)
        System.out.println("RPC Topics (required):");
        String responseTopic = instanceId + "." + RPC_RESPONSE_TOPIC_NAME;
        boolean responseExists = existingTopics.contains(responseTopic);
        printRequiredTopicLine(responseTopic, responseExists);
        if (!responseExists) {
            missingCount++;
        }
        System.out.println();

        // Per-location RPC request topics (required)
        System.out.println("RPC Topics (per-location - required):");
        for (String location : locations) {
            // Skip Default location - Kafka IPC not used for Core-only operations
            if (MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID.equals(location)) {
                continue;
            }

            String topic = instanceId + "." + location + "." + RPC_REQUEST_TOPIC_NAME;
            boolean exists = existingTopics.contains(topic);
            printRequiredTopicLine(topic, exists);
            if (!exists) {
                missingCount++;
            }
        }
        System.out.println();
        return missingCount;
    }

    private int printSinkTopics(Set<String> existingTopics, String instanceId) {
        int missingCount = 0;
        String sinkPrefix = instanceId + "." + KAFKA_TOPIC_PREFIX + ".";

        // Section 1: Required Sink Topic (Heartbeat)
        System.out.println("Sink Topics (required):");
        String heartbeatTopic = sinkPrefix + REQUIRED_SINK_MODULE;
        boolean heartbeatExists = existingTopics.contains(heartbeatTopic);
        printRequiredTopicLine(heartbeatTopic, heartbeatExists);
        if (!heartbeatExists) {
            missingCount++;
        }
        System.out.println();

        // Section 2: Optional Sink Topics (core features)
        System.out.println("Sink Topics (optional - feature dependent):");
        for (String moduleId : OPTIONAL_SINK_MODULES) {
            String topic = sinkPrefix + moduleId;
            boolean exists = existingTopics.contains(topic);
            printOptionalTopicLine(topic, exists);
        }
        System.out.println();

        // Section 3: Telemetry Sink Topics (optional)
        System.out.println("Sink Topics (telemetry - optional):");
        System.out.println("  Note: These are default queue names, if queue names were customized in");
        System.out.println("        telemetryd-configuration.xml, below topics may differ");
        String telemetryPrefix = sinkPrefix + TELEMETRY_MODULE_PREFIX;

        // Show known telemetry queues
        for (String queueName : KNOWN_TELEMETRY_QUEUES) {
            String topic = telemetryPrefix + queueName;
            boolean exists = existingTopics.contains(topic);
            printOptionalTopicLine(topic, exists);
        }

        // Also show any other telemetry topics that exist but aren't in our known list
        existingTopics.stream()
                .filter(t -> t.startsWith(telemetryPrefix))
                .filter(t -> KNOWN_TELEMETRY_QUEUES.stream()
                        .noneMatch(q -> t.equals(telemetryPrefix + q)))
                .sorted()
                .forEach(t -> printOptionalTopicLine(t, true));
        System.out.println();

        return missingCount;
    }

    private int printTwinTopics(Set<String> existingTopics, String instanceId, List<String> locations) {
        int missingCount = 0;

        // Global Twin topics (required)
        System.out.println("Twin Topics (required):");
        String twinRequest = instanceId + ".twin.request";
        String twinResponse = instanceId + ".twin.response";

        boolean requestExists = existingTopics.contains(twinRequest);
        boolean responseExists = existingTopics.contains(twinResponse);

        printRequiredTopicLine(twinRequest, requestExists);
        printRequiredTopicLine(twinResponse, responseExists);

        if (!requestExists) {
            missingCount++;
        }
        if (!responseExists) {
            missingCount++;
        }
        System.out.println();

        // Per-location Twin response topics (required)
        System.out.println("Twin Topics (per-location - required):");
        for (String location : locations) {
            // Skip Default location - Kafka IPC not used for Core-only operations
            if (MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID.equals(location)) {
                continue;
            }

            String topic = instanceId + ".twin.response." + location;
            boolean exists = existingTopics.contains(topic);
            printRequiredTopicLine(topic, exists);
            if (!exists) {
                missingCount++;
            }
        }
        return missingCount;
    }

    private void printRequiredTopicLine(String topic, boolean exists) {
        String status = exists ? STATUS_OK : STATUS_MISSING;
        System.out.printf("  %-60s %s%n", topic, status);
    }

    private void printOptionalTopicLine(String topic, boolean exists) {
        String status = exists ? STATUS_OK : STATUS_NOT_CONFIGURED;
        System.out.printf("  %-60s %s%n", topic, status);
    }
}
