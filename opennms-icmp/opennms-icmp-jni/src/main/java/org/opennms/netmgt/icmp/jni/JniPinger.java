/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.icmp.jni;

import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_PACKET_SIZE;
import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_RETRIES;
import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_TIMEOUT;

import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Callable;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.icmp.LogPrefixPreservingPingResponseCallback;
import org.opennms.netmgt.icmp.ParallelPingResponseCallback;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.SinglePingResponseCallback;
import org.opennms.protocols.rt.IDBasedRequestLocator;
import org.opennms.protocols.rt.RequestTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * JniPinger Design
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
 * <p>JniPinger class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class JniPinger implements Pinger {
    private static final Logger LOG = LoggerFactory.getLogger(JniPinger.class);

    private final int m_pingerId = new SecureRandom().nextInt(Short.MAX_VALUE);

    private RequestTracker<JniPingRequest, JniPingResponse> s_pingTracker;
    private Throwable m_error = null;
    private JniIcmpMessenger m_messenger;

    public JniPinger() {}

    /**
     * Initializes this singleton
     *
     * @throws java.io.IOException if any.
     */
    private synchronized void initialize() throws Exception {
        if (s_pingTracker != null) return;
        try {
            m_messenger = new JniIcmpMessenger(m_pingerId);
            s_pingTracker = Logging.withPrefix("icmp", new Callable<RequestTracker<JniPingRequest, JniPingResponse>>() {

                @Override public RequestTracker<JniPingRequest, JniPingResponse> call() throws Exception {
                    return new RequestTracker<JniPingRequest, JniPingResponse>("JNI-ICMP-"+m_pingerId, m_messenger, new IDBasedRequestLocator<JniPingRequestId, JniPingRequest, JniPingResponse>());
                }
            });
            s_pingTracker.start();
        } catch (final IOException ioe) {
            m_error = ioe;
            final String errorMessage = m_error.getMessage().toLowerCase();
            if (errorMessage.contains("permission denied") || errorMessage.contains("operation not permitted")) {
                LOG.error("Permission error received while attempting to open ICMP socket. See https://wiki.opennms.org/wiki/ICMP for information on configuring ICMP for non-root.");
            }
            s_pingTracker = null;
            throw ioe;
        } catch (final RuntimeException rte) {
            m_error = rte;
            s_pingTracker = null;
            throw rte;
        }
    }

    @Override
    public void initialize4() throws Exception {
        initialize();
    }

    @Override
    public void initialize6() throws Exception {
        throw new IllegalStateException("This pinger does not support IPv6.");
    }

    @Override
    public boolean isV4Available() {
        try {
            initialize();
        } catch (final Throwable t) {
            LOG.trace("Failed to initialize IPv4", t);
        }
        if (s_pingTracker != null && m_error == null) return true;
        return false;
    }

    @Override
    public boolean isV6Available() {
        return false;
    }

    /**
     * <p>ping</p>
     *
     * @param host a {@link java.net.InetAddress} object.
     * @param timeout a long.
     * @param retries a int.
     * @param packetsize The size in byte of the ICMP packet.
     * @param sequenceId a short.
     * @param cb a {@link org.opennms.netmgt.icmp.jni.PingResponseCallback} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void ping(final InetAddress host, final long timeout, final int retries, final int packetsize, final int sequenceId, final PingResponseCallback cb) throws Exception {
        initialize();
        s_pingTracker.sendRequest(new JniPingRequest(host, m_pingerId, sequenceId, timeout, retries, packetsize, new LogPrefixPreservingPingResponseCallback(cb)));
    }

    /**
     * <p>ping</p>
     *
     * @param host a {@link java.net.InetAddress} object.
     * @param timeout a long.
     * @param retries a int.
     * @param packetsize The size in byte of the ICMP packet.
     * @param sequenceId a short.
     * @param cb a {@link org.opennms.netmgt.icmp.jni.PingResponseCallback} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void ping(final InetAddress host, final long timeout, final int retries, final int sequenceId, final PingResponseCallback cb) throws Exception {
        initialize();
        s_pingTracker.sendRequest(new JniPingRequest(host, m_pingerId, sequenceId, timeout, retries, DEFAULT_PACKET_SIZE, new LogPrefixPreservingPingResponseCallback(cb)));
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
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
     * @throws InterruptedException if any.
     * @throws IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Number ping(final InetAddress host, final long timeout, final int retries, final int packetsize) throws Exception {
        final SinglePingResponseCallback cb = new SinglePingResponseCallback(host);
        ping(host, timeout, retries, packetsize,(short)1, cb);
        cb.waitFor();
        cb.rethrowError();
        return cb.getResponseTime();
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
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
     * @throws InterruptedException if any.
     * @throws IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
        return ping(host, timeout, retries, DEFAULT_PACKET_SIZE);
    }

    /**
     * Ping a remote host, using the default number of retries and timeouts.
     *
     * @param host the host to ping
     * @return the round-trip time of the packet
     * @throws IOException if any.
     * @throws InterruptedException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Number ping(final InetAddress host) throws Exception {
        return ping(host, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Ping a remote host, sending 1 or more packets at the given interval, and then
     * return the response times as a list.
     *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @param count The number of packets to send.
     * @param timeout The time to wait between each retry.
     * @param pingInterval The interval at which packets will be sent.
     * @param size The size of the packet to send.
     * @return a {@link java.util.List} of response times in microseconds.
     *     If, for a given ping request, the host is reachable and has responded with an
     *     echo reply, it will contain a number, otherwise a null value.
     */
    @Override
    public List<Number> parallelPing(final InetAddress host, final int count, final long timeout, final long pingInterval, final int size) throws Exception {
        initialize();
        final ParallelPingResponseCallback cb = new ParallelPingResponseCallback(count);

        final long threadId = JniPingRequest.getNextTID();
        for (int seqNum = 0; seqNum < count; seqNum++) {
            final JniPingRequest request = new JniPingRequest(host, m_pingerId, seqNum, threadId, timeout == 0? DEFAULT_TIMEOUT : timeout, 0, size, cb);
            s_pingTracker.sendRequest(request);
            Thread.sleep(pingInterval);
        }

        cb.waitFor();
        return cb.getResponseTimes();
    }

    /**
     * Ping a remote host, sending 1 or more packets at the given interval, and then
     * return the response times as a list.
     *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @param count The number of packets to send.
     * @param timeout The time to wait between each retry.
     * @param pingInterval The interval at which packets will be sent.
     * @return a {@link java.util.List} of response times in microseconds.
     *     If, for a given ping request, the host is reachable and has responded with an
     *     echo reply, it will contain a number, otherwise a null value.
     */
    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception {
        return parallelPing(host, count, timeout, pingInterval, DEFAULT_PACKET_SIZE);
    }

    @Override
    public void setTrafficClass(final int tc) throws Exception {
        initialize();
        m_messenger.setTrafficClass(tc);
    }

    @Override
    public void setAllowFragmentation(boolean allow) throws Exception {
        initialize();
        m_messenger.setAllowFragmentation(allow);
    }

}
