/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.common.kafka.shell;

import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME;
import static org.springframework.jdbc.support.DatabaseStartupValidator.DEFAULT_TIMEOUT;

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

@Command(scope = "opennms-kafka-rpc", name = "topics", description = "List RPC topics used by current system.")
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
            timeout = DEFAULT_TIMEOUT;
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
