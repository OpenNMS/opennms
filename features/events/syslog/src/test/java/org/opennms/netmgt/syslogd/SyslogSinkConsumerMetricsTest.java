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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;

import com.codahale.metrics.MetricRegistry;

public class SyslogSinkConsumerMetricsTest {

    @Test
    public void countsEveryMessageInABatch() {
        final MetricRegistry metrics = new MetricRegistry();
        final SyslogSinkConsumer consumer = new SyslogSinkConsumer(metrics);
        consumer.setSyslogdConfig(mock(SyslogdConfig.class));
        consumer.setEventForwarder(mock(EventForwarder.class));

        consumer.handleMessage(batch(3));
        consumer.handleMessage(batch(2));

        assertEquals(5, metrics.meter("consumer.messagesReceived").getCount());
        assertEquals(2, metrics.timer("consumer").getCount());
    }

    private static SyslogMessageLogDTO batch(final int size) {
        final List<SyslogMessageDTO> messages = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messages.add(new SyslogMessageDTO(ByteBuffer.wrap(("<13>test message " + i).getBytes(StandardCharsets.US_ASCII))));
        }
        return new SyslogMessageLogDTO("Default", "00000000-0000-0000-0000-000000ddba11",
                new InetSocketAddress(addr("127.0.0.1"), 514), messages);
    }
}
