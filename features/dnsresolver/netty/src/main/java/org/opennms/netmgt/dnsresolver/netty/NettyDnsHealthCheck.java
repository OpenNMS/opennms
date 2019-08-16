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

package org.opennms.netmgt.dnsresolver.netty;

import java.net.InetAddress;
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
