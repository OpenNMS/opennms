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

import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME;
import org.opennms.core.utils.SystemInfoUtils;

public class KafkaTopicProvider {

    private static final String TOPIC_NAME_AT_LOCATION = "%s.%s.%s";
    private static final String TOPIC_NAME_WITHOUT_LOCATION = "%s.%s";
    private static final String TOPIC_NAME_WITH_MODULE = "%s.%s.%s";
    private static final String TOPIC_NAME_WITH_MODULE_AND_LOCATION = "%s.%s.%s.%s";
    private final boolean singleTopic;

    public KafkaTopicProvider(boolean singleTopic) {
        this.singleTopic = singleTopic;
    }

    public KafkaTopicProvider() {
        this.singleTopic = true;
    }

    public String getRequestTopicAtLocation(String location, String module) {
        if (singleTopic) {
            return String.format(TOPIC_NAME_AT_LOCATION, SystemInfoUtils.getInstanceId(), location, RPC_REQUEST_TOPIC_NAME);
        }
        return String.format(TOPIC_NAME_WITH_MODULE_AND_LOCATION, SystemInfoUtils.getInstanceId(), location, RPC_REQUEST_TOPIC_NAME, module);
    }

    public String getResponseTopic(String module) {
        if (singleTopic) {
            return String.format(TOPIC_NAME_WITHOUT_LOCATION, SystemInfoUtils.getInstanceId(), RPC_RESPONSE_TOPIC_NAME);
        }
        return String.format(TOPIC_NAME_WITH_MODULE, SystemInfoUtils.getInstanceId(), RPC_RESPONSE_TOPIC_NAME, module);
    }

}
