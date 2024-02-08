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
package org.opennms.netmgt.newts.support;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.newts.api.Context;
import org.opennms.newts.cassandra.CassandraSession;
import org.opennms.newts.cassandra.search.CassandraCachePrimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class CachePrimer implements InitializingBean, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CachePrimer.class);

    @Autowired(required=false)
    private GuavaSearchableResourceMetadataCache resourceMetadataCache;

    @Autowired
    private CassandraSession session;

    @Autowired
    private Context context;

    private final boolean primingDisabled;
    private final long blockWhilePrimingMs;
    private final int fetchSize;
    private final int fetchMoreThreshold;

    @Inject
    public CachePrimer(@Named("cache.priming.disable") boolean primingDisabled,
                       @Named("cache.priming.block_ms") long blockWhilePrimingMs,
                       @Named("cache.priming.fetch_size") int fetchSize,
                       @Named("cache.priming.fetch_more_threshold") int fetchMoreThreshold) {
        this.primingDisabled = primingDisabled;
        this.blockWhilePrimingMs = blockWhilePrimingMs;
        this.fetchSize = fetchSize;
        this.fetchMoreThreshold = fetchMoreThreshold;
    }

    @Override
    public void afterPropertiesSet() {
        if (primingDisabled) {
            LOG.debug("Cache priming disabled. Skipping cache priming.");
            return;
        }

        if (resourceMetadataCache == null) {
            LOG.debug("Resource meta-data cache is not of type {}. Skipping cache priming.", GuavaSearchableResourceMetadataCache.class.getCanonicalName());
            return;
        }

        // Perform the priming on a separate thread, while optionally blocking for a certain amount of time
        final Thread thread = new Thread(this);
        thread.setName("Newts-CachePrimer");
        thread.start();
        if (blockWhilePrimingMs >= 0) {
            try {
                if (blockWhilePrimingMs == 0) {
                    LOG.info("Blocking startup while waiting for cache to be fully primed.");
                } else {
                    LOG.info("Blocking startup for up-to {}ms while waiting for cache to be primed.", blockWhilePrimingMs);
                }
                thread.join(blockWhilePrimingMs);
                if (thread.isAlive()) {
                    LOG.info("Cache is not yet done priming after waiting for {}ms. Current size is: {}." +
                            " The operation will continue in the background.",
                            blockWhilePrimingMs,
                            resourceMetadataCache.getSize());
                }
            } catch (InterruptedException e) {
                LOG.info("Thread was interrupted while waiting for the cache to be primed.");
            }
        }
    }

    @Override
    public void run() {
        final CassandraCachePrimer primer = new CassandraCachePrimer(session);
        primer.setFetchSize(fetchSize);
        primer.setFetchMoreThreshold(fetchMoreThreshold);
        LOG.info("Starting to prime the cache.");
        primer.prime(resourceMetadataCache, context);
        LOG.info("Done priming cache. Cache size: {}", resourceMetadataCache.getSize());
    }

    public void setResourceMetadataCache(GuavaSearchableResourceMetadataCache resourceMetadataCache) {
        this.resourceMetadataCache = resourceMetadataCache;
    }

    public void setSession(CassandraSession session) {
        this.session = session;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
