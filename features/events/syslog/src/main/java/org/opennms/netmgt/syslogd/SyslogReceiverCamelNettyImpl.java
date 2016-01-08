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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty.NettyConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.jboss.netty.buffer.ChannelBuffer;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.concurrent.WaterfallExecutor;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seth
 */
public class SyslogReceiverCamelNettyImpl implements SyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiverNioThreadPoolImpl.class);

    private static final int SOCKET_TIMEOUT = 500;

    private final InetAddress m_host;

    private final int m_port;

    private final SyslogdConfig m_config;

    private final ExecutorService m_executor;

    private DefaultCamelContext m_camel;

    /**
     * Construct a new receiver
     *
     * @param sock
     * @param matchPattern
     * @param hostGroup
     * @param messageGroup
     * @throws IOException 
     */
    public SyslogReceiverCamelNettyImpl(final SyslogdConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_host = config.getListenAddress() == null ? addr("0.0.0.0"): addr(config.getListenAddress());
        m_port = config.getSyslogPort();
        m_config = config;

        m_executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2,
            1000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), Integer.MAX_VALUE)
        );
    }

    @Override
    public String getName() {
        String listenAddress = (m_config.getListenAddress() != null && m_config.getListenAddress().length() > 0) ? m_config.getListenAddress() : "0.0.0.0";
        return getClass().getSimpleName() + " [" + listenAddress + ":" + m_config.getSyslogPort() + "]";
    }

    /**
     * stop the current receiver
     * @throws InterruptedException
     * 
     */
    @Override
    public void stop() throws InterruptedException {
        try {
            m_camel.shutdown();
        } catch (Exception e) {
            LOG.warn("Exception while shutting down syslog Camel context", e);
        }

        // Shut down the thread pools that are executing SyslogConnection and SyslogProcessor tasks
        m_executor.shutdown();
    }

    /**
     * The execution context.
     */
    @Override
    public void run() {

        // Get a log instance
        Logging.putPrefix(Syslogd.LOG4J_CATEGORY);

        SimpleRegistry registry = new SimpleRegistry();
        m_camel = new DefaultCamelContext(registry);
        try {
            m_camel.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    String from = String.format("netty:udp://%s:%s?sync=false&allowDefaultCodec=false&receiveBufferSize=%d&connectTimeout=%d",
                        InetAddressUtils.str(m_host),
                        m_port,
                        Integer.MAX_VALUE,
                        SOCKET_TIMEOUT
                    );

                    from(from)
                    //.convertBodyTo(java.nio.ByteBuffer.class)
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            ChannelBuffer buffer = exchange.getIn().getBody(ChannelBuffer.class);
                            // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because 
                            // we are listening on an InetAddress, it will always be of type InetAddressSocket
                            InetSocketAddress source = (InetSocketAddress)exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS); 
                            WaterfallExecutor.waterfall(m_executor, new SyslogConnection(source.getAddress(), source.getPort(), buffer.toByteBuffer(), m_config));
                        }
                    });
                }
            });

            m_camel.start();
        } catch (Exception e) {
            LOG.error("Could not configure Camel routes for syslog receiver", e);
        }
    }
}
