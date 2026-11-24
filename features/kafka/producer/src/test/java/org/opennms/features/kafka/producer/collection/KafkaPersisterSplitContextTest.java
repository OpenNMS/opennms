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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.ServiceParameters;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * An oversized single resource is split into several Kafka messages. Each one must keep the
 * resource context strings, and no other attribute may be lost or duplicated.
 */
public class KafkaPersisterSplitContextTest {

    private static final String TOPIC = "test-metrics";
    private static final int THRESHOLD = 20_000;
    private static final Set<String> CONTEXT = Set.of("agent_protocol");

    private MockProducer<String, byte[]> producer;
    private KafkaPersister persister;

    @Before
    public void setUp() {
        producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
        final KafkaPersister real = new KafkaPersister(new ServiceParameters(java.util.Collections.emptyMap()));
        real.setProducer(producer);
        real.setTopicName(TOPIC);
        persister = Mockito.spy(real);
        // Forces splitting: anything above THRESHOLD bytes is oversized
        Mockito.doAnswer(invocation -> (int) invocation.getArgument(0) > THRESHOLD)
                .when(persister).checkForMaxSize(anyInt());
    }

    @Test
    public void everyNumericChunkCarriesContext() throws InvalidProtocolBufferException {
        persister.bisectAndSendMessageToKafka(proto(2000, 200, true));

        int numericRecords = 0;
        for (final CollectionSetProtos.CollectionSetResource r : resources()) {
            if (r.getNumericCount() > 0) {
                numericRecords++;
                assertEquals(contextStrings(), r.getStringList().stream()
                        .map(s -> s.getName() + "=" + s.getValue()).collect(Collectors.toSet()));
                assertEquals(CONTEXT.size(), r.getStringCount());
            }
        }
        assertTrue(numericRecords > 1);
    }

    @Test
    public void everyStringChunkCarriesContext() throws InvalidProtocolBufferException {
        persister.bisectAndSendMessageToKafka(proto(2000, 200, true));

        int stringRecords = 0;
        for (final CollectionSetProtos.CollectionSetResource r : resources()) {
            if (r.getStringCount() > 0) {
                stringRecords++;
                assertEquals(1, r.getStringList().stream().filter(s -> s.getName().equals("agent_protocol")).count());
            }
        }
        assertTrue(stringRecords > 1);
    }

    @Test
    public void noAttributeLostOrDuplicated() throws InvalidProtocolBufferException {
        final CollectionSetProtos.CollectionSet proto = proto(2000, 200, true);
        persister.bisectAndSendMessageToKafka(proto);

        final List<String> numeric = new ArrayList<>();
        final List<String> strings = new ArrayList<>();
        for (final CollectionSetProtos.CollectionSetResource r : resources()) {
            r.getNumericList().forEach(n -> numeric.add(n.getName()));
            r.getStringList().stream().filter(s -> !CONTEXT.contains(s.getName())).forEach(s -> strings.add(s.getName()));
        }
        assertEquals(2000, numeric.size());
        assertEquals(2000, numeric.stream().distinct().count());
        assertEquals(200, strings.size());
        assertEquals(200, strings.stream().distinct().count());
    }

    @Test
    public void singleHugeStringTerminates() throws InvalidProtocolBufferException {
        final CollectionSetProtos.CollectionSetResource.Builder resource = baseResource(true);
        resource.addString(CollectionSetProtos.StringAttribute.newBuilder().setName("huge")
                .setValue("x".repeat(2 * 1024 * 1024)));
        persister.bisectAndSendMessageToKafka(CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(1).addResource(resource).build());

        assertEquals(1, producer.history().size());
        final CollectionSetProtos.CollectionSetResource r = resources().get(0);
        assertEquals(CONTEXT.size() + 1, r.getStringCount());
        assertEquals(contextStrings(), r.getStringList().stream().filter(s -> CONTEXT.contains(s.getName()))
                .map(s -> s.getName() + "=" + s.getValue()).collect(Collectors.toSet()));
    }

    @Test
    public void noContextUnchanged() throws InvalidProtocolBufferException {
        persister.bisectAndSendMessageToKafka(proto(2000, 200, false));

        for (final CollectionSetProtos.CollectionSetResource r : resources()) {
            if (r.getNumericCount() > 0) {
                assertEquals(0, r.getStringCount());
            }
            assertFalse(r.getStringList().stream().anyMatch(s -> CONTEXT.contains(s.getName())));
        }
    }

    private static Set<String> contextStrings() {
        return Set.of("agent_protocol=SNMP");
    }

    private List<CollectionSetProtos.CollectionSetResource> resources() throws InvalidProtocolBufferException {
        final List<CollectionSetProtos.CollectionSetResource> result = new ArrayList<>();
        for (final ProducerRecord<String, byte[]> record : producer.history()) {
            final CollectionSetProtos.CollectionSet set = CollectionSetProtos.CollectionSet.parseFrom(record.value());
            assertEquals(1, set.getResourceCount());
            result.add(set.getResource(0));
        }
        return result;
    }

    private static CollectionSetProtos.CollectionSetResource.Builder baseResource(final boolean withContext) {
        final CollectionSetProtos.CollectionSetResource.Builder resource = CollectionSetProtos.CollectionSetResource.newBuilder();
        resource.setNode(CollectionSetProtos.NodeLevelResource.newBuilder().setNodeId(1).setNodeLabel("TestNode"));
        if (withContext) {
            resource.addString(CollectionSetProtos.StringAttribute.newBuilder().setName("agent_protocol").setValue("SNMP"));
        }
        return resource;
    }

    private static CollectionSetProtos.CollectionSet proto(final int numerics, final int strings, final boolean withContext) {
        final CollectionSetProtos.CollectionSetResource.Builder resource = baseResource(withContext);
        for (int i = 0; i < numerics; i++) {
            resource.addNumeric(CollectionSetProtos.NumericAttribute.newBuilder()
                    .setGroup("g").setName("numeric-attribute-with-a-long-name-" + i).setValue(i));
        }
        for (int i = 0; i < strings; i++) {
            resource.addString(CollectionSetProtos.StringAttribute.newBuilder()
                    .setName("string-attribute-" + i).setValue("v".repeat(500)));
        }
        return CollectionSetProtos.CollectionSet.newBuilder().setTimestamp(1).addResource(resource).build();
    }
}
