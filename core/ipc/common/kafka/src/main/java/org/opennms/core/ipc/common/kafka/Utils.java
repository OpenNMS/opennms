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
                    KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX : KafkaRpcConstants.KAFKA_CONFIG_SYS_PROP_PREFIX;
            OnmsKafkaConfigProvider kafkaConfigProvider = new OnmsKafkaConfigProvider(sysPropPrefix);
            return kafkaConfigProvider.getProperties();
        } else {
            String pid = null;
            if(identity.getType().equals(SystemType.Minion.name())) {
                pid = type.equals(KafkaSinkConstants.KAFKA_TOPIC_PREFIX) ?
                        KafkaSinkConstants.KAFKA_CONFIG_PID : KafkaRpcConstants.KAFKA_CONFIG_PID;
            } else {
                //For sentinel, connect with consumer pid.
                pid = type.equals(KafkaSinkConstants.KAFKA_TOPIC_PREFIX) ?
                        KafkaSinkConstants.KAFKA_CONFIG_CONSUMER_PID : KafkaRpcConstants.KAFKA_CONFIG_PID;
            }
            OsgiKafkaConfigProvider kafkaConfigProvider = new OsgiKafkaConfigProvider(pid, configAdmin);
            return kafkaConfigProvider.getProperties();
        }
    }

}
