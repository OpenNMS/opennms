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

package org.opennms.features.kafka.producer;

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class NoOpProducer<K,V> implements Producer<K,V> {
    private static final Logger LOG = LoggerFactory.getLogger(NoOpProducer.class);

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
        return send(record, null);
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
        // Log at debug level instead of throwing exception
        LOG.debug("No-op producer: Not sending message to topic {}", record.topic());

        if (callback != null) {
            // Create a dummy RecordMetadata
            RecordMetadata dummyMetadata = new RecordMetadata(
                    new TopicPartition(record.topic(), -1),
                    -1L,
                    -1L,
                    System.currentTimeMillis(),
                    -1L,
                    -1,
                    -1
            );
            callback.onCompletion(dummyMetadata, null);
        }

        // Return a completed future with dummy metadata
        CompletableFuture<RecordMetadata> future = new CompletableFuture<>();
        RecordMetadata dummyMetadata = new RecordMetadata(
                new TopicPartition(record.topic(), -1),
                -1L,
                -1L,
                System.currentTimeMillis(),
                -1L,
                -1,
                -1
        );
        future.complete(dummyMetadata);
        return future;
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}

    @Override
    public void close(Duration duration) {}

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return Collections.emptyMap();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic) {
        return Collections.emptyList();
    }

    @Override
    public void initTransactions() {
        throw new UnsupportedOperationException("No-op producer does not support transactions");
    }

    @Override
    public void beginTransaction() {
        throw new UnsupportedOperationException("No-op producer does not support transactions");
    }

    @Override
    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> map, String s) throws ProducerFencedException {
        throw new UnsupportedOperationException("No-op producer does not support transactions");
    }

    @Override
    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> map, ConsumerGroupMetadata consumerGroupMetadata) throws ProducerFencedException {
        throw new UnsupportedOperationException("No-op producer does not support transactions");
    }

    @Override
    public void commitTransaction() {
        throw new UnsupportedOperationException("No-op producer does not support transactions");
    }

    @Override
    public void abortTransaction() {
        throw new UnsupportedOperationException("No-op producer does not support transactions");
    }
}