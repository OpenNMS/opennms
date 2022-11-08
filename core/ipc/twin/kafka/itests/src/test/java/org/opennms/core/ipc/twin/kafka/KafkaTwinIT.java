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

package org.opennms.core.ipc.twin.kafka;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;

import java.util.Properties;

import com.codahale.metrics.MetricRegistry;
import io.opentracing.util.GlobalTracer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriberImpl;
import org.opennms.core.ipc.twin.kafka.publisher.KafkaTwinPublisher;
import org.opennms.core.ipc.twin.kafka.subscriber.KafkaTwinSubscriber;
import org.opennms.core.ipc.twin.test.AbstractTwinBrokerIT;
import org.opennms.core.ipc.twin.test.MockMinionIdentity;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
})
@JUnitConfigurationEnvironment
public class KafkaTwinIT extends AbstractTwinBrokerIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    @Override
    protected TwinPublisher createPublisher() throws Exception {
        final KafkaConfigProvider config = () -> {
            final var properties = new Properties();
            properties.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
            properties.put("group.id", SystemInfoUtils.getInstanceId());
            return properties;
        };
        TracerRegistry tracerRegistry = Mockito.mock(TracerRegistry.class);
        Mockito.when(tracerRegistry.getTracer()).thenReturn(GlobalTracer.get());
        LocalTwinSubscriber localTwinSubscriber = new LocalTwinSubscriberImpl(new MockMinionIdentity("Default"), tracerRegistry, new MetricRegistry());
        final var kafkaTwinPublisher = new KafkaTwinPublisher(localTwinSubscriber, config, tracerRegistry, new MetricRegistry());
        kafkaTwinPublisher.init();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return kafkaTwinPublisher;
    }

    @Override
    protected TwinSubscriber createSubscriber(final MinionIdentity identity) {
        final KafkaConfigProvider config = () -> {
            final var properties = new Properties();
            properties.put("bootstrap.servers", kafkaServer.getKafkaConnectString());
            return properties;
        };
        TracerRegistry tracerRegistry = Mockito.mock(TracerRegistry.class);
        Mockito.when(tracerRegistry.getTracer()).thenReturn(GlobalTracer.get());
        final var kafkaTwinSubscriber = new KafkaTwinSubscriber(identity, config, tracerRegistry, new MetricRegistry());
        try {
            kafkaTwinSubscriber.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return kafkaTwinSubscriber;
    }

    @Test
    public void retryTest() throws Exception {
        // This test is kafka-specific for now.
        // There is no generic way to stop/break other message brokers.

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        this.kafkaServer.stopKafkaServer();

        Thread.sleep(5000);

        final var tracker = Tracker.subscribe(this.createSubscriber(new MockMinionIdentity("Default")), "test", String.class);

        this.kafkaServer.startKafkaServer();

        // Ensure Test1 is received.
        await().until(tracker::getLog, contains("Test1"));
    }
}
