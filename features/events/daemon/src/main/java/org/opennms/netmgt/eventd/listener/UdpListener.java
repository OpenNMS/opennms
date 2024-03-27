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