package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.CollectionMath;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

public class Pinger {
	private static final int DEFAULT_TIMEOUT = 800;
	private static final int DEFAULT_RETRIES = 2;
	private static final short FILTER_ID = (short) (new java.util.Random(System.currentTimeMillis())).nextInt();

	private short sequenceId = 1;
	private long timeout;
	private int retries;

    private static IcmpSocket icmpSocket = null;
    private static ReplyReceiver receiver = null;
    private static Thread worker = null;
    
    /**
     * The map of long thread identifiers to Packets that must be signaled.
     * The mapped objects are instances of the
     * {@link org.opennms.netmgt.ping.Reply Reply} class.
     */
    private static Map<Long, PingRequest> waiting = Collections.synchronizedMap(new TreeMap<Long, PingRequest>());
    private static Map<Long, ArrayList> parallelWaiting = Collections.synchronizedMap(new TreeMap<Long, ArrayList>());

    /**
     * Initialize a Pinger object, using the default timeout and retries.
     * @throws IOException
     */
    public Pinger() throws IOException {
		this(DEFAULT_TIMEOUT, DEFAULT_RETRIES);
	}
    
    /**
     * Initialize a Pinger object, specifying the timeout.
     * @param timeout the timeout, in milliseconds, to wait for returned packets.
     * @throws IOException
     */
    public Pinger(int timeout) throws IOException {
    	this(timeout, DEFAULT_RETRIES);
    }
    
    /**
     * Initialize a Pinger object, specifying the timeout and retries.
     * @param timeout the timeout, in milliseconds, to wait for returned packets.
     * @param retries the number of times to retry a given ping packet
     * @throws IOException
     */
	public Pinger(int timeout, int retries) throws IOException {
		this.timeout = timeout;
		this.retries = retries;
		synchronized (Pinger.class) {
			if (worker == null) {
			    final FifoQueueImpl<Reply> queue = new FifoQueueImpl<Reply>();
				icmpSocket = new IcmpSocket();
                receiver = new ReplyReceiver(icmpSocket, queue, FILTER_ID);
                receiver.start();
				
                worker = new Thread(new Runnable() {
                    public void run() {
                        for (;;) {
                            Reply pong = null;
                            try {
                                pong = queue.remove();
                            } catch (InterruptedException ex) {
                                break;
                            } catch (Exception ex) {
                                ThreadCategory.getInstance(this.getClass()).error("Error processing response queue", ex);
                            }

                            ICMPEchoPacket pongPacket = pong.getPacket();
                            Long key = new Long(pongPacket.getTID());
                            short sid = pongPacket.getSequenceId();
                            PingRequest ping = null;
                            if (waiting.containsKey(key)) {
                            	PingRequest p = waiting.get(key);
                            	if (p != null && p.isTarget(pong.getAddress(), sid)) {
                            		ping = p;
                            	}
                            } else if (parallelWaiting.containsKey(key)) {
                            	ArrayList list = parallelWaiting.get(key);
                            	for (int i = 0; i < list.size(); i++) {
                            		PingRequest p = (PingRequest)list.get(i);
                            		if (p != null && p.isTarget(pong.getAddress(), sid)) {
                            			ping = p;
                            		}
                            	}
                            } else {
                            	// hmm, should we do anything in this case?
                            }

                            if (ping != null) {
                            	ping.setPacket(pong.getPacket());
                            	ping.signal();
                            }
                        }
                    }
                });
                worker.setDaemon(true);
                worker.start();
			}
			
		}
	}

	/**
	 * Get the number of retries for this pinger.
	 * @return the number of retries, as an integer
	 */
	public int getRetries() {
		return this.retries;
	}
	
	/**
	 * Set the number of times to retry for this pinger.
	 * @param retries the number of retries, as an integer
	 */
	public void setRetries(int retries) {
		this.retries = retries;
	}

	/**
	 * Get the timeout for receiving ICMP echo replies for this pinger.
	 * @return the timeout in milliseconds, as a long integer
	 */
	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * Set the timeout for receiving ICMP echo replies for this pinger.
	 * @param timeout the timeout in milliseconds, as a long integer
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
    /**
     * This method is used to ping a remote host to test for ICMP support. If
     * the remote host responds within the specified period, defined by retries
     * and timeouts, then the response time is returned.
     * 
     * @param host
     *            The address to poll.
     * @param timeout
     *            The time to wait between each retry.
     * @param retries
     *            The number of times to retry
     * 
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
     */
    public Long ping(InetAddress host, long timeout, int retries) throws IOException {
        Category log = ThreadCategory.getInstance(this.getClass());
        
        Long tidKey = getTidKey();

        short sid = sequenceId++;
    	DatagramPacket pkt = getDatagram(host, tidKey, sid);
        PingRequest reply = new PingRequest(host, sid);
        
        waiting.put(tidKey, reply);
        for (int attempts = 0; attempts <= retries && !reply.isSignaled(); ++attempts) {
            synchronized (reply) {
                sendPacket(pkt);
                try {
                    reply.wait(timeout);
                } catch (InterruptedException ex) {
                    // interrupted so return, reset interrupt.
                    Thread.currentThread().interrupt();
                }
            }
        }
        waiting.remove(tidKey);
        
        Long rtt = getRTT(reply);
        if (rtt == null && log.isDebugEnabled()) {
        	log.debug("no response time for " + host + "received");
        } else if (log.isDebugEnabled()){
        	log.debug("Ping round trip time for " + host + ": " + rtt + "us");
        }
        return rtt;
    }

	/**
	 * Ping a remote host, using the default number of retries and timeouts.
	 * @param host the host to ping
	 * @return the round-trip time of the packet
	 * @throws IOException
	 */
	public Long ping(InetAddress host) throws IOException {
		return this.ping(host, timeout, retries);
	}

