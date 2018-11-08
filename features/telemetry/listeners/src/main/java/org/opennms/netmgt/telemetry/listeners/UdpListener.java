/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.listeners;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.api.receiver.Dispatchable;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

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

public class UdpListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(UdpListener.class);

    private final String name;
    private final List<UdpParser> parsers;

    private EventLoopGroup bossGroup;
    private ChannelFuture socketFuture;

    private String host = null;
    private int port = 50000;
    private int maxPacketSize = 8096;

    public UdpListener(final String name, final UdpParser parser) {
        this(name, Lists.newArrayList(Objects.requireNonNull(parser)));
    }

    public UdpListener(final String name, final List<UdpParser> parsers) {
        this.name = Objects.requireNonNull(name);
        this.parsers = Objects.requireNonNull(parsers);

        if (this.parsers.isEmpty()) {
            throw new IllegalArgumentException("At least 1 parsers must be defined");
        }
        if (this.parsers.size() > 1) {
            if (this.parsers.stream().filter(p -> p instanceof Dispatchable).count() != this.parsers.size()) {
                throw new IllegalArgumentException("If more than 1 parser is defined, all parsers must be Dispatchable");
            }
        }
    }

    public void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup();

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
        LOG.info("Closing channel...");
        this.socketFuture.channel().close().sync();

        this.parsers.forEach(parser -> parser.stop());

        LOG.info("Closing boss group...");
        this.bossGroup.shutdownGracefully().sync();
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


    private class DefaultChannelInitializer extends ChannelInitializer<DatagramChannel> {

        @Override
        protected void initChannel(DatagramChannel ch) throws Exception {

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
                            if (BufferUtils.peek(msg.content().nioBuffer(), ((Dispatchable) parser)::handles)) {
                                new SingleDatagramPacketParserHandler(parser).channelRead0(ctx, msg);
                                break;
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
                    LOG.debug("", cause);
                }
            });
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
                    ReferenceCountUtil.retain(msg.content()).nioBuffer(),
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
