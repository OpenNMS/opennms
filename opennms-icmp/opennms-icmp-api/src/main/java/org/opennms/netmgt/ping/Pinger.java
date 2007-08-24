package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ping.PingRequest.RequestId;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * 
 * Pinger Design
 * 
 * The pinger has four components that are all static
 * 
 * an icmpSocket
 * a pendingRequest map
 * a pendingReply queue (LinkedBlockingQueue)
 * a timeout queue (DelayQueue)
 * 
 * It also has three threads:
 * 
 * a thread to read from the icmpSocket - (icmp socket reader)
 * a thread to process the pendingReplyQueue - (icmp reply processor)
 * a thread to process the timeouts (icmp timeout processor)
 * 
 * Processing:
 * 
 * All requests are asynchronous (if synchronous requests are need that
 * are implemented using asynchronous requests and blocking callbacks)
 * 
 * Making a request: (client thread)
 * - create a pingRequest 
 * - add it to a pendingRequestMap
 * - send the request
 * - add it to the timeout queue
 * 
 * Reading from the icmp socket: (icmp socket reader)
 * - read a packet from the socket
 * - construct a reply object
 * - verify it is an opennms gen'd packet
 * - add it to the pendingReply queue
 * 
 * Processing a reply: (icmp reply processor)
 * - take a reply from the pendingReply queue
 * - look up and remove the matching request in the pendingRequest map
 * - call request.processReply(reply) - this will store the reply and
 *   call the handleReply call back
 * - pending request sets completed to true
 * 
 * Processing a timeout:
 * - take a request from the timeout queue
 * - if the request is completed discard it
 * - otherwise, call request.processTimeout(), this will check the number
 *   of retries and either return a new request with fewer retries or
 *   call the handleTimeout call back
 * - if processTimeout returns a new request than process it as in Making
 *   a request 
 * 
 * Thread Details:
 * 
 * 1.  The icmp socket reader that will listen on the ICMP socket.  It
 *     will pull packets off the socket and construct replies and add
 *     them to a LinkedBlockingQueue
 * 
 * 2.  The icmp reply processor that will pull replies off the linked
 *     blocking queue and process them.  This will result in calling the
 *     PingResponseCallback handleReply method.
 * 
 * 3.  The icmp timeout processor that will pull PingRequests off of a
 *     DelayQueue.  A DelayQueue does not allow things to be removed from
 *     them until the timeout has expired.
 * 
 */

public class Pinger {
    public static final int DEFAULT_TIMEOUT = 800;
    public static final int DEFAULT_RETRIES = 2;
    
    private static boolean s_initialized = false;
    
    private static IcmpSocket s_icmpSocket = null;
    private static Map<RequestId, PingRequest> s_pendingRequests;
    private static LinkedBlockingQueue<Reply> s_pendingReplyQueue;
    private static DelayQueue<PingRequest> s_timeoutQueue;
    
    private static Thread s_socketReader;
    private static Thread s_replyProcessor;
    private static Thread s_timeoutProcessor;
    
    /**
     * Initialize a Pinger object, specifying the timeout and retries.
     * @param defaultTimeout the timeout, in milliseconds, to wait for returned packets.
     * @param defaultRetries the number of times to retry a given ping packet
     * @throws IOException
     */
	public Pinger() throws IOException {
		initialize();
	}
	
	public synchronized static void initialize() throws IOException {   
	    if (s_initialized) return;
	    
	    s_icmpSocket = new IcmpSocket();
	    s_pendingRequests = Collections.synchronizedMap(new HashMap<RequestId, PingRequest>());
	    s_pendingReplyQueue = new LinkedBlockingQueue<Reply>();
	    s_timeoutQueue = new DelayQueue<PingRequest>();
	    
	    s_socketReader = new Thread("ICMP-Socket-Reader") {
	        public void run() {
	            try {
	                processPackets();
	            } catch (InterruptedException e) {
	                log().error("Thread "+this+" interrupted!");
	            }
	        }
	    };
	    
	    s_replyProcessor = new Thread("ICMP-Reply-Processor") {
	        public void run() {
	            try {
	                processReplies();
	            } catch (InterruptedException e) {
	                log().error("Thread "+this+" interrupted!");
	            }
	        }
	    };
	    
	    s_timeoutProcessor = new Thread("ICMP-Timeout-Processor") {
	        public void run() {
	            try {
	                processTimeouts();
	            } catch (InterruptedException e) {
	                log().error("Thread "+this+" interrupted!");
	            }
	        }
	    };
	    
	    s_timeoutProcessor.start();
	    s_replyProcessor.start();
	    s_socketReader.start();
	    s_initialized = true;
	}
	
