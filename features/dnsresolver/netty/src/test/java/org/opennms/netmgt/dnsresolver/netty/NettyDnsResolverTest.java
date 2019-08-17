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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.MetricRegistry;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@TestExecutionListeners(JUnitDNSServerExecutionListener.class)
@JUnitDNSServer(port=NettyDnsResolverTest.DNS_SERVER_PORT, zones={
        @DNSZone(name = "opennms.ca.", entries = {
                @DNSEntry(hostname = "rnd", data = "173.242.186.51"),
        }),
        @DNSZone(name = "in-addr.arpa.", entries = {
                @DNSEntry(hostname = "51.186.242.173", type = "PTR", data = "rnd.opennms.ca.")
        }),
        @DNSZone(name = "ip6.arpa.", entries = {
                @DNSEntry(hostname = "1.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.3.0.0.0.9.2.c.f.0.0.8.5.0.0.6.2", type = "PTR", data = "secret.opennms.com.")
        })
})
public class NettyDnsResolverTest {

    protected static final int DNS_SERVER_PORT = 9153;

    private NettyDnsResolver dnsResolver;

    @Before
    public void setUp() {
        EventForwarder eventForwarder = mock(EventForwarder.class);
        dnsResolver = new NettyDnsResolver(eventForwarder, new MetricRegistry());
        dnsResolver.setNameservers(String.format("%s:%d", InetAddressUtils.getLocalHostName(), DNS_SERVER_PORT));
        dnsResolver.init();
    }

    @After
    public void destroy() {
        dnsResolver.destroy();
    }

    @Test
    public void canDoLookups() throws UnknownHostException, ExecutionException, InterruptedException {
        // Our DNS server knows about this one
        assertThat(dnsResolver.lookup("rnd.opennms.ca").get().get(), equalTo(InetAddress.getByName("173.242.186.51")));
        // Our DNS server does not know about this one
        assertThat(dnsResolver.lookup("private.opennms.ca").get(), equalTo(Optional.empty()));
    }

    @Test
    public void canCacheLookups() throws UnknownHostException, ExecutionException, InterruptedException {
        // Cache should start empty
        assertThat(dnsResolver.getCache().getSize(), equalTo(0L));

        // Query for a known host
        assertThat(dnsResolver.lookup("rnd.opennms.ca").get().get(), equalTo(InetAddress.getByName("173.242.186.51")));

        // There should be 1 cached record now
        assertThat(dnsResolver.getCache().getSize(), equalTo(1L));

        // Cache hit
        assertThat(dnsResolver.lookup("rnd.opennms.ca").get().get(), equalTo(InetAddress.getByName("173.242.186.51")));

        // Our DNS server does not know about this one
        assertThat(dnsResolver.lookup("private.opennms.ca").get(), equalTo(Optional.empty()));

        // There should be at least 2 cached records now (there can be more that 2 if the system is configured with search domains)
        assertThat(dnsResolver.getCache().getSize(), greaterThanOrEqualTo(2L));

        // Cache hit
        assertThat(dnsResolver.lookup("private.opennms.ca").get(), equalTo(Optional.empty()));
    }

