/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.importer.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This nodes job is to tracks nodes that need to be deleted, added, or changed
 *
 * @author david
 * @version $Id: $
 */
public class ImportOperationsManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ImportOperationsManager.class);

    
	private List<ImportOperation> m_inserts = new LinkedList<ImportOperation>();
    private List<ImportOperation> m_updates = new LinkedList<ImportOperation>();
    private Map<String, Integer> m_foreignIdToNodeMap;
    
    private ImportOperationFactory m_operationFactory;
    private ImportStatistics m_stats = new DefaultImportStatistics();
	private EventIpcManager m_eventMgr;
	
	private int m_scanThreads = 50;
	private int m_writeThreads = 4;
    private String m_foreignSource;
    
    /**
     * <p>Constructor for ImportOperationsManager.</p>
     *
     * @param foreignIdToNodeMap a {@link java.util.Map} object.
     * @param operationFactory a {@link org.opennms.netmgt.importer.operations.ImportOperationFactory} object.
     */
    public ImportOperationsManager(Map<String, Integer> foreignIdToNodeMap, ImportOperationFactory operationFactory) {
        m_foreignIdToNodeMap = new HashMap<String, Integer>(foreignIdToNodeMap);
        m_operationFactory = operationFactory;
    }

    /**
     * <p>foundNode</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.importer.operations.SaveOrUpdateOperation} object.
     */
    public SaveOrUpdateOperation foundNode(String foreignId, String nodeLabel, String building, String city) {
        
        if (nodeExists(foreignId)) {
            return updateNode(foreignId, nodeLabel, building, city);
        } else {
            return insertNode(foreignId, nodeLabel, building, city);
        }        
    }

    private boolean nodeExists(String foreignId) {
        return m_foreignIdToNodeMap.containsKey(foreignId);
    }
    
    private SaveOrUpdateOperation insertNode(String foreignId, String nodeLabel, String building, String city) {
        InsertOperation insertOperation = m_operationFactory.createInsertOperation(getForeignSource(), foreignId, nodeLabel, building, city);
        m_inserts.add(insertOperation);
        return insertOperation;
    }

    private SaveOrUpdateOperation updateNode(String foreignId, String nodeLabel, String building, String city) {
        Integer nodeId = processForeignId(foreignId);
        UpdateOperation updateOperation = m_operationFactory.createUpdateOperation(nodeId, getForeignSource(), foreignId, nodeLabel, building, city);
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
    
    class DeleteIterator implements Iterator<ImportOperation> {
    	
    	private Iterator<Entry<String, Integer>> m_foreignIdIterator = m_foreignIdToNodeMap.entrySet().iterator();

            @Override
		public boolean hasNext() {
			return m_foreignIdIterator.hasNext();
		}

            @Override
		public ImportOperation next() {
            Entry<String, Integer> entry = m_foreignIdIterator.next();
            Integer nodeId = entry.getValue();
            String foreignId = entry.getKey();
            return m_operationFactory.createDeleteOperation(nodeId, m_foreignSource, foreignId);
			
		}

            @Override
		public void remove() {
			m_foreignIdIterator.remove();
		}
    	
    }
    
    class OperationIterator implements Iterator<ImportOperation> {
    	
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
        } catch (InterruptedException e) {
            LOG.error(msg, e);
        }
    }
 
    /**
     * <p>persistOperations</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     * @param dao a {@link org.opennms.netmgt.dao.api.OnmsDao} object.
     */
    public void persistOperations(TransactionTemplate template, OnmsDao<?, ?> dao) {
    	m_stats.beginProcessingOps();
    	m_stats.setDeleteCount(getDeleteCount());
    	m_stats.setInsertCount(getInsertCount());
    	m_stats.setUpdateCount(getUpdateCount());
    	ExecutorService pool = Executors.newFixedThreadPool(m_writeThreads, new LogPreservingThreadFactory(getClass().getSimpleName() + ".persistOperations", m_writeThreads, false));

		preprocessOperations(template, dao, new OperationIterator(), pool);

		shutdownAndWaitForCompletion(pool, "persister interrupted!");

		m_stats.finishProcessingOps();
    	
    }
    
	private void preprocessOperations(final TransactionTemplate template, final OnmsDao<?, ?> dao, OperationIterator iterator, final ExecutorService dbPool) {
		
		m_stats.beginPreprocessingOps();
		
		ExecutorService pool = Executors.newFixedThreadPool(m_scanThreads, new LogPreservingThreadFactory(getClass().getSimpleName() + ".preprocessOperations", m_scanThreads, false));
		for (Iterator<ImportOperation> it = iterator; it.hasNext();) {
    		final ImportOperation oper = it.next();
    		Runnable r = new Runnable() {
                            @Override
    			public void run() {
    				preprocessOperation(oper, template, dao, dbPool);
    			}
    		};
    		pool.execute(r);

    	}
		
		shutdownAndWaitForCompletion(pool, "preprocessor interrupted!");
		
		m_stats.finishPreprocessingOps();
	}

	/**
	 * <p>preprocessOperation</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
	 * @param dao a {@link org.opennms.netmgt.dao.api.OnmsDao} object.
	 * @param dbPool a {@link java.util.concurrent.ExecutorService} object.
	 */
	protected void preprocessOperation(final ImportOperation oper, final TransactionTemplate template, final OnmsDao<?, ?> dao, final ExecutorService dbPool) {
		m_stats.beginPreprocessing(oper);
		LOG.info("Preprocess: {}", oper);
		oper.gatherAdditionalData();
		Runnable r = new Runnable() {
                        @Override
			public void run() {
				persistOperation(oper, template, dao);
			}
		};

		dbPool.execute(r);

		m_stats.finishPreprocessing(oper);
	}

	/**
	 * <p>persistOperation</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.importer.operations.ImportOperation} object.
	 * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
	 * @param dao a {@link org.opennms.netmgt.dao.api.OnmsDao} object.
	 */
	protected void persistOperation(final ImportOperation oper, TransactionTemplate template, final OnmsDao<?, ?> dao) {
		m_stats.beginPersisting(oper);
		LOG.info("Persist: {}", oper);

		List<Event> events = persistToDatabase(oper, template);
		
		m_stats.finishPersisting(oper);
		
		
		if (m_eventMgr != null && events != null) {
			m_stats.beginSendingEvents(oper, events);
			LOG.info("Send Events: {}", oper);
			// now send the events for the update
			for (Iterator<Event> eventIt = events.iterator(); eventIt.hasNext();) {
				Event event = eventIt.next();
				m_eventMgr.sendNow(event);
			}
			m_stats.finishSendingEvents(oper, events);
		}

		LOG.info("Clear cache: {}", oper);
		// clear the cache to we don't use up all the memory
		dao.clear();
	}

	/**
     * Persist the import operation changes to the database.
     *  
     * @param oper changes to persist
     * @param template transaction template in which to perform the persist operation
     * @return list of events
	 */
    private List<Event> persistToDatabase(final ImportOperation oper, TransactionTemplate template) {
		List<Event> events = template.execute(new TransactionCallback<List<Event>>() {
                        @Override
			public List<Event> doInTransaction(TransactionStatus status) {
				List<Event> result = oper.persist();
                return result;
			}
		});
        return events;
    }


	/**
	 * <p>setScanThreads</p>
	 *
	 * @param scanThreads a int.
	 */
	public void setScanThreads(int scanThreads) {
		m_scanThreads = scanThreads;
	}
	
	/**
	 * <p>setWriteThreads</p>
	 *
	 * @param writeThreads a int.
	 */
	public void setWriteThreads(int writeThreads) {
		m_writeThreads = writeThreads;
	}



	/**
	 * <p>getEventMgr</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
	 */
	public EventIpcManager getEventMgr() {
		return m_eventMgr;
	}



	/**
	 * <p>setEventMgr</p>
	 *
	 * @param eventMgr a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
	 */
	public void setEventMgr(EventIpcManager eventMgr) {
		m_eventMgr = eventMgr;
	}

	/**
	 * <p>getStats</p>
	 *
	 * @return a {@link org.opennms.netmgt.importer.operations.ImportStatistics} object.
	 */
	public ImportStatistics getStats() {
		return m_stats;
	}

	/**
	 * <p>setStats</p>
	 *
	 * @param stats a {@link org.opennms.netmgt.importer.operations.ImportStatistics} object.
	 */
	public void setStats(ImportStatistics stats) {
		m_stats = stats;
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
}
