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
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.netmgt.telemetry.api.receiver.GracefulShutdownListener;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.utils.NettyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.swrve.ratelimitedlogger.RateLimitedLog;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.SocketUtils;

public class UdpListener implements GracefulShutdownListener {
    private static final Logger LOG = LoggerFactory.getLogger(UdpListener.class);

    public static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final String name;
    private final List<UdpParser> parsers;

    private final Meter packetsReceived;

    private EventLoopGroup bossGroup;
    private ChannelFuture socketFuture;

    private String host = null;
    private int port = 50000;
    private int maxPacketSize = 8096;

    private Future<String> stopFuture;

    public UdpListener(final String name, final List<UdpParser> parsers, final MetricRegistry metrics) {
        this.name = Objects.requireNonNull(name);
        this.parsers = Objects.requireNonNull(parsers);
        Objects.requireNonNull(metrics);

        if (this.parsers.isEmpty()) {
            throw new IllegalArgumentException("At least 1 parsers must be defined");
        }
        if (this.parsers.size() > 1) {
            if (this.parsers.stream().filter(p -> p instanceof Dispatchable).count() != this.parsers.size()) {
                throw new IllegalArgumentException("If more than 1 parser is defined, all parsers must be Dispatchable");
            }
        }

        packetsReceived = metrics.meter(MetricRegistry.name("listeners",  name, "packetsReceived"));
    }

    public void start() throws InterruptedException {
        // Netty defaults to 2 * num cores when the number of threads is set to 0
        this.bossGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder()
                .setNameFormat("telemetryd-nio-" + name + "-%d")
                .build() );

        this.parsers.forEach(parser -> parser.start(this.bossGroup));

        final InetSocketAddress address = this.host != null
                ? SocketUtils.socketAddress(this.host, this.port)
                : new InetSocketAddress(this.port);

        this.socketFuture = new Bootstrap()
                .group(this.bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_RCVBUF, Integer.MAX_VALUE)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(this.maxPacketSize))
                .handler(new DefaultChannelInitializer())
                .bind(address)
                .sync();
    }

    public void stop() throws InterruptedException {
        NettyEventListener bossListener = new NettyEventListener("boss");

        LOG.info("Closing boss group...");
        if (this.bossGroup != null) {
            // switch to use even listener rather than sync to prevent shutdown deadlock hang
            this.bossGroup.shutdownGracefully().addListener(bossListener);
        }

        if (this.socketFuture != null) {
            LOG.info("Closing channel...");
            this.socketFuture.channel().close().sync();
            if (this.socketFuture.channel().parent() != null) {
                this.socketFuture.channel().parent().close().sync();
            }
        }

        this.parsers.forEach(Parser::stop);

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
                return bossListener.isDone();
            }

            @Override
            public String get() {
                return name + "[" + bossListener.getName() + ":" + bossListener.isDone() + "]";
            }

            @Override
            public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return String.format("UDP %s:%s",  this.host != null ? this.host : "*", this.port);
    }

    @Override
    public Collection<? extends Parser> getParsers() {
        return this.parsers;
    }

    @Override
    public Future getShutdownFuture() {
        return stopFuture;
    }


    private class DefaultChannelInitializer extends ChannelInitializer<DatagramChannel> {

        @Override
        protected void initChannel(DatagramChannel ch) {
            // Accounting
            ch.pipeline().addFirst(new AccountingHandler());

            if (parsers.size() == 1) {
                final UdpParser parser = parsers.get(0);
                // If only one parser is defined, we can directly use the handler
                ch.pipeline().addLast(new SingleDatagramPacketParserHandler(parser));
            } else {
                // Otherwise dispatch
                ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(final ChannelHandlerContext ctx, final DatagramPacket msg) throws Exception {
                        for (final UdpParser parser : parsers) {
                            if (BufferUtils.peek(msg.content(), ((Dispatchable) parser)::handles)) {
                                new SingleDatagramPacketParserHandler(parser).channelRead0(ctx, msg);
                                return;
                            }
                        }
                        LOG.warn("Unhandled packet from {}", msg.sender());
                    }
                });
            }

            // Add error handling
            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
                    LOG.warn("Invalid packet: {}", cause.getMessage());
                    RATE_LIMITED_LOG.debug("", cause);
                }
            });
        }
    }

    private class AccountingHandler extends ChannelInboundHandlerAdapter {
        @Override
        public  void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            packetsReceived.mark();
            super.channelRead(ctx, msg);
        }
    }

    // Invokes parse of the provided parsers and also adds some error handling
    private static class SingleDatagramPacketParserHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        final UdpParser parser;

        private SingleDatagramPacketParserHandler(UdpParser parser) {
            this.parser = parser;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            parser.parse(
                    ReferenceCountUtil.retain(msg.content()),
                    msg.sender(), msg.recipient()
                ).handle((result, ex) -> {
                    ReferenceCountUtil.release(msg.content());
                    if (ex != null) {
                        ctx.fireExceptionCaught(ex);
                    }
                    return result;
                });
        }
    }

}
