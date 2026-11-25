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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.awaitility.Awaitility;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;

import com.codahale.metrics.MetricRegistry;

public class SyslogReceiverPacketMetricsIT {

    private static final int PACKETS = 25;

    @Test(timeout = 60000)
    public void camelNettyReceiverCountsPackets() throws Exception {
        final int port = freeUdpPort();
        final MetricRegistry metrics = new MetricRegistry();
        verifyPacketCount(new SyslogReceiverCamelNettyImpl(config(port), metrics), metrics, port);
    }

    @Test(timeout = 60000)
    public void javaNetReceiverCountsPackets() throws Exception {
        final int port = freeUdpPort();
        final MetricRegistry metrics = new MetricRegistry();
        verifyPacketCount(new SyslogReceiverJavaNetImpl(config(port), metrics), metrics, port);
    }

    // a port of its own per test, so a port still held by another test cannot stall the listener
    private static int freeUdpPort() throws Exception {
        try (DatagramSocket socket = new DatagramSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static SyslogdConfig config(final int port) {
        final SyslogdConfig config = mock(SyslogdConfig.class);
        when(config.getSyslogPort()).thenReturn(port);
        when(config.getListenAddress()).thenReturn("127.0.0.1");
        when(config.getNumThreads()).thenReturn(1);
        when(config.getQueueSize()).thenReturn(100);
        return config;
    }

    @SuppressWarnings("unchecked")
    private static void verifyPacketCount(final SinkDispatchingSyslogReceiver receiver, final MetricRegistry metrics,
            final int port) throws Exception {
        final AsyncDispatcher<SyslogConnection> dispatcher = mock(AsyncDispatcher.class);
        when(dispatcher.send(any())).thenReturn(CompletableFuture.completedFuture(AsyncDispatcher.DispatchStatus.DISPATCHED));
        final MessageDispatcherFactory dispatcherFactory = mock(MessageDispatcherFactory.class);
        when(dispatcherFactory.createAsyncDispatcher(any())).thenReturn((AsyncDispatcher) dispatcher);
        final DistPollerDao distPollerDao = mock(DistPollerDao.class, Mockito.RETURNS_DEEP_STUBS);
        when(distPollerDao.whoami().getId()).thenReturn("");
        when(distPollerDao.whoami().getLocation()).thenReturn("");

        receiver.setMessageDispatcherFactory(dispatcherFactory);
        receiver.setDistPollerDao(distPollerDao);

        final Thread thread = new Thread(receiver);
        thread.start();
        try {
            final SyslogClient client = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG, addr("127.0.0.1"));
            client.syslogPort = port;
            // the listener binds asynchronously, so keep sending until the first packet is counted
            Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(200, TimeUnit.MILLISECONDS).until(() -> {
                client.syslog(SyslogClient.LOG_DEBUG, "warm-up");
                return metrics.meter(SinkDispatchingSyslogReceiver.UDP_PACKETS_RECEIVED).getCount() > 0;
            });
            // take the baseline only once warm-up packets still in flight have all been counted
            final AtomicLong previous = new AtomicLong(-1);
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until(() -> {
                final long count = metrics.meter(SinkDispatchingSyslogReceiver.UDP_PACKETS_RECEIVED).getCount();
                return count == previous.getAndSet(count);
            });
            final long before = metrics.meter(SinkDispatchingSyslogReceiver.UDP_PACKETS_RECEIVED).getCount();
            for (int i = 0; i < PACKETS; i++) {
                client.syslog(SyslogClient.LOG_DEBUG, "packet " + i);
            }
            Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
                    metrics.meter(SinkDispatchingSyslogReceiver.UDP_PACKETS_RECEIVED).getCount() >= before + PACKETS);
            assertEquals(before + PACKETS, metrics.meter(SinkDispatchingSyslogReceiver.UDP_PACKETS_RECEIVED).getCount());
        } finally {
            receiver.stop();
            thread.join(10000);
        }
    }
}
