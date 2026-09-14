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
package org.opennms.features.kafka.producer.collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.util.JsonFormat;

/**
 * Verifies that the KafkaPersister can forward metrics encoded as JSON
 * instead of protobuf when the metrics.useJson option is enabled.
 */
public class KafkaPersisterJsonTest {

    private static final String TOPIC = "test-metrics";

    private MockProducer<String, byte[]> mockProducer;

    private KafkaPersister persister;

    @Before
    public void setUp() {
        mockProducer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
        persister = new KafkaPersister();
        persister.setProducer(mockProducer);
        persister.setTopicName(TOPIC);
    }

    private static CollectionSetProtos.CollectionSet buildCollectionSet(int numAttributes) {
        CollectionSetProtos.NodeLevelResource nodeLevelResource = CollectionSetProtos.NodeLevelResource.newBuilder()
                .setNodeId(5)
                .setNodeLabel("kafka-json-test")
                .setForeignSource("fs")
                .setForeignId("fid")
                .build();
        CollectionSetProtos.CollectionSetResource.Builder resourceBuilder = CollectionSetProtos.CollectionSetResource
                .newBuilder().setNode(nodeLevelResource);
        for (int i = 0; i < numAttributes; i++) {
            resourceBuilder.addNumeric(CollectionSetProtos.NumericAttribute.newBuilder()
                    .setName("metric" + i)
                    .setGroup("group" + i)
                    .setValue(100 + i)
                    .setMetricValue(DoubleValue.of(100 + i))
                    .setType(CollectionSetProtos.NumericAttribute.Type.GAUGE));
        }
        return CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(1662000000000L)
                .addResource(resourceBuilder)
                .build();
    }

    @Test
    public void testJsonPayloadRoundTrip() throws Exception {
        persister.setUseJson(true);
        CollectionSetProtos.CollectionSet collectionSet = buildCollectionSet(2);

        persister.bisectAndSendMessageToKafka(collectionSet);

        assertThat(mockProducer.history().size(), equalTo(1));
        ProducerRecord<String, byte[]> record = mockProducer.history().get(0);
        assertThat(record.topic(), equalTo(TOPIC));
        assertThat(record.key(), equalTo("5"));

        String json = new String(record.value(), StandardCharsets.UTF_8);
        assertThat(json, startsWith("{"));

        // The JSON must parse back into an equal CollectionSet
        CollectionSetProtos.CollectionSet.Builder parsed = CollectionSetProtos.CollectionSet.newBuilder();
        JsonFormat.parser().merge(json, parsed);
        assertThat(parsed.build(), equalTo(collectionSet));
    }

    @Test
    public void testProtobufPayloadByDefault() throws Exception {
        CollectionSetProtos.CollectionSet collectionSet = buildCollectionSet(2);

        persister.bisectAndSendMessageToKafka(collectionSet);

        assertThat(mockProducer.history().size(), equalTo(1));
        ProducerRecord<String, byte[]> record = mockProducer.history().get(0);
        assertThat(CollectionSetProtos.CollectionSet.parseFrom(record.value()), equalTo(collectionSet));
    }

    @Test
    public void testJsonPayloadIsSplitByJsonSize() throws Exception {
        persister = new KafkaPersister() {
            @Override
            boolean checkForMaxSize(int length) {
                // Force splitting on a small threshold; JSON is larger than
                // protobuf, so the encoded JSON size must be what is checked
                return length > 2048;
            }
        };
        persister.setProducer(mockProducer);
        persister.setTopicName(TOPIC);
        persister.setUseJson(true);

        CollectionSetProtos.CollectionSet collectionSet = buildCollectionSet(100);

        persister.bisectAndSendMessageToKafka(collectionSet);

        assertThat(mockProducer.history().size(), greaterThan(1));
        // Each message must honor the maximum size and parse as valid JSON
        CollectionSetProtos.CollectionSet.Builder merged = CollectionSetProtos.CollectionSet.newBuilder();
        for (ProducerRecord<String, byte[]> record : mockProducer.history()) {
            assertThat(record.value().length <= 2048, equalTo(true));
            CollectionSetProtos.CollectionSet.Builder part = CollectionSetProtos.CollectionSet.newBuilder();
            JsonFormat.parser().merge(new String(record.value(), StandardCharsets.UTF_8), part);
            merged.mergeFrom(part.build());
        }
        // All numeric attributes must survive the split
        List<String> names = merged.build().getResourceList().stream()
                .flatMap(r -> r.getNumericList().stream())
                .map(CollectionSetProtos.NumericAttribute::getName)
                .collect(Collectors.toList());
        assertThat(names.size(), equalTo(100));
    }

    @Test
    public void testOversizedSingleAttributeTerminates() throws Exception {
        persister = new KafkaPersister() {
            @Override
            boolean checkForMaxSize(int length) {
                return length > 2048;
            }
        };
        persister.setProducer(mockProducer);
        persister.setTopicName(TOPIC);
        persister.setUseJson(true);

        // A single string attribute whose JSON encoding alone exceeds the maximum:
        // it cannot be split, so it must be sent as-is rather than recursing forever
        StringBuilder bigValue = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            bigValue.append('x');
        }
        CollectionSetProtos.NodeLevelResource nodeLevelResource = CollectionSetProtos.NodeLevelResource.newBuilder()
                .setNodeId(5)
                .build();
        CollectionSetProtos.CollectionSet collectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(1662000000000L)
                .addResource(CollectionSetProtos.CollectionSetResource.newBuilder()
                        .setNode(nodeLevelResource)
                        .addString(CollectionSetProtos.StringAttribute.newBuilder()
                                .setName("bigString")
                                .setValue(bigValue.toString())))
                .build();

        persister.bisectAndSendMessageToKafka(collectionSet);

        assertThat(mockProducer.history().size(), equalTo(1));
        CollectionSetProtos.CollectionSet.Builder parsed = CollectionSetProtos.CollectionSet.newBuilder();
        JsonFormat.parser().merge(new String(mockProducer.history().get(0).value(), StandardCharsets.UTF_8), parsed);
        assertThat(parsed.build(), equalTo(collectionSet));
    }
}
