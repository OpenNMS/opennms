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
package org.opennms.netmgt.dnsresolver.netty;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.opennms.core.utils.InetAddressUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import static org.opennms.core.health.api.HealthCheckConstants.LOCAL;

public class NettyDnsHealthCheck implements HealthCheck {

    private final NettyDnsResolver dnsResolver;

    public NettyDnsHealthCheck(NettyDnsResolver dnsResolver) {
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
    }

    @Override
    public String getDescription() {
        return "DNS Lookups (Netty)";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(LOCAL);
    }

    @Override
    public Response perform(Context context) throws InterruptedException, ExecutionException, TimeoutException {
        final String hostnameToLookup = "www.opennms.com";
        final InetAddress ipAddressToReverseLookup = InetAddressUtils.getInetAddress("1.1.1.1");

        final CircuitBreaker.State cbState = dnsResolver.getCircuitBreaker().getState();
        if (!CircuitBreaker.State.CLOSED.equals(cbState)) {
            return new Response(Status.Failure, "Expected circuit breaker to be CLOSED, but was: " + cbState);
        }

        final Optional<InetAddress> addr = dnsResolver.lookup(hostnameToLookup)
                .get(context.getTimeout(), TimeUnit.SECONDS);
        if (!addr.isPresent()) {
            return new Response(Status.Failure, String.format("Lookup failed for '%s'. No A or AAAA records.", hostnameToLookup));
        }

        final Optional<String> hostname = dnsResolver.reverseLookup(ipAddressToReverseLookup)
                .get(context.getTimeout(), TimeUnit.SECONDS);
        if (!hostname.isPresent()) {
            return new Response(Status.Failure, String.format("Reverse failed for '%s'. No PTR record.", ipAddressToReverseLookup));
        }

        return new Response(Status.Success, String.format("%s is at %s (cache %d/%d)", hostnameToLookup, addr.get().getHostAddress(),
                dnsResolver.getCache().getSize(), dnsResolver.getMaxCacheSize()));
    }
}
