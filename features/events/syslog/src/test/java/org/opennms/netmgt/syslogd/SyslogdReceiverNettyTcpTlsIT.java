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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.syslogd.SyslogTcpConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;


/**
 * Exercises syslog over TLS against a real socket, per RFC 5425.
 *
 * A Netty client is used rather than an SSLSocket so that the test can trust a specific
 * PEM certificate without first converting it into a KeyStore.
 */
public class SyslogdReceiverNettyTcpTlsIT {

    private static final String MESSAGE = "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8";

    private static final long RECEIVE_TIMEOUT_SECONDS = 15;
    private static final long REJECTION_TIMEOUT_SECONDS = 5;

    private static SelfSignedCertificate s_serverCertificate;
    private static SelfSignedCertificate s_clientCertificate;

    private SyslogReceiverJavaNetImpl m_receiver;
    private EventLoopGroup m_clientGroup;

    private Thread m_receiverThread;

    private final LinkedBlockingQueue<String> m_received = new LinkedBlockingQueue<>();

    @BeforeClass
    public static void generateCertificates() throws Exception {
        s_serverCertificate = new SelfSignedCertificate();
        s_clientCertificate = new SelfSignedCertificate();
    }

    @AfterClass
    public static void deleteCertificates() {
        if (s_serverCertificate != null) {
            s_serverCertificate.delete();
        }
        if (s_clientCertificate != null) {
            s_clientCertificate.delete();
        }
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_clientGroup = new NioEventLoopGroup();
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
        if (m_clientGroup != null) {
            m_clientGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).awaitUninterruptibly(10, TimeUnit.SECONDS);
            m_clientGroup = null;
        }
    }

    @Test(timeout = 60 * 1000)
    public void receivesMessagesOverTls() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        final int port = startReceiver(config);

        final Channel client = connect(port, clientContext(false));
        try {
            client.writeAndFlush(Unpooled.copiedBuffer(MESSAGE + "\n", StandardCharsets.UTF_8)).sync();
            assertEquals(MESSAGE, nextMessage());
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void receivesOctetCountedMessagesOverTls() throws Exception {
        // RFC 5425 specifies octet counting for the TLS transport, so this is the
        // combination a standards-following sender actually produces.
        final SyslogTcpConfig config = tlsConfig();
        final int port = startReceiver(config);

        final Channel client = connect(port, clientContext(false));
        try {
            final String framed = MESSAGE.getBytes(StandardCharsets.UTF_8).length + " " + MESSAGE;
            client.writeAndFlush(Unpooled.copiedBuffer(framed, StandardCharsets.UTF_8)).sync();
            assertEquals(MESSAGE, nextMessage());
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void receivesABurstOverTlsOnOneConnection() throws Exception {
        // Reads are paused while each dispatch is outstanding. SslHandler can hold decrypted
        // data of its own, and re-enabling autoRead does not by itself make it hand that
        // data on, so a burst can stop after the first message even though the socket is
        // still open. That is what a Minion did over TLS: 1 of 25.
        final int count = 50;
        final SyslogTcpConfig config = tlsConfig();
        final int port = startReceiver(config);

        final Channel client = connect(port, clientContext(false));
        try {
            final StringBuilder burst = new StringBuilder();
            for (int i = 0; i < count; i++) {
                final String message = "<34>Oct 11 22:14:15 tlsburst app: message " + i;
                burst.append(message.getBytes(StandardCharsets.UTF_8).length).append(' ').append(message);
            }
            client.writeAndFlush(Unpooled.copiedBuffer(burst.toString(), StandardCharsets.UTF_8)).sync();

            for (int i = 0; i < count; i++) {
                assertEquals("<34>Oct 11 22:14:15 tlsburst app: message " + i, nextMessage());
            }
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void acceptsASenderPresentingATrustedCertificate() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        config.setTlsClientAuth("require");
        config.setTlsTrustCertFilePath(s_clientCertificate.certificate().getAbsolutePath());
        final int port = startReceiver(config);

        final Channel client = connect(port, clientContext(true));
        try {
            client.writeAndFlush(Unpooled.copiedBuffer(MESSAGE + "\n", StandardCharsets.UTF_8)).sync();
            assertEquals(MESSAGE, nextMessage());
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void rejectsASenderWithNoCertificateWhenClientAuthIsRequired() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        config.setTlsClientAuth("require");
        config.setTlsTrustCertFilePath(s_clientCertificate.certificate().getAbsolutePath());
        final int port = startReceiver(config);

        final Channel client = connect(port, clientContext(false));
        try {
            client.writeAndFlush(Unpooled.copiedBuffer(MESSAGE + "\n", StandardCharsets.UTF_8));

            assertNull("a sender with no certificate must not get its messages accepted",
                    m_received.poll(REJECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void acceptsASenderWithNoCertificateWhenClientAuthIsOptional() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        config.setTlsClientAuth("optional");
        config.setTlsTrustCertFilePath(s_clientCertificate.certificate().getAbsolutePath());
        final int port = startReceiver(config);

        final Channel client = connect(port, clientContext(false));
        try {
            client.writeAndFlush(Unpooled.copiedBuffer(MESSAGE + "\n", StandardCharsets.UTF_8)).sync();
            assertEquals(MESSAGE, nextMessage());
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void doesNotAcceptPlaintextOnATlsPort() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        final int port = startReceiver(config);

        try (java.net.Socket plaintext = new java.net.Socket("127.0.0.1", port)) {
            plaintext.getOutputStream().write((MESSAGE + "\n").getBytes(StandardCharsets.UTF_8));
            plaintext.getOutputStream().flush();

            assertNull("plaintext must not be accepted on a TLS port",
                    m_received.poll(REJECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }
    }

    @Test(timeout = 60 * 1000)
    public void doesNotStartWithAnUnusableCertificatePath() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        config.setTlsCertFilePath("/definitely/not/here/syslog.crt");
        final int port = findFreePort();
        config.setPort(port);

        startReceiverExpectingFailure(config);

        // Falling back to plaintext here would accept syslog on a port the operator
        // believes is encrypted, so the listener must simply not come up.
        assertFalse(m_receiver.getTcpListener() != null && m_receiver.getTcpListener().isStarted());
        try (ServerSocket rebind = new ServerSocket(port)) {
            assertNotNull("the TLS port should have been left free", rebind);
        }
    }

    // --- harness ------------------------------------------------------------

    private SyslogTcpConfig tlsConfig() throws Exception {
        final SyslogTcpConfig config = new SyslogTcpConfig();
        config.setPort(findFreePort());
        config.setListenAddress("127.0.0.1");
        config.setFraming("auto");
        config.setTlsEnabled(true);
        config.setTlsCertFilePath(s_serverCertificate.certificate().getAbsolutePath());
        config.setTlsPrivateKeyFilePath(s_serverCertificate.privateKey().getAbsolutePath());
        return config;
    }

    private SslContext clientContext(final boolean presentClientCertificate) throws Exception {
        final SslContextBuilder builder = SslContextBuilder.forClient()
                .trustManager(s_serverCertificate.certificate())
                // Netty 4.2 verifies the hostname by default. The generated certificate
                // names example.com and carries no SAN for the loopback address, and
                // hostname verification is the sender's job rather than the listener's,
                // so the chain stays pinned through trustManager and nothing else.
                .endpointIdentificationAlgorithm(null);
        if (presentClientCertificate) {
            builder.keyManager(s_clientCertificate.certificate(), s_clientCertificate.privateKey());
        }
        return builder.build();
    }

    private Channel connect(final int port, final SslContext sslContext) throws Exception {
        return new Bootstrap()
                .group(m_clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) {
                        ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                    }
                })
                .connect("127.0.0.1", port)
                .sync()
                .channel();
    }

    private int startReceiver(final SyslogTcpConfig config) throws Exception {
        buildReceiver(config);
        startReceiverThread();
        return config.getPort();
    }

    /**
     * Starts the receiver without waiting for a TCP bind, for the cases where the listener
     * is supposed to refuse to come up.
     */
    private void startReceiverExpectingFailure(final SyslogTcpConfig config) throws Exception {
        buildReceiver(config);
        m_receiverThread = new Thread(m_receiver, "test-syslog-receiver");
        m_receiverThread.setDaemon(true);
        m_receiverThread.start();
        Thread.sleep(2000);
    }

    private void buildReceiver(final SyslogTcpConfig config) {
        final SyslogConfigBean syslogConfig = new SyslogConfigBean();
        syslogConfig.setSyslogPort(0);
        syslogConfig.setListenAddress("127.0.0.1");
        syslogConfig.setNumThreads(2);
        syslogConfig.setQueueSize(1000);
        syslogConfig.setBatchSize(1);
        syslogConfig.setBatchIntervalMs(10);
        syslogConfig.setTcpConfig(config);

        final DistPollerDao distPollerDao = mock(DistPollerDao.class, Mockito.RETURNS_DEEP_STUBS);
        when(distPollerDao.whoami().getId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(distPollerDao.whoami().getLocation()).thenReturn("Default");

        final MockMessageDispatcherFactory<SyslogConnection, SyslogMessageLogDTO> dispatcherFactory =
                new MockMessageDispatcherFactory<>();
        dispatcherFactory.setConsumer(new CollectingConsumer());

        m_receiver = new SyslogReceiverJavaNetImpl(syslogConfig);
        m_receiver.setDistPollerDao(distPollerDao);
        m_receiver.setMessageDispatcherFactory(dispatcherFactory);
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
                m_received.add(new String(bytes, StandardCharsets.UTF_8));
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
