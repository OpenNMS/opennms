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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class RecordEnricher {
    private static final Logger LOG = LoggerFactory.getLogger(RecordEnricher.class);

    private final DnsResolver dnsResolver;
    private boolean dnsLookupsEnabled;

    public RecordEnricher(DnsResolver dnsResolver, boolean dnsLookupsEnabled) {
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
        this.dnsLookupsEnabled = dnsLookupsEnabled;
    }

    public CompletableFuture<RecordEnrichment> enrich(Iterable<Value<?>> record) {
        if (!this.dnsLookupsEnabled) {
            final CompletableFuture<RecordEnrichment> emptyFuture = new CompletableFuture<>();
            final RecordEnrichment emptyEnrichment = new DefaultRecordEnrichment(Collections.<InetAddress, String>emptyMap());
            emptyFuture.complete(emptyEnrichment);
            return emptyFuture;
        }
        final IpAddressCapturingVisitor ipAddressCapturingVisitor = new IpAddressCapturingVisitor();
        for (final Value<?> value : record) {
            value.visit(ipAddressCapturingVisitor);
        }
        final Set<InetAddress> addressesToReverseLookup = ipAddressCapturingVisitor.getAddresses();
        final Map<InetAddress, String> hostnamesByAddress = new HashMap<>(addressesToReverseLookup.size());
        final CompletableFuture reverseLookupFutures[] = addressesToReverseLookup.stream()
                .map(addr -> {
                    LOG.trace("Issuing reverse lookup for: {}", addr);
                    return dnsResolver.reverseLookup(addr).whenComplete((hostname, ex) -> {
                        if (ex == null) {
                            LOG.trace("Got reverse lookup answer for '{}': {}", addr, hostname);
                            synchronized (hostnamesByAddress) {
                                hostnamesByAddress.put(addr, hostname.orElse(null));
                            }
                        } else {
                            LOG.trace("Reverse lookup failed for '{}': {}", addr, ex);
                            synchronized (hostnamesByAddress) {
                                hostnamesByAddress.put(addr, null);
                            }
                        }
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Other lookups pending: {}", Sets.difference(addressesToReverseLookup, hostnamesByAddress.keySet()));
                        }
                    });
                }).toArray(CompletableFuture[]::new);

        final CompletableFuture<RecordEnrichment> future = new CompletableFuture<>();
        CompletableFuture.allOf(reverseLookupFutures).whenComplete((any, ex) -> {
            LOG.trace("All reverse lookups complete. Queries: {} Results: {}", addressesToReverseLookup, hostnamesByAddress);
            // All of the reverse lookups have completed, note that some may have failed though
            // Build the enrichment object with the results we do have
            final RecordEnrichment enrichment = new DefaultRecordEnrichment(hostnamesByAddress);
            future.complete(enrichment);
        });
        return future;
    }

    private static class DefaultRecordEnrichment implements RecordEnrichment {
        private final Map<InetAddress, String> hostnamesByAddress;

        DefaultRecordEnrichment(Map<InetAddress, String> hostnamesByAddress) {
            this.hostnamesByAddress = hostnamesByAddress;
        }

        @Override
        public Optional<String> getHostnameFor(InetAddress address) {
            return Optional.ofNullable(hostnamesByAddress.get(address));
        }
    }

    private static class IpAddressCapturingVisitor implements Value.Visitor {
        private final Set<InetAddress> addresses = new HashSet<>();

        public Set<InetAddress> getAddresses() {
            return addresses;
        }

        @Override
        public void accept(IPv4AddressValue value) {
            addresses.add(value.getValue());
        }

        @Override
        public void accept(IPv6AddressValue value) {
            addresses.add(value.getValue());
        }

        @Override
        public void accept(NullValue value) {
            // pass
        }

        @Override
        public void accept(BooleanValue value) {
            // pass
        }

        @Override
        public void accept(DateTimeValue value) {
            // pass
        }

        @Override
        public void accept(FloatValue value) {
            // pass
        }

        @Override
        public void accept(MacAddressValue value) {
            // pass
        }

        @Override
        public void accept(OctetArrayValue value) {
            // pass
        }

        @Override
        public void accept(SignedValue value) {
            // pass
        }

        @Override
        public void accept(StringValue value) {
            // pass
        }

        @Override
        public void accept(UnsignedValue value) {
            // pass
        }

        @Override
        public void accept(ListValue value) {
            // pass
        }

        @Override
        public void accept(UndeclaredValue value) {
            // pass
        }
    }
}
