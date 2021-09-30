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

package org.opennms.core.ipc.twin.common;

import static com.jayway.awaitility.Awaitility.await;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwinApiIT {

    private static final Logger LOG = LoggerFactory.getLogger(TwinApiIT.class);
    static String rpcRequestTopic = "OpenNMS-MINION-RPC-Request-onms-twin";
    static String rpcResponseTopic = "OpenNMS-MINION-RPC-Response-onms-twin";
    static String sinkTopic = "OpenNMS-MINION-Sink-onms-twin";

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockTwinPublisher mockTwinPublisher;

    private MockTwinSubscriber mockTwinSubscriber;

    private final List<MinionInfoBean> minionInfoBeans = new ArrayList<>();


    @Before
    public void setup() {
        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        mockTwinPublisher = new MockTwinPublisher(kafkaConfig);
        mockTwinSubscriber = new MockTwinSubscriber(kafkaConfig, new MockMinionIdentityImpl());
        mockTwinPublisher.init();
        mockTwinSubscriber.init();
    }

    @Test
    public void testTwinApiWithMocks() throws IOException {
        MinionInfoBean minionInfoBean = new MinionInfoBean(3, "minion1");
        String key = "minion-bean-module";
        TwinPublisher.Session<MinionInfoBean> session = mockTwinPublisher.register(key, MinionInfoBean.class);
        session.publish(minionInfoBean);
        Closeable closeable = mockTwinSubscriber.subscribe(key, MinionInfoBean.class, new ConsumerImpl());
        await().atMost(20, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(minionInfoBeans::size, Matchers.greaterThan(0));
        minionInfoBeans.clear();
        session.publish(new MinionInfoBean(4, "minion2"));
        await().atMost(20, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(minionInfoBeans::size, Matchers.greaterThan(0));
        MinionInfoBean response = minionInfoBeans.get(0);
        Assert.assertThat(response.getNodeId(), Matchers.is(4));
        closeable.close();
        session.close();
    }

    private static class MockMinionIdentityImpl implements MinionIdentity {

        @Override
        public String getId() {
            return "mock-id";
        }

        @Override
        public String getLocation() {
            return "mock-location";
        }

        @Override
        public String getType() {
            return SystemType.Minion.name();
        }
    }

    @After
    public void destroy() {
        mockTwinSubscriber.destroy();
        mockTwinPublisher.destroy();
    }

    private class ConsumerImpl implements Consumer<MinionInfoBean> {

        @Override
        public void accept(MinionInfoBean minionInfoBean) {
            minionInfoBeans.add(minionInfoBean);
            LOG.info("received minion bean {}", minionInfoBean);
        }
    }

}
