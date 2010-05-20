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
// 2008 Mar 20: Remove System.err.println. - dj@opennms.org
// 2007 Jun 24: Organize imports, use Java 5 generics. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.importer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.importer.operations.DefaultImportStatistics;
import org.opennms.netmgt.importer.operations.DeleteOperation;
import org.opennms.netmgt.importer.operations.ImportOperationFactory;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.operations.ImportStatistics;
import org.opennms.netmgt.importer.operations.InsertOperation;
import org.opennms.netmgt.importer.operations.UpdateOperation;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;
import org.opennms.netmgt.importer.specification.SpecFile;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PathElement;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class BaseImporter implements ImportOperationFactory {

    protected TransactionTemplate m_transTemplate;
    protected DistPollerDao m_distPollerDao;
    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private ServiceTypeDao m_serviceTypeDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private AssetRecordDao m_assetRecordDao;
    private CategoryDao m_categoryDao;
    private final ThreadLocal<HashMap<String, OnmsServiceType>> m_typeCache = new ThreadLocal<HashMap<String, OnmsServiceType>>();
    private final ThreadLocal<HashMap<String, OnmsCategory>> m_categoryCache = new ThreadLocal<HashMap<String, OnmsCategory>>();
	private int m_scanThreads = 50;
	private int m_writeThreads = 4;

    //FIXME: We have a setTransactionTemplate and a setTransTemplate for the same field.
    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    public AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }

    public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
        m_assetRecordDao = assetRecordDao;
    }

    public TransactionTemplate getTransTemplate() {
        return m_transTemplate;
    }

    public void setTransTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    public InsertOperation createInsertOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        InsertOperation insertOperation = new InsertOperation(foreignSource, foreignId, nodeLabel, building, city);
        insertOperation.setNodeDao(m_nodeDao);
        insertOperation.setDistPollerDao(m_distPollerDao);
        insertOperation.setServiceTypeDao(m_serviceTypeDao);
        insertOperation.setCategoryDao(m_categoryDao);
        insertOperation.setTypeCache(m_typeCache);
        insertOperation.setCategoryCache(m_categoryCache);
        return insertOperation;
        
    }

    public UpdateOperation createUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        UpdateOperation updateOperation = new UpdateOperation(nodeId, foreignSource, foreignId, nodeLabel, building, city);
        updateOperation.setNodeDao(m_nodeDao);
        updateOperation.setDistPollerDao(m_distPollerDao);
        updateOperation.setServiceTypeDao(m_serviceTypeDao);
        updateOperation.setCategoryDao(m_categoryDao);
        updateOperation.setTypeCache(m_typeCache);
        updateOperation.setCategoryCache(m_categoryCache);
        return updateOperation;
    }

    public DeleteOperation createDeleteOperation(Integer nodeId, String foreignSource, String foreignId) {
        return new DeleteOperation(nodeId, foreignSource, foreignId, m_nodeDao);
    }
    
    protected void importModelFromResource(Resource resource) throws IOException, ModelImportException {
    	importModelFromResource(resource, new DefaultImportStatistics(), null);
    }

    protected void importModelFromResource(Resource resource, ImportStatistics stats, Event event) throws IOException, ModelImportException {
        
    	stats.beginImporting();
    	stats.beginLoadingResource(resource);
    	
        SpecFile specFile = new SpecFile();
        specFile.loadResource(resource);
        
        stats.finishLoadingResource(resource);
        
        
        if (event != null && getEventForeignSource(event) != null) {
            specFile.setForeignSource(getEventForeignSource(event));
        }
        
        stats.beginAuditNodes();
        createDistPollerIfNecessary();
        
        Map<String, Integer> foreignIdsToNodes = getForeignIdToNodeMap(specFile.getForeignSource());
        
        ImportOperationsManager opsMgr = createImportOperationsManager(foreignIdsToNodes, stats);
        opsMgr.setForeignSource(specFile.getForeignSource());
        opsMgr.setScanThreads(m_scanThreads);
        opsMgr.setWriteThreads(m_writeThreads);
        
        auditNodes(opsMgr, specFile);
        
        stats.finishAuditNodes();
        
        opsMgr.persistOperations(m_transTemplate, getNodeDao());
        
        stats.beginRelateNodes();
        
        relateNodes(specFile);
        
        stats.finishRelateNodes();
    
        stats.finishImporting();
    }

    private String getEventForeignSource(Event event) {
        return EventUtil.getNamedParmValue("parm[foreignSource]", event);
    }

	protected ImportOperationsManager createImportOperationsManager(Map<String, Integer> foreignIdsToNodes, ImportStatistics stats) {
		ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, this);
        opsMgr.setStats(stats);
		return opsMgr;
	}

    private void auditNodes(final ImportOperationsManager opsMgr, final SpecFile specFile) {
    	m_transTemplate.execute(new TransactionCallback<Object>() {
    
            public Object doInTransaction(TransactionStatus status) {
                ImportAccountant accountant = new ImportAccountant(opsMgr);
                specFile.visitImport(accountant);
                return null;
            }
            
        });
    }

	class NodeRelator extends AbstractImportVisitor {
		String m_foreignSource;
		
		public NodeRelator(String foreignSource) {
			m_foreignSource = foreignSource;
		}

        public void visitNode(final Node node) {
			m_transTemplate.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					
					OnmsNode dbNode = findNodeByForeignId(m_foreignSource, node.getForeignId());
					if (dbNode == null) {
					    log().error("Error setting parent on node: "+node.getForeignId()+" node not in database");
					    return;
					}
					OnmsNode parent = findParent(node);
					
					OnmsIpInterface critIface = null;
					if (parent != null) {
						critIface = getCriticalInterface(parent);
					}
					
					log().info("Setting parent of node: "+dbNode+" to: "+parent);
					dbNode.setParent(parent);
					log().info("Setting criticalInterface of node: "+dbNode+" to: "+critIface);
					dbNode.setPathElement(critIface == null ? null : new PathElement(critIface.getIpAddress(), "ICMP"));
					getNodeDao().update(dbNode);
				}

				private OnmsIpInterface getCriticalInterface(OnmsNode parent) {
					
					OnmsIpInterface critIface = parent.getPrimaryInterface();
					if (critIface != null) {
						return critIface;
					}
					
					return parent.getInterfaceWithService("ICMP");
					
				}

			});
		}
		
		private OnmsNode findParent(Node node) {
			if (node.getParentForeignId() != null) {
                return findNodeByForeignId(m_foreignSource, node.getParentForeignId());
            } else if (node.getParentNodeLabel() != null) {
                return findNodeByNodeLabel(node.getParentNodeLabel());
            }
			
			return null;
		}

		private OnmsNode findNodeByNodeLabel(String label) {
			Collection<OnmsNode> nodes = getNodeDao().findByLabel(label);
			if (nodes.size() == 1) {
                return nodes.iterator().next();
            }
			
			log().error("Unable to locate a unique node using label "+label+" "+nodes.size()+" nodes found.  Ignoring relationship.");
			return null;
		}

		private OnmsNode findNodeByForeignId(String foreignSource, String foreignId) {
            return getNodeDao().findByForeignId(foreignSource, foreignId);
		}

	};

	private void relateNodes(SpecFile specFile) {
		specFile.visitImport(new NodeRelator(specFile.getForeignSource()));
	}

    public ThreadCategory log() {
    	return ThreadCategory.getInstance(getClass());
	}

	private Map<String, Integer> getForeignIdToNodeMap(final String foreignSource) {
        return m_transTemplate.execute(new TransactionCallback<Map<String, Integer>>() {
            public Map<String,Integer> doInTransaction(TransactionStatus status) {
                return getNodeDao().getForeignIdToNodeIdMap(foreignSource);
            }
        });
        
    }

    private OnmsDistPoller createDistPollerIfNecessary() {
        return m_transTemplate.execute(new TransactionCallback<OnmsDistPoller>() {
    
            public OnmsDistPoller doInTransaction(TransactionStatus status) {
                OnmsDistPoller distPoller = m_distPollerDao.get("localhost");
                if (distPoller == null) {
                    distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
                    m_distPollerDao.save(distPoller);
                }
                return distPoller;
            }
            
        });
    
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

	public int getScanThreads() {
		return m_scanThreads;
	}

	public void setScanThreads(int poolSize) {
		m_scanThreads = poolSize;
	}

	public int getWriteThreads() {
		return m_writeThreads;
	}

	public void setWriteThreads(int writeThreads) {
		m_writeThreads = writeThreads;
	}

}