    private static void ping(PingRequest request) throws IOException {
        initialize();
        synchronized(s_pendingRequests) {
            s_pendingRequests.put(request.getId(), request);
            request.sendRequest(s_icmpSocket);
        }
        debugf("Scheding timeout for request to %s in %d ms", request, request.getDelay(TimeUnit.MILLISECONDS));
        s_timeoutQueue.offer(request);
    }

	private static void processReplies() throws InterruptedException {
	    while (true) {
	        Reply reply = s_pendingReplyQueue.take();
            debugf("Found a reply to process: %s", reply);
	        RequestId id = new RequestId(reply);
	        debugf("Looking for request with Id: %s in map %s", id, s_pendingRequests);
	        PingRequest request = s_pendingRequests.remove(id);
	        if (request != null) {
	            debugf("Processing reply %s for request %s", reply, request);
	            request.processResponse(reply.getPacket());
	        } else {
	            debugf("No request found for reply %s", reply);
	        }
	    }
    }

	private static void processPackets() throws InterruptedException {
        while (true) {
            try {
                DatagramPacket packet = s_icmpSocket.receive();

                // Check the packet length
                if (packet.getLength() != ICMPEchoPacket.getNetworkSize()) {
                    // skip it its not an echo reply
                    continue;
                }
                
                ICMPEchoPacket pkt = new ICMPEchoPacket(packet.getData());
                Reply reply = new Reply(packet.getAddress(), pkt);
                
                if (reply.isEchoReply() && reply.getIdentity() == PingRequest.FILTER_ID) {
                    debugf("Found an echo packet addr = %s, port = %d, length = %d, created reply %s", packet.getAddress(), packet.getPort(), packet.getLength(), reply);
                    s_pendingReplyQueue.offer(reply);
                }
            } catch (IOException e) {
                log().error("I/O Error occurred reading from ICMP Socket", e);
            }
            
        }
    }

	private static void processTimeouts() throws InterruptedException {  
	    while (true) {
	        PingRequest request = s_timeoutQueue.take();
            debugf("Found a possibly timedout request: %s", request);
	        if (s_pendingRequests.remove(request.getId()) == request) {
	            // then this request is still pending so we must time it out
	            debugf("Processing timeout for: %s", request);
	            PingRequest retry = request.processTimeout();
	            if (retry != null) {
	                try {
                        ping(retry);
                    } catch (IOException e) {
                        retry.processError(e);
                    }
	            }
	        }
	        
	    }
	}
	
    public static void ping(InetAddress host, long timeout, int retries, short sequenceId, PingResponseCallback cb) throws IOException {
    	ping(new PingRequest(host, sequenceId, timeout, retries, cb));
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
     * @throws IOException 
     */
    public static Long ping(InetAddress host, long timeout, int retries) throws InterruptedException, IOException {
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
	public static Long ping(InetAddress host) throws IOException, InterruptedException {
	    return ping(host, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
	}

	public static List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws IOException, InterruptedException {
	    ParallelPingResponseCallback cb = new ParallelPingResponseCallback(count);
	    
	    for (int i = 0; i < count; i++) {
	        PingRequest request = new PingRequest(host, (short) i, DEFAULT_TIMEOUT, 0, cb);
	        ping(request);
	        Thread.sleep(pingInterval);
	    }
	    
	    cb.waitFor();
	    return cb.getResponseTimes();
	}

    private static Category log() {
        return ThreadCategory.getInstance(Pinger.class);
    }
    
    private static void debugf(String format, Object... args) {
        //if (log().isDebugEnabled()) {
        //    log().debug(String.format(format, args));
        //}
    }

}
