package org.opennms.features.elasticsearch.eventforwarder.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created:
 * User: unicoletti
 * Date: 11:21 AM 6/27/15
 */
public class GuavaCache {
    Logger logger = LoggerFactory.getLogger(GuavaCache.class);

    private long MAX_SIZE = 10000;
    private long MAX_TTL  = 5; // Minutes

    private volatile NodeDao nodeDao;
    private volatile TransactionOperations transactionOperations;

    private LoadingCache<Long, Map> cache=null;

    public GuavaCache() {}

    public void init() {
        if(cache==null) {
            logger.info("initializing node data cache (TTL="+MAX_TTL+"m, MAX_SIZE="+MAX_SIZE+")...");
            cache = CacheBuilder.newBuilder().expireAfterWrite(MAX_TTL, TimeUnit.MINUTES).maximumSize(MAX_SIZE).build(new CacheLoader<Long, Map>() {
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

    private Map getNodeAndCategoryInfo(Long nodeId) {
        final Map result=new HashMap();

        // safety check
        if(nodeId!=null) {
            logger.debug("Fetching node data from database into cache");

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
        body.put("nodelbl", node.getLabel());
        body.put("nodesysname", node.getSysName());
        body.put("nodesyslocation", node.getSysLocation());
        body.put("foreignsource", node.getForeignSource());
        body.put("operatingsystem", node.getOperatingSystem());
        StringBuilder categories=new StringBuilder();
        for (OnmsCategory cat : node.getCategories()) {
            categories.append(cat.getName()).append(" ");
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