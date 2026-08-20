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
import static org.junit.Assert.assertTrue;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.config.syslogd.SyslogTcpConfig;
import org.opennms.netmgt.config.syslogd.SyslogTcpTlsConfig;
import org.opennms.netmgt.syslogd.api.SyslogConnection;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Pins how the listener is allowed to call the sink dispatcher.
 *
 * AsyncDispatcher.send() blocks when the sink queue is full and the module asks for
 * blockWhenFull, which SyslogSinkModule does. Calling it from a Netty event loop therefore
 * stalls that worker and every connection on it. That happened on a real Minion: exactly one
 * message per connection was ingested and then the socket went quiet with nothing logged.
 *
 * MockMessageDispatcherFactory cannot reproduce it, because the dispatcher it builds never
 * blocks in send(), so this test supplies its own.
 */
public class SyslogTcpListenerDispatchIT {

    private static final long TIMEOUT_SECONDS = 20;

    private static SelfSignedCertificate s_certificate;

    private SyslogTcpListener m_listener;
    private EventLoopGroup m_clientGroup;

    @BeforeClass
    public static void generateCertificate() throws Exception {
        s_certificate = new SelfSignedCertificate();
    }

    @AfterClass
    public static void deleteCertificate() {
        if (s_certificate != null) {
            s_certificate.delete();
        }
    }

