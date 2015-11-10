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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.cassandra.concurrent.JMXEnabledSharedExecutorPool;
import org.apache.cassandra.config.Config;
import org.opennms.core.concurrent.WaterfallExecutor;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * @author Seth
 */
class SyslogReceiverNioDisruptorImpl implements SyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiverNioDisruptorImpl.class);

    private static final int SOCKET_TIMEOUT = 500;

    /**
     * This size is used as the size of each {@link ByteBuffer} used to capture syslog
     * messages.
     */
    public static final int MAX_PACKET_SIZE = 4096;

    /**
     * This is the number of NIO listener threads that will process {@link DatagramChannel#receive(ByteBuffer)}
     * calls on the syslog port.
     */
    public static final int SOCKET_RECEIVER_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * This is the size of the LMAX Disruptor queue that contains preallocated
     * {@link ByteBufferMessage} instances. This is the number of simultaneous
     * messages that can be in-flight at any given time.
     */
    public static final int SOCKET_BYTE_BUFFER_QUEUE_SIZE = 16384;

    /**
     * This is the size of the LMAX Disruptor queue that contains {@link SyslogConnection}
     * tasks that will convert the syslog bytes into OpenNMS events.
     */
    public static final int EVENT_CONVERSION_TASK_QUEUE_SIZE = 65536;

    /**
     * The Fiber's status.
     */
    private volatile boolean m_stop;

    private final DatagramChannel m_channel;

    /**
     * The context thread
     */
    private Thread m_context;

    private final SyslogdConfig m_config;

    private final ExecutorService m_executor;

    private final ExecutorService m_socketReceivers;

    //private final Disruptor<SyslogConnection> m_disruptor;

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

    /**
     * Construct a new receiver
     *
     * @param sock
     * @param matchPattern
     * @param hostGroup
     * @param messageGroup
     */
    SyslogReceiverNioDisruptorImpl(DatagramChannel channel, SyslogdConfig config) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null");
        } else if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_stop = false;
        m_channel = channel;
        m_config = config;

        // Turn Cassandra client mode on so that we can use the {@link SharedExecutorPool} classes.
        Config.setClientMode(true);

        m_executor = JMXEnabledSharedExecutorPool.SHARED.newExecutor(Runtime.getRuntime().availableProcessors() * 2, Integer.MAX_VALUE, "syslogConnections", "OpenNMS.Syslogd");

        /*
        m_executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2,
            1000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), Integer.MAX_VALUE)
        );
        */

        // This thread pool is used to process {@link DatagramChannel#receive(ByteBuffer)} calls
        // on the syslog port. By using multiple threads, we can optimize the receipt of
        // packet data from the syslog port and avoid discarding UDP syslog packets.
        m_socketReceivers = JMXEnabledSharedExecutorPool.SHARED.newExecutor(SOCKET_RECEIVER_COUNT, Integer.MAX_VALUE, "socketReceivers", "OpenNMS.Syslogd");

        /*
        m_socketReceivers = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            1000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new LogPreservingThreadFactory(getClass().getSimpleName() + "-SocketReceiver", Integer.MAX_VALUE)
        );
        */

        /*
        // Use an LMAX Disruptor to process incoming packets
        m_disruptor = new Disruptor<SyslogConnection>(SyslogConnection::new, EVENT_CONVERSION_TASK_QUEUE_SIZE, m_executor);
        // Handle each message by enqueuing it on an executor
        m_disruptor.handleEventsWith(
            (event, sequence, endOfBatch) -> 
            WaterfallExecutor.waterfall(m_executor, event)
        );
        m_disruptor.start();
        */

        /*
        // Use an LMAX Disruptor as a ring buffer for incoming packets
        m_packetRingBuffer = new Disruptor<PacketMessage>(PacketMessage::new, SOCKET_BYTE_BUFFER_QUEUE_SIZE, Executors.newCachedThreadPool());
        // Handle each message by enqueuing it on an executor
        m_disruptor.handleEventsWith(
            (event, sequence, endOfBatch) -> 
            WaterfallExecutor.waterfall(m_executor, new SyslogConnection(event))
        );
        */

        m_byteBuffers = new Disruptor<ByteBufferMessage>(ByteBufferMessage::new, SOCKET_BYTE_BUFFER_QUEUE_SIZE, m_executor);
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
        m_executor.shutdown();

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

        if (m_stop) {
            LOG.debug("Stop flag set before thread started, exiting");
            return;
        } else {
            LOG.debug("Thread context started");
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

        for (int i = 0; i < SOCKET_RECEIVER_COUNT; i++) {
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
                            if (!ioInterrupted) {
                                LOG.debug("Waiting on a datagram to arrive");
                            }

                            // Check out a ByteBufferMessage
                            final long sequence = m_ringBuffer.next();

                            // Fetch the ByteBufferMessage
                            final ByteBufferMessage message = m_ringBuffer.get(sequence);

                            // Write the datagram into the ByteBuffer
                            final InetSocketAddress source = (InetSocketAddress)m_channel.receive(message.buffer);

                            // Flip the buffer from write to read mode
                            message.buffer.flip();

                            // TODO: Change this into a disruptor call
                            WaterfallExecutor.waterfall(m_executor, new SyslogConnection(source.getAddress(), source.getPort(), message.buffer, m_config, new Runnable() {
                                @Override
                                public void run() {
                                    // Clear the buffer so that it's ready for writing again
                                    message.buffer.clear();

                                    if (sequence % 50 == 0) {
                                        LOG.debug("Released 50 more datagrams");
                                    }

                                    // Release the buffer back to the disruptor
                                    m_ringBuffer.publish(sequence);
                                }
                            }));

                            // reset the flag
                            ioInterrupted = false; 
                        } catch (SocketTimeoutException e) {
                            ioInterrupted = true;
                            continue;
                        } catch (InterruptedIOException e) {
                            ioInterrupted = true;
                            continue;
                        } catch (ExecutionException e) {
                            LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
                            break;
                        } catch (InterruptedException e) {
                            LOG.error("Task interrupted in {}", this.getClass().getSimpleName(), e);
                            break;
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
