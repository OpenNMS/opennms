/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.collection;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
        Mockito.when(kafkaPersister.checkForMaxSize(MockitoHamcrest.intThat(Matchers.greaterThan(120)))).thenReturn(true);
        CollectionSetProtos.CollectionSet.Builder builder = CollectionSetProtos.CollectionSet.newBuilder();
        builder.setTimestamp(System.currentTimeMillis());
        CollectionSetProtos.NodeLevelResource nodeLevelResource = CollectionSetProtos.NodeLevelResource.newBuilder().setNodeId(5).setNodeLabel("kafka-test")
                .setForeignId("fs").setForeignSource("fs").build();
        CollectionSetProtos.NumericAttribute numericAttribute = CollectionSetProtos.NumericAttribute.newBuilder().setName("num-interfaces").setGroup("interfaces").setValue(5).setType(CollectionSetProtos.NumericAttribute.Type.GAUGE).build();
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

        CollectionSetProtos.CollectionSet collectionSet = builder.build();
        kafkaPersister.bisectAndSendMessageToKafka(collectionSet);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(3));
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

    @After
    public void destroy() {
        kafkaConsumer.clearCollectionSetValues();
        kafkaConsumer.shutdown();
        executor.shutdown();
    }

}
