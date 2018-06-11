/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.listener;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.xml.XmlFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

public class UdpListener {
    private static final Logger LOG = LoggerFactory.getLogger(UdpListener.class);

    private EventLoopGroup bossGroup;
    private ChannelFuture future;

    private final EventIpcManager eventIpcManager;
    private final EventdConfig config;

    public UdpListener(EventdConfig config, EventIpcManager eventIpcManager) {
        this.config = Objects.requireNonNull(config);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        final Bootstrap b = new Bootstrap()
                .group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_RCVBUF, Integer.MAX_VALUE)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new MessageToMessageDecoder<DatagramPacket>() {
                            @Override
                            protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
                                msg.retain();
                                out.add(msg.content());
                            }
                        });
                        ch.pipeline().addLast(new XmlFrameDecoder(2147483647));
                        ch.pipeline().addLast(new XmlEventProcessor(eventIpcManager));
                    }
                });
        future = b.bind(config.getUDPIpAddress(), config.getUDPPort()).await();
    }

    public void stop() throws InterruptedException {
        LOG.info("Closing channel...");
        ChannelFuture cf = future.channel().closeFuture();
        LOG.info("Closing boss group...");
        bossGroup.shutdownGracefully().sync();
        cf.sync();
    }
}