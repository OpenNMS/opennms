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
package org.opennms.core.ipc.twin.kafka;

import static org.awaitility.Awaitility.await;
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
import org.opennms.core.ipc.twin.api.LocalTwinSubscriber;
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
