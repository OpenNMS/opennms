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
package org.opennms.core.ipc.common.kafka;

import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.SystemInfoUtils;

public class OnmsKafkaConfigProvider implements KafkaConfigProvider {

    private final String kafkaSysPropPrefix;

    private final String commonKafkaSysPropPrefix;

    public OnmsKafkaConfigProvider(String kafkaSysPropPrefix) {
        this.kafkaSysPropPrefix = kafkaSysPropPrefix;
        commonKafkaSysPropPrefix = null;
    }

    public OnmsKafkaConfigProvider(String kafkaSysPropPrefix, String commonKafkaSysPropPrefix) {
        this.kafkaSysPropPrefix = kafkaSysPropPrefix;
        this.commonKafkaSysPropPrefix = commonKafkaSysPropPrefix;
    }
    @Override
    public Properties getProperties() {
        final Properties kafkaConfig = new Properties();
        kafkaConfig.put("group.id", SystemInfoUtils.getInstanceId());
        Properties config = loadKafkaConfigFromSysPropPrefix(kafkaSysPropPrefix);
        if (config.containsKey("bootstrap.servers")) {
            kafkaConfig.putAll(config);
        } else if (commonKafkaSysPropPrefix != null) {
            Properties fallbackConfig = loadKafkaConfigFromSysPropPrefix(commonKafkaSysPropPrefix);
            kafkaConfig.putAll(fallbackConfig);
        }
        return kafkaConfig;
    }

    private Properties loadKafkaConfigFromSysPropPrefix(String kafkaSysPropPrefix) {
        final Properties kafkaConfig = new Properties();
        // Find all of the system properties that start with provided prefix (kafkaSysPropPrefix)
        // and add them to the config.
        // See https://kafka.apache.org/0100/documentation.html#newconsumerconfigs for the list of supported properties
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null || !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String) keyAsObject;
            if (key.length() > kafkaSysPropPrefix.length()
                    && key.startsWith(kafkaSysPropPrefix)) {
                final String kafkaConfigKey = key.substring(kafkaSysPropPrefix.length());
                kafkaConfig.put(kafkaConfigKey, entry.getValue());
            }
        }
        return kafkaConfig;
    }
}
