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
package org.opennms.core.ipc.sink.kafka.itests.offset;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private final String groupName = SystemInfoUtils.getInstanceId();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String bootStrapServer;

    private KafkaMessageConsumerRunner consumerRunner;

    public KafkaMessageConsumer(String bootStrapServer) {
        this.bootStrapServer = bootStrapServer;
    }

    private class KafkaMessageConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private KafkaConsumer<String, String> consumer;

        @Override
        public void run() {
            Properties props = new Properties();
            props.put("bootstrap.servers", getBootStrapServer());
            props.put("group.id", getGroupName());
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("USER_TOPIC"));
            LOGGER.info("subscribed to USER_TOPIC");
            while (!closed.get()) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    LOGGER.info(record.value());
                }
            }
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            if (consumer != null) {
                consumer.wakeup();
            }
        }
    }

    public String getBootStrapServer() {
        return bootStrapServer;
    }

    public void startConsumer() {
        consumerRunner = new KafkaMessageConsumerRunner();
        executor.execute(consumerRunner);
    }

    public void stopConsumer() {
        consumerRunner.shutdown();
    }

    public String getGroupName() {
        return groupName;
    }

}
