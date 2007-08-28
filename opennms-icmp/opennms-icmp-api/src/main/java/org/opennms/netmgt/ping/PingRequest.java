package org.opennms.netmgt.ping;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 */
final class PingRequest implements Delayed {
    public static class RequestId {
        InetAddress m_addr;
        long m_tid;
        short m_seqId;

        public RequestId(InetAddress addr, long tid, short seqId) {
            m_addr = addr;
            m_tid = tid;
            m_seqId = seqId;
        }
        
        public RequestId(Reply reply) {
            this(reply.getAddress(), reply.getPacket().getTID(), reply.getPacket().getSequenceId());
        }

        public InetAddress getAddress() {
            return m_addr;
        }

        public long getTid() {
            return m_tid;
        }

        public short getSequenceId() {
            return m_seqId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RequestId) {
                RequestId id = (RequestId)obj;
                return getAddress().equals(id.getAddress()) && getTid() == id.getTid() && getSequenceId() == id.getSequenceId();
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + m_addr.hashCode();
            hash = hash * 31 + (int)(m_tid >>> 32);
            hash = hash * 31 + (int)(m_tid);
            hash = hash * 31 + m_seqId;
            return hash;
        }


        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(super.toString());
            buf.append('[');
            buf.append("addr = ").append(m_addr);
            buf.append(", ");
            buf.append("tid = ").append(m_tid);
            buf.append(", ");
            buf.append("seqId = ").append(m_seqId);
            buf.append(']');
            return buf.toString();
        }
        

    }

    public static final short FILTER_ID = (short) (new java.util.Random(System.currentTimeMillis())).nextInt();
    private static final short DEFAULT_SEQUENCE_ID = 1;
    private static long s_nextTid = 1;

    /**
     * The id representing the packet
     */
    private RequestId m_id;

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
    
    PingRequest(InetAddress addr, long tid, short sequenceId, long timeout, int retries, PingResponseCallback cb) {
        m_id = new RequestId(addr, tid, sequenceId);
        m_retries    = retries;
        m_timeout    = timeout;
        m_callback   = cb;
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

            log().info(System.currentTimeMillis()+": Sending Ping Request: "+this);
            icmpSocket.send(createDatagram());
        } catch (Throwable t) {
            m_callback.handleError(this, t);
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
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
    
    public void processResponse(ICMPEchoPacket packet) {
        m_response = packet;
        log().info(System.currentTimeMillis()+": Ping Response Received "+this);
        m_callback.handleResponse(packet);
    }
    
    public PingRequest processTimeout() {
        PingRequest returnval = null;
        if (this.isExpired()) {
            if (this.getRetries() > 0) {
                returnval = new PingRequest(getAddress(), getTid(), getSequenceId(), getTimeout(), getRetries() - 1, m_callback);
                log().info(System.currentTimeMillis()+": Retrying Ping Request "+returnval);
            } else {
                log().info(System.currentTimeMillis()+": Ping Request Timed out "+this);
                m_callback.handleTimeout(getRequest());
            }
        }
        return returnval;
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

    public RequestId getId() {
        return m_id;
    }

    public void processError(Throwable t) {
        m_callback.handleError(this, t);
    }

}
