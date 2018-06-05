/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Created:
 * User: unicoletti
 * Date: 11:21 AM 6/27/15
 */
public class NodeCacheImpl implements NodeCache {
    private static final Logger LOG = LoggerFactory.getLogger(NodeCacheImpl.class);

    private long MAX_SIZE = 10000;
    private long MAX_TTL  = 5; // Minutes

    private volatile NodeDao nodeDao;
    private volatile TransactionOperations transactionOperations;

    private static boolean archiveAssetData =true;

    private LoadingCache<Long, Map<String,String>> cache = null;

    public NodeCacheImpl() {}

    public void init() {
        if(cache==null) {
            LOG.info("initializing node data cache (archiveAssetData="+archiveAssetData
                    + ", TTL="+MAX_TTL+"m, MAX_SIZE="+MAX_SIZE+")");
            CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
            if(MAX_TTL>0) {
                cacheBuilder.expireAfterWrite(MAX_TTL, TimeUnit.MINUTES);
            }
            if(MAX_SIZE>0) {
                cacheBuilder.maximumSize(MAX_SIZE);
            }

            cache=cacheBuilder.build(new CacheLoader<Long, Map<String,String>>() {
                                             @Override
                                             public Map<String,String> load(Long key) throws Exception {
                                                 return getNodeAndCategoryInfo(key);
                                             }
                                         }
            );
        }
    }

    public Map<String,String> getEntry(Long key) {
        return cache.getUnchecked(key);
    }

    public void refreshEntry(Long key) {
        LOG.debug("refreshing node cache entry: "+key);
        cache.refresh(key);
    }

    private Map<String,String> getNodeAndCategoryInfo(Long nodeId) {
        final Map<String,String> result=new HashMap<>();

        // safety check
        if(nodeId!=null) {
            LOG.debug("Fetching node data from database into cache");

            // wrap in a transaction so that Hibernate session is bound and getCategories works
            transactionOperations.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    OnmsNode node = nodeDao.get(nodeId.toString());
                    if (node != null) {
                        populateBodyWithNodeInfo(result, node);
                    }
                }
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
    private static void populateBodyWithNodeInfo(Map<String,String> body, OnmsNode node) {
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

    /* getters and setters */
    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public TransactionOperations getTransactionOperations() {
        return transactionOperations;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    public long getMAX_SIZE() {
        return MAX_SIZE;
    }

    public void setMAX_SIZE(long MAX_SIZE) {
        this.MAX_SIZE = MAX_SIZE;
    }

    public long getMAX_TTL() {
        return MAX_TTL;
    }

    public void setMAX_TTL(long MAX_TTL) {
        this.MAX_TTL = MAX_TTL;
    }

    public boolean getArchiveAssetData() {
        return archiveAssetData;
    }

    public void setArchiveAssetData(boolean archiveAssetData) {
        NodeCacheImpl.archiveAssetData = archiveAssetData;
    }
}