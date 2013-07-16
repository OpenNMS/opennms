/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.service.ProvisionService;
import org.opennms.netmgt.provision.service.RequisitionAccountant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tracks nodes that need to be deleted, inserted, or updated during
 * provisioning import operations.
 *
 * @author david
 */
public class ImportOperationsManager {
    private static final Logger LOG = LoggerFactory.getLogger(ImportOperationsManager.class);
	public static final class NullUpdateOperation extends UpdateOperation {
		public NullUpdateOperation(final Integer nodeId, final String foreignSource, final String foreignId, final String nodeLabel, final String building, final String city, final ProvisionService provisionService) {
			super(nodeId, foreignSource, foreignId, nodeLabel, building, city, provisionService);
		}

		@Override
	    protected void doPersist() {
			LOG.debug("Skipping persist for node {}: rescanExisting is false", getNode());
		}
	}

	/**
     * TODO: Seth 2012-03-08: These lists may consume a lot of RAM for large provisioning 
     * groups. We may need to figure out how to use flyweight objects instead of heavier 
     * {@link OnmsNode} objects in these lists. Our goal is to handle 50,000+ nodes per 
     * import operation.
     */
    private final List<ImportOperation> m_inserts = new LinkedList<ImportOperation>();
    private final List<ImportOperation> m_updates = new LinkedList<ImportOperation>();
    
    private final ProvisionService m_provisionService;
    private final Map<String, Integer> m_foreignIdToNodeMap;
    private Boolean m_rescanExisting;
    
    private String m_foreignSource;
    
    /**
     * <p>Constructor for ImportOperationsManager.</p>
     *
     * @param foreignIdToNodeMap a {@link java.util.Map} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param rescanExisting TODO
     */
    public ImportOperationsManager(Map<String, Integer> foreignIdToNodeMap, ProvisionService provisionService, final Boolean rescanExisting) {
        m_provisionService = provisionService;
        m_foreignIdToNodeMap = new HashMap<String, Integer>(foreignIdToNodeMap);
        m_rescanExisting = rescanExisting;
    }

    /**
     * <p>foundNode</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation} object.
     */
    public SaveOrUpdateOperation foundNode(String foreignId, String nodeLabel, String building, String city) {
        
        SaveOrUpdateOperation ret;
        if (nodeExists(foreignId)) {
        	ret = updateNode(foreignId, nodeLabel, building, city);
        } else {
            ret = insertNode(foreignId, nodeLabel, building, city);
        }        
        return ret;
    }

    private boolean nodeExists(String foreignId) {
        return m_foreignIdToNodeMap.containsKey(foreignId);
    }
    
    private SaveOrUpdateOperation insertNode(final String foreignId, final String nodeLabel, final String building, final String city) {
        SaveOrUpdateOperation insertOperation = new InsertOperation(getForeignSource(), foreignId, nodeLabel, building, city, m_provisionService);
        m_inserts.add(insertOperation);
        return insertOperation;
    }

    private SaveOrUpdateOperation updateNode(final String foreignId, final String nodeLabel, final String building, final String city) {
    	final Integer nodeId = processForeignId(foreignId);
    	final UpdateOperation updateOperation;
    	if (m_rescanExisting) {
            updateOperation = new UpdateOperation(nodeId, getForeignSource(), foreignId, nodeLabel, building, city, m_provisionService);
    	} else {
            updateOperation = new NullUpdateOperation(nodeId, getForeignSource(), foreignId, nodeLabel, building, city, m_provisionService);
    	}
        m_updates.add(updateOperation);
        return updateOperation;
    }

    /**
     * Return NodeId and remove it from the Map so we know which nodes have been operated on thereby
     * tracking nodes to be deleted.
     * @param foreignId
     * @return a nodeId
     */
    private Integer processForeignId(String foreignId) {
        return m_foreignIdToNodeMap.remove(foreignId);
    }
    
