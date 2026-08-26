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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;

/**
 * Drives the TCP listener over a real socket, from a real syslogd-configuration.xml, so
 * that the XSD, the JAXB model, the config factory and the listener are all exercised
 * together rather than only the decoder in isolation.
 */
public class SyslogdReceiverNettyTcpIT {

    private static final String MESSAGE = "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8";
    private static final String OTHER_MESSAGE = "<13>Oct 11 22:14:16 otherhost sshd: accepted publickey for lonvick";

    private static final long RECEIVE_TIMEOUT_SECONDS = 15;

    private SyslogReceiverJavaNetImpl m_receiver;

    private Thread m_receiverThread;

    private final LinkedBlockingQueue<String> m_received = new LinkedBlockingQueue<>();

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @After
    public void tearDown() throws Exception {
        if (m_receiver != null) {
            m_receiver.stop();
            m_receiver = null;
            if (m_receiverThread != null) {
                m_receiverThread.join(10000);
                m_receiverThread = null;
            }
        }
    }

    @Test(timeout = 60 * 1000)
    public void receivesLfDelimitedMessages() throws Exception {
        final int port = startReceiver("auto");

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, MESSAGE + "\n" + OTHER_MESSAGE + "\n");
        }

        assertEquals(MESSAGE, nextMessage());
        assertEquals(OTHER_MESSAGE, nextMessage());
    }

    @Test(timeout = 60 * 1000)
    public void receivesOctetCountedMessages() throws Exception {
        final int port = startReceiver("auto");

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, octetCounted(MESSAGE) + octetCounted(OTHER_MESSAGE));
        }

        assertEquals(MESSAGE, nextMessage());
        assertEquals(OTHER_MESSAGE, nextMessage());
    }

    @Test(timeout = 60 * 1000)
    public void preservesMessageOrderWithinAConnection() throws Exception {
        // Only guaranteed when asked for: the sink reorders pairs on its own, so ordered
        // holds one message in flight per connection to keep it out of that race.
        final int count = 200;
        final int port = startReceiver("auto", true);

        final StringBuilder burst = new StringBuilder();
        for (int i = 0; i < count; i++) {
            burst.append("<34>Oct 11 22:14:15 mymachine app: message ").append(i).append('\n');
        }

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, burst.toString());

            for (int i = 0; i < count; i++) {
                assertEquals("<34>Oct 11 22:14:15 mymachine app: message " + i, nextMessage());
            }
        }
    }

    @Test(timeout = 60 * 1000)
    public void preservesMessageOrderForOctetCountedBursts() throws Exception {
        final int count = 200;
        final int port = startReceiver("octet-counting", true);

        final StringBuilder burst = new StringBuilder();
        for (int i = 0; i < count; i++) {
            burst.append(octetCounted("<34>Oct 11 22:14:15 mymachine app: message " + i));
        }

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, burst.toString());

            for (int i = 0; i < count; i++) {
                assertEquals("<34>Oct 11 22:14:15 mymachine app: message " + i, nextMessage());
            }
        }
    }

    @Test(timeout = 60 * 1000)
    public void receivesMessagesAcrossSeparateWrites() throws Exception {
        // Each write becomes its own TCP segment, so the listener has to reassemble a
        // message that arrives in pieces.
        final int port = startReceiver("auto");

        try (Socket socket = new Socket("127.0.0.1", port)) {
            final OutputStream out = socket.getOutputStream();
            out.write(MESSAGE.substring(0, 20).getBytes(StandardCharsets.UTF_8));
            out.flush();
            Thread.sleep(250);
            out.write(MESSAGE.substring(20).getBytes(StandardCharsets.UTF_8));
            out.write('\n');
            out.flush();
        }

        assertEquals(MESSAGE, nextMessage());
    }

    @Test(timeout = 60 * 1000)
    public void receivesFromSeveralConnectionsAtOnce() throws Exception {
        final int port = startReceiver("auto");

        final Set<String> expected = new HashSet<>();
        final List<Socket> sockets = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                final Socket socket = new Socket("127.0.0.1", port);
                sockets.add(socket);
                final String message = "<34>Oct 11 22:14:15 host" + i + " app: message " + i;
                expected.add(message);
                write(socket, message + "\n");
            }

            final Set<String> received = new HashSet<>();
            for (int i = 0; i < expected.size(); i++) {
                received.add(nextMessage());
            }
            assertEquals(expected, received);
        } finally {
            for (final Socket socket : sockets) {
                socket.close();
            }
        }
    }

    @Test(timeout = 60 * 1000)
    public void keepsTheSenderAddressRatherThanTheListenerAddress() throws Exception {
        final int port = startReceiver("auto");
        final LinkedBlockingQueue<InetSocketAddress> sources = new LinkedBlockingQueue<>();
        m_sourceSink = sources;

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, MESSAGE + "\n");
            assertEquals(MESSAGE, nextMessage());

            final InetSocketAddress source = sources.poll(RECEIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(source);
            assertEquals(socket.getLocalPort(), source.getPort());
            assertEquals("127.0.0.1", source.getAddress().getHostAddress());
        }
    }

    @Test(timeout = 60 * 1000)
    public void honoursAForcedFraming() throws Exception {
        final int port = startReceiver("non-transparent");

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, octetCounted(MESSAGE) + "\n");
        }

        // The forced framing wins over what the bytes look like, which is what an
        // operator sees as a leaked length prefix when the two ends disagree.
        assertEquals(MESSAGE.length() + " " + MESSAGE, nextMessage());
    }

    @Test(timeout = 60 * 1000)
    public void keepsServingOtherConnectionsAfterOneIsDropped() throws Exception {
        final int port = startReceiver("octet-counting");

        // A framing error drops this connection.
        try (Socket bad = new Socket("127.0.0.1", port)) {
            write(bad, "12x4 " + MESSAGE);
        }

        try (Socket good = new Socket("127.0.0.1", port)) {
            write(good, octetCounted(OTHER_MESSAGE));
        }

        assertEquals(OTHER_MESSAGE, nextMessage());
    }

    // --- harness ------------------------------------------------------------

    private LinkedBlockingQueue<InetSocketAddress> m_sourceSink;

    private int startReceiver(final String framing) throws Exception {
        return startReceiver(framing, false);
    }

    private int startReceiver(final String framing, final boolean ordered) throws Exception {
        final int port = findFreePort();

        final String xml = "<syslogd-configuration>\n"
                + "  <configuration syslog-port=\"" + findFreePort() + "\"\n"
                + "                 batch-size=\"1\"\n"
                + "                 batch-interval=\"10\"\n"
                + "                 parser=\"org.opennms.netmgt.syslogd.RadixTreeSyslogParser\">\n"
                + "    <tcp port=\"" + port + "\" listen-address=\"127.0.0.1\" framing=\"" + framing + "\""
                + " ordered=\"" + ordered + "\"/>\n"
                + "  </configuration>\n"
                + "</syslogd-configuration>\n";

        final SyslogdConfigFactory config = new SyslogdConfigFactory(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        final DistPollerDao distPollerDao = mock(DistPollerDao.class, Mockito.RETURNS_DEEP_STUBS);
        when(distPollerDao.whoami().getId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(distPollerDao.whoami().getLocation()).thenReturn("Default");

        final MockMessageDispatcherFactory<SyslogConnection, SyslogMessageLogDTO> dispatcherFactory =
                new MockMessageDispatcherFactory<>();
        dispatcherFactory.setConsumer(new CollectingConsumer());

        m_receiver = new SyslogReceiverJavaNetImpl(config);
        m_receiver.setDistPollerDao(distPollerDao);
        m_receiver.setMessageDispatcherFactory(dispatcherFactory);
        startReceiverThread();


        return port;
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
                final byte[] bytes = new byte[buffer.remaining() > 0 ? buffer.remaining() : buffer.limit()];
                buffer.duplicate().rewind().get(bytes);
                m_received.add(new String(bytes, StandardCharsets.UTF_8));
            }
            if (m_sourceSink != null) {
                m_sourceSink.add(new InetSocketAddress(messageLog.getSourceAddress(), messageLog.getSourcePort()));
            }
        }
    }

    private String nextMessage() throws InterruptedException {
        final String message = m_received.poll(RECEIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull("timed out waiting for a syslog message", message);
        return message;
    }

    private static void write(final Socket socket, final String content) throws Exception {
        socket.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
    }

    private static String octetCounted(final String message) {
        return message.getBytes(StandardCharsets.UTF_8).length + " " + message;
    }

    private static int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * The receiver's run() blocks in its UDP receive loop and never returns, exactly as it
     * does under Syslogd, so it gets its own thread here. The TCP socket is bound from that
     * thread, hence the wait.
     */
    private void startReceiverThread() throws Exception {
        m_receiverThread = new Thread(m_receiver, "test-syslog-receiver");
        m_receiverThread.setDaemon(true);
        m_receiverThread.start();

        for (int i = 0; i < 100; i++) {
            final SyslogTcpListener listener = m_receiver.getTcpListener();
            if (listener != null && listener.isStarted()) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("the TCP listener never bound");
    }
}
