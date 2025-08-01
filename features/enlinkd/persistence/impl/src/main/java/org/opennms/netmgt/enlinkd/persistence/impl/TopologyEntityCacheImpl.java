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
package org.opennms.netmgt.enlinkd.persistence.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityDao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TopologyEntityCacheImpl implements TopologyEntityCache {

    private final static String CACHE_KEY = "CACHE_KEY";
    private final static String SYSTEM_PROPERTY_CACHE_DURATION = "org.opennms.ui.topology-entity-cache-duration";

    private TopologyEntityDao topologyEntityDao;

    private final LoadingCache<String, List<NodeTopologyEntity>> nodeTopologyEntities = createCache(
            () -> topologyEntityDao.getNodeTopologyEntities());

    private final LoadingCache<String, List<CdpLinkTopologyEntity>> cdpLinkTopologyEntities = createCache(
            () -> topologyEntityDao.getCdpLinkTopologyEntities());

    private final LoadingCache<String, List<IsIsLinkTopologyEntity>> isIsLinkTopologyEntities = createCache(
            ()-> topologyEntityDao.getIsIsLinkTopologyEntities());

    private final LoadingCache<String, List<OspfLinkTopologyEntity>> ospfLinkTopologyEntities = createCache (
            () -> topologyEntityDao.getOspfLinkTopologyEntities());

    private final LoadingCache<String, List<OspfAreaTopologyEntity>> ospfAreaTopologyEntities = createCache (
            () -> topologyEntityDao.getOspfAreaTopologyEntities());

    private final LoadingCache<String, List<LldpLinkTopologyEntity>> lldpLinkTopologyEntities = createCache (
            () -> topologyEntityDao.getLldpLinkTopologyEntities());

    private final LoadingCache<String, List<CdpElementTopologyEntity>> cdpElementTopologyEntities = createCache(
            () ->  topologyEntityDao.getCdpElementTopologyEntities());

    private final LoadingCache<String, List<IsIsElementTopologyEntity>> isIsElementTopologyEntities = createCache(
            () -> topologyEntityDao.getIsIsElementTopologyEntities());

    private final LoadingCache<String, List<LldpElementTopologyEntity>> lldpElementTopologyEntities = createCache(
            () -> topologyEntityDao.getLldpElementTopologyEntities());

    private final LoadingCache<String, List<SnmpInterfaceTopologyEntity>> snmpInterfaceTopologyEntities = createCache(
            () -> topologyEntityDao.getSnmpTopologyEntities());

    private final LoadingCache<String, List<IpInterfaceTopologyEntity>> ipInterfaceTopologyEntities = createCache(
            () ->  topologyEntityDao.getIpTopologyEntities());

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
    public List<NodeTopologyEntity> getNodeTopologyEntities() {
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
    public List<OspfAreaTopologyEntity> getOspfAreaTopologyEntities() {
        return this.ospfAreaTopologyEntities.getUnchecked(CACHE_KEY);
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
    public List<CdpElementTopologyEntity> getCdpElementTopologyEntities() {
        return this.cdpElementTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<IsIsElementTopologyEntity> getIsIsElementTopologyEntities() {
        return this.isIsElementTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<LldpElementTopologyEntity> getLldpElementTopologyEntities() {
        return this.lldpElementTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<SnmpInterfaceTopologyEntity> getSnmpInterfaceTopologyEntities(){
        return this.snmpInterfaceTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public List<IpInterfaceTopologyEntity> getIpInterfaceTopologyEntities(){
        return this.ipInterfaceTopologyEntities.getUnchecked(CACHE_KEY);
    }

    @Override
    public void refresh(){
        nodeTopologyEntities.refresh(CACHE_KEY);
        cdpLinkTopologyEntities.refresh(CACHE_KEY);
        isIsLinkTopologyEntities.refresh(CACHE_KEY);
        lldpLinkTopologyEntities.refresh(CACHE_KEY);
        ospfLinkTopologyEntities.refresh(CACHE_KEY);
        ospfAreaTopologyEntities.refresh(CACHE_KEY);
        cdpElementTopologyEntities.refresh(CACHE_KEY);
        isIsElementTopologyEntities.refresh(CACHE_KEY);
        lldpElementTopologyEntities.refresh(CACHE_KEY);
        snmpInterfaceTopologyEntities.refresh(CACHE_KEY);
        ipInterfaceTopologyEntities.refresh(CACHE_KEY);
    }

    private int getCacheDuration(){
        return SystemProperties.getInteger(SYSTEM_PROPERTY_CACHE_DURATION, 300);
    }

    public void setTopologyEntityDao(TopologyEntityDao topologyEntityDao){
        this.topologyEntityDao = topologyEntityDao;
    }
}