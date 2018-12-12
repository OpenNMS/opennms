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

package org.opennms.netmgt.enlinkd.persistence.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityDao;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TopologyEntityCacheImpl implements TopologyEntityCache {
    
    private final static Logger LOG = LoggerFactory.getLogger(TopologyEntityCacheImpl.class);
    private final static String KEY = "KEY";
    private final static String SYSTEM_PROPERTY_CACHE_DURATION = "org.opennms.ui.topology-entity-cache-duration";

    private TopologyEntityDao topologyEntityDao;

    private LoadingCache<String, List<NodeTopologyEntity>> nodeTopologyEntities = createCache(
            new CacheLoader<String, List<NodeTopologyEntity>>() {
                @Override
                public List<NodeTopologyEntity> load(String key) {
                    return topologyEntityDao.getNodeTopologyEntities();
                }
            }
    );

    private LoadingCache<String, List<CdpLinkTopologyEntity>> cdpLinkTopologyEntities = createCache(
            new CacheLoader<String, List<CdpLinkTopologyEntity>>() {
                @Override
                public List<CdpLinkTopologyEntity> load(String key) {
                    return topologyEntityDao.getCdpLinkTopologyEntities();
                }
            }
    );

    private <Key, Value> LoadingCache<Key, Value> createCache(CacheLoader<Key, Value> loader) {
        return CacheBuilder
                .newBuilder()
                .expireAfterWrite(getCacheDuration(), TimeUnit.SECONDS)
                .build(loader);
    }


    public List<NodeTopologyEntity> getNodeTopolgyEntities() {
        return this.nodeTopologyEntities.getUnchecked(KEY);
    }

    public List<CdpLinkTopologyEntity> getCdpLinkTopologyEntities() {
        return this.cdpLinkTopologyEntities.getUnchecked(KEY);
    }

    @Override
    public void refresh(){
        nodeTopologyEntities.refresh(KEY);
        cdpLinkTopologyEntities.refresh(KEY);
    }

    private int getCacheDuration(){
        return Integer.getInteger(SYSTEM_PROPERTY_CACHE_DURATION, 300);
    }

    public void setTopologyEntityDao(TopologyEntityDao topologyEntityDao){
        this.topologyEntityDao = topologyEntityDao;
    }
}
