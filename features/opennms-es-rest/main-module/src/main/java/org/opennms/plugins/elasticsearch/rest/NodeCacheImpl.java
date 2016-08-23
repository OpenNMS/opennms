package org.opennms.plugins.elasticsearch.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private LoadingCache<Long, Map> cache=null;

    public NodeCacheImpl() {}

    public void init() {
        if(cache==null) {
            LOG.info("initializing node data cache (TTL="+MAX_TTL+"m, MAX_SIZE="+MAX_SIZE+")");
            CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
            if(MAX_TTL>0) {
                cacheBuilder.expireAfterWrite(MAX_TTL, TimeUnit.MINUTES);
            }
            if(MAX_SIZE>0) {
                cacheBuilder.maximumSize(MAX_SIZE);
            }

            cache=cacheBuilder.build(new CacheLoader<Long, Map>() {
                                             @Override
                                             public Map load(Long key) throws Exception {
                                                 return getNodeAndCategoryInfo(key);
                                             }
                                         }
            );
        }
    }

    public Map getEntry(Long key) {
        return cache.getUnchecked(key);
    }

    public void refreshEntry(Long key) {
        LOG.debug("refreshing node cache entry: "+key);
        cache.refresh(key);
    }

    private Map getNodeAndCategoryInfo(Long nodeId) {
        final Map result=new HashMap();

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
    private void populateBodyWithNodeInfo(Map body, OnmsNode node) {
        body.put("nodelabel", node.getLabel());
        body.put("nodesysname", node.getSysName());
        body.put("nodesyslocation", node.getSysLocation());
        body.put("foreignsource", node.getForeignSource());
        body.put("foreignid", node.getForeignId());
        body.put("operatingsystem", node.getOperatingSystem());
        StringBuilder categories=new StringBuilder();
        for (Iterator i=node.getCategories().iterator();i.hasNext();) {
            categories.append(((OnmsCategory)i.next()).getName());
            if(i.hasNext()) {
                categories.append(",");
            }
        }
        body.put("categories", categories.toString());
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
}