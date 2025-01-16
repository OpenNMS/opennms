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

import java.util.Objects;

import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.xml.XmlFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

public class TcpListener {

    private static final Logger LOG = LoggerFactory.getLogger(TcpListener.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture future;

    private final EventIpcManager eventIpcManager;
    private final EventdConfig config;

    public TcpListener(EventdConfig config, EventIpcManager eventIpcManager) {
        this.config = Objects.requireNonNull(config);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new XmlFrameDecoder(2147483647));
                        ch.pipeline().addLast(new XmlEventProcessor(eventIpcManager));
                    }
                });

        // Bind and start to accept incoming connections.
        future = b.bind(config.getTCPIpAddress(), config.getTCPPort()).sync().await();
    }

    public void stop() throws InterruptedException {
        LOG.info("Closing channel...");
        ChannelFuture cf = future.channel().closeFuture();
        LOG.info("Closing boss group...");
        workerGroup.shutdownGracefully().sync();
        bossGroup.shutdownGracefully().sync();
        cf.sync();
    }



}
