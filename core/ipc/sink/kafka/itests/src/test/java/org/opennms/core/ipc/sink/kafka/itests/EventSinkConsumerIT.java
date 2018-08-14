/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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


package org.opennms.core.ipc.sink.kafka.itests;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.kafka.client.KafkaRemoteMessageDispatcherFactory;
import org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.server.KafkaMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.eventd.sink.EventsModule;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Malatesh Sudarshan
 * 
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml" })
@JUnitConfigurationEnvironment
public class EventSinkConsumerIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Autowired
    private MessageDispatcherFactory localMessageDispatcherFactory;

    @Autowired
    private KafkaMessageConsumerManager consumerManager;

    @Autowired
    private EventdConfig m_config;

    private KafkaRemoteMessageDispatcherFactory remoteMessageDispatcherFactory = new KafkaRemoteMessageDispatcherFactory();

    @Before
    public void setUp() throws Exception {
        Hashtable<String, Object> kafkaConfig = new Hashtable<String, Object>();
        kafkaConfig.put("bootstrap.servers",
                        kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class,
                                              RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants.KAFKA_CONFIG_PID).getProperties()).thenReturn(kafkaConfig);
        remoteMessageDispatcherFactory.setConfigAdmin(configAdmin);
        remoteMessageDispatcherFactory.init();

        System.setProperty(String.format("%sbootstrap.servers",
                                         org.opennms.core.ipc.sink.kafka.common.KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                           kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset",
                                         KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                           "earliest");
        consumerManager.afterPropertiesSet();
    }

    @Test(timeout = 30000)
    public void canProduceAndConsumeMessages() throws Exception {

        EventsModule eventsModule = new EventsModule(m_config);
        AtomicInteger eventsCount = new AtomicInteger();

        final MessageConsumer<Event, Log> eventMessageConsumer = new MessageConsumer<Event, Log>() {

            @Override
            public void handleMessage(Log messageLog) {
                eventsCount.incrementAndGet();
            }

            @Override
            public SinkModule<Event, Log> getModule() {
                return eventsModule;
            }
        };

        try {
            consumerManager.registerConsumer(eventMessageConsumer);

            final SyncDispatcher<Event> localDispatcher = localMessageDispatcherFactory.createSyncDispatcher(eventsModule);
            localDispatcher.send(new Event());
            await().atMost(1, MINUTES).until(() -> eventsCount.get(),
                                             equalTo(1));

            final SyncDispatcher<Event> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(new EventsModule(m_config));

            dispatcher.send(new Event());
            await().atMost(1, MINUTES).until(() -> eventsCount.get(),
                                             equalTo(2));
        } finally {
            consumerManager.unregisterConsumer(eventMessageConsumer);
        }

    }

}
