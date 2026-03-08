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
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.Test;

public class KafkaConsumerFactoryTest {

    @Test
    public void buildPropertiesShouldSetBootstrapServers() {
        Properties props = KafkaConsumerFactory.buildProperties("broker1:9092", "my-group");

        assertThat(props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG))
                .isEqualTo("broker1:9092");
    }

    @Test
    public void buildPropertiesShouldSetGroupId() {
        Properties props = KafkaConsumerFactory.buildProperties("localhost:9092", "opennms-core");

        assertThat(props.getProperty(ConsumerConfig.GROUP_ID_CONFIG))
                .isEqualTo("opennms-core");
    }

    @Test
    public void buildPropertiesShouldUseLongKeyDeserializer() {
        Properties props = KafkaConsumerFactory.buildProperties("localhost:9092", "test-group");

        assertThat(props.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(LongDeserializer.class.getName());
    }

    @Test
    public void buildPropertiesShouldUseByteArrayValueDeserializer() {
        Properties props = KafkaConsumerFactory.buildProperties("localhost:9092", "test-group");

        assertThat(props.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(ByteArrayDeserializer.class.getName());
    }

    @Test
    public void buildPropertiesShouldSetAutoOffsetResetToEarliest() {
        Properties props = KafkaConsumerFactory.buildProperties("localhost:9092", "test-group");

        assertThat(props.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG))
                .isEqualTo("earliest");
    }

    @Test
    public void buildPropertiesShouldEnableAutoCommit() {
        Properties props = KafkaConsumerFactory.buildProperties("localhost:9092", "test-group");

        assertThat(props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG))
                .isEqualTo("true");
    }

    @Test
    public void buildPropertiesShouldContainExactlySixEntries() {
        Properties props = KafkaConsumerFactory.buildProperties("localhost:9092", "test-group");

        assertThat(props).hasSize(6);
    }
}
