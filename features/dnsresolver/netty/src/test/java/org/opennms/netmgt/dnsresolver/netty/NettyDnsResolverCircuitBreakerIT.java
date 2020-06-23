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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventForwarder;

import com.codahale.metrics.MetricRegistry;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

public class NettyDnsResolverCircuitBreakerIT {

    @Test
    public void canTriggerOpenCircuit() throws InterruptedException, TimeoutException {
        // Create the resolver
        EventForwarder eventForwarder = mock(EventForwarder.class);
        NettyDnsResolver dnsResolver = new NettyDnsResolver(eventForwarder, new MetricRegistry());
        // Use a non-routable address as the target - we want the queries to fail due to timeouts
        dnsResolver.setNameservers(InetAddressUtils.str(InetAddressUtils.UNPINGABLE_ADDRESS));
        dnsResolver.init();

        // Now trigger enough requests to open the circuit breaker
        final int N = 2 * dnsResolver.getCircuitBreaker().getCircuitBreakerConfig().getRingBufferSizeInClosedState();
        final CompletableFuture futures[] = new CompletableFuture[N];
        for (int i = 0; i < N; i++) {
            futures[i] = dnsResolver.reverseLookup(InetAddressUtils.addr("fe80::"));
        }

        // Wait for the requests to complete
        try {
            CompletableFuture.allOf(futures)
                    // This should not take longer than the query timeout
                    .get(2 * dnsResolver.getQueryTimeoutMillis(), TimeUnit.MILLISECONDS);
            fail("Expected an ExecutionException to be thrown");
        } catch (ExecutionException e) {
            // pass
        }

        // The circuit breaker should be in a open state now and start rejecting calls
        try {
            dnsResolver.reverseLookup(InetAddressUtils.addr("fe80::")).get();
            fail("Expected an CallNotPermittedException to be thrown");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(CallNotPermittedException.class)));
        }
    }

    @Test
    public void canDisableCircuitBreaker() throws InterruptedException, TimeoutException {
        // Create the resolver
        EventForwarder eventForwarder = mock(EventForwarder.class);
        NettyDnsResolver dnsResolver = new NettyDnsResolver(eventForwarder, new MetricRegistry());
        // Use a non-routable address as the target - we want the queries to fail due to timeouts
        dnsResolver.setNameservers(InetAddressUtils.str(InetAddressUtils.UNPINGABLE_ADDRESS));
        dnsResolver.setBreakerEnabled(false);
        dnsResolver.init();

        // Now trigger enough requests to open the circuit breaker
        final int N = 2 * dnsResolver.getCircuitBreaker().getCircuitBreakerConfig().getRingBufferSizeInClosedState();
        final CompletableFuture futures[] = new CompletableFuture[N];
        for (int i = 0; i < N; i++) {
            futures[i] = dnsResolver.reverseLookup(InetAddressUtils.addr("fe80::"));
        }

        // Wait for the requests to complete
        try {
            CompletableFuture.allOf(futures)
                    // This should not take longer than the query timeout
                    .get(2 * dnsResolver.getQueryTimeoutMillis(), TimeUnit.MILLISECONDS);
            fail("Expected an ExecutionException to be thrown");
        } catch (ExecutionException e) {
            // pass
        }

        // The circuit breaker should be disabled and netty should return a DnsNameResolverTimeoutException
        try {
            dnsResolver.reverseLookup(InetAddressUtils.addr("fe80::")).get();
            fail("Expected an DnsNameResolverTimeoutException to be thrown");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(io.netty.resolver.dns.DnsNameResolverTimeoutException.class)));
        }
    }
}
