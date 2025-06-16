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

import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.opennms.distributed.core.api.Identity;
import org.opennms.distributed.core.api.SystemType;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.KAFKA_IPC_CONFIG_PID;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.KAFKA_IPC_CONFIG_SYS_PROP_PREFIX;

public class Utils {
    // HACK: When defining key.deserializer/value.deserializer classes, the kafka client library
    // tries to instantiate them by using the ClassLoader returned by Thread.currentThread().getContextClassLoader() if defined.
    // As that ClassLoader does not know anything about that classes a ClassNotFoundException is thrown
    // By setting the ClassLoader to null, the BundleContextClassLoader of the kafka client library is used instead,
    // which can instantiate those classes more likely (depending on Import/DynamicImport-Package definitions)
    public static <T> T runWithNullContextClassLoader(final Supplier<T> supplier) {
       return runWithGivenClassLoader(supplier, null);
    }

    public static <T> T runWithGivenClassLoader(final Supplier<T> supplier, ClassLoader classLoader) {
        Objects.requireNonNull(supplier);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return supplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static Set<String> getTopics(Properties kafkaConfig) throws ExecutionException, InterruptedException {
        try (AdminClient client = Utils.runWithGivenClassLoader(() ->
                AdminClient.create(kafkaConfig), KafkaAdminClient.class.getClassLoader())) {
            ListTopicsResult listTopicsResult = client.listTopics();
            return listTopicsResult.names().get();
        }
    }

    public static Properties getKafkaConfig(Identity identity, ConfigurationAdmin configAdmin, String type) {
        if(identity.getType().equals(SystemType.OpenNMS.name())) {
            String sysPropPrefix = type.equals(KafkaSinkConstants.KAFKA_TOPIC_PREFIX) ?
                    KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX : KafkaRpcConstants.KAFKA_RPC_CONFIG_SYS_PROP_PREFIX;
            OnmsKafkaConfigProvider kafkaConfigProvider = new OnmsKafkaConfigProvider(sysPropPrefix, KAFKA_IPC_CONFIG_SYS_PROP_PREFIX);
            return kafkaConfigProvider.getProperties();
        } else {
            String pid = null;
            if(identity.getType().equals(SystemType.Minion.name())) {
                pid = type.equals(KafkaSinkConstants.KAFKA_TOPIC_PREFIX) ?
                        KafkaSinkConstants.KAFKA_CONFIG_PID : KafkaRpcConstants.KAFKA_RPC_CONFIG_PID;
            } else {
                //For sentinel, connect with consumer pid.
                pid = type.equals(KafkaSinkConstants.KAFKA_TOPIC_PREFIX) ?
                        KafkaSinkConstants.KAFKA_CONFIG_CONSUMER_PID : KafkaRpcConstants.KAFKA_RPC_CONFIG_PID;
            }
            OsgiKafkaConfigProvider kafkaConfigProvider = new OsgiKafkaConfigProvider(pid, configAdmin, KAFKA_IPC_CONFIG_PID);
            return kafkaConfigProvider.getProperties();
        }
    }

}
