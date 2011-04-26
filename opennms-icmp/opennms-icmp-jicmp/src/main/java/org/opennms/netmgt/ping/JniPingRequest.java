/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ping;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.spi.LogPrefixPreservingPingResponseCallback;
import org.opennms.netmgt.icmp.spi.PingReply;
import org.opennms.netmgt.icmp.spi.PingRequestId;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.protocols.rt.Request;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class JniPingRequest implements Request<PingRequestId, JniPingRequest, PingReply> {
    /** Constant <code>FILTER_ID=(short) (new java.util.Random(System.currentTimeMillis())).nextInt()</code> */
    public static final short FILTER_ID = (short) (new java.util.Random(System.currentTimeMillis())).nextInt();
    private static long s_nextTid = 1;

    public static final long getNextTID() {
        return s_nextTid++;
    }

    /**
     * The id representing the packet
     */
    protected final PingRequestId m_id;

	/**
	 * the request packet
	 */
	protected EchoPacket m_request = null;
	
    /**
     * The callback to use when this object is ready to do something
     */
    protected final PingResponseCallback m_callback;
    
    /**
     * How many retries
     */
    protected final int m_retries;
    
    /**
     * how long to wait for a response
     */
    protected final long m_timeout;
    
    /**
     * The expiration time of this request
     */
    protected long m_expiration = -1L;
    
    /**
     * The thread logger associated with this request.
     */
    protected final ThreadCategory m_log;
    
    
    protected final AtomicBoolean m_processed = new AtomicBoolean(false);
    

    public JniPingRequest(InetAddress addr, long tid, int sequenceId, long timeout, int retries, ThreadCategory logger, PingResponseCallback cb) {
        m_id = new PingRequestId(addr, tid, sequenceId);
        m_retries    = retries;
        m_timeout    = timeout;
        m_log        = logger;
        // Wrap the callback in another callback that will reset the logging suffix after the callback has executed
        m_callback   = new LogPrefixPreservingPingResponseCallback(cb);
    }
    
    public JniPingRequest(InetAddress addr, long tid, int sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(addr, tid, sequenceId, timeout, retries, ThreadCategory.getInstance(JniPingRequest.class), cb);
    }
    
    public JniPingRequest(InetAddress addr, int sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(addr, getNextTID(), sequenceId, timeout, retries, cb);
    }


    /**
     * <p>processResponse</p>
     *
     * @param reply a {@link org.opennms.netmgt.icmp.spi.ping.PingReply} object.
     * @return a boolean.
     */
    public boolean processResponse(PingReply reply) {
        try {
            processResponse(reply.getPacket());
        } finally {
            setProcessed(true);
        }
        return true;
    }

    private void processResponse(EchoPacket packet) {
        m_log.debug(System.currentTimeMillis()+": Ping Response Received "+this);
        m_callback.handleResponse(m_id.getAddress(), packet);
    }

    /**
     * <p>processTimeout</p>
     *
     * @return a {@link org.opennms.netmgt.ping.JniPingRequest} object.
     */
    public JniPingRequest processTimeout() {
        try {
            JniPingRequest returnval = null;
            if (this.isExpired()) {
                if (m_retries > 0) {
                    returnval = constructNewRequest(m_id.getAddress(), m_id.getTid(), m_id.getSequenceId(), m_timeout, m_retries - 1, m_log, m_callback);
                    m_log.debug(System.currentTimeMillis()+": Retrying Ping Request "+returnval);
                } else {
                    m_log.debug(System.currentTimeMillis()+": Ping Request Timed out "+this);
                    m_callback.handleTimeout(m_id.getAddress(), m_request);
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
        sb.append("Expiration=").append(m_expiration).append(',');
        sb.append("Callback=").append(m_callback);
        sb.append("]");
        return sb.toString();
    }

    /** {@inheritDoc} */
    public long getDelay(TimeUnit unit) {
        return unit.convert(m_expiration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * <p>compareTo</p>
     *
     * @param request a {@link java.util.concurrent.Delayed} object.
     * @return a int.
     */
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
     * @return a {@link org.opennms.netmgt.icmp.spi.ping.PingRequestId} object.
     */
    public PingRequestId getId() {
        return m_id;
    }

    public void processError(Throwable t) {
        try {
            m_callback.handleError(m_id.getAddress(), m_request, t);
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
            m_request = createRequestPacket();
    
            m_log.debug(System.currentTimeMillis()+": Sending Ping Request: "+this);
            byte[] data = m_request.toBytes();
            m_expiration = System.currentTimeMillis() + m_timeout;
            icmpSocket.send(new DatagramPacket(data, data.length, m_id.getAddress(), 0));
        } catch (Throwable t) {
            m_callback.handleError(m_id.getAddress(), m_request, t);
        }
    }

    /**
     * <p>createRequestPacket</p>
     */
    private EchoPacket createRequestPacket() {
        org.opennms.protocols.icmp.ICMPEchoPacket iPkt = new org.opennms.protocols.icmp.ICMPEchoPacket(m_id.getTid());
        iPkt.setIdentity(FILTER_ID);
        iPkt.setSequenceId((short) m_id.getSequenceId());
        iPkt.computeChecksum();
        return new JICMPEchoPacket(iPkt);
    }

    public JniPingRequest constructNewRequest(InetAddress inetAddress, long tid, int sequenceId, long timeout, int retries, ThreadCategory log, PingResponseCallback callback) {
        return new JniPingRequest(inetAddress, tid, sequenceId, timeout, retries, log, callback);
    }
}
