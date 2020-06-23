/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
