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

package org.opennms.core.twin.api;

import static com.jayway.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.twin.publisher.api.OnmsTwinPublisher;
import org.opennms.core.twin.subscriber.api.OnmsTwinSubscriber;

public class OnmsTwinApiIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockTwinPublisher mockTwinPublisher;

    private MockTwinSubscriber mockTwinSubscriber;

    private MockBrokerOnMinion mockBrokerOnMinion;

    private MockBrokerOnOpenNMS mockBrokerOnOpenNMS;

    private String moduleName = "mock-module";

    @Before
    public void setup() {
        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        mockBrokerOnMinion= new MockBrokerOnMinion(kafkaConfig);
        mockBrokerOnOpenNMS = new MockBrokerOnOpenNMS(kafkaConfig);
        mockBrokerOnMinion.init();
        mockBrokerOnOpenNMS.init();
        mockTwinPublisher = new MockTwinPublisher(mockBrokerOnOpenNMS);
        mockTwinSubscriber = new MockTwinSubscriber(mockBrokerOnMinion);
    }

    @Test
    public void testOnmsTwinWithMockProvider() throws ExecutionException, InterruptedException {
        mockTwinPublisher.init();
        mockTwinSubscriber.init();
        MockTwinModuleMinion moduleMinion = new MockTwinModuleMinion(mockTwinSubscriber);
        String valueToReplicate = "mock-object-value";
        MockTwinModuleOnOpenNMS moduleOnOpenNMS = new MockTwinModuleOnOpenNMS(mockTwinPublisher, new MockOnmsTwinBean(valueToReplicate));
        moduleOnOpenNMS.init();
        moduleMinion.init();
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS)
                .until(moduleMinion::getOnmsTwin, Matchers.notNullValue());
        Assert.assertThat(moduleMinion.getOnmsTwin().getObjectValue(), Matchers.is(valueToReplicate.getBytes(StandardCharsets.UTF_8)));
        String updatedValue = "updated-object-value";
        moduleOnOpenNMS.updateTwin(new MockOnmsTwinBean(updatedValue));
        await().atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> moduleMinion.getOnmsTwin().getObjectValue(),
                Matchers.is(updatedValue.getBytes(StandardCharsets.UTF_8)));
    }

    @After
    public void destroy(){
        mockBrokerOnOpenNMS.destroy();
        mockBrokerOnMinion.destroy();
    }


    public class MockTwinModuleMinion {

        private OnmsTwin onmsTwin;
        private final OnmsTwinSubscriber twinProvider;

        public MockTwinModuleMinion(OnmsTwinSubscriber twinProvider) {
            this.twinProvider = twinProvider;
        }

        public void init() throws ExecutionException, InterruptedException {
            // Initiate RPC call providing a subscriber that can get updates through reverse-sink
               CompletableFuture<OnmsTwin> future = twinProvider.getObject(moduleName, new OnmsTwinSubscriber.SinkCallback() {
                   @Override
                   public void sinkUpdate(OnmsTwin onmsTwin) {
                        // Partial updates.
                        setOnmsTwin(onmsTwin);
                   }
               });
               future.whenComplete((response, ex) -> {
                   if(response != null) {
                       setOnmsTwin(response);
                   }
               });
        }

        public OnmsTwin getOnmsTwin() {
            return onmsTwin;
        }

        public void setOnmsTwin(OnmsTwin onmsTwin) {
            this.onmsTwin = onmsTwin;
        }
    }

    public class MockTwinModuleOnOpenNMS {

        private OnmsTwin onmsTwin;
        private final OnmsTwinPublisher twinProvider;
        private OnmsTwinPublisher.Callback callback;

        public MockTwinModuleOnOpenNMS(OnmsTwinPublisher twinProvider, OnmsTwin onmsTwin) {
            this.twinProvider = twinProvider;
            this.onmsTwin = onmsTwin;
        }

        public void init() {
            callback = twinProvider.register(onmsTwin);
        }

        public void updateTwin(OnmsTwin onmsTwin) {
            callback.onUpdate(onmsTwin);
        }
    }

    public class MockOnmsTwinBean implements OnmsTwin {

        private final String value;

        public MockOnmsTwinBean(String objectValue) {
            this.value = objectValue;
        }

        @Override
        public String getKey() {
            return moduleName;
        }

        @Override
        public String getLocation() {
            return "MINION";
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public byte[] getObjectValue() {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }




}
