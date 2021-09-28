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

package org.opennms.features.kafka.consumer.events;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.features.kafka.consumer.OpenNMSKafkaConsumer;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
public class OpenNMSKafkaConsumerIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private MockEventIpcManager eventdIpcMgr;

    private OpenNMSKafkaConsumer consumer;


    @Before
    public void setUp() throws IOException {

        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, OpenNMSKafkaConsumerIT.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        consumer = new OpenNMSKafkaConsumer(configAdmin, eventdIpcMgr);
        consumer.setEventsTopic("opennms-events");
        when(configAdmin.getConfiguration(OpenNMSKafkaConsumer.KAFKA_CLIENT_PID).getProperties()).thenReturn(kafkaConfig);

    }

    @Test
    public void testEventsComingFromKafka() throws IOException {

        consumer.init();
        Hashtable<String, Object> kafkaConfig = new Hashtable<>();
        kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        EventBuilder eventBuilder = new EventBuilder().setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
                .setSource("kafka-events-test").addParam("interface", "192.168.0.1");
        Event event = eventBuilder.getEvent();
        eventdIpcMgr.getEventAnticipator().anticipateEvent(event);
        KafkaProducer<String, byte[]> kafkaProducer = new KafkaProducer<String, byte[]>(kafkaConfig);
        EventsProto.Event eventsProto = EventsProto.Event.newBuilder().setSeverity(EventsProto.Severity.NORMAL).setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
                                    .setSource("kafka-events-test").addParameter(EventsProto.EventParameter.newBuilder().setName("interface").setValue("192.168.0.1").build())
                                    .build();
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>("opennms-events", eventsProto.toByteArray());
        kafkaProducer.send(producerRecord);

        await().atMost(30, TimeUnit.SECONDS).until(eventdIpcMgr.getEventAnticipator()::getAnticipatedEvents, hasSize(0));
        consumer.shutdown();

    }
}
