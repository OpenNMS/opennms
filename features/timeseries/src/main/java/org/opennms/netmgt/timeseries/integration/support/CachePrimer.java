/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.support;

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
        if (!primingDisabled) {
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
