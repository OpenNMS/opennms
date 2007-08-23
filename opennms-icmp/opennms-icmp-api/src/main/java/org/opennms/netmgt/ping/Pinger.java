package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

public class Pinger {
    public static final int DEFAULT_TIMEOUT = 800;
    public static final int DEFAULT_RETRIES = 2;
    public static final int DEFAULT_WAIT_TIME = 2000;
	
    private static IcmpSocket icmpSocket = null;
    private static ReplyReceiver receiver = null;
    private static Thread worker = null;

    /**
     * The map of long thread identifiers to Packets that must be signaled.
     * The mapped objects are instances of the
     * {@link org.opennms.netmgt.ping.Reply Reply} class.
     */
    private static Map<Long, PingRequest> waiting = Collections.synchronizedMap(new TreeMap<Long, PingRequest>());

    public long minimumWaitTime() {
    	ArrayList<PingRequest> pr = null;
    	synchronized(waiting) {
    	    if (waiting.size() == 0) {
    	        return -1L;
    	    }
    	    
            pr = new ArrayList<PingRequest>(waiting.values());
    	}
        Collections.sort(pr, new Comparator<PingRequest>() {
            public int compare(PingRequest arg0, PingRequest arg1) {
                if (arg0 == null) {
                    return -1;
                } else if (arg1 == null) {
                    return 1;
                } else if (arg0.getExpiration() == arg0.getExpiration()) {
                    return 0;
                } else {
                    return (arg1.getExpiration() > arg0.getExpiration()? 1 : -1);
                }
            }
        });
        ThreadCategory.getInstance(this.getClass()).info(System.currentTimeMillis() + ": " + pr.size() + " packets in the queue, lowest is " + pr.get(0));
        long waitTime = pr.get(0).getExpiration() - System.currentTimeMillis();
        if (waitTime < 0) {
            return 0;
        } else {
            return waitTime;
        }
    }
    
    /**
     * Initialize a Pinger object, specifying the timeout and retries.
     * @param defaultTimeout the timeout, in milliseconds, to wait for returned packets.
     * @param defaultRetries the number of times to retry a given ping packet
     * @throws IOException
     */
	public Pinger() throws IOException {
		synchronized (Pinger.class) {
			if (worker == null) {
			    final FifoQueueImpl<Reply> queue = new FifoQueueImpl<Reply>();
				icmpSocket = new IcmpSocket();
                receiver = new ReplyReceiver(icmpSocket, queue, PingRequest.FILTER_ID);
                receiver.start();
				
                worker = new Thread(new Runnable() {
                    public void run() {
                        for (;;) {
                        	long waitTime = minimumWaitTime();
                        	ThreadCategory.getInstance(this.getClass()).info("minimum wait time: " + waitTime);
                        	if (waitTime > 0) {
                        	    try {
                                    Reply pong = queue.remove(waitTime);
                                    if (pong != null) {
                                        processReply(pong);
                                    }
                        	    } catch (InterruptedException ie) {
                        	        break;
                        	    } catch (FifoQueueException fqe) {
                        	        ThreadCategory.getInstance(this.getClass()).error("Error processing response queue", fqe);
                        	    }
                        	} else if (waitTime == -1L) {
                        	    try {
                                    Thread.sleep(DEFAULT_WAIT_TIME);
                                } catch (InterruptedException e) {
                                    ThreadCategory.getInstance(this.getClass()).info("interrupted while waiting for new packets to handle", e);
                                }
                        	} else {
                        	    processTimeouts();
                        	}
                        }
                    }
                });
                worker.setDaemon(true);
                worker.start();
			}
			
		}
	}
	
    protected void processTimeouts() {
        synchronized(waiting) {
            for (Iterator<Entry<Long, PingRequest>> it = waiting.entrySet().iterator(); it.hasNext(); ) {
                PingRequest request = it.next().getValue();
                ThreadCategory.getInstance(this.getClass()).debug("checking request " + request);
                if (request.isExpired()) {
                    it.remove();
                    PingRequest retry = request.processTimeout();
                    if (retry != null) {
                        waiting.put(retry.getTid(), retry);
                        retry.sendRequest(icmpSocket);
                    }
                }
            }
        }
    }

    protected void processReply(Reply pong) {
        ICMPEchoPacket pongPacket = pong.getPacket();
        Long key = new Long(pongPacket.getTID());
        short sid = pongPacket.getSequenceId();
        PingRequest ping = null;
        synchronized(waiting) {
            if (waiting.containsKey(key)) {
                PingRequest p = waiting.get(key);
                if (p != null && p.isTarget(pong.getAddress(), sid)) {
                    ping = p;
                    waiting.remove(key);
                }
            }
        }

        if (ping != null) {
            ping.processResponse(pong.getPacket());
        }
    }

    public void ping(PingRequest request, PingResponseCallback cb) {
        synchronized(waiting) {
            waiting.put(request.getTid(), request);
            request.sendRequest(icmpSocket);
        }
    }
    
    public void ping(InetAddress host, long timeout, int retries, short sequenceId, PingResponseCallback cb) {
    	PingRequest request = new PingRequest(host, timeout, retries, sequenceId, cb);
    	synchronized(waiting) {
    	    waiting.put(request.getTid(), request);
    	    request.sendRequest(icmpSocket);
    	}
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
     * @throws InterruptedException 
     */
    public Long ping(InetAddress host, long timeout, int retries) throws InterruptedException {
        SinglePingResponseCallback cb = new SinglePingResponseCallback();
        ping(host, timeout, retries, (short) 1, cb);
        cb.waitFor();
        return cb.getResponseTime();
    }

	/**
	 * Ping a remote host, using the default number of retries and timeouts.
	 * @param host the host to ping
	 * @return the round-trip time of the packet
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public Long ping(InetAddress host) throws IOException, InterruptedException {
	    return this.ping(host, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
	}

	public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws IOException, InterruptedException {
	    ParallelPingResponseCallback cb = new ParallelPingResponseCallback(count);
	    
	    for (int i = 0; i < count; i++) {
	        PingRequest request = new PingRequest(host, DEFAULT_TIMEOUT, 0, (short) i, cb);
	        ping(request, cb);
	        Thread.sleep(pingInterval);
	    }
	    
	    cb.waitFor();
	    return cb.getResponseTimes();
	}

}
