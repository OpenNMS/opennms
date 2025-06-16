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
                    if (ex == null) {
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
