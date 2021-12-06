/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

import java.util.Properties;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.PropertiesUtils;

/**
 * This handles all the configuration specific to RPC and some utils common to OpenNMS/Minion.
 */
public interface KafkaRpcConstants {

    String KAFKA_RPC_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka";
    String KAFKA_IPC_CONFIG_PID = "org.opennms.core.ipc.kafka";
    String KAFKA_RPC_CONFIG_SYS_PROP_PREFIX = KAFKA_RPC_CONFIG_PID + ".";
    String KAFKA_IPC_CONFIG_SYS_PROP_PREFIX = KAFKA_IPC_CONFIG_PID + ".";
    String RPC_TOPIC_PREFIX = "rpc";
    String RPC_REQUEST_TOPIC_NAME = "rpc-request";
    String RPC_RESPONSE_TOPIC_NAME = "rpc-response";
    String SINGLE_TOPIC_FOR_ALL_MODULES = "single-topic";
    //By default, kafka allows 1MB buffer sizes, here rpcContent (refer to proto/rpc.proto) is limited to 900KB to allow space for other parameters in proto file.
    int MAX_BUFFER_SIZE_CONFIGURED = 921600;
    String MAX_BUFFER_SIZE_PROPERTY = "max.buffer.size";
    String MAX_CONCURRENT_CALLS_PROPERTY = "max.concurrent.calls";
    String MAX_DURATION_BULK_HEAD = "max.wait.time";
    long DEFAULT_TTL_CONFIGURED = 20000;
    String DEFAULT_TTL_PROPERTY = "ttl";
    int MAX_CONCURRENT_CALLS = 1000;
    int MAX_WAIT_DURATION_BULK_HEAD = 100; // 100msec.


    // Calculate remaining buffer size for each chunk.
    static int getBufferSize(int messageSize, int maxBufferSize, int chunk) {
        int bufferSize = messageSize;
        if (messageSize > maxBufferSize) {
            int remaining = messageSize - chunk * maxBufferSize;
            bufferSize = (remaining > maxBufferSize) ? maxBufferSize : remaining;
        }
        return bufferSize;
    }

    // Retrieve max buffer size from karaf configuration properties.
    static int getMaxBufferSize(Properties properties) {
        int maxBufferSize = MAX_BUFFER_SIZE_CONFIGURED;
        try {
            maxBufferSize = Math.min(MAX_BUFFER_SIZE_CONFIGURED, PropertiesUtils.getProperty(properties, MAX_BUFFER_SIZE_PROPERTY, MAX_BUFFER_SIZE_CONFIGURED));
        } catch (NumberFormatException e) {
            // pass
        }
        return maxBufferSize;
    }

}
