/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.jna;

import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_PACKET_SIZE;
import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_RETRIES;
import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_TIMEOUT;

import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Callable;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.icmp.ParallelPingResponseCallback;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.SinglePingResponseCallback;
import org.opennms.protocols.rt.IDBasedRequestLocator;
import org.opennms.protocols.rt.RequestTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main
 *
 * @author brozow
 */
public class JnaPinger implements Pinger {
    private static final Logger LOG = LoggerFactory.getLogger(JnaPinger.class);

    private final int m_pingerId = new SecureRandom().nextInt(Short.MAX_VALUE);

    private RequestTracker<JnaPingRequest, JnaPingReply> m_pingTracker;
    private JnaIcmpMessenger m_messenger;

    /**
     * Initializes this singleton
     * @throws Exception 
     */
    private synchronized void initialize() throws Exception {
        if (m_pingTracker != null) return;
        try {
            m_messenger = new JnaIcmpMessenger(m_pingerId);
            m_pingTracker = Logging.withPrefix("icmp", new Callable<RequestTracker<JnaPingRequest,JnaPingReply>>() {
                @Override public RequestTracker<JnaPingRequest, JnaPingReply> call() throws Exception {
                    return new RequestTracker<JnaPingRequest, JnaPingReply>("JNA-ICMP-"+m_pingerId, m_messenger, new IDBasedRequestLocator<JnaPingRequestId, JnaPingRequest, JnaPingReply>());
                }
            });
            m_pingTracker.start();
        } catch (final IOException e) {
            final String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("permission denied") || errorMessage.contains("operation not permitted")) {
                LOG.error("Permission error received while attempting to open ICMP socket. See https://wiki.opennms.org/wiki/ICMP for information on configuring ICMP for non-root.");
            }
            throw e;
        }
    }

    @Override
    public void initialize4() throws Exception {
        initialize();
    }

    @Override
    public void initialize6() throws Exception {
        initialize();
    }

    @Override
    public boolean isV4Available() {
        try {
            initialize();
        } catch (final Throwable t) {
            LOG.trace("Failed to initialize IPv4", t);
        }
        if (m_messenger == null) return false;
        return m_messenger.isV4Available();
    }

    @Override
    public boolean isV6Available() {
        try {
            initialize();
        } catch (final Throwable t) {
            LOG.trace("Failed to initialize IPv6", t);
        }
        if (m_messenger == null) return false;
        return m_messenger.isV6Available();
    }

    /**
     * <p>ping</p>
     *
     * @param host a {@link java.net.InetAddress} object.
     * @param timeout a long.
     * @param retries a int.
     * @param packetsize The size in byte of the ICMP packet.
     * @param sequenceId a short.
     * @param cb a {@link org.opennms.netmgt.ping.PingResponseCallback} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void ping(final InetAddress host, final long timeout, final int retries, final int packetsize, final int sequenceId, final PingResponseCallback cb) throws Exception {
        initialize();
        m_pingTracker.sendRequest(new JnaPingRequest(host, m_pingerId, sequenceId, timeout, retries, packetsize, cb));
    }

    /**
     * <p>ping</p>
     *
     * @param host a {@link java.net.InetAddress} object.
     * @param timeout a long.
     * @param retries a int.
     * @param sequenceId a short.
     * @param cb a {@link org.opennms.netmgt.ping.PingResponseCallback} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void ping(final InetAddress host, final long timeout, final int retries, final int sequenceId, final PingResponseCallback cb) throws Exception {
        initialize();
        m_pingTracker.sendRequest(new JnaPingRequest(host, m_pingerId, sequenceId, timeout, retries, DEFAULT_PACKET_SIZE, cb));
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
        ping(host, timeout, retries, packetsize, 1, cb);
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

        final long threadId = JnaPingRequest.getNextTID();
        for (int seqNum = 0; seqNum < count; seqNum++) {
            final JnaPingRequest request = new JnaPingRequest(host, m_pingerId, seqNum, threadId, timeout == 0? DEFAULT_TIMEOUT : timeout, 0, size, cb);
            m_pingTracker.sendRequest(request);
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
    public List<Number> parallelPing(final InetAddress host, final int count, final long timeout, final long pingInterval) throws Exception {
        return parallelPing(host, count, timeout, pingInterval, DEFAULT_PACKET_SIZE);
    }

    @Override
    public void setAllowFragmentation(boolean allow) throws Exception {
        initialize();
        m_messenger.setAllowFragmentation(allow);
    }

    @Override
    public void setTrafficClass(int tc) throws Exception {
        initialize();
        m_messenger.setTrafficClass(tc);
    }

}
