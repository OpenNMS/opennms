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

package org.opennms.netmgt.telemetry.listeners.sflow;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.Listener;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdpListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(UdpListener.class);

    private String name;

    private String host = "::";
    private int port = 4738;

    private AsyncDispatcher<TelemetryMessage> dispatcher;

    private EventLoopGroup bossGroup;
    private ChannelFuture socketFuture;

    public UdpListener() {
    }

    public void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup();

        this.socketFuture = new Bootstrap()
                .group(this.bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(final DatagramChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
                                        LOG.warn("Invalid packet: {}", cause.getMessage());
                                        LOG.debug("", cause);
                                    }
                                });
                    }
                })
                .bind(this.host, this.port)
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
}
