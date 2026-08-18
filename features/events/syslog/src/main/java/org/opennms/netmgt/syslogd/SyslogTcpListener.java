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

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.config.syslogd.SyslogTcpConfig;
import org.opennms.netmgt.config.syslogd.SyslogTcpFraming;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Accepts syslog messages over TCP and feeds them to a dispatcher owned by someone else.
 *
 * Deliberately not a {@link SyslogReceiver}: a receiver creates its own sink dispatcher, and
 * the Sink API names its metrics after the module id, so a second dispatcher for the same
 * module throws and takes its listener down.
 */
public class SyslogTcpListener {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogTcpListener.class);

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 15;

    private static final long DEFAULT_DISPATCH_TIMEOUT_MS = 30_000;

    private final SyslogTcpConfig m_config;

    private final AsyncDispatcher<SyslogConnection> m_dispatcher;

    private final ChannelGroup m_channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private volatile EventLoopGroup m_bossGroup;

    private volatile EventLoopGroup m_workerGroup;

    private volatile ChannelFuture m_socketFuture;

    private volatile SslContext m_sslContext;

    private volatile SyslogTcpFraming m_framing;

    /** Runs the calls into the sink dispatcher, which block and so must stay off the event loops. */
    private volatile ExecutorService m_dispatchPool;

    /** Overridden only by tests, which cannot afford to wait out the real timeout. */
    private long m_dispatchTimeoutMs = DEFAULT_DISPATCH_TIMEOUT_MS;

    public SyslogTcpListener(final SyslogTcpConfig config, final AsyncDispatcher<SyslogConnection> dispatcher) {
        m_config = Objects.requireNonNull(config);
        m_dispatcher = Objects.requireNonNull(dispatcher);
    }

    void setDispatchTimeoutMs(final long dispatchTimeoutMs) {
        m_dispatchTimeoutMs = dispatchTimeoutMs;
    }

    public boolean isStarted() {
        return m_socketFuture != null && m_socketFuture.channel().isActive();
    }

    public String describeAddress() {
        if (!m_config.isEnabled()) {
            return "disabled";
        }
        final String address = m_config.getListenAddress() == null ? "0.0.0.0" : m_config.getListenAddress();
        return address + ":" + m_config.getPort();
    }

    /**
     * Binds the socket. Failures are logged rather than thrown, so a misconfigured TCP
     * listener cannot stop the UDP one that shares this receiver from starting.
     */
    public void start() {
        if (!m_config.isEnabled()) {
            LOG.debug("Syslog TCP ingestion is not configured, nothing to start");
            return;
        }

        // Both before the bind: an unparseable framing or a bad certificate path has to stop
        // the listener, not leave a port that accepts and then drops every connection, or one
        // serving plaintext where operators believe it is encrypted.
        try {
            m_framing = m_config.resolveFraming();
            if (m_config.isTlsEnabled()) {
                m_sslContext = SyslogTcpSslContextFactory.create(m_config);
                LOG.info("TLS enabled for the syslog TCP listener on {}, client authentication is {}",
                        describeAddress(), m_config.getTlsClientAuth());
            }
        } catch (Throwable e) {
            LOG.error("Not starting the syslog TCP listener on {}: {}", describeAddress(), e.getMessage(), e);
            return;
        }

        try {
            m_bossGroup = new NioEventLoopGroup();
            m_workerGroup = new NioEventLoopGroup();
            m_dispatchPool = Executors.newFixedThreadPool(
                    Math.max(2, Runtime.getRuntime().availableProcessors()),
                    new LogPreservingThreadFactory("syslog-tcp-dispatch", Integer.MAX_VALUE));

            m_socketFuture = new ServerBootstrap()
                    .group(m_bossGroup, m_workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) {
                            // Counts channels that are still closing: they hold resources until
                            // their close completes, so a connection-per-message sender can be
                            // refused below its apparent concurrency.
                            if (m_channels.size() >= m_config.getMaxConnections()) {
                                // Debug: a client retrying in a loop would fill the log.
                                LOG.debug("Refusing syslog TCP connection from {}: already at the {} connection limit",
                                        ch.remoteAddress(), m_config.getMaxConnections());
                                ch.close();
                                return;
                            }
                            m_channels.add(ch);
                            initSyslogPipeline(ch);
                        }
                    })
                    .bind(bindAddress())
                    .sync();

            LOG.info("Listening for syslog messages over TCP on {}", describeAddress());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while binding the syslog TCP listener on {}", describeAddress(), e);
            stop();
        } catch (Throwable e) {
            // Throwable, so an Error cannot leave nothing in the log to say why the port is dead.
            LOG.error("Failed to bind the syslog TCP listener on {}", describeAddress(), e);
            // The groups and the pool exist by now, and nothing else will come back to close them.
            stop();
        }
    }

    private void initSyslogPipeline(final SocketChannel ch) {
        final InetSocketAddress source = ch.remoteAddress();
        final SyslogDispatchHandler dispatch = new SyslogDispatchHandler();

        // First, so everything after it sees decrypted bytes.
        if (m_sslContext != null) {
            ch.pipeline().addLast(m_sslContext.newHandler(ch.alloc()));
        }

        if (m_config.getIdleTimeoutSeconds() > 0) {
            ch.pipeline().addLast(new IdleStateHandler(m_config.getIdleTimeoutSeconds(), 0, 0));
            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
                    if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                        // Reads are paused while a dispatch is outstanding, so read idleness
                        // alone would close a connection that is sending as fast as we accept.
                        if (dispatch.isBusy()) {
                            return;
                        }
                        LOG.debug("Closing idle syslog TCP connection from {}", source);
                        ctx.close();
                        return;
                    }
                    ctx.fireUserEventTriggered(evt);
                }
            });
        }

        ch.pipeline().addLast(new SyslogTcpFrameDecoder(source, m_framing, m_config.getMaxMessageSize()));
        ch.pipeline().addLast(dispatch);
        ch.pipeline().addLast(new SyslogTcpExceptionHandler(source));
    }

    /**
     * Hands decoded messages to the dispatcher one at a time, in arrival order, from a pool
     * thread. One outstanding dispatch is what preserves ordering, since the sink drains its
     * queue with several threads. Reads stay off until it returns, so a slow sink becomes TCP
     * backpressure rather than unbounded buffering.
     *
     * One instance per channel; pending and dispatchInFlight are event-loop confined.
     */
    private class SyslogDispatchHandler extends SimpleChannelInboundHandler<SyslogConnection> {

        private final Queue<SyslogConnection> pending = new ArrayDeque<>();

        private boolean dispatchInFlight;

        /** Cleared for the rest of the connection once the sink stops confirming dispatches. */
        private volatile boolean waitForDispatch = true;

        /** Event loop only. Tells the idle timeout that a quiet socket is our doing, not the sender's. */
        boolean isBusy() {
            return dispatchInFlight || !pending.isEmpty();
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final SyslogConnection connection) {
            pending.add(connection);
            ctx.channel().config().setAutoRead(false);
            dispatchNext(ctx);
        }

        private void dispatchNext(final ChannelHandlerContext ctx) {
            if (dispatchInFlight) {
                return;
            }

            final SyslogConnection next = pending.poll();
            if (next == null) {
                ctx.channel().config().setAutoRead(true);
                return;
            }

            final ExecutorService pool = m_dispatchPool;
            if (pool == null) {
                // stop() ran while this channel still had work queued.
                LOG.debug("Dropping a syslog message from {}: the listener is shutting down", next.getSource());
                return;
            }

            dispatchInFlight = true;
            try {
                pool.execute(() -> {
                    Throwable failure = null;
                    try {
                        final CompletableFuture<?> dispatched = m_dispatcher.send(next);
                        if (waitForDispatch) {
                            try {
                                dispatched.get(m_dispatchTimeoutMs, TimeUnit.MILLISECONDS);
                            } catch (TimeoutException e) {
                                // Paying this per message is worse than losing the ordering
                                // guarantee, so stop waiting for the rest of the connection.
                                waitForDispatch = false;
                                LOG.warn("The sink took a syslog message from {} but did not report it dispatched"
                                        + " within {}ms. No longer waiting for confirmation on this connection,"
                                        + " so its messages may reach the consumer out of order.",
                                        next.getSource(), m_dispatchTimeoutMs);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable e) {
                        failure = e;
                    }
                    onDispatched(ctx, failure);
                });
            } catch (RejectedExecutionException e) {
                dispatchInFlight = false;
                ctx.fireExceptionCaught(e);
            }
        }

        /** Hops back onto the event loop, because this runs on a dispatch or sink thread. */
        private void onDispatched(final ChannelHandlerContext ctx, final Throwable ex) {
            if (ctx.executor().isShuttingDown()) {
                return;
            }
            try {
                ctx.executor().execute(() -> {
                    dispatchInFlight = false;
                    if (ex != null) {
                        ctx.fireExceptionCaught(ex);
                        return;
                    }
                    dispatchNext(ctx);
                });
            } catch (RejectedExecutionException e) {
                LOG.debug("Syslog TCP channel on {} went away before its dispatch completed", m_config.getPort(), e);
            }
        }
    }

    /**
     * Closes the socket and every connection. The dispatcher belongs to the caller and is
     * left alone, but nothing is in flight to it once this returns.
     */
    public void stop() {
        if (m_socketFuture == null && m_bossGroup == null && m_workerGroup == null) {
            return;
        }

        LOG.debug("Stopping the syslog TCP listener on {}", describeAddress());

        m_channels.close().awaitUninterruptibly();

        if (m_socketFuture != null) {
            final Channel channel = m_socketFuture.channel();
            channel.close().awaitUninterruptibly();
            if (channel.parent() != null) {
                channel.parent().close().awaitUninterruptibly();
            }
            m_socketFuture = null;
        }

        // After the channels are closed, so nothing new is submitted, and before the caller
        // closes the dispatcher these tasks are calling into.
        if (m_dispatchPool != null) {
            m_dispatchPool.shutdown();
            try {
                if (!m_dispatchPool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    LOG.warn("Syslog TCP dispatch pool did not drain within {} seconds", SHUTDOWN_TIMEOUT_SECONDS);
                    m_dispatchPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                m_dispatchPool.shutdownNow();
            }
            m_dispatchPool = null;
        }

        shutdownGracefully(m_workerGroup, "worker");
        m_workerGroup = null;
        shutdownGracefully(m_bossGroup, "boss");
        m_bossGroup = null;

        m_sslContext = null;
    }

    private void shutdownGracefully(final EventLoopGroup group, final String description) {
        if (group == null) {
            return;
        }
        if (!group.shutdownGracefully(0, SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .awaitUninterruptibly(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            LOG.warn("Syslog TCP {} group did not shut down within {} seconds", description, SHUTDOWN_TIMEOUT_SECONDS);
        }
    }

    private InetSocketAddress bindAddress() {
        return m_config.getListenAddress() == null
                ? new InetSocketAddress(m_config.getPort())
                : new InetSocketAddress(m_config.getListenAddress(), m_config.getPort());
    }
}
