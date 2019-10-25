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

@Command(scope = "opennms-kafka-sink", name = "topics", description = "List Sink Topics used by current system.")
@Service
public class KafkaSinkTopics implements Action {

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference
    private Identity identity;

    @Option(name = "-t", aliases = "--timeout", description = "Connection timeout for Kafka Server")
    private int timeout;

    private static final int DEFAULT_TIMEOUT = 5000;

    @Override
    public Object execute() throws Exception {
        Properties kafkaConfig = Utils.getKafkaConfig(identity, configAdmin, KafkaSinkConstants.KAFKA_TOPIC_PREFIX);
        // Pre-check for bootstrap.servers.
        if (kafkaConfig.isEmpty() || (kafkaConfig.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG) == null)) {
            System.out.println("Kafka not configured for Sink");
            return null;
        }
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
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