    @Test
    public void canDoReverseLookups() throws UnknownHostException, ExecutionException, InterruptedException {
        // Our DNS server knows about these
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("173.242.186.51")).get().get(), equalTo("rnd.opennms.ca"));
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("2600:5800:fc29:0003:0000:0000:0000:0001")).get().get(), equalTo("secret.opennms.com"));

        // But not about these
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("1.1.1.1")).get(), equalTo(Optional.empty()));
        assertThat(dnsResolver.reverseLookup(InetAddressUtils.addr("2606:4700:4700::1111")).get(), equalTo(Optional.empty()));
    }

    @Test
    public void canCacheReverseLookups() throws UnknownHostException, ExecutionException, InterruptedException {
        // Cache should start empty
        assertThat(dnsResolver.getCache().getSize(), equalTo(0L));

        // Query for a known host
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("173.242.186.51")).get().get(), equalTo("rnd.opennms.ca"));

        // There should be 1 cached record now
        assertThat(dnsResolver.getCache().getSize(), equalTo(1L));

        // Cache hit
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("173.242.186.51")).get().get(), equalTo("rnd.opennms.ca"));

        // Now query for an unknown host
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("1.1.1.1")).get(), equalTo(Optional.empty()));

        // There should be 2 cached record now
        assertThat(dnsResolver.getCache().getSize(), equalTo(2L));

        // Cache hit
        assertThat(dnsResolver.reverseLookup(InetAddress.getByName("1.1.1.1")).get(), equalTo(Optional.empty()));
    }

    @Test
    public void canTriggerTimeoutException() throws InterruptedException {
        // Reinitialize the resolver using a non-routable address as the target - we want the queries to fail due to timeouts
        dnsResolver.destroy();
        dnsResolver.setNameservers(InetAddressUtils.str(InetAddressUtils.UNPINGABLE_ADDRESS));
        dnsResolver.init();

        try {
            dnsResolver.lookup("rnd.opennms.ca").get();
            fail("Expected a DnsNameResolverTimeoutException to be thrown");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(DnsNameResolverTimeoutException.class)));
        }

        try {
            dnsResolver.reverseLookup(InetAddressUtils.addr("fe80::")).get();
            fail("Expected a DnsNameResolverTimeoutException to be thrown");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(DnsNameResolverTimeoutException.class)));
        }
    }

    @Test
    public void canTriggerOpenCircuit() throws InterruptedException, TimeoutException {
        // Reinitialize the resolver using a non-routable address as the target - we want the queries to fail due to timeouts
        dnsResolver.destroy();
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
    public void canParseNameserversFromString() {
        assertThat(NettyDnsResolver.toSocketAddresses("8.8.8.8 "),
                contains(new InetSocketAddress("8.8.8.8", 53)));
        assertThat(NettyDnsResolver.toSocketAddresses("8.8.8.8:53 ,1.1.1.1, 1.1.2.2:1153 "),
                contains(new InetSocketAddress("8.8.8.8", 53),
                        new InetSocketAddress("1.1.1.1", 53),
                        new InetSocketAddress("1.1.2.2", 1153)));
        assertThat(NettyDnsResolver.toSocketAddresses("[::1], [::1]:5353 ,1.1.1.1:54 "),
                contains(new InetSocketAddress("::1", 53),
                        new InetSocketAddress("::1", 5353),
                        new InetSocketAddress("1.1.1.1", 54)));
    }

    @Test
    @Ignore
    public void reverseLookupUsingPublicDnsServers() throws UnknownHostException, ExecutionException, InterruptedException {
        // Use the system's servers
        dnsResolver.destroy();
        dnsResolver.setNameservers("4.2.2.2");
        dnsResolver.init();

        final Optional<String> hostname1 = dnsResolver.reverseLookup(InetAddress.getByAddress(new byte[]{1, 1, 1, 1})).get();
        assertEquals("one.one.one.one", hostname1.get());

        final Optional<String> hostname2 = dnsResolver.reverseLookup(InetAddressUtils.addr("1.1.1.1")).get();
        assertEquals("one.one.one.one", hostname2.get());

        final Optional<String> hostname3 = dnsResolver.reverseLookup(InetAddressUtils.addr("2606:4700:4700::1111")).get();
        assertEquals("one.one.one.one", hostname3.get());

        // 198.51.100.0/24 should be TEST-NET-2 (see RFC #5737). Should fail...
        final Optional<String> hostname4 = dnsResolver.reverseLookup(InetAddressUtils.addr("198.51.100.1")).get();
        assertEquals(Optional.empty(), hostname4);

        final Optional<String> hostname5 = dnsResolver.reverseLookup(InetAddressUtils.addr("fe80::")).get();
        assertEquals(Optional.empty(), hostname5);
    }

}
