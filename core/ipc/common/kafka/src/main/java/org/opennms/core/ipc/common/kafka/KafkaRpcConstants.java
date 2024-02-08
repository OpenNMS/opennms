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

import java.util.Properties;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handles all the configuration specific to RPC and some utils common to OpenNMS/Minion.
 */
public interface KafkaRpcConstants {

    static final Logger LOG = LoggerFactory.getLogger(KafkaRpcConstants.class);
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
            LOG.warn("Configured max buffered size at {} is invalid", MAX_BUFFER_SIZE_PROPERTY);
        }
        return maxBufferSize;
    }

}
