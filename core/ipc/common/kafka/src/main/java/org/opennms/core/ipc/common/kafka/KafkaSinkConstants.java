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

public interface KafkaSinkConstants {

    String KAFKA_TOPIC_PREFIX = "Sink";

    String KAFKA_CONFIG_PID = "org.opennms.core.ipc.sink.kafka";

    String KAFKA_COMMON_CONFIG_PID = "org.opennms.core.ipc.kafka";

    String KAFKA_CONFIG_CONSUMER_PID = KAFKA_CONFIG_PID + ".consumer";

    String KAFKA_CONFIG_SYS_PROP_PREFIX = KAFKA_CONFIG_PID + ".";

    String KAFKA_COMMON_CONFIG_SYS_PROP_PREFIX = KAFKA_COMMON_CONFIG_PID + ".";

    // Configurable max buffer size for kafka that should be less than 900KB.
    String MAX_BUFFER_SIZE_PROPERTY = "max.buffer.size";
    //By default, kafka allows 1MB buffer sizes, here message (content in sink-message.proto) is limited to 900KB.
    int DEFAULT_MAX_BUFFER_SIZE = 921600;

    // Configurable messageId cache config to specify number of messages and time to expire.
    String MESSAGEID_CACHE_CONFIG = "messageId.cache.config";
    // Default to 1000 messages (large) in 10 minute interval.
    String DEFAULT_MESSAGEID_CONFIG = "maximumSize=1000,expireAfterWrite=10m";
}