	public Map<String, Number> parallelPing(InetAddress host, int count) throws IOException {
        Category log = ThreadCategory.getInstance(this.getClass());
        LinkedHashMap<String, Number> returnval = new LinkedHashMap<String, Number>();
        
        Long tidKey = getTidKey();
        ArrayList<PingRequest> requests = new ArrayList<PingRequest>();
        parallelWaiting.put(tidKey, requests);
        for (int i = 0; i < count; i++) {
        	short sid = sequenceId++;
        	PingRequest reply = new PingRequest(host, sid);
        	// log.debug("sending packet with ID '" + tidKey + "' and sequence '" + reply.getSequenceId());
        	requests.add(reply);
        	DatagramPacket pkt = getDatagram(host, tidKey, reply.getSequenceId());
        	synchronized(reply) {
        		sendPacket(pkt);
        	}
       		try {
       			Thread.sleep(100);
       		} catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
       		}
        }
        
        try {
            synchronized(requests) {
            	requests.wait(timeout);
            }
        } catch (InterruptedException ex) {
            // interrupted so return, reset interrupt.
            Thread.currentThread().interrupt();
        }

        parallelWaiting.remove(tidKey);

        Collections.sort(requests, new Comparator<PingRequest>() {
			public int compare(PingRequest arg0, PingRequest arg1) {
				if (!arg0.isSignaled()) {
					return -1;
				}
				if (!arg1.isSignaled()) {
					return 1;
				}
				return (int)(arg0.getPacket().getPingRTT() - arg1.getPacket().getPingRTT());
			}
        });
        for (int i = 0; i < requests.size(); i++) {
        	PingRequest reply = requests.get(i);
        	if (reply.isSignaled()) {
        		Long rtt = getRTT(reply);
        		if (rtt <= (timeout * 1000)) {
        			returnval.put("ping" + (i+1), rtt);
        		} else {
        			log.debug("a response came back, but it was too old: sid = " + reply.getSequenceId() + ", rtt = " + rtt);
        			returnval.put("ping" + (i+1), null);
        		}
    		} else {
    			log.debug("no response came back: sid = " + reply.getSequenceId());
    			returnval.put("ping" + (i+1), null);
        	}
        }

        ArrayList<Number> al = new ArrayList<Number>(returnval.values());
        returnval.put("loss", new Long(CollectionMath.countNull(al)));
        returnval.put("median", new Long(CollectionMath.median(al).longValue()));
        returnval.put("response-time", new Long(CollectionMath.average(al).longValue()));
        
        return returnval;
	}

    private void sendPacket(DatagramPacket pkt) throws IOException {
        try {
            icmpSocket.send(pkt);
        } catch (IOException ioE) {
            Category log = ThreadCategory.getInstance(this.getClass());
            log.info("isPingable: Failed to send to address " + pkt.getAddress(), ioE);
        } catch (Throwable t) {
            Category log = ThreadCategory.getInstance(this.getClass());
            log.info("isPingable: Undeclared throwable exception caught sending to " + pkt.getAddress(), t);
        }
    }

    private Long getTidKey() {
    	Long tidKey = null;
    	long tid = (long) Thread.currentThread().hashCode();
    	synchronized (waiting) {
    		while (waiting.containsKey(tidKey = new Long(tid))) {
    			++tid;
    		}
    	}
    	return tidKey;
    }

    private Long getRTT(PingRequest reply) {
        if (reply.isSignaled()) {
        	ICMPEchoPacket p = reply.getPacket();
        	if (p != null) {
                return p.getPingRTT();
        	}
        }
        return null;
    }
    
    /**
     * Builds a datagram compatible with the ping ReplyReceiver class.
     */
    private synchronized static DatagramPacket getDatagram(InetAddress addr, long tid, short sid) {
        ICMPEchoPacket iPkt = new ICMPEchoPacket(tid);
        iPkt.setIdentity(FILTER_ID);
        iPkt.setSequenceId(sid);
        iPkt.computeChecksum();

        byte[] data = iPkt.toBytes();
        return new DatagramPacket(data, data.length, addr, 0);
    }

    /**
     * This class is used to encapsulate a ping request. A request consist of
     * the pingable address and a signaled state.
     */
    private static final class PingRequest {
        /**
         * The address being pinged
         */
        private final InetAddress m_addr;

        /**
         * The sequence ID of the packet
         */
        private final short m_sequence;
        
        /**
         * The ping packet (contains sent/received time stamps)
         */
        private ICMPEchoPacket m_packet;

        /**
         * The state of the ping
         */
        private boolean m_signaled;

        /**
         * Constructs a new ping object
         */
        PingRequest(InetAddress addr, short sequenceId) {
            m_addr = addr;
            m_sequence = sequenceId;
        }

        InetAddress getAddress() {
        	return m_addr;
        }
        
        short getSequenceId() {
        	return m_sequence;
        }
        
        /**
         * Returns true if signaled.
         */
        synchronized boolean isSignaled() {
            return m_signaled;
        }

        /**
         * Sets the signaled state and awakes the blocked threads.
         */
        synchronized void signal() {
            m_signaled = true;
            notifyAll();
        }

        /**
         * Returns true if the passed address is the target of the ping.
         */
        boolean isTarget(InetAddress addr, short sequenceId) {
            return (m_addr.equals(addr) && m_sequence == sequenceId);
        }
        
        void setPacket(ICMPEchoPacket packet) {
            m_packet = packet;
        }

        ICMPEchoPacket getPacket() {
            return m_packet;
        }

    }

}
