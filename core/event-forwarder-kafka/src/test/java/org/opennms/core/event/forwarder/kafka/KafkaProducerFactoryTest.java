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

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.Test;

public class KafkaProducerFactoryTest {

    @Test
    public void buildPropertiesShouldSetBootstrapServers() {
        Properties props = KafkaProducerFactory.buildProperties("broker1:9092,broker2:9092");

        assertThat(props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))
                .isEqualTo("broker1:9092,broker2:9092");
    }

    @Test
    public void buildPropertiesShouldUseLongKeySerializer() {
        Properties props = KafkaProducerFactory.buildProperties("localhost:9092");

        assertThat(props.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))
                .isEqualTo(LongSerializer.class.getName());
    }

    @Test
    public void buildPropertiesShouldUseByteArrayValueSerializer() {
        Properties props = KafkaProducerFactory.buildProperties("localhost:9092");

        assertThat(props.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))
                .isEqualTo(ByteArraySerializer.class.getName());
    }

    @Test
    public void buildPropertiesShouldSetAcksToAll() {
        Properties props = KafkaProducerFactory.buildProperties("localhost:9092");

        assertThat(props.getProperty(ProducerConfig.ACKS_CONFIG))
                .isEqualTo("all");
    }

    @Test
    public void buildPropertiesShouldContainExactlyFourEntries() {
        Properties props = KafkaProducerFactory.buildProperties("localhost:9092");

        assertThat(props).hasSize(4);
    }
}
