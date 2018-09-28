/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.server.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants;
import org.osgi.service.cm.ConfigurationAdmin;

public class OsgiKafkaConfigProvider implements KafkaConfigProvider {

    private final String groupId;

    private final ConfigurationAdmin configAdmin;

    public OsgiKafkaConfigProvider(final String groupId, final ConfigurationAdmin configAdmin) {
        this.groupId = Objects.requireNonNull(groupId);
        this.configAdmin = Objects.requireNonNull(configAdmin);
    }

    @Override
    public synchronized Properties getProperties() {
        final Properties kafkaConfig = new Properties();
        kafkaConfig.put("group.id", groupId);

        // Retrieve all of the properties from org.opennms.core.ipc.sink.kafka.consumer.cfg
        try {
            final Dictionary<String, Object> properties = configAdmin.getConfiguration(KafkaSinkConstants.KAFKA_CONFIG_CONSUMER_PID).getProperties();
            if (properties != null) {
                final Enumeration<String> keys = properties.keys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    kafkaConfig.put(key, properties.get(key));
                }
            }
            return kafkaConfig;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load properties", e);
        }
    }
}
