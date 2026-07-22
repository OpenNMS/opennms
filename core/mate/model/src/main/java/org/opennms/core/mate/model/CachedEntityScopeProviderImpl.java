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
package org.opennms.core.mate.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Scope;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.opennms.core.mate.api.EmptyScope.EMPTY;

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
    private final LoadingCache<Tuple<Integer, String>, Scope> interfaceScopesByIfName;
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

        this.interfaceScopesByIfName = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, new CacheLoader<>() {
            @Override
            public Scope load(final Tuple<Integer, String> tuple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForInterfaceByIfName(tuple.a, tuple.b);
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
        if (nodeId == null) {
            return EMPTY;
        }
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

    @Override
    public Scope getScopeForInterfaceByIfName(final Integer nodeId, final String ifName) {
        return interfaceScopesByIfName.getUnchecked(new Tuple<>(nodeId, ifName));
    }
}
