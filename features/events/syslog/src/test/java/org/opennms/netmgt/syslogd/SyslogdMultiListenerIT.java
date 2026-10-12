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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.codahale.metrics.MetricRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.syslogd.SyslogTcpConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;

/**
 * Runs the UDP and TCP receivers together under one Syslogd, which is what an install
 * that switches TCP on actually gets.
 */
public class SyslogdMultiListenerIT {

    private static final String UDP_MESSAGE = "<34>Oct 11 22:14:15 udphost app: over udp";
    private static final String TCP_MESSAGE = "<34>Oct 11 22:14:15 tcphost app: over tcp";

    private static final long RECEIVE_TIMEOUT_SECONDS = 15;

    private Syslogd m_syslogd;
    private SharedRegistryDispatcherFactory m_dispatcherFactory;
    private DistPollerDao m_distPollerDao;
    private int m_udpPort;
    private int m_tcpPort;

    private final LinkedBlockingQueue<String> m_received = new LinkedBlockingQueue<>();

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        m_udpPort = findFreePort();
        m_tcpPort = findFreePort();

        // No listen-address on the tcp element, so this also covers the fallback to the
        // address the UDP listener was given.
        final String xml = "<syslogd-configuration>\n"
                + "  <configuration syslog-port=\"" + m_udpPort + "\"\n"
                + "                 listen-address=\"127.0.0.1\"\n"
                + "                 batch-size=\"1\"\n"
                + "                 batch-interval=\"10\"\n"
                + "                 parser=\"org.opennms.netmgt.syslogd.RadixTreeSyslogParser\">\n"
                + "    <tcp port=\"" + m_tcpPort + "\"/>\n"
                + "  </configuration>\n"
                + "</syslogd-configuration>\n";

