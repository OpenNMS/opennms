/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.sink.EventModule;
import org.opennms.netmgt.eventd.sink.EventSinkConsumer;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
                EventModule.MODULE_ID);
        XmlHandler<Log> xmlHandler = new XmlHandler<>(Log.class);
        String marshalledEvent = xmlHandler.marshal(getEventLog());
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<String, byte[]>(topicNameFactory.getName(),
                marshalledEvent.getBytes(StandardCharsets.UTF_8));
        producer.send(producerRecord);
        m_eventMgr.getEventAnticipator().anticipateEvent(buildEvent());
        m_eventMgr.getEventAnticipator().verifyAnticipated(15000, 0, 0, 0, 0);
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
