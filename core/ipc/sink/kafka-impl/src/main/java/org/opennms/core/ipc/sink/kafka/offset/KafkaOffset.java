/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.offset;

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
