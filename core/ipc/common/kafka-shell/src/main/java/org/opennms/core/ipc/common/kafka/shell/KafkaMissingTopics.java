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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.common.kafka.KafkaRpcConstants;
import org.opennms.core.ipc.common.kafka.KafkaTopicValidator;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.Identity;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import com.google.common.base.Strings;

@Command(scope = "opennms", name = "kafka-missing-topics", description = "List missing Kafka topics expected by the system.")
@Service
public class KafkaMissingTopics implements Action {

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference
    private Identity identity;

    @Option(name = "-t", aliases = "--timeout", description = "Connection timeout for Kafka Server")
    private int timeout;

    @Option(name = "-l", aliases = "--location", description = "Target Minion Location(s) to check (comma separated). Default: MINION", required = false)
    private String locations = "MINION";

    protected static final int DEFAULT_TIMEOUT = 5000;

    private static final Set<String> SINK_MODULES = Stream.of(
            "Events",
            "Heartbeat",
            "Trap",
            "DeviceConfig",
            "Syslog",
            "Telemetry-JTI",
            "Telemetry-NXOS"
    ).collect(Collectors.toSet());

    @Override
    public Object execute() throws Exception {
        Properties kafkaConfig = Utils.getKafkaConfig(identity, configAdmin, KafkaSinkConstants.KAFKA_TOPIC_PREFIX);
        if (kafkaConfig.isEmpty() || (kafkaConfig.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG) == null)) {
            kafkaConfig = Utils.getKafkaConfig(identity, configAdmin, KafkaRpcConstants.RPC_TOPIC_PREFIX);
        }
        if (kafkaConfig.isEmpty() || (kafkaConfig.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG) == null)) {
            System.out.println("Kafka not configured (checked Sink and RPC configs)");
            return null;
        }

        if (timeout <= 0) {
            String requestTimeout = kafkaConfig.getProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG);
            timeout = !Strings.isNullOrEmpty(requestTimeout) ? Integer.parseInt(requestTimeout) : DEFAULT_TIMEOUT;
        }
        kafkaConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, timeout);

        Set<String> requiredTopics = new HashSet<>();
        String instanceId = SystemInfoUtils.getInstanceId();

        String[] locationList = locations.split(",");

        for (String module : SINK_MODULES) {
            requiredTopics.add(String.format("%s.Sink.%s", instanceId, module));
        }

        requiredTopics.add(String.format("%s.rpc-response", instanceId));

        for (String loc : locationList) {
            requiredTopics.add(String.format("%s.%s.rpc-request", instanceId, loc.trim()));
        }

        requiredTopics.add(String.format("%s.twin.request", instanceId));
        requiredTopics.add(String.format("%s.twin.response", instanceId));
        for (String loc : locationList) {
            requiredTopics.add(String.format("%s.twin.response.%s", instanceId, loc.trim()));
        }

        try {
            Configuration prodConfig = configAdmin.getConfiguration("org.opennms.features.kafka.producer");
            Dictionary<String, Object> props = prodConfig.getProperties();

            requiredTopics.add(getProperty(props, "alarmTopic", "alarms"));
            requiredTopics.add(getProperty(props, "eventTopic", "events"));
            requiredTopics.add(getProperty(props, "nodeTopic", "nodes"));
            requiredTopics.add(getProperty(props, "metricTopic", "metrics"));
            requiredTopics.add(getProperty(props, "alarmFeedbackTopic", "alarmFeedback"));
            requiredTopics.add(getProperty(props, "topologyVertexTopic", "vertices"));
            requiredTopics.add(getProperty(props, "topologyEdgeTopic", "edges"));
        } catch (Exception e) {
            requiredTopics.add("alarms");
            requiredTopics.add("events");
            requiredTopics.add("nodes");
            requiredTopics.add("metrics");
            requiredTopics.add("alarmFeedback");
            requiredTopics.add("vertices");
            requiredTopics.add("edges");
        }

        KafkaTopicValidator validator = new KafkaTopicValidator(kafkaConfig, timeout);
        KafkaTopicValidator.ValidationResult result = validator.validateTopics(requiredTopics);

        if (result.getError() != null) {
            System.out.println("Error validating topics: " + result.getError());
            return null;
        }

        if (result.isValid()) {
            System.out.println("\nSUCCESS: All " + result.getRequiredTopics().size() + " required Kafka topics are present.");
        } else {
            System.out.println("\nMISSING TOPICS DETECTED!");
            System.out.println("The following topics are missing and must be created manually:");
            System.out.println("-----------------------------------------------------------");
            result.getMissingTopics().stream().sorted().forEach(System.out::println);
            System.out.println("-----------------------------------------------------------");
        }

        return null;
    }

    private String getProperty(Dictionary<String, Object> props, String key, String defaultValue) {
        if (props != null && props.get(key) != null) {
            return String.valueOf(props.get(key));
        }
        return defaultValue;
    }
}