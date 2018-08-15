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

import java.util.Map;
import java.util.Properties;

import org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants;
import org.opennms.core.utils.SystemInfoUtils;

public class OnmsKafkaConfigProvider implements KafkaConfigProvider {

    @Override
    public Properties getProperties() {
        final Properties kafkaConfig = new Properties();
        kafkaConfig.put("group.id", SystemInfoUtils.getInstanceId());

        // Find all of the system properties that start with
        // 'org.opennms.core.ipc.sink.kafka.'
        // and add them to the config. See
        // https://kafka.apache.org/0100/documentation.html#newconsumerconfigs
        // for the list of supported properties
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null || !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String) keyAsObject;
            if (key.length() > KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX.length()
                    && key.startsWith(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX)) {
                final String kafkaConfigKey = key.substring(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX.length());
                kafkaConfig.put(kafkaConfigKey, entry.getValue());
            }
        }
        return kafkaConfig;
    }
}
