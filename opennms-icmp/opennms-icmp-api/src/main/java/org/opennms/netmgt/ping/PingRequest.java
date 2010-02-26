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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.protocols.rt.Request;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 * 
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
final public class PingRequest implements Request<PingRequestId, PingRequest, PingReply> {
    public static final short FILTER_ID = (short) (new java.util.Random(System.currentTimeMillis())).nextInt();
    private static final short DEFAULT_SEQUENCE_ID = 1;
    private static long s_nextTid = 1;

    /**
     * The id representing the packet
     */
    private PingRequestId m_id;

	/**
	 * the request packet
	 */
	private ICMPEchoPacket m_request = null;
	
	/**
	 * the response packet
	 */
	private ICMPEchoPacket m_response = null;

    /**
     * The callback to use when this object is ready to do something
     */
    private PingResponseCallback m_callback = null;
    
    /**
     * How many retries
     */
    private int m_retries;
    
    /**
     * how long to wait for a response
     */
    private long m_timeout;
    
    /**
     * The expiration time of this request
     */
    private long m_expiration = -1L;
    
    /**
     * The thread logger associated with this request.
     */
    private Category m_log = ThreadCategory.getInstance(this.getClass());
    
    
    private AtomicBoolean m_processed = new AtomicBoolean(false);
    

    PingRequest(InetAddress addr, long tid, short sequenceId, long timeout, int retries, Category logger, PingResponseCallback cb) {
        m_id = new PingRequestId(addr, tid, sequenceId);
        m_retries    = retries;
        m_timeout    = timeout;
        m_log        = logger;
        m_callback   = new LogPrefixPreservingCallbackAdapter(cb);
    }
    
    PingRequest(InetAddress addr, long tid, short sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(addr, tid, sequenceId, timeout, retries, ThreadCategory.getInstance(PingRequest.class), cb);
    }
    
    PingRequest(InetAddress addr, short sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(addr, s_nextTid++, sequenceId, timeout, retries, cb);
    }
    
    PingRequest(InetAddress addr, long timeout, int retries, PingResponseCallback cb) {
        this(addr, DEFAULT_SEQUENCE_ID, timeout, retries, cb);
    }
    

    public InetAddress getAddress() {
        return m_id.getAddress();
    }
    
    public long getTid() {
        return m_id.getTid();
    }
    
    public short getSequenceId() {
        return m_id.getSequenceId();
    }

    public int getRetries() {
        return m_retries;
    }

    public long getTimeout() {
        return m_timeout;
    }
    
    public ICMPEchoPacket getRequest() {
        return m_request;
    }

    public ICMPEchoPacket getResponse() {
        return m_response;
    }


    public long getExpiration() {
        return m_expiration;
    }
    
    /**
     * Returns true if the passed address and sequence ID is the target of the ping.
     */
    boolean isTarget(InetAddress addr, short sequenceId) {
        return (getAddress().equals(addr) && getSequenceId() == sequenceId);
    }

    /**
     * Send this PingRequest through the given icmpSocket
     * @param icmpSocket
     */
    public void sendRequest(IcmpSocket icmpSocket) {
        try {
            createRequestPacket();

            log().debug(System.currentTimeMillis()+": Sending Ping Request: "+this);
            icmpSocket.send(createDatagram());
        } catch (Throwable t) {
            m_callback.handleError(getAddress(), getRequest(), t);
        }
    }

    private Category log() {
        return m_log;
    }

    private DatagramPacket createDatagram() {
        byte[] data = m_request.toBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, getAddress(), 0);
        return packet;
    }

    public void createRequestPacket() {
        m_expiration = System.currentTimeMillis() + m_timeout;
        ICMPEchoPacket iPkt = new ICMPEchoPacket(getTid());
        iPkt.setIdentity(FILTER_ID);
        iPkt.setSequenceId(getSequenceId());
        iPkt.computeChecksum();
        m_request = iPkt;
    }
    
    public boolean processResponse(PingReply reply) {
        try {
            processResponse(reply.getPacket());
        } finally {
            setProcessed(true);
        }
        return true;
    }

    private void processResponse(ICMPEchoPacket packet) {
        m_response = packet;
        log().debug(System.currentTimeMillis()+": Ping Response Received "+this);
        m_callback.handleResponse(getAddress(), packet);
    }

    public PingRequest processTimeout() {
        try {
            PingRequest returnval = null;
            if (this.isExpired()) {
                if (this.getRetries() > 0) {
                    returnval = new PingRequest(getAddress(), getTid(), getSequenceId(), getTimeout(), getRetries() - 1, log(), m_callback);
                    log().debug(System.currentTimeMillis()+": Retrying Ping Request "+returnval);
                } else {
                    log().debug(System.currentTimeMillis()+": Ping Request Timed out "+this);
                    m_callback.handleTimeout(getAddress(), getRequest());
                }
            }
            return returnval;
        } finally {
            setProcessed(true);
        }
    }
    
    public boolean isExpired() {
        return (System.currentTimeMillis() >= getExpiration());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("ID=").append(getId()).append(',');
        sb.append("Retries=").append(getRetries()).append(",");
        sb.append("Timeout=").append(getTimeout()).append(",");
        sb.append("Expiration=").append(getExpiration()).append(',');
        sb.append("Callback=").append(m_callback);
        sb.append("]");
        return sb.toString();
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(getExpiration() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed request) {
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        long otherDelay = request.getDelay(TimeUnit.MILLISECONDS);
        if (myDelay < otherDelay) return -1;
        if (myDelay == otherDelay) return 0;
        return 1;
    }

    public PingRequestId getId() {
        return m_id;
    }

    public void processError(Throwable t) {
        try {
            m_callback.handleError(getAddress(), getRequest(), t);
        } finally {
            setProcessed(true);
        }
    }
    
    static class LogPrefixPreservingCallbackAdapter implements PingResponseCallback {
        private PingResponseCallback m_cb;
        private String m_prefix = ThreadCategory.getPrefix();
        
        public LogPrefixPreservingCallbackAdapter(PingResponseCallback cb) {
            m_cb = cb;
        }

        public void handleError(InetAddress address, ICMPEchoPacket packet, Throwable t) {
            String oldPrefix = ThreadCategory.getPrefix();
            try {
                ThreadCategory.setPrefix(m_prefix);
                m_cb.handleError(address, packet, t);
            } finally {
                ThreadCategory.setPrefix(oldPrefix);
            }
        }

        public void handleResponse(InetAddress address, ICMPEchoPacket packet) {
            String oldPrefix = ThreadCategory.getPrefix();
            try {
                ThreadCategory.setPrefix(m_prefix);
                m_cb.handleResponse(address, packet);
            } finally {
                ThreadCategory.setPrefix(oldPrefix);
            }
        }

        public void handleTimeout(InetAddress address, ICMPEchoPacket packet) {
            String oldPrefix = ThreadCategory.getPrefix();
            try {
                ThreadCategory.setPrefix(m_prefix);
                m_cb.handleTimeout(address, packet);
            } finally {
                ThreadCategory.setPrefix(oldPrefix);
            }
        }
        
        
        
    }
    
    private void setProcessed(boolean processed) {
        m_processed.set(processed);
    }

    public boolean isProcessed() {
        return m_processed.get();
    }

}
