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
package org.opennms.netmgt.telemetry.daemon;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.config.api.QueueDefinition;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.ByteString;

public class TelemetryMessageConsumerMetricsTest {

    @Test
    public void countsEveryMessageInALogPerQueue() throws Exception {
        final MetricRegistry metrics = new MetricRegistry();
        final TelemetryRegistry registry = mock(TelemetryRegistry.class);
        when(registry.getMetricRegistry()).thenReturn(metrics);
        final QueueDefinition queue = mock(QueueDefinition.class);
        when(queue.getName()).thenReturn("Netflow-9");

        final TelemetryMessageConsumer consumer = new TelemetryMessageConsumer(queue, Collections.emptyList(), mock(TelemetrySinkModule.class));
        consumer.setRegistry(registry);
        consumer.init();

        consumer.handleMessage(log(4));
        consumer.handleMessage(log(1));

        assertEquals(5, metrics.meter("consumers.Netflow-9.messagesReceived").getCount());
    }

    private static TelemetryProtos.TelemetryMessageLog log(final int size) {
        final TelemetryProtos.TelemetryMessageLog.Builder builder = TelemetryProtos.TelemetryMessageLog.newBuilder()
                .setLocation("Default")
                .setSystemId("00000000-0000-0000-0000-000000ddba11");
        for (int i = 0; i < size; i++) {
            builder.addMessage(TelemetryProtos.TelemetryMessage.newBuilder()
                    .setTimestamp(i)
                    .setBytes(ByteString.copyFromUtf8("record " + i)));
        }
        return builder.build();
    }
}
