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

package org.opennms.core.mate.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Scope;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CachedEntityScopeProviderImpl implements EntityScopeProvider {

    private final static class Tuple<A, B> {
        final A a;
        final B b;

        public Tuple(A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Tuple<?, ?> tuple = (Tuple<?, ?>) o;
            return Objects.equals(a, tuple.a) && Objects.equals(b, tuple.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }

    private final static class Triple<A, B, C> {
        final A a;
        final B b;
        final C c;

        public Triple(A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
            return Objects.equals(a, triple.a) && Objects.equals(b, triple.b) && Objects.equals(c, triple.c);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c);
        }
    }

    private final LoadingCache<Integer, Scope> nodeScopes;
    private final LoadingCache<Tuple<Integer, String>, Scope> interfaceScopes;
    private final LoadingCache<Tuple<Integer, Integer>, Scope> interfaceScopesByIfIndex;
    private final LoadingCache<Triple<Integer, InetAddress, String>, Scope> serviceScopes;
    private final EntityScopeProvider entityScopeProvider;

    public CachedEntityScopeProviderImpl(final EntityScopeProvider entityScopeProvider, final long expireAfterWrite, final long expireAfterAccess, final long refreshAfterWrite, final long maximumSize) {
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);

        this.nodeScopes = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, new CacheLoader<>() {
            @Override
            public Scope load(final Integer integer) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForNode(integer);
            }
        });

        this.interfaceScopes = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, new CacheLoader<>() {
            @Override
            public Scope load(final Tuple<Integer, String> tuple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForInterface(tuple.a, tuple.b);
            }
        });

        this.interfaceScopesByIfIndex = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, new CacheLoader<>() {
            @Override
            public Scope load(final Tuple<Integer, Integer> tuple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForInterfaceByIfIndex(tuple.a, tuple.b);
            }
        });

        this.serviceScopes = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, new CacheLoader<>() {
            @Override
            public Scope load(final Triple<Integer, InetAddress, String> triple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForService(triple.a, triple.b, triple.c);
            }
        });
    }

    private <K, V> LoadingCache<K, V> createCache(final long expireAfterWrite, final long expireAfterAccess, final long refreshAfterWrite, final long maximumSize, CacheLoader<K, V> loader) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        if (expireAfterWrite >= 0) {
            cacheBuilder.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS);
        }

        if (expireAfterAccess >= 0) {
            cacheBuilder.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS);
        }

        if (refreshAfterWrite >= 0) {
            cacheBuilder.refreshAfterWrite(refreshAfterWrite, TimeUnit.SECONDS);
        }

        if (maximumSize > 0) {
            cacheBuilder.maximumSize(maximumSize);
        }

        return cacheBuilder.build(loader);
    }

    @Override
    public Scope getScopeForScv() {
        return entityScopeProvider.getScopeForScv();
    }

    @Override
    public Scope getScopeForNode(final Integer nodeId) {
        return nodeScopes.getUnchecked(nodeId);
    }

    @Override
    public Scope getScopeForInterface(final Integer nodeId, final String ipAddress) {
        return interfaceScopes.getUnchecked(new Tuple<>(nodeId, ipAddress));
    }

    @Override
    public Scope getScopeForInterfaceByIfIndex(final Integer nodeId, final int ifIndex) {
        return interfaceScopesByIfIndex.getUnchecked(new Tuple<>(nodeId, ifIndex));
    }

    @Override
    public Scope getScopeForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName) {
        return serviceScopes.getUnchecked(new Triple<>(nodeId, ipAddress, serviceName));
    }
}
