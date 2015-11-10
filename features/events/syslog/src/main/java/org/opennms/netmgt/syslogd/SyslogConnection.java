/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class is a {@link Callable} task that is responsible for converting
 * the syslog payload into an OpenNMS event by using the {@link ConvertToEvent}
 * class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class SyslogConnection implements Callable<Callable<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogConnection.class);

    private InetAddress m_sourceAddress;
    private int m_port;
    private ByteBuffer m_bytes;
    private SyslogdConfig m_config;
    private Runnable m_callback;

    /**
     * No-arg constructor so that we can preallocate this object for use with
     * an LMAX Disruptor.
     */
    public SyslogConnection() {
    }

    public SyslogConnection(final DatagramPacket packet, final SyslogdConfig config) {
        this(packet, config, null);
    }

    public SyslogConnection(final DatagramPacket packet, final SyslogdConfig config, final Runnable callback) {
        this(packet.getAddress(), packet.getPort(), ByteBuffer.wrap(packet.getData()), config, callback);
    }

    public SyslogConnection(final InetAddress sourceAddress, final int port, final ByteBuffer bytes, final SyslogdConfig config, final Runnable callback) {
        if (sourceAddress == null) {
            throw new IllegalArgumentException("Source address cannot be null");
        } else if (bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        } else if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_sourceAddress = sourceAddress;
        m_port = port;
        m_bytes = bytes;
        m_config = config;
        m_callback = callback;
    }

    public InetAddress getSourceAddress() {
        return m_sourceAddress;
    }

    public void setSourceAddress(InetAddress sourceAddress) {
        m_sourceAddress = sourceAddress;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public ByteBuffer getByteBuffer() {
        return m_bytes;
    }

    public void setByteBuffer(ByteBuffer bytes) {
        m_bytes = bytes;
    }

    public Runnable getCallback() {
        return m_callback;
    }

    public void setCallback(Runnable callback) {
        m_callback = callback;
    }

    public SyslogdConfig getConfig() {
        return m_config;
    }

    public void setConfig(SyslogdConfig config) {
        m_config = config;
    }

    @Override
    public SyslogProcessor call() {

        try {
            LOG.debug("Converting syslog message into event");

            // TODO: Change to a static call?
            ConvertToEvent re = new ConvertToEvent(
                m_sourceAddress,
                m_port,
                // Decode the packet content as ASCII
                // TODO: Support more character encodings?
                StandardCharsets.US_ASCII.decode(m_bytes).toString(),
                m_config
            );

            LOG.debug("Sending syslog event to the SyslogProcessor queue");

            return new SyslogProcessor(re.getEvent(), m_config.getNewSuspectOnMessage());

        } catch (final UnsupportedEncodingException e1) {
            LOG.debug("Failure to convert package", e1);
        } catch (final MessageDiscardedException e) {
            LOG.debug("Message discarded, returning without enqueueing event.", e);
        } finally {
            // TODO: Make this async?
            if (m_callback != null) {
                m_callback.run();
            }
        }
        return null;
    }

    private static DatagramPacket copyPacket(final DatagramPacket packet) {
        byte[] message = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(packet.getAddress().getHostName(), packet.getAddress().getAddress());
            DatagramPacket retPacket = new DatagramPacket(
                message,
                packet.getOffset(),
                packet.getLength(),
                addr,
                packet.getPort()
            );
            return retPacket;
        } catch (UnknownHostException e) {
            LOG.warn("unable to clone InetAddress object for {}", packet.getAddress());
        }
        return null;
    }

    public static DatagramPacket copyPacket(final InetAddress sourceAddress, final int sourcePort, final ByteBuffer buffer) {
        byte[] message = new byte[SyslogReceiverNioThreadPoolImpl.MAX_PACKET_SIZE];
        int i = 0;
        // Copy the buffer into the byte array
        while (buffer.hasRemaining()) {
            message[i++] = buffer.get();
        }
        return copyPacket(sourceAddress, sourcePort, message, i);
    }

    private static DatagramPacket copyPacket(final InetAddress sourceAddress, final int sourcePort, final byte[] buffer, final int length) {
        DatagramPacket retPacket = new DatagramPacket(
            buffer,
            0,
            length,
            sourceAddress,
            sourcePort
        );
        return retPacket;
    }
}
