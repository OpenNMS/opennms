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

package org.opennms.core.ipc.twin.api;

import static com.jayway.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.kafka.JUnitKafkaServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TwinApiIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockTwinPublisher mockTwinPublisher;

    private MockTwinSubscriber mockTwinSubscriber;

    @Before
    public void setup() {
        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        MockTwinSubscriberBroker mockBrokerOnMinion = new MockTwinSubscriberBroker(kafkaConfig);
        MockTwinPublisherBroker mockBrokerOnOpenNMS = new MockTwinPublisherBroker(kafkaConfig);
        mockBrokerOnMinion.init();
        mockBrokerOnOpenNMS.init();
        mockTwinPublisher = new MockTwinPublisher(mockBrokerOnOpenNMS);
        mockTwinSubscriber = new MockTwinSubscriber(mockBrokerOnMinion);
    }

    @Test
    public void testTwinApiWithMocks() throws JsonProcessingException {
        MinionInfoBean minionInfoBean = new MinionInfoBean(3, "minion1");
        TwinPublisher.Session<MinionInfoBean> session = mockTwinPublisher.register(minionInfoBean, "minion-bean");
        Map<String, MinionInfoBean> minionBeans = new HashMap<>();
        mockTwinSubscriber.getObject("minion-bean", MinionInfoBean.class, new Consumer<MinionInfoBean>() {
            @Override
            public void accept(MinionInfoBean minionInfoBean) {
                minionBeans.put("minion-bean", minionInfoBean);
            }
        });
        await().atMost(15, TimeUnit.SECONDS).until(minionBeans::size, Matchers.is(1));
        minionBeans.clear();
        session.publish(new MinionInfoBean(4, "minion2"));
        await().atMost(15, TimeUnit.SECONDS).until(minionBeans::size, Matchers.is(1));
        MinionInfoBean response = minionBeans.get("minion-bean");
        Assert.assertThat(response.getNodeId(), Matchers.is(4));
    }

    @Test
    public void testObjectMapperWithMocks() throws IOException {
        MinionInfoBean minionInfoBean = new MinionInfoBean(3, "minion1");
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] value = objectMapper.writeValueAsBytes(minionInfoBean);
        MinionInfoBean bean = objectMapper.readValue(value, MinionInfoBean.class);
        Assert.assertThat(bean.getNodeId(), Matchers.is(3));
        MockTwinResponse mockTwinResponse = new MockTwinResponse("mock", "minion".getBytes(StandardCharsets.UTF_8));
        byte[] response = objectMapper.writeValueAsBytes(mockTwinResponse);
        MockTwinResponse twinResponse = objectMapper.readValue(response, MockTwinResponse.class);
        Assert.assertThat(twinResponse.getKey(), Matchers.is("mock"));
    }
}
