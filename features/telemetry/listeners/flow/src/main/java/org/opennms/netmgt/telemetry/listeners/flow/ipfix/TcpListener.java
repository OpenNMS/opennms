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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix;

import java.net.InetSocketAddress;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.Listener;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.flow.PacketHandler;
import org.opennms.netmgt.telemetry.listeners.flow.Protocol;
import org.opennms.netmgt.telemetry.listeners.flow.session.Session;
import org.opennms.netmgt.telemetry.listeners.flow.session.TcpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SocketUtils;

public class TcpListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(TcpListener.class);

    private String name;

    private String host = null;
    private int port = 4739;

    private AsyncDispatcher<TelemetryMessage> dispatcher;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelFuture socketFuture;

    public void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

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
                        final Session session = new TcpSession();

                        ch.pipeline()
                                .addLast(new TcpPacketDecoder(ch.remoteAddress(), ch.localAddress(), session))
                                .addLast(new PacketHandler(Protocol.IPFIX, TcpListener.this.dispatcher))
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
                                        cause.printStackTrace();

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
        socketFuture.channel().close().sync();

        LOG.info("Closing boss group...");
        bossGroup.shutdownGracefully().sync();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
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
    public void setDispatcher(final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static void main(final String... args) throws Exception {
        final UdpListener l = new UdpListener();
        l.setName("sflow-test");
        l.start();

        while (true) {
            Thread.sleep(1000);
        }
    }
}
