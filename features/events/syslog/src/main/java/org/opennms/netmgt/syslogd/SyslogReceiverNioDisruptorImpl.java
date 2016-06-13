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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.opennms.core.concurrent.ExecutorFactory;
import org.opennms.core.concurrent.ExecutorFactoryJavaImpl;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * This implementation of {@link SyslogReceiver} uses multithreaded NIO to
 * handle socket receive() calls and then uses an LMAX disruptor ring buffer
 * to store the packet data. The packets are then decoded using async 
 * {@link CompletableFuture} calls that are executed on a set of {@link ExecutorService}
 * thread pools.
 * 
 * @author Seth
 */
public class SyslogReceiverNioDisruptorImpl implements SyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiverNioDisruptorImpl.class);
    private static final MetricRegistry METRICS = new MetricRegistry();

    private static final int SOCKET_TIMEOUT = 500;

    /**
     * This size is used as the size of each {@link ByteBuffer} used to capture syslog
     * messages.
     * 
     * TODO: Make this configurable
     */
    public static final int MAX_PACKET_SIZE = 4096;

    /**
     * This is the number of NIO listener threads that will process {@link DatagramChannel#receive(ByteBuffer)}
     * calls on the syslog port.
     * 
     * TODO: Make this configurable
     */
    public static final int SOCKET_RECEIVER_THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * This is the number of threads that are used to parse syslog messages into
     * OpenNMS events.
     * 
     * TODO: Make this configurable
     */
    public static final int EVENT_PARSER_THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * This is the number of threads that are used to broadcast the OpenNMS events.
     * 
     * TODO: Make this configurable
     */
    public static final int EVENT_SENDER_THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * This is the size of the LMAX Disruptor queue that contains preallocated
     * {@link ByteBufferMessage} instances. This is the number of simultaneous
     * messages that can be in-flight at any given time.
     * 
     * TODO: Make this configurable
     */
    public static final int SOCKET_BYTE_BUFFER_QUEUE_SIZE = 65536;

    /**
     * This is the size of the LMAX Disruptor queue that contains {@link SyslogConnection}
     * tasks that will convert the syslog bytes into OpenNMS events.
     * 
     * TODO: Make this configurable
     */
    public static final int EVENT_CONVERSION_TASK_QUEUE_SIZE = 65536;

    /**
     * {@link ExecutorFactory} that is used to construct the thread pools that execute
     * the async operations for processing syslog messages. By default, this is set to
     * the {@link ExecutorFactoryJavaImpl}.
     */
    private ExecutorFactory m_executorFactory = new ExecutorFactoryJavaImpl();

    /**
     * The Fiber's status.
     */
    private volatile boolean m_stop;

    private DatagramChannel m_channel;

    /**
     * The context thread
     */
    private Thread m_context;

    private final SyslogdConfig m_config;

    private DistPollerDao m_distPollerDao = null;

    /**
     * This thread pool is used to process {@link DatagramChannel#receive(ByteBuffer)} 
     * calls on the syslog port. By using multiple threads, we can optimize the receipt of
     * packet data from the syslog port and avoid discarding UDP syslog packets.
     */
    private ExecutorService m_socketReceivers;
    private ExecutorService m_syslogConnectionExecutor;
    private ExecutorService m_syslogProcessorExecutor;

    private final Disruptor<ByteBufferMessage> m_byteBuffers;
    private final RingBuffer<ByteBufferMessage> m_ringBuffer;

    /**
     * This class is a container for a preallocated {@link ByteBuffer} that is
     * used to hold incoming syslog packet data.
     */
    private static class ByteBufferMessage {
        // Allocate a buffer that's big enough to handle any sane syslog message
        public final ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
    }

    public static DatagramChannel openChannel(SyslogdConfig config) throws SocketException, IOException {
        DatagramChannel channel = DatagramChannel.open();
        // Set SO_REUSEADDR so that we don't run into problems in
        // unit tests trying to rebind to an address where other tests
        // also bound. This shouldn't have any effect at runtime.
        channel.socket().setReuseAddress(true);
        if (config.getListenAddress() != null && config.getListenAddress().length() != 0) {
            channel.socket().bind(new InetSocketAddress(InetAddressUtils.addr(config.getListenAddress()), config.getSyslogPort()));
        } else {
            channel.socket().bind(new InetSocketAddress(config.getSyslogPort()));
        }
        return channel;
    }

    /**
     * Construct a new receiver
     *
     * @param sock
     * @param matchPattern
     * @param hostGroup
     * @param messageGroup
     */
    public SyslogReceiverNioDisruptorImpl(SyslogdConfig config) throws SocketException, IOException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_stop = false;
        m_channel = null;
        m_config = config;

        //m_socketReceivers = m_executorFactory.newExecutor(SOCKET_RECEIVER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "socketReceivers");
        //m_syslogConnectionExecutor = m_executorFactory.newExecutor(EVENT_PARSER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "syslogConnections");
        //m_syslogProcessorExecutor = m_executorFactory.newExecutor(EVENT_SENDER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "syslogProcessors");

        // We can use a null executor here because we're not queueing executable tasks.
        // We're just using this ring buffer to access preallocated ByteBuffers.
        m_byteBuffers = new Disruptor<ByteBufferMessage>(ByteBufferMessage::new, SOCKET_BYTE_BUFFER_QUEUE_SIZE, null);
        m_byteBuffers.start();
        m_ringBuffer = m_byteBuffers.getRingBuffer();

        /*
         * TODO: Do we need to do anything to warm up the ring buffer? Probably not I guess.

        for (int i = 0; i < 8192; i++) {
            long sequence = ringBuffer.next();
            try {
                ringBuffer.get(sequence);
            }
        }
        */
    }

    @Override
    public String getName() {
        String listenAddress = (m_config.getListenAddress() != null && m_config.getListenAddress().length() > 0) ? m_config.getListenAddress() : "0.0.0.0";
        return getClass().getSimpleName() + " [" + listenAddress + ":" + m_config.getSyslogPort() + "]";
    }

    // Getter and setter for DistPollerDao
    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public ExecutorFactory getExecutorFactory() {
        return m_executorFactory;
    }

    public void setExecutorFactory(ExecutorFactory executorFactory) {
        m_executorFactory = executorFactory;
    }

    /**
     * stop the current receiver
     * @throws InterruptedException
     * 
     */
    @Override
    public void stop() throws InterruptedException {
        m_stop = true;

        // Shut down the thread pool that is processing DatagramChannel.receive() calls
        m_socketReceivers.shutdown();

        // Shut down the thread pools that are executing SyslogConnection and SyslogProcessor tasks
        m_syslogConnectionExecutor.shutdown();
        m_syslogProcessorExecutor.shutdown();

        try {
            m_channel.close();
        } catch (IOException e) {
            LOG.warn("Exception while closing syslog channel: " + e.getMessage());
        } finally {
            m_channel = null;
        }

        if (m_context != null) {
            LOG.debug("Stopping and joining thread context {}", m_context.getName());
            m_context.interrupt();
            m_context.join();
            LOG.debug("Thread context stopped and joined");
        }
    }

    /**
     * The execution context.
     */
    @Override
    public void run() {
        // get the context
        m_context = Thread.currentThread();

        // Get a log instance
        Logging.putPrefix(Syslogd.LOG4J_CATEGORY);

        // Create some metrics
        Meter packetMeter = METRICS.meter(MetricRegistry.name(getClass(), "packets"));
        Meter processorMeter = METRICS.meter(MetricRegistry.name(getClass(), "processors"));
        Meter connectionMeter = METRICS.meter(MetricRegistry.name(getClass(), "connections"));
        Histogram packetSizeHistogram = METRICS.histogram(MetricRegistry.name(getClass(), "packetSize"));

        if (m_stop) {
            LOG.debug("Stop flag set before thread started, exiting");
            return;
        } else {
            LOG.debug("Thread context started");
        }

        m_socketReceivers = m_executorFactory.newExecutor(SOCKET_RECEIVER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "socketReceivers");
        m_syslogConnectionExecutor = m_executorFactory.newExecutor(EVENT_PARSER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "syslogConnections");
        m_syslogProcessorExecutor = m_executorFactory.newExecutor(EVENT_SENDER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "syslogProcessors");

        try {
            LOG.debug("Opening syslog channel...");
            m_channel = openChannel(m_config);
        } catch (IOException e) {
            LOG.warn("An I/O error occured while trying to set the socket timeout", e);
        }

        // set an SO timeout to make sure we don't block forever
        // if a socket is closed.
        try {
            LOG.debug("Setting socket timeout to {}ms", SOCKET_TIMEOUT);
            m_channel.socket().setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e) {
            LOG.warn("An I/O error occured while trying to set the socket timeout", e);
        }

        // Increase the receive buffer for the socket
        try {
            LOG.debug("Attempting to set receive buffer size to {}", Integer.MAX_VALUE);
            m_channel.socket().setReceiveBufferSize(Integer.MAX_VALUE);
            LOG.debug("Actual receive buffer size is {}", m_channel.socket().getReceiveBufferSize());
        } catch (SocketException e) {
            LOG.info("Failed to set the receive buffer to {}", Integer.MAX_VALUE, e);
        }

        for (int i = 0; i < SOCKET_RECEIVER_THREADS; i++) {
            m_socketReceivers.execute(new Runnable() {
                public void run() {

                    // set to avoid numerous tracing message
                    boolean ioInterrupted = false;

                    // now start processing incoming requests
                    while (!m_stop) {
                        if (m_context.isInterrupted()) {
                            LOG.debug("Thread context interrupted");
                            break;
                        }

                        try {
                            /*
                            if (!ioInterrupted) {
                                LOG.debug("Waiting on a datagram to arrive");
                            }
                            */

                            // Check out a ByteBufferMessage
                            final long sequence = m_ringBuffer.next();

                            // Fetch the ByteBufferMessage
                            final ByteBufferMessage message = m_ringBuffer.get(sequence);

                            // Write the datagram into the ByteBuffer
                            final InetSocketAddress source = (InetSocketAddress)m_channel.receive(message.buffer);

                            // Increment the packet counter
                            packetMeter.mark();

                            // Flip the buffer from write to read mode
                            message.buffer.flip();

                            // Create a metric for the syslog packet size
                            packetSizeHistogram.update(message.buffer.remaining());

                            /*
                            CompletableFuture<Void> processPacket = CompletableFuture.supplyAsync(
                                () -> new SyslogConnection(source.getAddress(), source.getPort(), message.buffer, m_config, null),
                                m_syslogConnectionExecutor
                            )
                            .thenApplyAsync(c -> c.call(), m_syslogConnectionExecutor)
                            .thenAcceptAsync(c -> c.call(), m_syslogProcessorExecutor);
                            */

                            SyslogConnection conn = new SyslogConnection(source.getAddress(), source.getPort(), message.buffer, m_config, m_distPollerDao.whoami().getId());

                            // Convert the syslog packet into an OpenNMS event
                            CompletableFuture<SyslogProcessor> proc = CompletableFuture.supplyAsync(conn::call, m_syslogConnectionExecutor);

                            // After the bytes are converted into an event...
                            proc.thenRun(() -> {
                                // Clear the buffer so that it's ready for writing again
                                message.buffer.clear();

                                if (sequence % 50 == 0) {
                                    LOG.debug("Released 50 more datagrams");
                                }

                                // Release the buffer back to the disruptor
                                m_ringBuffer.publish(sequence);

                                // Increment the counter
                                processorMeter.mark();
                            });

                            // Broadcast the event on the event channel
                            proc.thenAcceptAsync(c -> c.call(), m_syslogProcessorExecutor).thenRun(() -> connectionMeter.mark());

                            // reset the flag
                            ioInterrupted = false; 
                        } catch (SocketTimeoutException e) {
                            ioInterrupted = true;
                            continue;
                        } catch (InterruptedIOException e) {
                            ioInterrupted = true;
                            continue;
                        /*
                        TODO: Figure out how to handle exceptions appropriately in the async code
                        } catch (ExecutionException e) {
                            LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
                            break;
                        } catch (InterruptedException e) {
                            LOG.error("Task interrupted in {}", this.getClass().getSimpleName(), e);
                            break;
                        */
                        } catch (IOException e) {
                            LOG.error("An I/O exception occured on the datagram receipt port, exiting", e);
                            break;
                        }

                    } // end while status OK

                    LOG.debug("Thread context exiting");
                }
            });
        }
    }
}
