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
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty.CamelNettyThreadNameDeterminer;
import org.apache.camel.component.netty.NettyComponent;
import org.apache.camel.component.netty.NettyConstants;
import org.apache.camel.component.netty.NettyHelper;
import org.apache.camel.component.netty.NettyWorkerPoolBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultManagementNameStrategy;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.channel.socket.nio.WorkerPool;
import org.opennms.core.camel.DispatcherWhiteboard;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * @author Seth
 */
public class SyslogReceiverCamelNettyImpl implements SyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiverCamelNettyImpl.class);
    
    private static final MetricRegistry METRICS = new MetricRegistry();

    private static final int SOCKET_TIMEOUT = 500;

    private final InetAddress m_host;

    private final int m_port;

    private final SyslogdConfig m_config;

    private DefaultCamelContext m_camel;

    private DistPollerDao m_distPollerDao = null;
    
    /**
     * {@link DispatcherWhiteboard} for broadcasting {@link SyslogConnection}
     * objects to multiple channels such as AMQ and Kafka.
     */
    private DispatcherWhiteboard syslogDispatcher;

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
    }

    // Getter and setter for DistPollerDao
    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public static class LinkedListAggregationStrategy implements AggregationStrategy {
        private final Meter packetMeter;
        
        public LinkedListAggregationStrategy(Meter packetMeter) {
            this.packetMeter = Objects.requireNonNull(packetMeter);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (newExchange == null) {
                return oldExchange;
            }

            final ChannelBuffer buffer = newExchange.getIn().getBody(ChannelBuffer.class);
            final UDPMessageDTO message = new UDPMessageDTO(buffer.toByteBuffer());
            packetMeter.mark();

            LinkedList<UDPMessageDTO> list = null;
            if (oldExchange == null) {
                list = new LinkedList<UDPMessageDTO>();
                list.add(message);
                newExchange.getIn().setBody(list);
                return newExchange;
            } else {
                list = (LinkedList<UDPMessageDTO>)oldExchange.getIn().getBody(LinkedList.class);
                list.add(message);
                return oldExchange;
            }
        }
    }
    
    /**
     * The execution context.
     */
    @Override
    public void run() {
        // Get a log instance
        Logging.putPrefix(Syslogd.LOG4J_CATEGORY);

        // Create some metrics
        Meter packetMeter = METRICS.meter(MetricRegistry.name(getClass(), "packets"));
        Timer processTimer = METRICS.timer(MetricRegistry.name(getClass(), "process"));
        Meter connectionMeter = METRICS.meter(MetricRegistry.name(getClass(), "connections"));
        Histogram packetSizeHistogram = METRICS.histogram(MetricRegistry.name(getClass(), "packetSize"));

        final JmxReporter reporter = JmxReporter.forRegistry(METRICS).build();
        reporter.start();

        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("SyslogNettyWorker-%d").build();
        final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(100000);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                queue, threadFactory);
        final NioWorkerPool workerPool = new NioWorkerPool(executor, NettyHelper.DEFAULT_IO_THREADS,
                new CamelNettyThreadNameDeterminer("NettyWorker", "syslogd"));
 


        METRICS.register(MetricRegistry.name(getClass(), "queueSize"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return queue.size();
                    }
                });

        SimpleRegistry registry = new SimpleRegistry();
        registry.put("syslogDispatcher", syslogDispatcher);
        registry.put("workerPool", workerPool);

        //Adding netty component to camel inorder to resolve OSGi loading issues
        NettyComponent nettyComponent = new NettyComponent();
        m_camel = new DefaultCamelContext(registry);

        // Set the context name so that it shows up nicely in JMX
        //
        // @see org.apache.camel.management.DefaultManagementNamingStrategy
        //
        //m_camel.setManagementName("org.opennms.features.events.syslog.listener");
        m_camel.setName("syslogdListenerCamelNettyContext");
        m_camel.setManagementNameStrategy(new DefaultManagementNameStrategy(m_camel, "#name#", null));

        m_camel.addComponent("netty", nettyComponent);
        
        final String currentSystemId = m_distPollerDao.whoami().getId();
        final String currentLocation = m_distPollerDao.whoami().getLocation();

        try {
            m_camel.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    String from = String.format("netty:udp://%s:%s?sync=false&allowDefaultCodec=false&receiveBufferSize=%d&connectTimeout=%d&synchronous=true&workerPool=workerPool&orderedThreadPoolExecutor=false",
                        InetAddressUtils.str(m_host),
                        m_port,
                        Integer.MAX_VALUE,
                        SOCKET_TIMEOUT
                    );
                    from(from)
                    // Polled via JMX
                    .routeId("syslogListen")
                    .aggregate(header(NettyConstants.NETTY_REMOTE_ADDRESS), new LinkedListAggregationStrategy(packetMeter))
                    .completionSize(1000)
                    .completionInterval(500)
                    .parallelProcessing() // JW: TODO: FIXME
                    .process(new Processor() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because 
                            // we are listening on an InetAddress, it will always be of type InetAddressSocket
                            final InetSocketAddress source = (InetSocketAddress)exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS); 
                            final LinkedList<UDPMessageDTO> messages = (LinkedList<UDPMessageDTO>)exchange.getIn().getBody(LinkedList.class);
                            final UDPMessageLogDTO messageLog = new UDPMessageLogDTO(currentLocation, currentSystemId, source, messages);
                            exchange.getIn().setBody(messageLog, UDPMessageLogDTO.class);
                        }
                    })
                    .to("bean:syslogDispatcher?method=dispatch");
                }
            });

            m_camel.start();
        } catch (Throwable e) {
            LOG.error("Could not configure Camel routes for syslog receiver", e);
        }
    }

    public DispatcherWhiteboard getSyslogDispatcher() {
        return syslogDispatcher;
    }

    public void setSyslogDispatcher(DispatcherWhiteboard syslogDispatcher) {
        this.syslogDispatcher = syslogDispatcher;
    }
}
