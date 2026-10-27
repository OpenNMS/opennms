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

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Scope;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.opennms.core.mate.api.EmptyScope.EMPTY;

public class CachedEntityScopeProviderImpl implements EntityScopeProvider {

    private final LoadingCache<Integer, Scope> nodeScopes;
    private final LoadingCache<Pair<Integer, String>, Scope> interfaceScopes;
    private final LoadingCache<Pair<Integer, Integer>, Scope> interfaceScopesByIfIndex;
    private final LoadingCache<Pair<Integer, String>, Scope> interfaceScopesByIfName;
    private final LoadingCache<Triple<Integer, InetAddress, String>, Scope> serviceScopes;
    private final EntityScopeProvider entityScopeProvider;

    private final static long EXPIRE_AFTER_WRITE = Long.parseLong(System.getProperty("org.opennms.core.mate.cache.expireAfterWrite", "900"));
    private final static long EXPIRE_AFTER_ACCESS = Long.parseLong(System.getProperty("org.opennms.core.mate.cache.expireAfterAccess", "-1"));
    private final static long REFRESH_AFTER_WRITE = Long.parseLong(System.getProperty("org.opennms.core.mate.cache.refreshAfterWrite", "-1"));
    private final static long MAXIMUM_SIZE = Long.parseLong(System.getProperty("org.opennms.core.mate.cache.maximumSize", "-1"));

    public CachedEntityScopeProviderImpl(final EntityScopeProvider entityScopeProvider) {
        this(entityScopeProvider, EXPIRE_AFTER_WRITE, EXPIRE_AFTER_ACCESS, REFRESH_AFTER_WRITE, MAXIMUM_SIZE);
    }

    public CachedEntityScopeProviderImpl(final EntityScopeProvider entityScopeProvider, final long expireAfterWrite, final long expireAfterAccess, final long refreshAfterWrite, final long maximumSize) {
        this(entityScopeProvider, expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, Ticker.systemTicker());
    }

    CachedEntityScopeProviderImpl(final EntityScopeProvider entityScopeProvider, final long expireAfterWrite, final long expireAfterAccess, final long refreshAfterWrite, final long maximumSize, final Ticker ticker) {
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
        Objects.requireNonNull(ticker);

        this.nodeScopes = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, ticker, new CacheLoader<>() {
            @Override
            public Scope load(final Integer integer) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForNode(integer);
            }
        });

        this.interfaceScopes = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, ticker, new CacheLoader<>() {
            @Override
            public Scope load(final Pair<Integer, String> tuple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForInterface(tuple.getLeft(), tuple.getRight());
            }
        });

        this.interfaceScopesByIfIndex = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, ticker, new CacheLoader<>() {
            @Override
            public Scope load(final Pair<Integer, Integer> tuple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForInterfaceByIfIndex(tuple.getLeft(), tuple.getRight());
            }
        });

        this.interfaceScopesByIfName = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, ticker, new CacheLoader<>() {
            @Override
            public Scope load(final Pair<Integer, String> tuple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForInterfaceByIfName(tuple.getLeft(), tuple.getRight());
            }
        });

        this.serviceScopes = createCache(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, ticker, new CacheLoader<>() {
            @Override
            public Scope load(final Triple<Integer, InetAddress, String> triple) {
                return CachedEntityScopeProviderImpl.this.entityScopeProvider.getScopeForService(triple.getLeft(), triple.getMiddle(), triple.getRight());
            }
        });
    }

    private <K, V> LoadingCache<K, V> createCache(final long expireAfterWrite, final long expireAfterAccess, final long refreshAfterWrite, final long maximumSize, final Ticker ticker, CacheLoader<K, V> loader) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        cacheBuilder.ticker(ticker);

        if (expireAfterWrite >= 0) {
            cacheBuilder.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS);
        }

        if (expireAfterAccess >= 0) {
            cacheBuilder.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS);
        }

        if (refreshAfterWrite > 0) {
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
        return interfaceScopes.getUnchecked(Pair.of(nodeId, ipAddress));
    }

    @Override
    public Scope getScopeForInterfaceByIfIndex(final Integer nodeId, final int ifIndex) {
        return interfaceScopesByIfIndex.getUnchecked(Pair.of(nodeId, ifIndex));
    }

    @Override
    public Scope getScopeForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName) {
        return serviceScopes.getUnchecked(Triple.of(nodeId, ipAddress, serviceName));
    }

    @Override
    public Scope getScopeForInterfaceByIfName(final Integer nodeId, final String ifName) {
        return interfaceScopesByIfName.getUnchecked(Pair.of(nodeId, ifName));
    }
}
