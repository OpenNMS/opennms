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

package org.opennms.netmgt.dao.hibernate;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.netmgt.dao.api.TopologyEntityCache;
import org.opennms.netmgt.dao.api.TopologyEntityDao;
import org.opennms.netmgt.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.model.NodeTopologyEntity;
import org.opennms.netmgt.model.OspfLinkTopologyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class TopologyEntityCacheImpl implements TopologyEntityCache {

    private final static Logger LOG = LoggerFactory.getLogger(TopologyEntityCacheImpl.class);
    private final static String CACHE_KEY = "CACHE_KEY";
    private final static String SYSTEM_PROPERTY_CACHE_DURATION = "org.opennms.ui.topology-entity-cache-duration";

    private TopologyEntityDao topologyEntityDao;

    private LoadingCache<String, List<NodeTopologyEntity>> nodeTopologyEntities = createCache(
            () -> topologyEntityDao.getNodeTopologyEntities());

    private LoadingCache<String, List<CdpLinkTopologyEntity>> cdpLinkTopologyEntities = createCache(
            () -> topologyEntityDao.getCdpLinkTopologyEntities());

    private LoadingCache<String, List<IsIsLinkTopologyEntity>> isIsLinkTopologyEntities = createCache(
            ()-> topologyEntityDao.getIsIsLinkTopologyEntities());

    private LoadingCache<String, List<OspfLinkTopologyEntity>> ospfLinkTopologyEntities = createCache (
            () -> topologyEntityDao.getOspfLinkTopologyEntities());

    private LoadingCache<String, List<LldpLinkTopologyEntity>> lldpLinkTopologyEntities = createCache (
            () -> topologyEntityDao.getLldpLinkTopologyEntities());

    private <KEY, VALUE> LoadingCache<KEY, VALUE> createCache(Supplier<VALUE> entitySupplier) {
        CacheLoader<KEY, VALUE> loader = new CacheLoader<KEY, VALUE>() {
            @Override
            public VALUE load(KEY key) {
                return entitySupplier.get();
            }
        };
        return CacheBuilder
                .newBuilder()
                .expireAfterWrite(getCacheDuration(), TimeUnit.SECONDS)
                .build(loader);
    }

    @Override
    public List<NodeTopologyEntity> getNodeTopolgyEntities() {
        return this.nodeTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<CdpLinkTopologyEntity> getCdpLinkTopologyEntities() {
        return this.cdpLinkTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<OspfLinkTopologyEntity> getOspfLinkTopologyEntities() {
        return this.ospfLinkTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<IsIsLinkTopologyEntity> getIsIsLinkTopologyEntities() {
        return this.isIsLinkTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<LldpLinkTopologyEntity> getLldpLinkTopologyEntities() {
        return this.lldpLinkTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public void refresh(){
        nodeTopologyEntities.refresh(CACHE_KEY);
        cdpLinkTopologyEntities.refresh(CACHE_KEY);
        nodeTopologyEntities.refresh(CACHE_KEY);
        isIsLinkTopologyEntities.refresh(CACHE_KEY);
        lldpLinkTopologyEntities.refresh(CACHE_KEY);
    }

    private int getCacheDuration(){
        return Integer.getInteger(SYSTEM_PROPERTY_CACHE_DURATION, 300);
    }

    public void setTopologyEntityDao(TopologyEntityDao topologyEntityDao){
        this.topologyEntityDao = topologyEntityDao;
    }
}
