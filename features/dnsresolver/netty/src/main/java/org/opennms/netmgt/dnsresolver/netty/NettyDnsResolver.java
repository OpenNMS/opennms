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
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.netty.resolver.dns.DefaultDnsCache;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.SequentialDnsServerAddressStreamProvider;
import io.netty.util.internal.SocketUtils;

/**
 * Asynchronous DNS resolution using Netty.
 *
 * Creates multiple resolvers (aka contexts) (defaults to 2*num cores) against which the queries
 * are randomized in order to improve performance.
 *
 * Uses a circuit breaker in order to ensure that callers do not continue to be bogged down
 * if resolution fails.
 *
 * @author jwhite
 */
public class NettyDnsResolver implements DnsResolver {
    private static final Logger LOG = LoggerFactory.getLogger(NettyDnsResolver.class);

    public static final String CIRCUIT_BREAKER_STATE_CHANGE_EVENT_UEI = "uei.opennms.org/circuitBreaker/stateChange";

    private final EventForwarder eventForwarder;
    private final MetricRegistry metrics;
    private final Timer lookupTimer;
    private final Meter lookupsSuccessful;
    private final Meter lookupsFailed;
    private final Meter lookupsRejectedByCircuitBreaker;

    private int numContexts = 0;
    private String nameservers = null;
    private long queryTimeoutMillis = TimeUnit.SECONDS.toMillis(5);
    private int minTtlSeconds = -1;
    private int maxTtlSeconds = -1;
    private int negativeTtlSeconds = -1;
    private long maxCacheSize = -1;

    private boolean breakerEnabled = true;
    private int breakerFailureRateThreshold = 80;
    private int breakerWaitDurationInOpenState = 15;
    private int breakerRingBufferSizeInHalfOpenState = 10;
    private int breakerRingBufferSizeInClosedState = 100;

    private int bulkheadMaxConcurrentCalls = 1000;
    private long bulkheadMaxWaitDurationMillis = queryTimeoutMillis + 100;

    private List<NettyResolverContext> contexts;
    private Iterator<NettyResolverContext> iterator;
    private CaffeineDnsCache cache;

    private Bulkhead bulkhead;
    private CircuitBreaker circuitBreaker;

    public NettyDnsResolver(EventForwarder eventForwarder, MetricRegistry metrics) {
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.metrics = Objects.requireNonNull(metrics);
        lookupTimer = metrics.timer("lookups");
        lookupsSuccessful = metrics.meter("lookupsSuccessful");
        lookupsFailed = metrics.meter("lookupsFailed");
        lookupsRejectedByCircuitBreaker = metrics.meter("lookupsRejectedByCircuitBreaker");
        metrics.register("availableConcurrentCalls", (Gauge<Integer>) () -> bulkhead.getMetrics().getAvailableConcurrentCalls());
        metrics.register("maxAllowedConcurrentCalls", (Gauge<Integer>) () -> bulkhead.getMetrics().getMaxAllowedConcurrentCalls());
    }

    public void init() {
        numContexts = Math.max(0, numContexts);
        if (numContexts == 0) {
            numContexts = Runtime.getRuntime().availableProcessors() * 2;
        }
        LOG.debug("Initializing Netty resolver with {} contexts and nameservers: {}", numContexts, nameservers);

        // Initialize the cache with the given TTL settings - use defaults if the configured values
        // are less than 0
        final CaffeineDnsCache cacheWithDefaults = new CaffeineDnsCache();
        cache = new CaffeineDnsCache(minTtlSeconds < 0 ? cacheWithDefaults.minTtl() : minTtlSeconds,
                maxTtlSeconds < 0 ? cacheWithDefaults.maxTtl() : maxTtlSeconds,
                negativeTtlSeconds < 0 ? cacheWithDefaults.negativeTtl() : negativeTtlSeconds,
                maxCacheSize < 0 ? cacheWithDefaults.maxSize() : maxCacheSize);
        cache.registerMetrics(metrics);

        final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(bulkheadMaxConcurrentCalls)
                .maxWaitDuration(Duration.ofMillis(bulkheadMaxWaitDurationMillis))
                .build();
        bulkhead = Bulkhead.of("nettyDnsResolver", bulkheadConfig);

        contexts = new ArrayList<>(numContexts);
        for (int i = 0; i < numContexts; i++) {
            // Share the same cache across all of the contexts
            NettyResolverContext context = new NettyResolverContext(this, cache, bulkhead, i);
            context.init();
            contexts.add(context);
        }
        iterator = new RandomIterator<>(contexts).iterator();

        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(breakerFailureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(breakerWaitDurationInOpenState))
                .ringBufferSizeInHalfOpenState(breakerRingBufferSizeInHalfOpenState)
                .ringBufferSizeInClosedState(breakerRingBufferSizeInClosedState)
                .recordExceptions(DnsNameResolverTimeoutException.class)
                .build();
        circuitBreaker = CircuitBreaker.of("nettyDnsResolver", circuitBreakerConfig);

        circuitBreaker.getEventPublisher()
                .onStateTransition(e -> {
                    // Send an event when the circuit breaker's state changes
                    final Event event = new EventBuilder(CIRCUIT_BREAKER_STATE_CHANGE_EVENT_UEI, NettyDnsResolver.class.getCanonicalName())
                            .addParam("name", circuitBreaker.getName())
                            .addParam("fromState", e.getStateTransition().getFromState().toString())
                            .addParam("toState", e.getStateTransition().getToState().toString())
                            .getEvent();
                    eventForwarder.sendNow(event);
                })
                .onSuccess(e -> {
                    lookupsSuccessful.mark();
                })
                .onError(e -> {
                    lookupsFailed.mark();
                })
                .onCallNotPermitted(e -> {
                    lookupsRejectedByCircuitBreaker.mark();
                });

        if (breakerEnabled) {
            circuitBreaker.transitionToClosedState();
        } else {
            circuitBreaker.transitionToDisabledState();
        }
    }

