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
package org.opennms.core.ipc.sink.kafka.itests;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.kafka.client.KafkaRemoteMessageDispatcherFactory;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-opennms-identity.xml"
})
@JUnitConfigurationEnvironment
public class KafkaLargeBufferSinkIT {

    public static final String TEST_UEI = "uei/test/kafka/largeBuffer";
    public static final long NODE_ID = 2345;

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    private KafkaRemoteMessageDispatcherFactory remoteMessageDispatcherFactory = new KafkaRemoteMessageDispatcherFactory();

    @Before
    public void setUp() throws Exception {
        Hashtable<String, Object> kafkaConfig = new Hashtable<String, Object>();
        kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
        // To test large buffer size, keeping max buffer size as 1KB.
        kafkaConfig.put(KafkaSinkConstants.MAX_BUFFER_SIZE_PROPERTY, 1000);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaSinkConstants.KAFKA_CONFIG_PID).getProperties())
                .thenReturn(kafkaConfig);
        remoteMessageDispatcherFactory.setConfigAdmin(configAdmin);
        remoteMessageDispatcherFactory.setTracerRegistry(new MockTracerRegistry());
        remoteMessageDispatcherFactory.setIdentity(new MinionIdentity() {
            @Override
            public String getId() {
                return "0";
            }
            @Override
            public String getLocation() {
                return "some location";
            }
            @Override
            public String getType() {
                return SystemType.Minion.name();
            }
        });
        remoteMessageDispatcherFactory.init();

        System.setProperty(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX + ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        System.setProperty(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumerManager.afterPropertiesSet();
    }

    @Test
    public void testLargeBufferSinkMessage() throws Exception {
        EventsMockModule module = EventsMockModule.INSTANCE;
        List<Event> outputEvents = new ArrayList<Event>();
        Event event = buildEventWithRandomStrings();
        MessageConsumer<Event, Event> eventLogMessageConsumer = new MessageConsumer<Event, Event>() {
            @Override
            public SinkModule<Event, Event> getModule() {
                return module;
            }

            @Override
            public void handleMessage(Event event) {
                outputEvents.add(event);
            }
        };
        try {
            consumerManager.registerConsumer(eventLogMessageConsumer);
            final SyncDispatcher<Event> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(EventsMockModule.INSTANCE);
            dispatcher.send(event);
            await().atMost(1, MINUTES).until(() -> outputEvents.size(), equalTo(1));
            Event outputEvent = outputEvents.get(0);
            assertThat(outputEvent.getUei(), is(TEST_UEI));
            assertThat(outputEvent.getNodeid(), is(NODE_ID));
        } finally {
            consumerManager.unregisterConsumer(eventLogMessageConsumer);
        }


    }

    @Test
    public void testLargeBufferSinkMessageWithGroupId() throws Exception {
        EventsMockModule module = EventsMockModule.INSTANCE;
        List<Event> outputEvents = new ArrayList<Event>();
        Event event = buildEventWithRandomStrings();
        MessageConsumer<Event, Event> eventLogMessageConsumer = new MessageConsumer<Event, Event>() {
            @Override
            public SinkModule<Event, Event> getModule() {
                return module;
            }

            @Override
            public void handleMessage(Event event) {
                outputEvents.add(event);
            }
        };
        try {
            consumerManager.registerConsumer(eventLogMessageConsumer);
            final SyncDispatcher<Event> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(new EventsMockModule() {
                @Override
                public Optional<String> getRoutingKey(final Event message) {
                    return Optional.of("test");
                }
            });
            dispatcher.send(event);
            await().atMost(1, MINUTES).until(() -> outputEvents.size(), equalTo(1));
            Event outputEvent = outputEvents.get(0);
            assertThat(outputEvent.getUei(), is(TEST_UEI));
            assertThat(outputEvent.getNodeid(), is(NODE_ID));
        } finally {
            consumerManager.unregisterConsumer(eventLogMessageConsumer);
        }
    }

    private Event buildEventWithRandomStrings() throws UnknownHostException {
        EventBuilder eventBldr = new EventBuilder(TEST_UEI, "kafka-test");
        eventBldr.setInterface(InetAddress.getLocalHost());
        eventBldr.setHost(InetAddressUtils.getLocalHostName());
        eventBldr.setNodeid(NODE_ID);
        // Set description and log to random strings of size 5K.
        eventBldr.setDescription(RandomStringUtils.random(5000, true, true));
        eventBldr.setLogMessage(RandomStringUtils.random(5000, true, true));
        Event event = eventBldr.getEvent();
        return event;
    }

    @After
    public void destroy() {
        remoteMessageDispatcherFactory.destroy();
    }

}
