/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty4.NettyComponent;
import org.apache.camel.component.netty4.NettyConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultManagementNameStrategy;
import org.apache.camel.impl.SimpleRegistry;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

/**
 * @author Seth
 */
public class SyslogReceiverCamelNettyImpl extends SinkDispatchingSyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiverCamelNettyImpl.class);

    private static final int SOCKET_TIMEOUT = 500;

    private final InetAddress m_host;

    private final int m_port;

    private final SyslogdConfig m_config;

    private DefaultCamelContext m_camel;

    public SyslogReceiverCamelNettyImpl(final SyslogdConfig config) {
        super(config);
        m_host = addr(config.getListenAddress() == null? "0.0.0.0" : config.getListenAddress());
        m_port = config.getSyslogPort();
        m_config = config;
    }

    @Override
    public String getName() {
        String listenAddress = m_config.getListenAddress() == null? "0.0.0.0" : m_config.getListenAddress();
        return getClass().getSimpleName() + " [" + listenAddress + ":" + m_config.getSyslogPort() + "]";
    }

    public boolean isStarted() {
        if (m_camel == null) {
            return false;
        } else {
            return m_camel.isStarted();
        }
    }

    /**
     * stop the current receiver
     * @throws InterruptedException
     */
    @Override
    public void stop() throws InterruptedException {
        try {
            if (m_camel != null) {
                m_camel.shutdown();
            }
        } catch (Exception e) {
            LOG.warn("Exception while shutting down syslog Camel context", e);
        }
        super.stop();
    }

    /**
     * The execution context.
     */
    @Override
    public void run() {
        // Setup logging and create the dispatcher
        super.run();

        SimpleRegistry registry = new SimpleRegistry();

        //Adding netty component to camel in order to resolve OSGi loading issues
        NettyComponent nettyComponent = new NettyComponent();
        m_camel = new DefaultCamelContext(registry);

        // Set the context name so that it shows up nicely in JMX
        //
        // @see org.apache.camel.management.DefaultManagementNamingStrategy
        //
        //m_camel.setManagementName("org.opennms.features.events.syslog.listener");
        m_camel.setName("syslogdListenerCamelNettyContext");
        m_camel.setManagementNameStrategy(new DefaultManagementNameStrategy(m_camel, "#name#", null));

        m_camel.addComponent("netty4", nettyComponent);

        m_camel.getShutdownStrategy().setShutdownNowOnTimeout(true);
        m_camel.getShutdownStrategy().setTimeout(15);
        m_camel.getShutdownStrategy().setTimeUnit(TimeUnit.SECONDS);

        try {
            m_camel.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    String from = String.format("netty4:udp://%s:%d?sync=false&allowDefaultCodec=false&receiveBufferSize=%d&connectTimeout=%d",
                        InetAddressUtils.str(m_host),
                        m_port,
                        Integer.MAX_VALUE,
                        SOCKET_TIMEOUT
                    );
                    from(from)
                    // Polled via JMX
                    .routeId("syslogListen")
                    .process(new AsyncProcessor() {

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            final ByteBuf buffer = exchange.getIn().getBody(ByteBuf.class);

                            // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because 
                            // we are listening on an InetAddress, it will always be of type InetAddressSocket
                            InetSocketAddress source = (InetSocketAddress)exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS);

                            // Synchronously invoke the dispatcher
                            m_dispatcher.send(new SyslogConnection(source, buffer.nioBuffer())).get();
                        }

                        @Override
                        public boolean process(Exchange exchange, AsyncCallback callback) {
                            final ByteBuf buffer = exchange.getIn().getBody(ByteBuf.class);

                            // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because 
                            // we are listening on an InetAddress, it will always be of type InetAddressSocket
                            InetSocketAddress source = (InetSocketAddress)exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS);

                            ByteBuffer bufferCopy = ByteBuffer.allocate(buffer.readableBytes());
                            buffer.getBytes(buffer.readerIndex(), bufferCopy);

                            m_dispatcher.send(new SyslogConnection(source, bufferCopy)).whenComplete((r,e) -> {
                                if (e != null) {
                                    exchange.setException(e);
                                }
                                callback.done(false);
                            });
                            return false;
                        }
                    });
                }
            });
            m_camel.start();
        } catch (Throwable e) {
            LOG.error("Could not configure Camel routes for syslog receiver", e);
        }
    }
}
