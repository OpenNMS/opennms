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

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.Listener;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.flow.PacketHandler;
import org.opennms.netmgt.telemetry.listeners.flow.Protocol;
import org.opennms.netmgt.telemetry.listeners.flow.session.TcpSession;
import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;
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

public class TcpListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(TcpListener.class);

    private String name;

    private String bindHost = "::";
    private int bindPort = 4739;

    private AsyncDispatcher<TelemetryMessage> dispatcher;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelFuture socketFuture;

    public void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        this.socketFuture = new ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) throws Exception {
                        final TemplateManager templateManager = new TcpSession();

                        ch.pipeline()
                                .addLast(new TcpPacketDecoder(ch.remoteAddress(), ch.localAddress(), templateManager))
                                .addLast(new PacketHandler(Protocol.IPFIX, TcpListener.this.dispatcher))
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
                .bind(this.bindHost, this.bindPort)
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

    public String getBindHost() {
        return this.bindHost;
    }

    public void setBindHost(final String bindHost) {
        this.bindHost = bindHost;
    }

    public int getBindPort() {
        return this.bindPort;
    }

    public void setBindPort(final int bindPort) {
        this.bindPort = bindPort;
    }

    @Override
    public void setDispatcher(final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.dispatcher = dispatcher;
    }
}
