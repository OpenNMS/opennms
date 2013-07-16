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

package org.opennms.netmgt.icmp.jni;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.HostIsDownException;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;
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
public class JniPingRequest implements Request<JniPingRequestId, JniPingRequest, JniPingResponse>, EchoPacket {

	private static final Logger LOG = LoggerFactory.getLogger(JniPingRequest.class);

    private static long s_nextTid = 1;

    public static synchronized final long getNextTID() {
        return s_nextTid++;
    }

    /**
     * The id representing the packet
     */
    private final JniPingRequestId m_id;

    /**
     * the request packet
     */
    private ICMPEchoPacket m_requestPacket = null;

    /**
     * The callback to use when this object is ready to do something
     */
    private final PingResponseCallback m_callback;
    
    /**
     * How many retries
     */
    private final int m_retries;
    
    /**
     * The ICMP packet size
     */
    private final int m_packetsize;
    

    /**
     * how long to wait for a response
     */
    private final long m_timeout;
    
    /**
     * The expiration time of this request
     */
    private long m_expiration = -1L;
    
    /**
     * The thread logger associated with this request.
     */
    
    
    private final AtomicBoolean m_processed = new AtomicBoolean(false);
    

    public JniPingRequest(JniPingRequestId id, long timeout, int retries, int packetsize, PingResponseCallback callback) {
        m_id = id;
        m_timeout = timeout;
        m_retries = retries;
        m_packetsize = packetsize;
        m_callback = callback;
    }
    
    public JniPingRequest(InetAddress addr, int identifier, int sequenceNumber, long threadId, long timeout, int retries, int packetsize, PingResponseCallback cb) {
        this(new JniPingRequestId(addr, identifier, sequenceNumber, threadId), timeout, retries, packetsize, cb);
    }
    

    public JniPingRequest(InetAddress addr, int identifier, int sequenceNumber, long timeout, int retries, int packetsize, PingResponseCallback cb) {
        this(addr, identifier, sequenceNumber, getNextTID(), timeout, retries, packetsize, cb);
    }


    /**
     * <p>processResponse</p>
     *
     * @param reply a {@link org.opennms.netmgt.icmp.spi.JniPingResponse.PingReply} object.
     * @return a boolean.
     */
    @Override
    public boolean processResponse(JniPingResponse reply) {
        try {
            LOG.debug("{}: Ping Response Received {}", System.currentTimeMillis(), this);
            m_callback.handleResponse(m_id.getAddress(), reply);
        } finally {
            setProcessed(true);
        }
        return true;
    }
    /**
     * <p>processTimeout</p>
     *
     * @return a {@link org.opennms.netmgt.icmp.jni.JniPingRequest} object.
     */
    @Override
    public JniPingRequest processTimeout() {
        try {
            JniPingRequest returnval = null;
            if (this.isExpired()) {
                if (m_retries > 0) {
                    returnval = new JniPingRequest(m_id, m_timeout, (m_retries - 1), m_packetsize, m_callback);
                    LOG.debug("{}: Retrying Ping Request {}", System.currentTimeMillis(), returnval);
                } else {
                    LOG.debug("{}: Ping Request Timed out {}", System.currentTimeMillis(), this);
                    m_callback.handleTimeout(m_id.getAddress(), this);
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

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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

    /** {@inheritDoc} */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(m_expiration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * <p>compareTo</p>
     *
     * @param request a {@link java.util.concurrent.Delayed} object.
     * @return a int.
     */
    @Override
    public int compareTo(Delayed request) {
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        long otherDelay = request.getDelay(TimeUnit.MILLISECONDS);
        if (myDelay < otherDelay) return -1;
        if (myDelay == otherDelay) return 0;
        return 1;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link org.opennms.netmgt.icmp.spi.JniPingRequestId.PingRequestId} object.
     */
    @Override
    public JniPingRequestId getId() {
        return m_id;
    }

    @Override
    public void processError(Throwable t) {
        try {
            m_callback.handleError(m_id.getAddress(), this, t);
        } finally {
            setProcessed(true);
        }
    }
    
    private void setProcessed(boolean processed) {
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
     * Send this JniPingRequest through the given icmpSocket
     *
     * @param icmpSocket a {@link org.opennms.protocols.icmp.IcmpSocket} object.
     */
    public void send(IcmpSocket icmpSocket) {
        try {
            m_requestPacket = createRequestPacket();
    
            LOG.debug("{}: Sending Ping Request: {}", System.currentTimeMillis(), this);
            byte[] data = m_requestPacket.toBytes();
            m_expiration = System.currentTimeMillis() + m_timeout;
            send(icmpSocket, new DatagramPacket(data, data.length, m_id.getAddress(), 0));

        } catch (Throwable t) {
            m_callback.handleError(m_id.getAddress(), this, t);
        }
    }

    private void send(IcmpSocket icmpSocket, DatagramPacket packet) throws IOException {
        try {
            icmpSocket.send(packet);
        } catch(IOException e) {
            if (e.getMessage().matches("sendto error \\(65, .*\\)")) {
                throw new NoRouteToHostException("No Route to Host " + m_id.getAddress() + ": " + e.getMessage());
            } else if (e.getMessage().matches("sendto error \\(64, .*\\)")) {
                throw new HostIsDownException("Host " + m_id.getAddress() + " is down: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private ICMPEchoPacket getRequestPacket() {
        return m_requestPacket;
    }

    private ICMPEchoPacket createRequestPacket() {
        ICMPEchoPacket iPkt = new ICMPEchoPacket(m_id.getThreadId(), m_packetsize);
        iPkt.setIdentity((short)m_id.getIdentifier());
        iPkt.setSequenceId((short) m_id.getSequenceNumber());
        iPkt.computeChecksum();
        return iPkt;
    }

    @Override
    public boolean isEchoReply() {
        return getRequestPacket().isEchoReply();
    }

    @Override
    public int getIdentifier() {
        return getRequestPacket().getIdentity();
    }

    @Override
    public int getSequenceNumber() {
        return getRequestPacket().getSequenceId();
    }

    @Override
    public long getThreadId() {
        return getRequestPacket().getTID();
    }
    @Override
    public long getReceivedTimeNanos() {
        return getRequestPacket().getReceivedTime();
    }

    @Override
    public long getSentTimeNanos() {
        return getRequestPacket().getSentTime();
    }

    @Override
    public double elapsedTime(TimeUnit timeUnit) {
        // {@link org.opennms.protocols.icmp.ICMPEchoPacket.getPingRTT()} returns microseconds.
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, timeUnit);
        return (getRequestPacket().getPingRTT() * 1000) / nanosPerUnit;
    }

}
