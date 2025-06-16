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
import org.opennms.distributed.core.api.SystemType;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "kafka-rpc-topics", description = "List RPC topics used by current system.")
@Service
public class KafkaRpcTopics implements Action {

    @Reference
    private Identity identity;

    @Reference
    private ConfigurationAdmin configAdmin;

    @Option(name = "-t", aliases = "--timeout", description = "Connection timeout for Kafka Server")
    private int timeout;

    @Override
    public Object execute() throws Exception {
        Properties kafkaConfig = Utils.getKafkaConfig(identity, configAdmin, KafkaRpcConstants.RPC_TOPIC_PREFIX);
        // Pre-check for bootstrap.servers.
        if (kafkaConfig.isEmpty() || (kafkaConfig.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG) == null)) {
            System.out.println("Kafka not configured for RPC");
            return null;
        }
        if (timeout <= 0) {
            final String requestTimeoutMsConfig = kafkaConfig.getProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG);
            if (!Strings.isNullOrEmpty(requestTimeoutMsConfig)) {
                timeout = Integer.parseInt(requestTimeoutMsConfig);
            } else {
                timeout = KafkaSinkTopics.DEFAULT_TIMEOUT;
            }
        }
        kafkaConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, timeout);
        try {
            Set<String> topics = Utils.getTopics(kafkaConfig);
            if (!topics.isEmpty()) {
                final String opennmsInstance = SystemInfoUtils.getInstanceId();
                String locationSuffix = "";
                if (identity.getType().equals(SystemType.Minion.name())) {
                    locationSuffix = "." + identity.getLocation();
                }
                final String topicName = opennmsInstance + locationSuffix;
                Set<String> rpcRequestTopics = topics.stream()
                        .filter(topic -> topic.contains(topicName))
                        .filter(topic -> topic.contains(RPC_REQUEST_TOPIC_NAME)).collect(Collectors.toSet());
                if (!rpcRequestTopics.isEmpty()) {
                    System.out.println("\nRPC Request Topics:");
                    rpcRequestTopics.forEach(System.out::println);
                } else {
                    System.out.println("No RPC Request topics found.");
                }
                Set<String> rpcResponseTopics = topics.stream()
                        .filter(topic -> topic.contains(opennmsInstance))
                        .filter(topic -> topic.contains(RPC_RESPONSE_TOPIC_NAME)).collect(Collectors.toSet());
                if (!rpcRequestTopics.isEmpty()) {
                    System.out.println("\nRPC Response Topics:");
                    rpcResponseTopics.forEach(System.out::println);
                } else {
                    System.out.println("No RPC Response topics found.");
                }
                return null;
            }
            System.out.println("No topics listed for Kafka RPC");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
