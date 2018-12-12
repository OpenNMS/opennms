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
import java.util.Objects;

import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.SocketUtils;

public class TcpListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(TcpListener.class);

    private final String name;

    private final TcpParser parser;

    private String host = null;
    private int port = 50000;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelFuture socketFuture;

    public TcpListener(final String name,
                       final TcpParser parser) {
        this.name = Objects.requireNonNull(name);
        this.parser = Objects.requireNonNull(parser);
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
                                .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                    @Override
                                    protected void channelRead0(final ChannelHandlerContext ctx,
                                                                final ByteBuf msg) throws Exception {
                                        session.parse(ReferenceCountUtil.retain(msg).nioBuffer())
                                                .handle((result, ex) -> {
                                                    ReferenceCountUtil.release(msg);
                                                    if (ex != null) {
                                                        ctx.fireExceptionCaught(ex);
                                                    }
                                                    return result;
                                                });;
                                    }
                                })
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
                                        LOG.warn("Invalid packet: {}", cause.getMessage());
                                        LOG.debug("", cause);

                                        ctx.close();
                                    }
                                });
                    }
                })
                .bind(address)
                .sync();
    }

    public void stop() throws InterruptedException {
        LOG.info("Closing channel...");
        this.socketFuture.channel().close().sync();

        this.parser.stop();

        LOG.info("Closing boss group...");
        this.bossGroup.shutdownGracefully().sync();
    }

    @Override
    public String getName() {
        return this.name;
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
}
