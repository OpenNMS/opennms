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
package org.opennms.core.ipc.sink.kafka.server.offset;

public interface KafkaOffsetConstants {

    String OFFSET = "offset";

    String TOPIC = "topic";

    String GROUP = "group";

    String PARTITION = "partition";

    int TIMEOUT = 100000;

    int BUFFERSIZE = 64 * 1024;

    int POLL_INTERVAL = 500;

    String OFFSETS_TOPIC = "__consumer_offsets";
    
    String CLIENT_NAME = "OpenNMS-Kafka-Monitor";

}
