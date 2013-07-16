/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.icmp.jna;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.LogPrefixPreservingPingResponseCallback;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.protocols.rt.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class JnaPingRequest implements Request<JnaPingRequestId, JnaPingRequest, JnaPingReply>, EchoPacket {

	
	private static final Logger LOG = LoggerFactory
			.getLogger(JnaPingRequest.class);
	
    private static long s_nextTid = 1;

    public static synchronized final long getNextTID() {
        return s_nextTid++;
    }

    /**
     * The id representing the packet
     */
    private final JnaPingRequestId m_id;

    /**
     * The callback to use when this object is ready to do something
     */
	private final PingResponseCallback m_callback;
    
    /**
     * How many retries
     */
	private final int m_retries;
    
    /**
     * how long to wait for a response
     */
	private final long m_timeout;

    /**
     * The ICMP packet size including the header
     */
	
	private final int m_packetsize;
	
    /**
     * The expiration time of this request
     */
	private long m_expiration = -1L;
    
    /**
     * The thread logger associated with this request.
     */
    
    
	private final AtomicBoolean m_processed = new AtomicBoolean(false);
	
    public JnaPingRequest(final JnaPingRequestId id, final long timeout, final int retries, final int packetsize, final PingResponseCallback cb) {
        m_id = id;
        m_retries = retries;
        m_packetsize = packetsize;
        m_timeout = timeout;
        m_callback = new LogPrefixPreservingPingResponseCallback(cb);
    }
	
    public JnaPingRequest(final InetAddress addr, final int identifier, final int sequenceId, final long threadId, final long timeout, final int retries, final int packetsize, final PingResponseCallback cb) {
        this(new JnaPingRequestId(addr, identifier, sequenceId, threadId), timeout, retries, packetsize, cb);
    }
    
    public JnaPingRequest(final InetAddress addr, final int identifier, final int sequenceId, final long timeout, final int retries, final int packetsize, final PingResponseCallback cb) {
        this(addr, identifier, sequenceId, getNextTID(), timeout, retries, packetsize, cb);
    }
        
    /**
     * <p>processResponse</p>
     *
     * @param reply a {@link org.opennms.netmgt.icmp.spi.JnaPingReply.PingReply} object.
     * @return a boolean.
     */
    @Override
    public boolean processResponse(final JnaPingReply reply) {
        try {
            LOG.debug("{}: Ping Response Received for request: {}", System.currentTimeMillis(), this);
            m_callback.handleResponse(getAddress(), reply);
        } finally {
            setProcessed(true);
        }
        return true;
    }

    /**
     * <p>processTimeout</p>
     *
     * @return a {@link org.opennms.netmgt.JnaPingRequest.AbstractPingRequest} object.
     */
    @Override
    public JnaPingRequest processTimeout() {
        try {
            JnaPingRequest returnval = null;
            if (this.isExpired()) {
                if (m_retries > 0) {
                    returnval = new JnaPingRequest(m_id, m_timeout, (m_retries - 1),m_packetsize, m_callback);
                    LOG.debug("{}: Retrying Ping Request {}", System.currentTimeMillis(), returnval);
                } else {
                    LOG.debug("{}: Ping Request Timed out {}", System.currentTimeMillis(), this);
                    m_callback.handleTimeout(getAddress(), this);
                }
            }
            return returnval;
        } finally {
            setProcessed(true);
        }
    }
    
    /**
     * <p>isExpired</p>
     *
     * @return a boolean.
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() >= m_expiration);
    }

    /** {@inheritDoc} */
    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(m_expiration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * <p>compareTo</p>
     *
     * @param request a {@link java.util.concurrent.Delayed} object.
     * @return a int.
     */
    @Override
    public int compareTo(final Delayed request) {
        final long myDelay = getDelay(TimeUnit.MILLISECONDS);
        final long otherDelay = request.getDelay(TimeUnit.MILLISECONDS);
        if (myDelay < otherDelay) return -1;
        if (myDelay == otherDelay) return 0;
        return 1;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link org.opennms.netmgt.icmp.spi.JnaPingRequestId.PingRequestId} object.
     */
    @Override
    public JnaPingRequestId getId() {
        return m_id;
    }

    /** {@inheritDoc} */
    @Override
    public void processError(final Throwable t) {
        try {
            m_callback.handleError(getAddress(), this, t);
        } finally {
            setProcessed(true);
        }
    }
    
    private void setProcessed(final boolean processed) {
        m_processed.set(processed);
    }

    /**
     * <p>isProcessed</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isProcessed() {
        return m_processed.get();
    }

    /**
     * Send this V4PingRequest through the given icmpSocket
     * @param icmpSocket a {@link org.opennms.protocols.icmp.IcmpSocket} object.
     */
    public void send(final V4Pinger v4, final V6Pinger v6) {
        InetAddress addr = getAddress();
        if (addr instanceof Inet4Address) {
            send(v4, (Inet4Address)addr);
        } else if (addr instanceof Inet6Address) {
            send(v6, (Inet6Address)addr);
        }
    }

    public InetAddress getAddress() {
        return m_id.getAddress();
    }

    public void send(final V6Pinger v6, final Inet6Address addr6) {
        try {
            //throw new IllegalStateException("The m_request field should be set here!!!");
            LOG.debug("{}: Sending Ping Request: {}", System.currentTimeMillis(), this);
        
            m_expiration = System.currentTimeMillis() + m_timeout;
            v6.ping(addr6, m_id.getIdentifier(), m_id.getSequenceNumber(), m_id.getThreadId(), 1, 0, m_packetsize);
        } catch (final Throwable t) {
            m_callback.handleError(getAddress(), this, t);
        }
    }

    public void send(final V4Pinger v4, final Inet4Address addr4) {
        try {
            //throw new IllegalStateException("The m_request field should be set here!!!");
            LOG.debug("{}: Sending Ping Request: {}", System.currentTimeMillis(), this);
            m_expiration = System.currentTimeMillis() + m_timeout;
            v4.ping(addr4, m_id.getIdentifier(), m_id.getSequenceNumber(), m_id.getThreadId(), 1, 0, m_packetsize);
        } catch (final Throwable t) {
            m_callback.handleError(getAddress(), this, t);
        }
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("ID=").append(m_id).append(',');
        sb.append("Retries=").append(m_retries).append(",");
        sb.append("Timeout=").append(m_timeout).append(",");
        sb.append("Packet-Size=").append(m_packetsize).append(",");
        sb.append("Expiration=").append(m_expiration).append(',');
        sb.append("Callback=").append(m_callback);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean isEchoReply() {
        return false;
    }

    @Override
    public int getIdentifier() {
        return m_id.getIdentifier();
    }

    @Override
    public int getSequenceNumber() {
        return m_id.getSequenceNumber();
    }

    @Override
    public long getThreadId() {
        return m_id.getThreadId();
    }

    @Override
    public long getReceivedTimeNanos() {
        throw new UnsupportedOperationException("EchoPacket.getReceivedTimeNanos is not yet implemented");
    }

    @Override
    public long getSentTimeNanos() {
        throw new UnsupportedOperationException("EchoPacket.getSentTimeNanos is not yet implemented");
    }

    @Override
    public double elapsedTime(TimeUnit timeUnit) {
        throw new UnsupportedOperationException("EchoPacket.elapsedTime is not yet implemented");
    }
}
