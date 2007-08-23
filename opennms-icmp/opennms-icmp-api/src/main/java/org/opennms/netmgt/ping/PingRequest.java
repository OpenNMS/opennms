package org.opennms.netmgt.ping;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 */
final class PingRequest {
    public static final short FILTER_ID = (short) (new java.util.Random(System.currentTimeMillis())).nextInt();
    private static final short DEFAULT_SEQUENCE_ID = 1;
    private static long s_nextTid = 1;

	/**
	 * the request packet
	 */
	private ICMPEchoPacket m_request = null;
	
	/**
	 * the response packet
	 */
	private ICMPEchoPacket m_response = null;

	/**
	 * the host being pinged
	 */
	private InetAddress m_addr = null;
	
    /**
     * The callback to use when this object is ready to do something
     */
    private PingResponseCallback m_callback = null;

    /**
     * The request ID
     */
    private long m_tid;

    /**
     * The packet sequence ID to use
     */
    private short m_sequenceId;

    /**
     * The expiration time of this request
     */
    private long m_expiration = -1L;
    
    /**
     * How many retries
     */
    private int m_retries;
    
    /**
     * how long to wait for a response
     */
    private long m_timeout;
    
    PingRequest(InetAddress addr, long timeout, int retries, long tid, short sequenceId, PingResponseCallback cb) {
        m_addr       = addr;
        m_retries    = retries;
        m_timeout    = timeout;
        m_callback   = cb;
        m_tid        = tid;
        m_sequenceId = sequenceId;
    }
    
    PingRequest(InetAddress addr, long timeout, int retries, short sequenceId, PingResponseCallback cb) {
        this(addr, timeout, retries, s_nextTid++, sequenceId, cb);
    }
    
    PingRequest(InetAddress addr, long timeout, int retries, PingResponseCallback cb) {
        this(addr, timeout, retries, DEFAULT_SEQUENCE_ID, cb);
    }
    


    InetAddress getAddress() {
    	return m_addr;
    }
    
    short getSequenceId() {
        return m_sequenceId;
    }

    public long getTid() {
        return m_tid;
    }
    
    public ICMPEchoPacket getRequest() {
        return m_request;
    }

    public ICMPEchoPacket getResponse() {
        return m_response;
    }

    public int getRetries() {
        return m_retries;
    }

    public long getTimeout() {
        return m_timeout;
    }
    
    public long getExpiration() {
        return m_expiration;
    }
    
    /**
     * Returns true if the passed address and sequence ID is the target of the ping.
     */
    boolean isTarget(InetAddress addr, short sequenceId) {
        return (m_addr.equals(addr) && getSequenceId() == sequenceId);
    }

    /**
     * Send this PingRequest through the given icmpSocket
     * @param icmpSocket
     */
    public void sendRequest(IcmpSocket icmpSocket) {
        try {
            m_expiration = System.currentTimeMillis() + m_timeout;
            ICMPEchoPacket iPkt = new ICMPEchoPacket(getTid());
            iPkt.setIdentity(FILTER_ID);
            iPkt.setSequenceId(getSequenceId());
            iPkt.computeChecksum();
            m_request = iPkt;

            byte[] data = iPkt.toBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, getAddress(), 0);
            ThreadCategory.getInstance(this.getClass()).info(System.currentTimeMillis()+": Sending Ping Request: "+this);
            icmpSocket.send(packet);
        } catch (Throwable t) {
            m_callback.handleError(this, t);
        }
    }
    
    public void processResponse(ICMPEchoPacket packet) {
        m_response = packet;
        m_callback.handleResponse(packet);
    }
    
    public PingRequest processTimeout() {
        PingRequest returnval = null;
        if (this.isExpired()) {
            if (this.getRetries() > 0) {
                returnval = new PingRequest(getAddress(), getTimeout(), getRetries() - 1, getTid(), getSequenceId(), m_callback);
            } else {
                m_callback.handleTimeout(getRequest());
            }
        }
        return returnval;
    }
    
    public boolean isExpired() {
        return (System.currentTimeMillis() > getExpiration());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getAddress()).append(" [");
        sb.append("TID=").append(this.getTid()).append(",");
        sb.append("Sequence ID=").append(this.getSequenceId()).append(",");
        // sb.append("Callback=").append(m_callback.getClass().getName()).append(",");
        sb.append("Retries=").append(getRetries()).append(",");
        sb.append("Timeout=").append(getTimeout()).append(",");
        sb.append("Expiration=").append(getExpiration());
        sb.append("]");
        return sb.toString();
    }

}