        final SyslogdConfigFactory config = new SyslogdConfigFactory(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        m_distPollerDao = mock(DistPollerDao.class, Mockito.RETURNS_DEEP_STUBS);
        when(m_distPollerDao.whoami().getId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(m_distPollerDao.whoami().getLocation()).thenReturn("Default");

        m_dispatcherFactory = new SharedRegistryDispatcherFactory();
        m_dispatcherFactory.setConsumer(new CollectingConsumer());

        // One receiver, both sockets, one dispatcher. Two receivers would each create a
        // dispatcher for the same sink module and the loser of that race would die.
        final SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(config);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(m_dispatcherFactory);

        m_syslogd = new Syslogd();
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        m_syslogd.start();

        waitForTcpListener();
    }

    @After
    public void tearDown() {
        if (m_syslogd != null) {
            m_syslogd.stop();
            m_syslogd = null;
        }
    }

    @Test(timeout = 60 * 1000)
    public void receivesOverBothTransportsAtOnce() throws Exception {
        sendUdp(UDP_MESSAGE);
        sendTcp(TCP_MESSAGE);

        final Set<String> received = new HashSet<>();
        received.add(nextMessage());
        received.add(nextMessage());

        assertEquals(new HashSet<>(Arrays.asList(UDP_MESSAGE, TCP_MESSAGE)), received);
    }

    @Test(timeout = 60 * 1000)
    public void bothSocketsShareOneDispatcher() throws Exception {
        // The Sink API names its metrics after the module id, so a second dispatcher for the
        // same module throws and kills whichever listener lost the race. The plain mock
        // factory hands out a fresh registry per call and cannot catch it.
        sendUdp(UDP_MESSAGE);
        sendTcp(TCP_MESSAGE);
        nextMessage();
        nextMessage();

        assertEquals("both sockets must share a single dispatcher",
                1, m_dispatcherFactory.getDispatchersCreated());
    }

    @Test(timeout = 60 * 1000)
    public void stoppingReleasesBothPorts() throws Exception {
        sendTcp(TCP_MESSAGE);
        assertEquals(TCP_MESSAGE, nextMessage());

        m_syslogd.stop();
        m_syslogd = null;

        // Rebinding is the only assertion that actually proves the sockets were released
        // rather than merely marked as stopped.
        try (ServerSocket tcp = new ServerSocket()) {
            tcp.bind(new java.net.InetSocketAddress("127.0.0.1", m_tcpPort));
            assertTrue("the TCP port should have been released", tcp.isBound());
        }
        try (DatagramSocket udp = new DatagramSocket(null)) {
            udp.bind(new java.net.InetSocketAddress("127.0.0.1", m_udpPort));
            assertTrue("the UDP port should have been released", udp.isBound());
        }
    }

    /**
     * getTcpConfig() throws on an unparseable framing or client-auth value, and the XSD that
     * would have caught it is only consulted when validation is switched on. That exception
     * used to escape run() and take the UDP loop with it.
     */
    @Test(timeout = 60 * 1000)
    public void udpKeepsRunningWhenTheTcpConfigIsUnusable() throws Exception {
        m_syslogd.stop();
        m_syslogd = null;
        m_received.clear();

        m_udpPort = findFreePort();
        final SyslogdConfig broken = mock(SyslogdConfig.class);
        when(broken.getListenAddress()).thenReturn("127.0.0.1");
        when(broken.getSyslogPort()).thenReturn(m_udpPort);
        when(broken.getNumThreads()).thenReturn(1);
        when(broken.getBatchSize()).thenReturn(1);
        when(broken.getBatchIntervalMs()).thenReturn(10);
        when(broken.getQueueSize()).thenReturn(10);
        when(broken.getTcpConfig())
                .thenThrow(new IllegalArgumentException("Unsupported syslog TCP framing 'octetcounting'"));

        // A fresh factory: the fixture's dispatcher registered its metrics in the shared
        // registry and closing it does not release the names.
        final SharedRegistryDispatcherFactory dispatcherFactory = new SharedRegistryDispatcherFactory();
        dispatcherFactory.setConsumer(new CollectingConsumer());

        final SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(broken);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(dispatcherFactory);

        m_syslogd = new Syslogd();
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        m_syslogd.start();

        // Resent until it lands: there is no TCP listener to wait for here, and a datagram
        // sent before the socket binds is simply gone.
        String received = null;
        for (int i = 0; i < 50 && received == null; i++) {
            sendUdp(UDP_MESSAGE);
            received = m_received.poll(1, TimeUnit.SECONDS);
        }

        assertEquals("UDP must keep working when the TCP configuration is unusable", UDP_MESSAGE, received);
        assertNull("no TCP listener should have been started", receiver.getTcpListener());
    }

    /**
     * The Minion shape: flat properties set on the config bean, one of them a typo. Setting it
     * must not throw, because there it is a Blueprint property injection and a bean that
     * throws fails the container that also owns the UDP listener.
     */
    @Test(timeout = 60 * 1000)
    public void udpKeepsRunningWhenATcpPropertyIsATypo() throws Exception {
        m_syslogd.stop();
        m_syslogd = null;
        m_received.clear();

        m_udpPort = findFreePort();
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(findFreePort());
        tcpConfig.setListenAddress("127.0.0.1");
        tcpConfig.setFraming("octetcounting");

        final SyslogConfigBean bean = new SyslogConfigBean();
        bean.setSyslogPort(m_udpPort);
        bean.setListenAddress("127.0.0.1");
        bean.setParser("org.opennms.netmgt.syslogd.RadixTreeSyslogParser");
        bean.setBatchSize(1);
        bean.setBatchIntervalMs(10);
        bean.setNumThreads(1);
        bean.setQueueSize(16);
        bean.setTcpConfig(tcpConfig);

        final SharedRegistryDispatcherFactory dispatcherFactory = new SharedRegistryDispatcherFactory();
        dispatcherFactory.setConsumer(new CollectingConsumer());

        final SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(bean);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(dispatcherFactory);

        m_syslogd = new Syslogd();
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        m_syslogd.start();

        String received = null;
        for (int i = 0; i < 50 && received == null; i++) {
            sendUdp(UDP_MESSAGE);
            received = m_received.poll(1, TimeUnit.SECONDS);
        }

        assertEquals("UDP must survive a typo in a TCP property", UDP_MESSAGE, received);
        assertFalse("the TCP listener must not have bound",
                receiver.getTcpListener() != null && receiver.getTcpListener().isStarted());
    }

    /**
     * A UDP-only install builds no listener at all, so it pays none of the waits in
     * SyslogTcpListener.stop() for a feature it does not use.
     */
    @Test(timeout = 60 * 1000)
    public void aUdpOnlyInstallBuildsNoTcpListener() throws Exception {
        m_syslogd.stop();
        m_syslogd = null;
        m_received.clear();

        m_udpPort = findFreePort();
        final SyslogConfigBean bean = new SyslogConfigBean();
        bean.setSyslogPort(m_udpPort);
        bean.setListenAddress("127.0.0.1");
        bean.setParser("org.opennms.netmgt.syslogd.RadixTreeSyslogParser");
        bean.setBatchSize(1);
        bean.setBatchIntervalMs(10);
        bean.setNumThreads(1);
        bean.setQueueSize(16);

        final SharedRegistryDispatcherFactory dispatcherFactory = new SharedRegistryDispatcherFactory();
        dispatcherFactory.setConsumer(new CollectingConsumer());

        final SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(bean);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(dispatcherFactory);

        m_syslogd = new Syslogd();
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        m_syslogd.start();

        // A delivered message proves run() is past the point where it would have built a
        // listener, so the assertion below is not just winning a race.
        String received = null;
        for (int i = 0; i < 50 && received == null; i++) {
            sendUdp(UDP_MESSAGE);
            received = m_received.poll(1, TimeUnit.SECONDS);
        }

        assertEquals(UDP_MESSAGE, received);
        assertNull("a config with no tcp port must not build a listener", receiver.getTcpListener());
    }

    /**
     * Every other test here sets batch-size to 1, which makes each message its own batch and
     * hides how the listener interacts with real aggregation. This one leaves the shipped
     * batch settings alone.
     */
    @Test(timeout = 120 * 1000)
    public void deliversABurstUnderTheShippedBatchSettings() throws Exception {
        m_syslogd.stop();
        m_syslogd = null;
        m_received.clear();

        final int count = 200;
        m_udpPort = findFreePort();
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(findFreePort());
        tcpConfig.setListenAddress("127.0.0.1");

        final SyslogConfigBean bean = new SyslogConfigBean();
        bean.setSyslogPort(m_udpPort);
        bean.setListenAddress("127.0.0.1");
        bean.setParser("org.opennms.netmgt.syslogd.RadixTreeSyslogParser");
        bean.setNumThreads(4);
        bean.setQueueSize(10000);
        // batch-size and batch-interval left at their defaults on purpose
        bean.setTcpConfig(tcpConfig);

        final SharedRegistryDispatcherFactory dispatcherFactory = new SharedRegistryDispatcherFactory();
        dispatcherFactory.setConsumer(new CollectingConsumer());

        final SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(bean);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(dispatcherFactory);

        m_syslogd = new Syslogd();
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        m_syslogd.start();
        for (int i = 0; i < 100 && (receiver.getTcpListener() == null
                || !receiver.getTcpListener().isStarted()); i++) {
            Thread.sleep(50);
        }

        try (Socket socket = new Socket("127.0.0.1", tcpConfig.getPort())) {
            final StringBuilder burst = new StringBuilder();
            for (int i = 0; i < count; i++) {
                burst.append("<34>Oct 11 22:14:15 tcphost app: batched ").append(i).append('\n');
            }
            socket.getOutputStream().write(burst.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            for (int i = 0; i < count; i++) {
                assertNotNull("stalled after " + i + " of " + count + " under real batching",
                        m_received.poll(30, TimeUnit.SECONDS));
            }
        }
    }

    // --- harness ------------------------------------------------------------


    private void waitForTcpListener() throws Exception {
        for (int i = 0; i < 100; i++) {
            try (Socket probe = new Socket("127.0.0.1", m_tcpPort)) {
                return;
            } catch (Exception e) {
                Thread.sleep(100);
            }
        }
        throw new AssertionError("the TCP listener never came up on port " + m_tcpPort);
    }

    private void sendUdp(final String message) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            final byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), m_udpPort));
        }
    }

    private void sendTcp(final String message) throws Exception {
        try (Socket socket = new Socket("127.0.0.1", m_tcpPort)) {
            socket.getOutputStream().write((message + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
        }
    }

    /**
     * A dispatcher factory that shares one MetricRegistry across every dispatcher it
     * creates, which is what the production factory does. The plain mock returns a fresh
     * registry per call and therefore cannot reproduce a duplicate metric registration.
     */
    private static class SharedRegistryDispatcherFactory
            extends MockMessageDispatcherFactory<SyslogConnection, SyslogMessageLogDTO> {

        private final MetricRegistry metrics = new MetricRegistry();
        private final AtomicInteger dispatchersCreated = new AtomicInteger();

        @Override
        public MetricRegistry getMetrics() {
            return metrics;
        }

        @Override
        public <S extends org.opennms.core.ipc.sink.api.Message, T extends org.opennms.core.ipc.sink.api.Message>
                org.opennms.core.ipc.sink.api.AsyncDispatcher<S> createAsyncDispatcher(
                        final org.opennms.core.ipc.sink.api.SinkModule<S, T> module) {
            dispatchersCreated.incrementAndGet();
            return super.createAsyncDispatcher(module);
        }

        int getDispatchersCreated() {
            return dispatchersCreated.get();
        }
    }

    private class CollectingConsumer implements MessageConsumer<SyslogConnection, SyslogMessageLogDTO> {
        @Override
        public SinkModule<SyslogConnection, SyslogMessageLogDTO> getModule() {
            return null;
        }

        @Override
        public void handleMessage(final SyslogMessageLogDTO messageLog) {
            for (final SyslogMessageDTO message : messageLog.getMessages()) {
                final ByteBuffer buffer = message.getBytes();
                final byte[] bytes = new byte[buffer.limit()];
                buffer.duplicate().rewind().get(bytes);
                m_received.add(new String(bytes, StandardCharsets.UTF_8).trim());
            }
        }
    }

    private String nextMessage() throws InterruptedException {
        final String message = m_received.poll(RECEIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull("timed out waiting for a syslog message", message);
        return message;
    }

    private static int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
