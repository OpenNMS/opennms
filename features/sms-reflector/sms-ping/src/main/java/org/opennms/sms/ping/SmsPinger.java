/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.ping;

import static org.opennms.sms.ping.PingConstants.DEFAULT_RETRIES;
import static org.opennms.sms.ping.PingConstants.DEFAULT_TIMEOUT;

import java.io.IOException;

import org.opennms.sms.ping.internal.SinglePingResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * <p>SmsPinger class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class SmsPinger {
    
    private static SmsPingTracker s_pingTracker;
    private static Logger log = LoggerFactory.getLogger(SmsPinger.class);
    
	/**
	 * Initializes this singleton
	 *
	 * @throws java.io.IOException if any.
	 */
	public synchronized static void initialize() throws IOException {
	    if (s_pingTracker == null) throw new IllegalStateException("SmsPinger not yet initialized!!"); 
	}
	
	/**
	 * <p>setSmsPingTracker</p>
	 *
	 * @param pingTracker a {@link org.opennms.sms.ping.SmsPingTracker} object.
	 */
	public synchronized static void setSmsPingTracker(SmsPingTracker pingTracker) {
	    log.debug("Initializing SmsPinger with pingTracker {}", pingTracker);
	    s_pingTracker = pingTracker;
	}

    /**
     * <p>ping</p>
     *
     * @param phoneNumber a {@link java.lang.String} object.
     * @param timeout a long.
     * @param retries a int.
     * @param cb a {@link org.opennms.sms.ping.PingResponseCallback} object.
     * @throws java.lang.Exception if any.
     */
    public static void ping(String phoneNumber, long timeout, int retries, PingResponseCallback cb) throws Exception {
        initialize();
        s_pingTracker.sendRequest(phoneNumber, timeout, retries, cb);
	}

    /**
     * This method is used to ping a remote host to test for ICMP support. If
     * the remote host responds within the specified period, defined by retries
     * and timeouts, then the response time is returned.
     *
     * @param phoneNumber
     *            The address to poll.
     * @param timeout
     *            The time to wait between each retry.
     * @param retries
     *            The number of times to retry
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
     * @throws java.lang.InterruptedException if any.
     * @throws IOException if any.
     * @throws java.lang.Exception if any.
     */
    public static Long ping(String phoneNumber, long timeout, int retries) throws InterruptedException, Exception {
        SinglePingResponseCallback cb = new SinglePingResponseCallback(phoneNumber);
        SmsPinger.ping(phoneNumber, timeout, retries, cb);
        cb.waitFor();
        return cb.getResponseTime();
    }
    

	/**
	 * Ping a remote host, using the default number of retries and timeouts.
	 *
	 * @param host the host to ping
	 * @return the round-trip time of the packet
	 * @throws IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 * @throws java.lang.Exception if any.
	 */
	public static Long ping(String host) throws Exception, InterruptedException {
        SinglePingResponseCallback cb = new SinglePingResponseCallback(host);
        SmsPinger.ping(host, DEFAULT_TIMEOUT, DEFAULT_RETRIES, cb);
        cb.waitFor();
        return cb.getResponseTime();
	}

}
