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
package org.opennms.plugins.elasticsearch.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;

/**
 * Created:
 * User: unicoletti
 * Date: 11:21 AM 6/27/15
 */
public class NodeCacheImpl implements NodeCache {
    private static final Logger LOG = LoggerFactory.getLogger(NodeCacheImpl.class);

    private final NodeDao nodeDao;
    private final SessionUtils sessionUtils;
    private final boolean archiveAssetData;

    private final Cache<Long, Map<String,String>> cache;

    public NodeCacheImpl(final NodeDao nodeDao, final SessionUtils sessionUtils, final CacheConfig cacheConfig, final boolean archiveAssetData) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.archiveAssetData = archiveAssetData;

        // Initialize cache
        LOG.info("initializing node data cache (archiveAssetData={}, cacheConfig={})", archiveAssetData, cacheConfig);
        cache = new CacheBuilder()
                .withConfig(cacheConfig)
                .withCacheLoader(new CacheLoader<Long, Map<String, String>>() {
                     @Override
                     public Map<String,String> load(Long nodeId) {
                         return loadNodeAndCategoryInfo(nodeId);
                     }
                 })
                .build();
    }

    public Map<String,String> getEntry(Long nodeId) {
        try {
            return cache.get(nodeId);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error loading entry with key " + nodeId + " from cache", e);
        }
    }

    public void refreshEntry(Long nodeId) {
        LOG.debug("refreshing node cache entry: {}", nodeId);
        cache.refresh(nodeId);
    }

    private Map<String,String> loadNodeAndCategoryInfo(Long nodeId) {
        final Map<String,String> result = new HashMap<>();

        // safety check
        if(nodeId != null) {
            LOG.debug("Fetching node data from database into cache");

            // wrap in a transaction so that Hibernate session is bound and getCategories works
            sessionUtils.withReadOnlyTransaction(() -> {
                final OnmsNode node = nodeDao.get(nodeId.toString());
                if (node != null) {
                    populateBodyWithNodeInfo(result, node);
                }
                return null;
            });
        }
        return result;
    }

    /**
     * utility method to populate a Map with the most import node attributes
     *
     * @param body the map
     * @param node the node object
     */
    private void populateBodyWithNodeInfo(Map<String,String> body, OnmsNode node) {
        body.put("nodelabel", node.getLabel());
        body.put("nodesysname", node.getSysName());
        body.put("nodesyslocation", node.getSysLocation());
        body.put("foreignsource", node.getForeignSource());
        body.put("foreignid", node.getForeignId());
        body.put("operatingsystem", node.getOperatingSystem());
        final StringBuilder categories=new StringBuilder();
        for (Iterator<OnmsCategory> i=node.getCategories().iterator();i.hasNext();) {
            categories.append(((OnmsCategory)i.next()).getName());
            if(i.hasNext()) {
                categories.append(",");
            }
        }
        body.put("categories", categories.toString());

        if(archiveAssetData){

            // parent information
            OnmsNode parent = node.getParent();
            if (parent!=null){
                if (parent.getLabel()!=null)body.put("parent-nodelabel", parent.getLabel());
                if (parent.getNodeId() !=null)body.put("parent-nodeid", parent.getNodeId());
                if (parent.getForeignSource() !=null)body.put("parent-foreignsource", parent.getForeignSource());
                if (parent.getForeignId() !=null)body.put("parent-foreignid", parent.getForeignId());
            }

            //assetRecord.
            OnmsAssetRecord assetRecord= node.getAssetRecord() ;
            if(assetRecord!=null){

                //geolocation
                OnmsGeolocation gl = assetRecord.getGeolocation();
                if (gl !=null){
                        if (gl.getLatitude() !=null)body.put("asset-latitude",  gl.getLatitude().toString());
                        if (gl.getLongitude()!=null)body.put("asset-longitude", gl.getLongitude().toString());
                }

                //assetRecord
                if (assetRecord.getRegion() !=null && ! "".equals(assetRecord.getRegion())) body.put("asset-region", assetRecord.getRegion());
                if (assetRecord.getBuilding() !=null && ! "".equals(assetRecord.getBuilding())) body.put("asset-building", assetRecord.getBuilding());
                if (assetRecord.getFloor() !=null && ! "".equals(assetRecord.getFloor())) body.put("asset-floor",  assetRecord.getFloor());
                if (assetRecord.getRoom() !=null && ! "".equals(assetRecord.getRoom())) body.put("asset-room",   assetRecord.getRoom());
                if (assetRecord.getRack() !=null && ! "".equals(assetRecord.getRack())) body.put("asset-rack",  assetRecord.getRack());
                if (assetRecord.getSlot() !=null && ! "".equals(assetRecord.getSlot())) body.put("asset-slot",  assetRecord.getSlot());
                if (assetRecord.getPort() !=null && ! "".equals(assetRecord.getPort())) body.put("asset-port",  assetRecord.getPort());
                if (assetRecord.getCategory() !=null && ! "".equals(assetRecord.getCategory())) body.put("asset-category",  assetRecord.getCategory());
                if (assetRecord.getDisplayCategory() !=null && ! "".equals(assetRecord.getDisplayCategory())) body.put("asset-displaycategory",  assetRecord.getDisplayCategory());
                if (assetRecord.getNotifyCategory() !=null && ! "".equals(assetRecord.getNotifyCategory())) body.put("asset-notifycategory",  assetRecord.getNotifyCategory());
                if (assetRecord.getPollerCategory() !=null && ! "".equals(assetRecord.getPollerCategory())) body.put("asset-pollercategory",   assetRecord.getPollerCategory());
                if (assetRecord.getThresholdCategory() !=null && ! "".equals(assetRecord.getThresholdCategory())) body.put("asset-thresholdcategory",   assetRecord.getThresholdCategory());
                if (assetRecord.getManagedObjectType() !=null && ! "".equals(assetRecord.getManagedObjectType())) body.put("asset-managedobjecttype",   assetRecord.getManagedObjectType());
                if (assetRecord.getManagedObjectInstance() !=null && ! "".equals(assetRecord.getManagedObjectInstance())) body.put("asset-managedobjectinstance", assetRecord.getManagedObjectInstance());
                if (assetRecord.getManufacturer() !=null && ! "".equals(assetRecord.getManufacturer())) body.put("asset-manufacturer", assetRecord.getManufacturer());
                if (assetRecord.getVendor() !=null && ! "".equals(assetRecord.getVendor())) body.put("asset-vendor", assetRecord.getVendor());
                if (assetRecord.getModelNumber() !=null && ! "".equals(assetRecord.getModelNumber())) body.put("asset-modelnumber", assetRecord.getModelNumber());
            }
        }

    }
}