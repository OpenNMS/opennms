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

import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.KAFKA_TOPIC_PREFIX;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.Identity;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "kafka-sink-topics", description = "List Sink Topics used by current system.")
@Service
public class KafkaSinkTopics implements Action {

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference
    private Identity identity;

    @Option(name = "-t", aliases = "--timeout", description = "Connection timeout for Kafka Server")
    private int timeout;

    protected static final int DEFAULT_TIMEOUT = 5000;

    @Override
    public Object execute() throws Exception {
        Properties kafkaConfig = Utils.getKafkaConfig(identity, configAdmin, KafkaSinkConstants.KAFKA_TOPIC_PREFIX);
        // Pre-check for bootstrap.servers.
        if (kafkaConfig.isEmpty() || (kafkaConfig.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG) == null)) {
            System.out.println("Kafka not configured for Sink");
            return null;
        }
        if (timeout <= 0) {
            final String requestTimeoutMsConfig = kafkaConfig.getProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG);
            if (!Strings.isNullOrEmpty(requestTimeoutMsConfig)) {
                timeout = Integer.parseInt(requestTimeoutMsConfig);
            } else {
                timeout = DEFAULT_TIMEOUT;
            }
        }
        kafkaConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, timeout);
        try {
            Set<String> topics = Utils.getTopics(kafkaConfig);
            if (!topics.isEmpty()) {
                String opennmsInstance = SystemInfoUtils.getInstanceId();
                Set<String> sinkTopics = topics.stream()
                        .filter(topic -> topic.contains(opennmsInstance))
                        .filter(topic -> topic.contains(KAFKA_TOPIC_PREFIX)).collect(Collectors.toSet());
                if (!sinkTopics.isEmpty()) {
                    System.out.println("\nSink topics:");
                    sinkTopics.forEach(System.out::println);
                    return null;
                }
            }
            System.out.println("No topics listed for Kafka Sink");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
