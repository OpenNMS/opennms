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

import java.util.Objects;

import org.opennms.core.ipc.sink.api.Message;

public class KafkaOffset implements Message {

    private String consumerGroupName;
    private String topic;
    private int partition;
    private long logSize;
    private long consumerOffset;
    private long lag;

    public KafkaOffset() {

    }

    public KafkaOffset(String consumerGroupName, String topic, int partition, long logSize, long consumerOffset,
            long lag) {
        super();
        this.consumerGroupName = consumerGroupName;
        this.topic = topic;
        this.partition = partition;
        this.logSize = logSize;
        this.consumerOffset = consumerOffset;
        this.lag = lag;
    }

    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    public void setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getLogSize() {
        return logSize;
    }

    public void setLogSize(long logSize) {
        this.logSize = logSize;
    }

    public long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public long getLag() {
        return lag;
    }

    public void setLag(long lag) {
        this.lag = lag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerGroupName, topic, partition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KafkaOffset other = (KafkaOffset) obj;
        return Objects.equals(this.consumerGroupName, other.consumerGroupName)
                && Objects.equals(this.topic, other.topic) && Objects.equals(this.partition, other.partition);
    }

    @Override
    public String toString() {
        return "KafkaOffset [consumerGroupName=" + consumerGroupName + ", topic=" + topic + ", partition=" + partition
                + ", logSize=" + logSize + ", consumerOffset=" + consumerOffset + ", lag=" + lag + "]";
    }

}
