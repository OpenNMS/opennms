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
package org.opennms.netmgt.eventd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.ipc.sink.model.SinkMessage;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.XmlHandler;
import org.opennms.features.events.sink.module.EventSinkModule;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.sink.EventSinkConsumer;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.protobuf.ByteString;

/**
 * @author Malatesh Sudarshan
 * @author cgorantla
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml" })
@JUnitConfigurationEnvironment
public class EventSinkConsumerIT {

    private static final String EVENT_SOURCE = "event-sink-producer";

    private static final String LOCATION = "Default";

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private EventdConfig m_config;

    private KafkaProducer<String, byte[]> producer;

    @Before
    public void setUp() throws Exception {
        System.setProperty(String.format("%sbootstrap.servers", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                "earliest");
        // Since Kafka consumer depends on above system properties to start, can't autowire below beans.
        KafkaMessageConsumerManager consumerManager = new KafkaMessageConsumerManager();
        consumerManager.afterPropertiesSet();
        EventSinkConsumer m_eventSinkConsumer = new EventSinkConsumer();
        m_eventSinkConsumer.setconfig(m_config);
        m_eventSinkConsumer.setEventForwarder(m_eventMgr);
        m_eventSinkConsumer.setMessageConsumerManager(consumerManager);
        consumerManager.registerConsumer(m_eventSinkConsumer);
        final Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        producer = new KafkaProducer<>(producerConfig);
    }

    @Test
    public void verifyEventSinkConsumerWithKafka() throws Exception {
        final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaSinkConstants.KAFKA_TOPIC_PREFIX,
                EventSinkModule.MODULE_ID);
        XmlHandler<Log> xmlHandler = new XmlHandler<>(Log.class);
        String marshalledEvent = xmlHandler.marshal(getEventLog());
        byte[] sinkMessageInBytes = wrapMessageToProto("1", marshalledEvent.getBytes(StandardCharsets.UTF_8));
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<String, byte[]>(topicNameFactory.getName(),
                sinkMessageInBytes);
        producer.send(producerRecord);
        m_eventMgr.getEventAnticipator().anticipateEvent(buildEvent());
        m_eventMgr.getEventAnticipator().verifyAnticipated(15000, 0, 0, 0, 0);
    }

    private byte[] wrapMessageToProto(String messageId, byte[] messageInBytes) {
        SinkMessage sinkMessage = SinkMessage.newBuilder()
                .setMessageId(messageId)
                .setContent(ByteString.copyFrom(messageInBytes))
                .build();
        return sinkMessage.toByteArray();
    }

    private Log getEventLog() throws UnknownHostException {
        Log eventLog = new Log();
        eventLog.addEvent(buildEvent());
        return eventLog;
    }

    private Event buildEvent() throws UnknownHostException {
        EventBuilder eventBldr = new EventBuilder(
                org.opennms.netmgt.events.api.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, EVENT_SOURCE);
        eventBldr.setInterface(InetAddress.getLocalHost());
        eventBldr.setHost(InetAddressUtils.getLocalHostName());
        eventBldr.setDistPoller(LOCATION);
        return eventBldr.getEvent();
    }

    @After
    public void destroy() {
        producer.close();
    }

}
