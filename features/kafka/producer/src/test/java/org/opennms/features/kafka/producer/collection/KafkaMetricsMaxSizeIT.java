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

import com.google.protobuf.DoubleValue;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.features.kafka.producer.KafkaForwarderIT;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;


public class KafkaMetricsMaxSizeIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private KafkaPersister kafkaPersister;

    private ExecutorService executor;

    private KafkaForwarderIT.KafkaMessageConsumerRunner kafkaConsumer;

    private List<String> output = new ArrayList<>();

    @Before
    public void setup() throws IOException {
        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaMetricsMaxSizeIT.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        KafkaProducer<String, byte[]> producer = new KafkaProducer<String, byte[]>(kafkaConfig);
        kafkaPersister = Mockito.spy(KafkaPersister.class);
        kafkaPersister.setProducer(producer);
        kafkaPersister.setTopicName("test-metrics");
        executor = Executors.newSingleThreadExecutor();
        kafkaConsumer = new KafkaForwarderIT.KafkaMessageConsumerRunner(kafkaServer.getKafkaConnectString());
        executor.execute(kafkaConsumer);

    }

    @Test
    public void testHandlingMaxSizeWithMultipleResources() {
        // Mock max size to be 120 bytes ( At least minimum of one resource size)
        Mockito.when(kafkaPersister.checkForMaxSize(MockitoHamcrest.intThat(Matchers.greaterThan(140)))).thenReturn(true);
        CollectionSetProtos.CollectionSet collectionSet = buildCollectionSet();
        kafkaPersister.bisectAndSendMessageToKafka(collectionSet);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(3));
        MatcherAssert.assertThat(kafkaConsumer.getNumOfMetricRecords().get(), Matchers.greaterThan(1));
        List<CollectionSetProtos.CollectionSet> collectionSetList = kafkaConsumer.getCollectionSetValues();
        CollectionSetProtos.CollectionSet.Builder result = CollectionSetProtos.CollectionSet.newBuilder();
        collectionSetList.forEach(result::mergeFrom);
        Assert.assertEquals(collectionSet, result.build());
    }

    @Test
    public void testHandlingOfOneResourceWithTooManyAttributes() {
        // Test one resource that can divide into multiple chunks with different numeric attributes.
        Mockito.when(kafkaPersister.checkForMaxSize(MockitoHamcrest.intThat(Matchers.greaterThan(500)))).thenReturn(true);
        CollectionSetProtos.CollectionSet.Builder builderWithResources = CollectionSetProtos.CollectionSet.newBuilder();
        CollectionSetProtos.NodeLevelResource nodeLevelResource = CollectionSetProtos.NodeLevelResource.newBuilder().setNodeId(5).setNodeLabel("kafka-test")
                .setForeignId("fs").setForeignSource("fs").build();
        CollectionSetProtos.CollectionSetResource.Builder collectionSetBuilder = CollectionSetProtos.CollectionSetResource.newBuilder().setNode(nodeLevelResource);
        for(int i=0; i < 100; i++) {
            CollectionSetProtos.NumericAttribute attribute = CollectionSetProtos.NumericAttribute.newBuilder()
                    .setName("num-interfaces"+i).setGroup("interfaces"+i)
                    .setValue(5+i)
                    .setMetricValue(DoubleValue.of(5+i))
                    .setType(CollectionSetProtos.NumericAttribute.Type.GAUGE).build();
            collectionSetBuilder.addNumeric(attribute);
        }
        long timeStamp = System.currentTimeMillis();
        builderWithResources.setTimestamp(timeStamp);
        builderWithResources.addResource(collectionSetBuilder);

        CollectionSetProtos.CollectionSet input = builderWithResources.build();
        kafkaPersister.bisectAndSendMessageToKafka(input);
        await().atMost(60, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(16));
        CollectionSetProtos.CollectionSet.Builder result = CollectionSetProtos.CollectionSet.newBuilder();
        result.setTimestamp(timeStamp);
        CollectionSetProtos.CollectionSetResource.Builder resourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
        resourceBuilder.setNode(nodeLevelResource);
        kafkaConsumer.getCollectionSetValues().forEach(cs -> resourceBuilder.addAllNumeric(cs.getResource(0).getNumericList()));
        result.addResource(resourceBuilder);
        Assert.assertEquals(input, result.build());

    }

    private CollectionSetProtos.CollectionSet buildCollectionSet() {
        CollectionSetProtos.CollectionSet.Builder builder = CollectionSetProtos.CollectionSet.newBuilder();
        builder.setTimestamp(System.currentTimeMillis());
        CollectionSetProtos.NodeLevelResource nodeLevelResource = CollectionSetProtos.NodeLevelResource.newBuilder().setNodeId(5).setNodeLabel("kafka-test")
                .setForeignId("fs").setForeignSource("fs").build();
        CollectionSetProtos.NumericAttribute numericAttribute = CollectionSetProtos.NumericAttribute.newBuilder()
                .setName("num-interfaces")
                .setGroup("interfaces")
                .setValue(5)
                .setMetricValue(DoubleValue.of(5))
                .setType(CollectionSetProtos.NumericAttribute.Type.GAUGE).build();
        builder.addResource(CollectionSetProtos.CollectionSetResource.newBuilder()
                .setNode(nodeLevelResource)
                .addNumeric(numericAttribute)
                .build());
        CollectionSetProtos.InterfaceLevelResource interfaceLevelResource = CollectionSetProtos.InterfaceLevelResource.newBuilder().setNode(nodeLevelResource).setInstance("lo").build();
        builder.addResource(CollectionSetProtos.CollectionSetResource.newBuilder().setInterface(interfaceLevelResource).addNumeric(numericAttribute).build());

        CollectionSetProtos.GenericTypeResource genericTypeResource = CollectionSetProtos.GenericTypeResource.newBuilder().setNode(nodeLevelResource)
                .setInstance("sink-consumer-events")
                .setType("sink-consumer").build();
        builder.addResource(CollectionSetProtos.CollectionSetResource.newBuilder().setGeneric(genericTypeResource).addNumeric(numericAttribute)).build();

        return builder.build();
    }

    @Test
    public void testDisablingOfMetricsSplitting() {
        Mockito.when(kafkaPersister.checkForMaxSize(MockitoHamcrest.intThat(Matchers.greaterThan(140)))).thenReturn(true);
        Mockito.when(kafkaPersister.getDisableMetricsSplitting()).thenReturn(true);
        CollectionSetProtos.CollectionSet collectionSet = buildCollectionSet();
        kafkaPersister.bisectAndSendMessageToKafka(collectionSet);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(1));
        MatcherAssert.assertThat(kafkaConsumer.getNumOfMetricRecords().get(), Matchers.is(1));
        List<CollectionSetProtos.CollectionSet> collectionSetList = kafkaConsumer.getCollectionSetValues();
        CollectionSetProtos.CollectionSet.Builder result = CollectionSetProtos.CollectionSet.newBuilder();
        collectionSetList.forEach(result::mergeFrom);
        Assert.assertEquals(collectionSet, result.build());
    }

    @After
    public void destroy() {
        kafkaConsumer.clearCollectionSetValues();
        kafkaConsumer.clearNumofMetricRecords();
        kafkaConsumer.shutdown();
        executor.shutdown();
    }

}
