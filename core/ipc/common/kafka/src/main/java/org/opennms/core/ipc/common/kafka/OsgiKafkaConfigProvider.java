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

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import org.osgi.service.cm.ConfigurationAdmin;

public class OsgiKafkaConfigProvider implements KafkaConfigProvider {

    private final String groupId;

    private final String pid;

    private final String commonPID;

    private final ConfigurationAdmin configAdmin;


    public OsgiKafkaConfigProvider(final String groupId, final String pid, final  ConfigurationAdmin configAdmin) {
        this(groupId, pid, configAdmin, null);
    }

    public OsgiKafkaConfigProvider(final String groupId, final String pid, final  ConfigurationAdmin configAdmin, final String commonPID) {
        this.groupId = groupId;
        this.pid = pid;
        this.configAdmin = configAdmin;
        this.commonPID = commonPID;
    }

    public OsgiKafkaConfigProvider(final String pid, final  ConfigurationAdmin configAdmin) {
        this(null, pid, configAdmin, null);
    }

    public OsgiKafkaConfigProvider(final String pid, final  ConfigurationAdmin configAdmin, final  String commonPID) {
        this(null, pid, configAdmin, commonPID);
    }

    @Override
    public synchronized Properties getProperties() {
        final Properties kafkaConfig = new Properties();
        // Only consumer properties require group.id
        if (groupId != null) {
            kafkaConfig.put("group.id", groupId);
        }

        // Retrieve all of the properties from the given pid
        try {
            final Dictionary<String, Object> properties = configAdmin.getConfiguration(pid).getProperties();
            if (properties != null && properties.get("bootstrap.servers") != null) {
                convertFromDictionaryToProperties(properties, kafkaConfig);
            } else {
                // Fallback to common pid
                final Dictionary<String, Object> commonPidProperties = configAdmin.getConfiguration(commonPID).getProperties();
                if (commonPidProperties != null && commonPidProperties.get("bootstrap.servers") != null) {
                    convertFromDictionaryToProperties(commonPidProperties, kafkaConfig);
                }
            }
            return kafkaConfig;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load properties", e);
        }
    }

    private void convertFromDictionaryToProperties(Dictionary<String, Object> properties, Properties kafkaConfig) {
        final Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            kafkaConfig.put(key, properties.get(key));
        }
    }
}