    public void destroy() {
        for (NettyResolverContext context : contexts) {
            try {
                context.destroy();
            } catch (Exception e) {
                LOG.warn("Error occurred while destroying context.", e);
            }
        }
        contexts.clear();
        cache.unregisterMetrics(metrics);
    }

    @Override
    public CompletableFuture<Optional<InetAddress>> lookup(String hostname) {
        return circuitBreaker.executeCompletionStage(() -> {
            final NettyResolverContext resolverContext = iterator.next();
            final Timer.Context timerContext = lookupTimer.time();
            return resolverContext.lookup(hostname).whenComplete((res, ex) -> {
                timerContext.stop();
            });
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
        return circuitBreaker.executeCompletionStage(() -> {
            final NettyResolverContext resolverContext = iterator.next();
            final Timer.Context timerContext = lookupTimer.time();
            return resolverContext.reverseLookup(inetAddress).whenComplete((res, ex) -> {
                timerContext.stop();
            });
        }).toCompletableFuture();
    }

    @VisibleForTesting
    CaffeineDnsCache getCache() {
        return cache;
    }

    public boolean getBreakerEnabled() {
        return breakerEnabled;
    }

    public void setBreakerEnabled(boolean breakerEnabled) {
        this.breakerEnabled = breakerEnabled;
    }

    public int getBreakerFailureRateThreshold() {
        return breakerFailureRateThreshold;
    }

    public void setBreakerFailureRateThreshold(int breakerFailureRateThreshold) {
        this.breakerFailureRateThreshold = breakerFailureRateThreshold;
    }

    public int getBreakerWaitDurationInOpenState() {
        return breakerWaitDurationInOpenState;
    }

    public void setBreakerWaitDurationInOpenState(int breakerWaitDurationInOpenState) {
        this.breakerWaitDurationInOpenState = breakerWaitDurationInOpenState;
    }

    public int getBreakerRingBufferSizeInHalfOpenState() {
        return breakerRingBufferSizeInHalfOpenState;
    }

    public void setBreakerRingBufferSizeInHalfOpenState(int breakerRingBufferSizeInHalfOpenState) {
        this.breakerRingBufferSizeInHalfOpenState = breakerRingBufferSizeInHalfOpenState;
    }

    public int getBreakerRingBufferSizeInClosedState() {
        return breakerRingBufferSizeInClosedState;
    }

    public void setBreakerRingBufferSizeInClosedState(int breakerRingBufferSizeInClosedState) {
        this.breakerRingBufferSizeInClosedState = breakerRingBufferSizeInClosedState;
    }

    public int getBulkheadMaxConcurrentCalls() {
        return bulkheadMaxConcurrentCalls;
    }

    public void setBulkheadMaxConcurrentCalls(int bulkheadMaxConcurrentCalls) {
        this.bulkheadMaxConcurrentCalls = bulkheadMaxConcurrentCalls;
    }

    public long getBulkheadMaxWaitDurationMillis() {
        return bulkheadMaxWaitDurationMillis;
    }

    public void setBulkheadMaxWaitDurationMillis(long bulkheadMaxWaitDurationMillis) {
        this.bulkheadMaxWaitDurationMillis = bulkheadMaxWaitDurationMillis;
    }

    public int getNumContexts() {
        return numContexts;
    }

    public void setNumContexts(int numContexts) {
        this.numContexts = numContexts;
    }

    public String getNameservers() {
        return nameservers;
    }

    public void setNameservers(String nameservers) {
        this.nameservers = nameservers;
    }

    public long getQueryTimeoutMillis() {
        return queryTimeoutMillis;
    }

    public void setQueryTimeoutMillis(long queryTimeoutMillis) {
        this.queryTimeoutMillis = queryTimeoutMillis;
    }

    public int getMinTtlSeconds() {
        return minTtlSeconds;
    }

    public void setMinTtlSeconds(int minTtlSeconds) {
        this.minTtlSeconds = minTtlSeconds;
    }

    public int getMaxTtlSeconds() {
        return maxTtlSeconds;
    }

    public void setMaxTtlSeconds(int maxTtlSeconds) {
        this.maxTtlSeconds = maxTtlSeconds;
    }

    public int getNegativeTtlSeconds() {
        return negativeTtlSeconds;
    }

    public void setNegativeTtlSeconds(int negativeTtlSeconds) {
        this.negativeTtlSeconds = negativeTtlSeconds;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public DnsServerAddressStreamProvider getNameServerProvider() {
        if (Strings.isNullOrEmpty(nameservers)) {
            // Use the platform default
            return DnsServerAddressStreamProviders.platformDefault();
        }
        return new SequentialDnsServerAddressStreamProvider(toSocketAddresses(nameservers).toArray(new InetSocketAddress[]{}));
    }

    public static List<InetSocketAddress> toSocketAddresses(String commaSeparatedAddressesWithPorts) {
        final String[] servers = commaSeparatedAddressesWithPorts.split(",");
        return Arrays.stream(servers)
                .map(s -> {
                    final HostAndPort hp = HostAndPort.fromString(s.trim())
                            .withDefaultPort(53)
                            .requireBracketsForIPv6();
                    return SocketUtils.socketAddress(hp.getHostText(), hp.getPort());
                })
                .collect(Collectors.toList());
    }
}