    @After
    public void tearDown() {
        if (m_listener != null) {
            m_listener.stop();
            m_listener = null;
        }
        if (m_clientGroup != null) {
            m_clientGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).awaitUninterruptibly(10, TimeUnit.SECONDS);
            m_clientGroup = null;
        }
    }

    @Test(timeout = 120 * 1000)
    public void keepsIngestingOverTlsWhenEachDispatchIsSlow() throws Exception {
        // TLS plus a slow sink is the combination that failed on a Minion: 1 of 25. Reads are
        // paused while a dispatch is outstanding, and SslHandler holds decrypted data of its
        // own that re-enabling autoRead does not by itself hand on. A fast dispatcher hides
        // this because the pause is too short to matter.
        final int count = 25;
        final RecordingDispatcher dispatcher = new RecordingDispatcher(0);
        dispatcher.setDelayMillis(40);
        final int port = startTls(dispatcher);

        m_clientGroup = new NioEventLoopGroup();
        final Channel client = new Bootstrap()
                .group(m_clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                    @Override
                    protected void initChannel(final io.netty.channel.socket.SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(SslContextBuilder.forClient()
                                .trustManager(s_certificate.certificate())
                                .endpointIdentificationAlgorithm(null)
                                .build()
                                .newHandler(ch.alloc()));
                    }
                })
                .connect("127.0.0.1", port).sync().channel();

        try {
            final StringBuilder burst = new StringBuilder();
            for (int i = 0; i < count; i++) {
                burst.append("<34>Oct 11 22:14:15 host app: message ").append(i).append('\n');
            }
            client.writeAndFlush(Unpooled.copiedBuffer(burst.toString(), StandardCharsets.UTF_8)).sync();

            for (int i = 0; i < count; i++) {
                assertEquals("message " + i, dispatcher.nextMessage());
            }
        } finally {
            client.close().awaitUninterruptibly();
        }
    }

    @Test(timeout = 60 * 1000)
    public void keepsIngestingWhenTheDispatchFutureNeverCompletes() throws Exception {
        // A Minion whose syslog configuration had been reloaded delivered the message but
        // never completed the future for it, because a previous dispatcher's drain thread
        // completed the wrong one. The wait on that future has to be bounded, or ingestion
        // stops after a single message. The timeout is shortened here because the shipped one
        // is thirty seconds.
        final int count = 5;
        final RecordingDispatcher dispatcher = new RecordingDispatcher(0);
        dispatcher.setNeverCompleteFutures(true);
        final int port = start(dispatcher, 250);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            final StringBuilder burst = new StringBuilder();
            for (int i = 0; i < count; i++) {
                burst.append("<34>Oct 11 22:14:15 host app: message ").append(i).append('\n');
            }
            write(socket, burst.toString());

            for (int i = 0; i < count; i++) {
                assertEquals("message " + i, dispatcher.nextMessage());
            }
        }
    }

    @Test(timeout = 120 * 1000)
    public void doesNotCloseABusyConnectionAsIdle() throws Exception {
        // The idle timeout is meant to catch a sender that has gone quiet. Reads are paused
        // while a dispatch is outstanding, so a slow sink makes an actively sending
        // connection look idle.
        final int count = 400;
        final RecordingDispatcher dispatcher = new RecordingDispatcher(0);
        dispatcher.setDelayMillis(25);
        final int port = startWithIdleTimeout(dispatcher, 1);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            final StringBuilder burst = new StringBuilder();
            for (int i = 0; i < count; i++) {
                burst.append("<34>Oct 11 22:14:15 host app: message ").append(i).append('\n');
            }
            write(socket, burst.toString());

            for (int i = 0; i < count; i++) {
                assertEquals("message " + i, dispatcher.nextMessage());
            }
        }
    }

    @Test(timeout = 60 * 1000)
    public void neverCallsTheDispatcherFromAnEventLoopThread() throws Exception {
        final RecordingDispatcher dispatcher = new RecordingDispatcher(0);
        final int port = start(dispatcher);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            write(socket, "<34>Oct 11 22:14:15 host app: one\n");
            assertEquals("one", dispatcher.nextMessage());
        }

        // Netty names its event loop threads nioEventLoopGroup-N-M. A dispatch from one of
        // those is the defect, because send() is allowed to block.
        for (final String thread : dispatcher.callingThreads()) {
            assertFalse("send() was called on an event loop thread: " + thread,
                    thread.startsWith("nioEventLoopGroup"));
        }
    }

    @Test(timeout = 60 * 1000)
    public void keepsIngestingWhenTheSinkBlocks() throws Exception {
        // Blocks the first send until released, which is what a full sink queue does. With
        // the dispatch on the event loop this delivered the first message and then nothing.
        final RecordingDispatcher dispatcher = new RecordingDispatcher(1);
        final int port = start(dispatcher);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            final StringBuilder burst = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                burst.append("<34>Oct 11 22:14:15 host app: message ").append(i).append('\n');
            }
            write(socket, burst.toString());

            assertEquals("message 0", dispatcher.nextMessage());
            dispatcher.release();

            for (int i = 1; i < 5; i++) {
                assertEquals("message " + i, dispatcher.nextMessage());
            }
        }
    }

    /**
     * A failed bind used to leave the event loop groups running, since start() only logged and
     * returned and nothing else came back to close them. The dispatch pool is not the tell
     * here: a fixed pool creates no threads until something is submitted to it.
     */
    @Test(timeout = 60 * 1000)
    public void releasesItsThreadsWhenTheBindFails() throws Exception {
        final int before = countEventLoopThreads();

        try (ServerSocket occupied = new ServerSocket()) {
            occupied.bind(new java.net.InetSocketAddress("127.0.0.1", 0));

            final SyslogTcpConfig config = new SyslogTcpConfig();
            config.setPort(occupied.getLocalPort());
            config.setListenAddress("127.0.0.1");
            config.setFraming("non-transparent");

            m_listener = new SyslogTcpListener(config, new RecordingDispatcher(0));
            m_listener.start();
            assertFalse("the listener cannot have bound a port that was already taken",
                    m_listener.isStarted());
        }

        for (int i = 0; i < 150 && countEventLoopThreads() > before; i++) {
            Thread.sleep(100);
        }
        assertEquals("a failed bind must not leave event loop threads behind",
                before, countEventLoopThreads());
    }

    private static int countEventLoopThreads() {
        final Thread[] threads = new Thread[Thread.activeCount() * 2 + 64];
        final int found = Thread.enumerate(threads);
        int count = 0;
        for (int i = 0; i < found; i++) {
            if (threads[i] != null && threads[i].getName().startsWith("nioEventLoopGroup")) {
                count++;
            }
        }
        return count;
    }

    // --- harness ------------------------------------------------------------

    /** Lazily attaches the tls element, so each test only sets what it cares about. */
    private static SyslogTcpTlsConfig tlsOf(final SyslogTcpConfig config) {
        if (config.getTls() == null) {
            config.setTls(new SyslogTcpTlsConfig());
        }
        return config.getTls();
    }


    private int startTls(final AsyncDispatcher<SyslogConnection> dispatcher) throws Exception {
        final int port = findFreePort();

        final SyslogTcpConfig config = new SyslogTcpConfig();
        config.setPort(port);
        config.setListenAddress("127.0.0.1");
        config.setFraming("non-transparent");
        tlsOf(config).setEnabled(true);
        tlsOf(config).setCertFilePath(s_certificate.certificate().getAbsolutePath());
        tlsOf(config).setPrivateKeyFilePath(s_certificate.privateKey().getAbsolutePath());

        m_listener = new SyslogTcpListener(config, dispatcher);
        m_listener.start();
        assertTrue("the listener did not bind", m_listener.isStarted());
        return port;
    }

    private int start(final AsyncDispatcher<SyslogConnection> dispatcher) throws Exception {
        return start(dispatcher, 0);
    }

    private int startWithIdleTimeout(final AsyncDispatcher<SyslogConnection> dispatcher, final int idleSeconds)
            throws Exception {
        return start(dispatcher, 0, idleSeconds);
    }

    private int start(final AsyncDispatcher<SyslogConnection> dispatcher, final long dispatchTimeoutMs)
            throws Exception {
        return start(dispatcher, dispatchTimeoutMs, 0);
    }

    private int start(final AsyncDispatcher<SyslogConnection> dispatcher, final long dispatchTimeoutMs,
            final int idleSeconds) throws Exception {
        final int port = findFreePort();

        final SyslogTcpConfig config = new SyslogTcpConfig();
        config.setPort(port);
        config.setListenAddress("127.0.0.1");
        config.setFraming("non-transparent");
        if (idleSeconds > 0) {
            config.setIdleTimeoutSeconds(idleSeconds);
        }

        m_listener = new SyslogTcpListener(config, dispatcher);
        if (dispatchTimeoutMs > 0) {
            m_listener.setDispatchTimeoutMs(dispatchTimeoutMs);
        }
        m_listener.start();
        assertTrue("the listener did not bind", m_listener.isStarted());
        return port;
    }

    /**
     * Records the thread each send() came in on, and optionally blocks the first few sends
     * the way a full sink queue would.
     */
    private static class RecordingDispatcher implements AsyncDispatcher<SyslogConnection> {

        private final Set<String> callingThreads = ConcurrentHashMap.newKeySet();
        private final LinkedBlockingQueue<String> delivered = new LinkedBlockingQueue<>();
        private final CountDownLatch release = new CountDownLatch(1);
        private final int blockFirst;

        private volatile long delayMillis;

        private volatile boolean neverCompleteFutures;

        private int seen;

        RecordingDispatcher(final int blockFirst) {
            this.blockFirst = blockFirst;
        }

        @Override
        public CompletableFuture<DispatchStatus> send(final SyslogConnection message) {
            callingThreads.add(Thread.currentThread().getName());

            final boolean block;
            synchronized (this) {
                block = ++seen <= blockFirst;
            }

            final ByteBuffer buffer = message.getBuffer();
            final byte[] bytes = new byte[buffer.limit()];
            buffer.duplicate().rewind().get(bytes);
            delivered.add(new String(bytes, StandardCharsets.UTF_8).replaceAll("^<\\d+>.*app: ", ""));

            if (block) {
                try {
                    release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // An uncompleted future, the way a reloaded Minion leaves them.
            return neverCompleteFutures ? new CompletableFuture<>()
                    : CompletableFuture.completedFuture(DispatchStatus.DISPATCHED);
        }

        void setDelayMillis(final long delayMillis) {
            this.delayMillis = delayMillis;
        }

        void setNeverCompleteFutures(final boolean neverCompleteFutures) {
            this.neverCompleteFutures = neverCompleteFutures;
        }

        void release() {
            release.countDown();
        }

        String nextMessage() throws InterruptedException {
            final String message = delivered.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull("timed out waiting for a dispatched message", message);
            return message;
        }

        List<String> callingThreads() {
            return new ArrayList<>(callingThreads);
        }

        @Override
        public int getQueueSize() {
            return 0;
        }

        @Override
        public void close() {
            // nothing to release
        }
    }

    private static void write(final Socket socket, final String content) throws Exception {
        socket.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
    }

    private static int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
