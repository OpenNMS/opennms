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
// 2008 Mar 20: println -> log().info. - dj@opennms.org
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

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.service.RequisitionAccountant;
import org.opennms.netmgt.provision.service.ProvisionService;

/**
 * This nodes job is to tracks nodes that need to be deleted, added, or changed
 *
 * @author david
 * @version $Id: $
 */
public class ImportOperationsManager {
    
	private final List<ImportOperation> m_inserts = new LinkedList<ImportOperation>();
    private final List<ImportOperation> m_updates = new LinkedList<ImportOperation>();
    private final Map<String, Integer> m_foreignIdToNodeMap;
    
    private final ProvisionService m_provisionService;
    
    private String m_foreignSource;
    
    /**
     * <p>Constructor for ImportOperationsManager.</p>
     *
     * @param foreignIdToNodeMap a {@link java.util.Map} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public ImportOperationsManager(Map<String, Integer> foreignIdToNodeMap, ProvisionService provisionService) {
        m_provisionService = provisionService;
        m_foreignIdToNodeMap = new HashMap<String, Integer>(foreignIdToNodeMap);
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
    
    private SaveOrUpdateOperation insertNode(String foreignId, String nodeLabel, String building, String city) {
        SaveOrUpdateOperation insertOperation = new InsertOperation(getForeignSource(), foreignId, nodeLabel, building, city, m_provisionService);
        m_inserts.add(insertOperation);
        return insertOperation;
    }

    private SaveOrUpdateOperation updateNode(String foreignId, String nodeLabel, String building, String city) {
        Integer nodeId = processForeignId(foreignId);
        UpdateOperation updateOperation = new UpdateOperation(nodeId, getForeignSource(), foreignId, nodeLabel, building, city, m_provisionService);
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
    	
    	private final Iterator<Entry<String, Integer>> m_foreignIdIterator = m_foreignIdToNodeMap.entrySet().iterator();

		public boolean hasNext() {
			return m_foreignIdIterator.hasNext();
		}

		public ImportOperation next() {
            Entry<String, Integer> entry = m_foreignIdIterator.next();
            return new DeleteOperation(entry.getValue(), getForeignSource(), entry.getKey(), m_provisionService);
			
		}

		public void remove() {
			m_foreignIdIterator.remove();
		}
    	
    }
    
    class OperationIterator implements Iterator<ImportOperation>, Enumeration<ImportOperation> {
    	
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

        public boolean hasMoreElements() {
            return hasNext();
        }

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
        } catch (InterruptedException e) {
            log().error(msg, e);
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
            public void run() {
                a.run();
                pool.execute(b);
            }
        };
    }

	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
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
        	public void run() {
        		oper.persist();
        	}
        };
        return r;
    }
    
    @SuppressWarnings("unused")
    private Runnable scanner(final ImportOperation oper) {
        return new Runnable() {
            public void run() {
                log().info("Preprocess: "+oper);
                oper.scan();
            }
        };
    }
}
