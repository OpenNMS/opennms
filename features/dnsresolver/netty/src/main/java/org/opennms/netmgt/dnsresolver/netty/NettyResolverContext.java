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
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.xbill.DNS.ReverseMap;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsPtrRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.netty.util.concurrent.Future;

/**
 * Asynchronous resolution using Netty.
 *
 * Managed by the {@link NettyDnsResolver}.
 *
 * @author jwhite
 */
public class NettyResolverContext implements DnsResolver {

    private final NettyDnsResolver parent;
    private final DnsCache cache;
    private final int idx;

    private EventLoopGroup group;
    private DnsNameResolver resolver;

    public NettyResolverContext(NettyDnsResolver parent, DnsCache cache, int idx) {
        this.parent = Objects.requireNonNull(parent);
        this.cache = Objects.requireNonNull(cache);
        this.idx = idx;
    }

    public void init() {
        group = new NioEventLoopGroup(0, new ThreadFactoryBuilder()
                .setNameFormat("NettyDnsResolver-NIO-Event-Loop-" + idx + "-%d")
                .build());

        resolver = new DnsNameResolverBuilder(group.next())
                .channelType(NioDatagramChannel.class)
                .nameServerProvider(parent.getNameServerProvider())
                .queryTimeoutMillis(parent.getQueryTimeoutMillis())
                .maxQueriesPerResolve(1)
                .optResourceEnabled(false)
                .resolveCache(cache)
                .build();
    }

    public void destroy() {
        if (group != null) {
            group.shutdownGracefully();
        }
        if (resolver != null) {
            resolver.close();
        }
    }

    @Override
    public CompletableFuture<Optional<InetAddress>> lookup(String hostname) {
        final CompletableFuture<Optional<InetAddress>> future = new CompletableFuture<>();
        final Future<InetAddress> requestFuture = resolver.resolve(hostname);
        requestFuture.addListener(responseFuture -> {
            try {
                InetAddress addr = (InetAddress)responseFuture.get();
                future.complete(Optional.ofNullable(addr));
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException e) {
                final DnsNameResolverTimeoutException timeoutException = Throwables.getCausalChain(e).stream()
                        .filter((DnsNameResolverTimeoutException.class)::isInstance)
                        .map(ex -> (DnsNameResolverTimeoutException)ex)
                        .findFirst()
                        .orElse(null);

                // If the cause is an UnknownHostException, then return an empty result
                // unless we failed due to a timeout, in this case throw the timeout exception
                if (e.getCause() != null) {
                    if (timeoutException != null) {
                        future.completeExceptionally(timeoutException);
                    } else if (e.getCause() instanceof UnknownHostException) {
                        future.complete(Optional.empty());
                    } else {
                        // Fail with the cause
                        future.completeExceptionally(e.getCause());
                    }
                } else {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final String name = ReverseMap.fromAddress(inetAddress).toString();
        final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> requestFuture = resolver.query(new DefaultDnsQuestion(name, DnsRecordType.PTR, DnsRecord.CLASS_IN));
        requestFuture.addListener(responseFuture -> {
            try {
                final AddressedEnvelope<DnsResponse, InetSocketAddress> envelope = (AddressedEnvelope<DnsResponse, InetSocketAddress>) responseFuture.get();
                final DnsResponse response = envelope.content();
                if (response.code() != DnsResponseCode.NOERROR) {
                    future.complete(Optional.empty());
                    return;
                }

                final DnsPtrRecord ptrRecord = envelope.content().recordAt(DnsSection.ANSWER);
                if (ptrRecord == null) {
                    future.complete(Optional.empty());
                    return;
                }

                final String hostname = ptrRecord.hostname();
                // Strip of the trailing dot
                final String trimmedHostname = hostname.substring(0, hostname.length() - 1);
                future.complete(Optional.of(trimmedHostname));
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            } catch (ExecutionException e) {
                if (e.getCause() != null) {
                    // Pass the cause if we can
                    future.completeExceptionally(e.getCause());
                } else {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }
}
