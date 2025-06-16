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
package org.opennms.netmgt.telemetry.listeners;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.netmgt.telemetry.api.receiver.GracefulShutdownListener;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.listeners.utils.NettyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.SocketUtils;

public class TcpListener implements GracefulShutdownListener {
    private static final Logger LOG = LoggerFactory.getLogger(TcpListener.class);

    private final String name;
    private final TcpParser parser;

    private final Meter packetsReceived;

    private String host = null;
    private int port = 50000;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelFuture socketFuture;

    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Future<String> stopFuture;

    public TcpListener(final String name,
                       final TcpParser parser,
                       final MetricRegistry metrics) {
        this.name = Objects.requireNonNull(name);
        this.parser = Objects.requireNonNull(parser);

        packetsReceived = metrics.meter(MetricRegistry.name("listeners", name, "packetsReceived"));
    }

    public void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        this.parser.start(this.bossGroup);

        final InetSocketAddress address = this.host != null
                ? SocketUtils.socketAddress(this.host, this.port)
                : new InetSocketAddress(this.port);

        this.socketFuture = new ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) {
                        final TcpParser.Handler session = TcpListener.this.parser.accept(ch.remoteAddress(), ch.localAddress());
                        ch.pipeline()
                                .addFirst(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        packetsReceived.mark();
                                        super.channelRead(ctx, msg);
                                    }
                                })
                                .addLast(new ByteToMessageDecoder() {
                                    @Override
                                    protected void decode(final ChannelHandlerContext ctx,
                                                          final ByteBuf in,
                                                          final List<Object> out) throws Exception {
                                        session.parse(in).ifPresent(out::add);
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        super.channelActive(ctx);
                                        session.active();
                                    }

                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                        super.channelInactive(ctx);
                                        session.inactive();
                                    }
                                })
                                .addLast(new SimpleChannelInboundHandler<CompletableFuture<?>>() {
                                    @Override
                                    protected void channelRead0(final ChannelHandlerContext ctx,
                                                                final CompletableFuture<?> future) throws Exception {
                                        future.handle((result, ex) -> {
                                            if (ex != null) {
                                                ctx.fireExceptionCaught(ex);
                                            }
                                            return result;
                                        });
                                    }
                                })
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
                                        LOG.warn("Invalid packet: {}", cause.getMessage());
                                        LOG.debug("", cause);

                                        session.inactive();

                                        ctx.close();
                                    }
                                });
                    }

                    @Override
                    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                        TcpListener.this.channels.add(ctx.channel());
                        super.channelActive(ctx);
                    }

                    @Override
                    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
                        TcpListener.this.channels.remove(ctx.channel());
                        super.channelInactive(ctx);
                    }
                })
                .bind(address)
                .sync();
    }

    public void stop() throws InterruptedException {
        NettyEventListener workerListener = new NettyEventListener("worker");
        NettyEventListener bossListener = new NettyEventListener("boss");

        LOG.info("Disconnecting clients...");
        this.channels.close().awaitUninterruptibly();

        LOG.info("Closing worker group...");
        // switch to use even listener rather than sync to prevent shutdown deadlock hang
        this.workerGroup.shutdownGracefully().addListener(workerListener);

        LOG.info("Closing boss group...");
        this.bossGroup.shutdownGracefully().addListener(bossListener);

        if (this.socketFuture != null) {
            LOG.info("Closing channel...");
            this.socketFuture.channel().close().sync();
            if (this.socketFuture.channel().parent() != null) {
                this.socketFuture.channel().parent().close().sync();
            }
        }

        LOG.info("Stopping parser...");
        if (this.parser != null) {
            this.parser.stop();
        }

        stopFuture = new Future<String>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return workerListener.isDone() && bossListener.isDone();
            }

            @Override
            public String get() {
                return name + "[" + workerListener.getName() + ":" + workerListener.isDone() + ","
                        + bossListener.getName() + ":" + bossListener.isDone() + "]";
            }

            @Override
            public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return String.format("TCP %s:%s",  this.host != null ? this.host : "*", this.port);
    }

    @Override
    public Collection<? extends Parser> getParsers() {
        return Collections.singleton(this.parser);
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public Future getShutdownFuture() {
        return stopFuture;
    }
}