    /**
     * <p>getOperationCount</p>
     *
     * @return a int.
     */
    public int getOperationCount() {
        return m_inserts.size() + m_updates.size() + m_foreignIdToNodeMap.size();
    }
    
    /**
     * <p>getInsertCount</p>
     *
     * @return a int.
     */
    public int getInsertCount() {
    	return m_inserts.size();
    }

    /**
     * <p>getUpdateCount</p>
     *
     * @return a int.
     */
    public int  getUpdateCount() {
        return m_updates.size();
    }

    /**
     * <p>getDeleteCount</p>
     *
     * @return a int.
     */
    public int getDeleteCount() {
    	return m_foreignIdToNodeMap.size();
    }
    
    private class DeleteIterator implements Iterator<ImportOperation> {
    	
    	private final Iterator<Entry<String, Integer>> m_foreignIdIterator = m_foreignIdToNodeMap.entrySet().iterator();

            @Override
		public boolean hasNext() {
			return m_foreignIdIterator.hasNext();
		}

            @Override
		public ImportOperation next() {
            Entry<String, Integer> entry = m_foreignIdIterator.next();
            return new DeleteOperation(entry.getValue(), getForeignSource(), entry.getKey(), m_provisionService);
			
		}

            @Override
		public void remove() {
			m_foreignIdIterator.remove();
		}
    	
    }
    
    private class OperationIterator implements Iterator<ImportOperation>, Enumeration<ImportOperation> {
    	
    	Iterator<Iterator<ImportOperation>> m_iterIter;
    	Iterator<ImportOperation> m_currentIter;
    	
    	OperationIterator() {
    		List<Iterator<ImportOperation>> iters = new ArrayList<Iterator<ImportOperation>>(3);
    		iters.add(new DeleteIterator());
    		iters.add(m_updates.iterator());
    		iters.add(m_inserts.iterator());
    		m_iterIter = iters.iterator();
    	}
    	
            @Override
		public boolean hasNext() {
			while((m_currentIter == null || !m_currentIter.hasNext()) && m_iterIter.hasNext()) {
				m_currentIter = m_iterIter.next();
				m_iterIter.remove();
			}
			
			return (m_currentIter == null ? false: m_currentIter.hasNext());
		}

            @Override
		public ImportOperation next() {
			return m_currentIter.next();
		}

            @Override
		public void remove() {
			m_currentIter.remove();
		}

            @Override
        public boolean hasMoreElements() {
            return hasNext();
        }

            @Override
        public ImportOperation nextElement() {
            return next();
        }
    	
    	
    }
    
    /**
     * <p>shutdownAndWaitForCompletion</p>
     *
     * @param executorService a {@link java.util.concurrent.ExecutorService} object.
     * @param msg a {@link java.lang.String} object.
     */
    public void shutdownAndWaitForCompletion(ExecutorService executorService, String msg) {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // loop util the await returns false
            }
        } catch (final InterruptedException e) {
            LOG.error(msg, e);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * <p>getOperations</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<ImportOperation> getOperations() {
        return Collections.list(new OperationIterator());
    }
    
    @SuppressWarnings("unused")
    private Runnable sequence(final Executor pool, final Runnable a, final Runnable b) {
        return new Runnable() {
            @Override
            public void run() {
                a.run();
                pool.execute(b);
            }
        };
    }

    /**
     * <p>setForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     */
    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    public Boolean getRescanExisting() {
        return m_rescanExisting;
    }
    
    /**
     * <p>auditNodes</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    public void auditNodes(Requisition requisition) {
        requisition.visit(new RequisitionAccountant(this));
    }

    @SuppressWarnings("unused")
    private Runnable persister(final ImportOperation oper) {
        Runnable r = new Runnable() {
                @Override
        	public void run() {
        		oper.persist();
        	}
        };
        return r;
    }
    
    @SuppressWarnings("unused")
    private Runnable scanner(final ImportOperation oper) {
        return new Runnable() {
            @Override
            public void run() {
                LOG.info("Preprocess: {}", oper);
                oper.scan();
            }
        };
    }
}
