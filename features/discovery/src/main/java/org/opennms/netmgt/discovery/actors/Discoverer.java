/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery.actors;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.opennms.netmgt.discovery.messages.DiscoveryJob;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;

/**
 * <p>This class processes a {@link DiscoveryJob} by executing ICMP pings against
 * the targets of the job and returns the results in a {@link DiscoveryResults}
 * message.</p>
 * 
 * <ul>
 * <li>Input:  {@link DiscoveryJob}</li>
 * <li>Output: {@link DiscoveryResults}</li>
 * </ul>
 */
public class Discoverer {

    private static final Logger LOG = LoggerFactory.getLogger(Discoverer.class);

    private final Pinger m_pinger;

    public Discoverer(Pinger pinger) {
        m_pinger = Preconditions.checkNotNull(pinger, "pinger argument");
    }

    public DiscoveryResults discover(DiscoveryJob job) {
        // Track the results of this particular job
        final PingResponseTracker tracker = new PingResponseTracker();

        // Filter out any entries where getAddress() == null
        List<IPPollAddress> addresses = StreamSupport.stream(job.getAddresses().spliterator(), false)
            .filter(j -> j.getAddress() != null)
            .collect(Collectors.toList());

        // Expect callbacks for all of the addresses before issuing any pings
        addresses.stream()
            .map(a -> a.getAddress())
            .forEach(a -> tracker.expectCallbackFor(a));

        // Use a RateLimiter to limit the ping packets per second that we send
        RateLimiter limiter = RateLimiter.create(job.getPacketsPerSecond());

        // Issue all of the pings
        addresses.stream().forEach(a -> {
            limiter.acquire();
            ping(a, tracker);
        });

        // Don't bother waiting if there aren't any addresses
        if (!addresses.isEmpty()) {
            // Wait for the pings to complete
            try {
                tracker.getLatch().await();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        // We're done
        return new DiscoveryResults(tracker.getResponses(), job.getForeignSource(), job.getLocation());
    }

    private void ping(IPPollAddress pollAddress, PingResponseTracker tracker) {
        InetAddress address = pollAddress.getAddress();
        try {
            m_pinger.ping(address, pollAddress.getTimeout(), pollAddress.getRetries(), (short) 1, tracker);
        } catch (Throwable e) {
            LOG.debug("Error pinging {}", address.getAddress(), e);
            tracker.handleError(address, null, e);
        }
    }

    private static class PingResponseTracker implements PingResponseCallback {
        private final Set<InetAddress> waitingFor = Sets.newConcurrentHashSet();
        private final Map<InetAddress, Long> m_responses = Maps.newConcurrentMap();
        private final CountDownLatch m_doneSignal = new CountDownLatch(1);

        public void expectCallbackFor(InetAddress address) {
            waitingFor.add(address);
        }

        @Override
        public void handleResponse(InetAddress address, EchoPacket response) {
            if (response != null) {
                m_responses.put(address, response.getReceivedTimeNanos() - response.getSentTimeNanos());
            }
            afterHandled(address);
        }

        @Override
        public void handleTimeout(InetAddress address, EchoPacket request) {
            LOG.debug("Request timed out: {}", address);
            afterHandled(address);
        }

        @Override
        public void handleError(InetAddress address, EchoPacket request, Throwable t) {
            LOG.debug("Request timed out: {}", address);
            afterHandled(address);
        }

        private void afterHandled(InetAddress address) {
            waitingFor.remove(address);
            if (waitingFor.isEmpty()) {
                m_doneSignal.countDown();
            }
        }

        public CountDownLatch getLatch() {
            return m_doneSignal;
        }

        public Map<InetAddress, Long> getResponses() {
            return m_responses;
        }
    }
}
