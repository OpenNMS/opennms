//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 20: System.err.println -> log().info. - dj@opennms.org
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.provision.service.operations;

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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventForwarder;

/**
 * This nodes job is to tracks nodes that need to be deleted, added, or changed
 * @author david
 *
 */
public class ImportOperationsManager {
    
	private List<ImportOperation> m_inserts = new LinkedList<ImportOperation>();
    private List<ImportOperation> m_updates = new LinkedList<ImportOperation>();
    private Map<String, Integer> m_foreignIdToNodeMap;
    
    private ImportOperationFactory m_operationFactory;
    private ProvisionMonitor m_monitor = new NoOpProvisionMonitor();
	private EventForwarder m_eventForwarder;
	
	private int m_scanThreads = 50;
	private int m_writeThreads = 4;
    private String m_foreignSource;
    
    public ImportOperationsManager(Map<String, Integer> foreignIdToNodeMap, ImportOperationFactory operationFactory) {
        m_foreignIdToNodeMap = new HashMap<String, Integer>(foreignIdToNodeMap);
        m_operationFactory = operationFactory;
    }

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
        return (Integer)m_foreignIdToNodeMap.remove(foreignId);
    }
    
    public int getOperationCount() {
        return m_inserts.size() + m_updates.size() + m_foreignIdToNodeMap.size();
    }
    
    public int getInsertCount() {
    	return m_inserts.size();
    }

    public int  getUpdateCount() {
        return m_updates.size();
    }

    public int getDeleteCount() {
    	return m_foreignIdToNodeMap.size();
    }
    
    class DeleteIterator implements Iterator<ImportOperation> {
    	
    	private Iterator<Entry<String, Integer>> m_foreignIdIterator = m_foreignIdToNodeMap.entrySet().iterator();

		public boolean hasNext() {
			return m_foreignIdIterator.hasNext();
		}

		public ImportOperation next() {
            Entry<String, Integer> entry = m_foreignIdIterator.next();
            Integer nodeId = entry.getValue();
            String foreignId = entry.getKey();
            return m_operationFactory.createDeleteOperation(nodeId, m_foreignSource, foreignId);
			
		}

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
    	
		public boolean hasNext() {
			while((m_currentIter == null || !m_currentIter.hasNext()) && m_iterIter.hasNext()) {
				m_currentIter = m_iterIter.next();
				m_iterIter.remove();
			}
			
			return (m_currentIter == null ? false: m_currentIter.hasNext());
		}

		public ImportOperation next() {
			return m_currentIter.next();
		}

		public void remove() {
			m_currentIter.remove();
		}
    	
    	
    }
    
    public void shutdownAndWaitForCompletion(ExecutorService executorService, String msg) {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // loop util the await returns false
            }
        } catch (InterruptedException e) {
            log().error(msg, e);
        }
    }
 
    public void persistOperations() {

    	m_monitor.beginProcessingOps();
    	m_monitor.setDeleteCount(getDeleteCount());
    	m_monitor.setInsertCount(getInsertCount());
    	m_monitor.setUpdateCount(getUpdateCount());
    	ExecutorService dbPool = Executors.newFixedThreadPool(m_writeThreads);

		preprocessOperations(new OperationIterator(), dbPool);

		shutdownAndWaitForCompletion(dbPool, "persister interrupted!");

		m_monitor.finishProcessingOps();
    	
    }
    
	private void preprocessOperations(OperationIterator iterator, final ExecutorService dbPool) {
		
		m_monitor.beginPreprocessingOps();
		
		ExecutorService threadPool = Executors.newFixedThreadPool(m_scanThreads);
		for (Iterator<ImportOperation> it = iterator; it.hasNext();) {
    		final ImportOperation oper = it.next();
    		Runnable r = new Runnable() {
    			public void run() {
    				preprocessOperation(oper, dbPool);
    			}
    		};
    		threadPool.execute(r);

    	}
		
		shutdownAndWaitForCompletion(threadPool, "preprocessor interrupted!");
		
		m_monitor.finishPreprocessingOps();
	}

	protected void preprocessOperation(final ImportOperation oper, final ExecutorService dbPool) {
		m_monitor.beginPreprocessing(oper);
		log().info("Preprocess: "+oper);
		oper.gatherAdditionalData();
		Runnable r = new Runnable() {
			public void run() {
				oper.persist(m_monitor);
			}
		};

		dbPool.execute(r);

		m_monitor.finishPreprocessing(oper);
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public void setScanThreads(int scanThreads) {
		m_scanThreads = scanThreads;
	}
	
	public void setWriteThreads(int writeThreads) {
		m_writeThreads = writeThreads;
	}



	public EventForwarder getEventForwarder() {
		return m_eventForwarder;
	}



	public void setEventForwarder(EventForwarder eventForwarder) {
		m_eventForwarder = eventForwarder;
	}

	public ProvisionMonitor getMonitor() {
		return m_monitor;
	}

	public void setMonitor(ProvisionMonitor stats) {
		m_monitor = stats;
	}

    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }
}
