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

package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.IpV4;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.IpV6;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagram;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.Inet4Header;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.Inet6Header;

/**
 * Used to asynchronously generate {@link SampleDatagramEnrichment}s which are
 * used to augment the serialization process with additional facts (i.e. hostnames).
 */
public class SampleDatagramEnricher {

    private final DnsResolver dnsResolver;
    private final boolean dnsLookupsEnabled;

    public SampleDatagramEnricher(DnsResolver dnsResolver, boolean dnsLookupsEnabled) {
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
        this.dnsLookupsEnabled = dnsLookupsEnabled;
    }

    public CompletableFuture<SampleDatagramEnrichment> enrich(SampleDatagram datagram) {
        if (!this.dnsLookupsEnabled) {
            final CompletableFuture<SampleDatagramEnrichment> emptyFuture = new CompletableFuture<>();
            final SampleDatagramEnrichment emptyEnrichment = new DefaultSampleDatagramEnrichment(Collections.<InetAddress, String>emptyMap());
            emptyFuture.complete(emptyEnrichment);
            return emptyFuture;
        }
        final Set<InetAddress> addressesToReverseLookup = new HashSet<>();
        datagram.visit(new SampleDatagramVisitor() {
            @Override
            public void accept(Inet4Header inet4Header) {
                addressesToReverseLookup.add(inet4Header.getSrcAddress());
                addressesToReverseLookup.add(inet4Header.getDstAddress());
            }

            @Override
            public void accept(Inet6Header inet6Header) {
                addressesToReverseLookup.add(inet6Header.getSrcAddress());
                addressesToReverseLookup.add(inet6Header.getDstAddress());
            }

            @Override
            public void accept(IpV4 ipV4) {
                addressesToReverseLookup.add(ipV4.getAddress());
            }

            @Override
            public void accept(IpV6 ipV6) {
                addressesToReverseLookup.add(ipV6.getAddress());
            }
        });

        final Map<InetAddress, String> hostnamesByAddress = new HashMap<>(addressesToReverseLookup.size());
        final List<CompletableFuture<Optional<String>>> reverseLookupFutures = addressesToReverseLookup.stream()
                .map(addr -> dnsResolver.reverseLookup(addr).whenComplete((hostname, ex) -> {
                    if (ex != null) {
                        synchronized (hostnamesByAddress) {
                            hostnamesByAddress.put(addr, hostname.orElse(null));
                        }
                    }
                })).collect(Collectors.toList());

        final CompletableFuture<SampleDatagramEnrichment> future = new CompletableFuture<>();
        CompletableFuture.allOf(reverseLookupFutures.toArray(new CompletableFuture[]{})).whenComplete((any, ex) -> {
            // All of the reverse lookups have completed, note that some may have failed though
            // Build the enrichment object with the results we do have
            final SampleDatagramEnrichment enrichment = new DefaultSampleDatagramEnrichment(hostnamesByAddress);
            future.complete(enrichment);
        });
        return future;
    }

    private static class DefaultSampleDatagramEnrichment implements SampleDatagramEnrichment {
        private final Map<InetAddress, String> hostnamesByAddress;

        DefaultSampleDatagramEnrichment(Map<InetAddress, String> hostnamesByAddress) {
            this.hostnamesByAddress = hostnamesByAddress;
        }

        @Override
        public Optional<String> getHostnameFor(InetAddress address) {
            return Optional.ofNullable(hostnamesByAddress.get(address));
        }
    }

}
