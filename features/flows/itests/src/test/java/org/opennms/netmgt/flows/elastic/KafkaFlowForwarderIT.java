/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.netmgt.flows.persistence.KafkaFlowForwarder;
import org.opennms.netmgt.flows.persistence.model.FlowDocument;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfo;
import org.osgi.service.cm.ConfigurationAdmin;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class KafkaFlowForwarderIT {

    private KafkaFlowForwarder flowForwarder;

    private final String topicName = "flowDocuments";

    private final List<FlowDocument> flowDocuments = new ArrayList<>();
    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Before
    public void setup() throws IOException {
        kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "flow-test");
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaFlowForwarder.KAFKA_CLIENT_PID).getProperties()).thenReturn(kafkaConfig);
        flowForwarder = new KafkaFlowForwarder(configAdmin, new MetricRegistry());
        flowForwarder.setTopicName(topicName);
    }

    @Parameters({"true", "false"})
    @Test(timeout = 60000)
    public void testKafkaPersistenceForFlows(boolean useJson) throws Exception {
        flowForwarder.setUseJson(useJson);
        flowForwarder.init();

        // start ES
        final var flow = EnrichedFlow.from(FlowDocumentTest.getMockFlow());
        flow.setSrcNodeInfo(new NodeInfo() {{
            this.setNodeId(1);
            this.setForeignId("");
        }});
        flow.setDstNodeInfo(new NodeInfo() {{
            this.setNodeId(2);
            this.setForeignId("fid");
        }});
        this.flowForwarder.persist(Lists.newArrayList(flow));

        KafkaConsumerRunner kafkaConsumerRunner = new KafkaConsumerRunner(kafkaConfig, topicName, useJson);
        Executors.newSingleThreadExecutor().execute(kafkaConsumerRunner);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() ->
                getFlowDocuments().size(), Matchers.greaterThan(0));
        getFlowDocuments().forEach(flowDocument -> {
            assertEquals(FlowDocumentTest.getMockFlow().getSrcAddr(), flowDocument.getSrcAddress());
            assertEquals(FlowDocumentTest.getMockFlow().getDstAddr(), flowDocument.getDstAddress());
            assertEquals(1, flowDocument.getSrcNode().getNodeId());
            assertEquals(2, flowDocument.getDestNode().getNodeId());
            assertEquals("", flowDocument.getSrcNode().getForeginId());
            assertEquals("fid", flowDocument.getDestNode().getForeginId());
        });
        kafkaConsumerRunner.destroy();
    }

    @After
    public void destroy() {
        flowForwarder.destroy();
    }

    public List<FlowDocument> getFlowDocuments() {
        return flowDocuments;
    }

    private class KafkaConsumerRunner implements Runnable {

        private final KafkaConsumer<String, byte[]> kafkaConsumer;
        private final String topic;
        private final boolean useJson;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public KafkaConsumerRunner(Hashtable properties, String topic, boolean useJson) {
            this.kafkaConsumer = new KafkaConsumer<String, byte[]>(properties);
            this.topic = topic;
            this.useJson = useJson;
        }

        @Override
        public void run() {
            try {
                kafkaConsumer.subscribe(Arrays.asList(topic));
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    records.forEach(consumerRecord -> {
                        try {
                            if (useJson) {
                                FlowDocument.Builder builder = FlowDocument.newBuilder();
                                JsonFormat.parser().merge(new String(consumerRecord.value(), StandardCharsets.UTF_8), builder);
                                flowDocuments.add(builder.build());
                            } else {
                                flowDocuments.add(FlowDocument.parseFrom(consumerRecord.value()));
                            }
                        } catch (InvalidProtocolBufferException e) {
                            //pass
                        }
                    });
                }
            } catch (Exception e) {

            }
        }

        public void destroy() {
            closed.set(true);
        }

    }
}
