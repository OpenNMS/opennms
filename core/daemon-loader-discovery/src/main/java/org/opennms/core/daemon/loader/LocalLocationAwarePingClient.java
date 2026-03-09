/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.daemon.loader;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingRequestBuilder;
import org.opennms.netmgt.icmp.proxy.PingSummary;
import org.opennms.netmgt.icmp.proxy.PingSweepRequestBuilder;
import org.opennms.netmgt.icmp.proxy.PingSweepSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local implementation of {@link LocationAwarePingClient} for standalone
 * daemon containers that don't have the RPC infrastructure. Uses Java's
 * {@code InetAddress.isReachable()} for connectivity checks.
 */
public class LocalLocationAwarePingClient implements LocationAwarePingClient {
    private static final Logger LOG = LoggerFactory.getLogger(LocalLocationAwarePingClient.class);

    @Override
    public PingRequestBuilder ping(InetAddress inetAddress) {
        return new LocalPingRequestBuilder(inetAddress);
    }

    @Override
    public PingSweepRequestBuilder sweep() {
        return new LocalPingSweepRequestBuilder();
    }

    private static class LocalPingRequestBuilder implements PingRequestBuilder {
        private InetAddress address;
        private int timeoutMs = 2000;
        private int retries = 1;

        LocalPingRequestBuilder(InetAddress address) {
            this.address = address;
        }

        @Override
        public PingRequestBuilder withTimeout(long timeout, TimeUnit unit) {
            this.timeoutMs = (int) unit.toMillis(timeout);
            return this;
        }

        @Override
        public PingRequestBuilder withPacketSize(int packageSize) {
            return this;
        }

        @Override
        public PingRequestBuilder withRetries(int retries) {
            this.retries = retries;
            return this;
        }

        @Override
        public PingRequestBuilder withInetAddress(InetAddress inetAddress) {
            this.address = inetAddress;
            return this;
        }

        @Override
        public PingRequestBuilder withLocation(String location) {
            return this;
        }

        @Override
        public PingRequestBuilder withSystemId(String systemId) {
            return this;
        }

        @Override
        public PingRequestBuilder withNumberOfRequests(int numberOfRequests) {
            return this;
        }

        @Override
        public PingRequestBuilder withProgressCallback(Callback callback) {
            return this;
        }

        @Override
        public CompletableFuture<PingSummary> execute() {
            // Discovery only uses sweep(), not ping(). This is a stub for API completeness.
            throw new UnsupportedOperationException("LocalLocationAwarePingClient does not support ping(); use sweep() instead");
        }
    }

    private static class LocalPingSweepRequestBuilder implements PingSweepRequestBuilder {
        private final List<SweepRange> ranges = new ArrayList<>();
        private int packetSize = 64;
        private double packetsPerSecond = 1.0;

        @Override
        public PingSweepRequestBuilder withLocation(String location) {
            return this;
        }

        @Override
        public PingSweepRequestBuilder withSystemId(String systemId) {
            return this;
        }

        @Override
        public PingSweepRequestBuilder withPacketSize(int packetSize) {
            this.packetSize = packetSize;
            return this;
        }

        @Override
        public PingSweepRequestBuilder withPacketsPerSecond(double packetsPerSecond) {
            this.packetsPerSecond = packetsPerSecond;
            return this;
        }

        @Override
        public PingSweepRequestBuilder withRange(InetAddress begin, InetAddress end) {
            ranges.add(new SweepRange(begin, end, 1, 2000));
            return this;
        }

        @Override
        public PingSweepRequestBuilder withRange(InetAddress begin, InetAddress end, int retries, long timeout, TimeUnit timeoutUnit) {
            ranges.add(new SweepRange(begin, end, retries, (int) timeoutUnit.toMillis(timeout)));
            return this;
        }

        @Override
        public CompletableFuture<PingSweepSummary> execute() {
            return CompletableFuture.supplyAsync(() -> {
                PingSweepSummary summary = new PingSweepSummary();
                for (SweepRange range : ranges) {
                    sweepRange(range, summary);
                }
                return summary;
            });
        }

        private void sweepRange(SweepRange range, PingSweepSummary summary) {
            byte[] beginBytes = range.begin.getAddress();
            byte[] endBytes = range.end.getAddress();

            // For simple cases (single IP or small range), iterate through addresses
            byte[] current = beginBytes.clone();
            while (compareAddresses(current, endBytes) <= 0) {
                try {
                    InetAddress addr = InetAddress.getByAddress(current);
                    for (int attempt = 0; attempt <= range.retries; attempt++) {
                        try {
                            long start = System.nanoTime();
                            if (addr.isReachable(range.timeoutMs)) {
                                double rttMs = (System.nanoTime() - start) / 1_000_000.0;
                                summary.getResponses().put(addr, rttMs);
                                LOG.info("Ping sweep: {} responded in {} ms", addr, String.format("%.2f", rttMs));
                                break;
                            }
                        } catch (Exception e) {
                            LOG.debug("Ping sweep attempt {} to {} failed: {}", attempt, addr, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Failed to create address from bytes: {}", e.getMessage());
                }
                if (!incrementAddress(current)) {
                    break;
                }
            }
        }

        private static int compareAddresses(byte[] a, byte[] b) {
            for (int i = 0; i < a.length; i++) {
                int diff = (a[i] & 0xFF) - (b[i] & 0xFF);
                if (diff != 0) return diff;
            }
            return 0;
        }

        private static boolean incrementAddress(byte[] address) {
            for (int i = address.length - 1; i >= 0; i--) {
                int val = (address[i] & 0xFF) + 1;
                address[i] = (byte) val;
                if (val <= 255) return true;
                address[i] = 0;
            }
            return false; // overflow
        }
    }

    private static class SweepRange {
        final InetAddress begin;
        final InetAddress end;
        final int retries;
        final int timeoutMs;

        SweepRange(InetAddress begin, InetAddress end, int retries, int timeoutMs) {
            this.begin = begin;
            this.end = end;
            this.retries = retries;
            this.timeoutMs = timeoutMs;
        }
    }
}
