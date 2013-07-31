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

package org.opennms.netmgt.importer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.importer.config.Node;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>BaseImporter class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BaseImporter implements ImportOperationFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(BaseImporter.class);


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
    /**
     * <p>setTransactionTemplate</p>
     *
     * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    /**
     * <p>getDistPollerDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.DistPollerDao} object.
     */
    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    /**
     * <p>setDistPollerDao</p>
     *
     * @param distPollerDao a {@link org.opennms.netmgt.dao.api.DistPollerDao} object.
     */
    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getIpInterfaceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     */
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    /**
     * <p>setIpInterfaceDao</p>
     *
     * @param ipInterfaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     */
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    /**
     * <p>getMonitoredServiceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.MonitoredServiceDao} object.
     */
    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }

    /**
     * <p>setMonitoredServiceDao</p>
     *
     * @param monitoredServiceDao a {@link org.opennms.netmgt.dao.api.MonitoredServiceDao} object.
     */
    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    /**
     * <p>getServiceTypeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ServiceTypeDao} object.
     */
    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

    /**
     * <p>setServiceTypeDao</p>
     *
     * @param serviceTypeDao a {@link org.opennms.netmgt.dao.api.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    /**
     * <p>getAssetRecordDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.AssetRecordDao} object.
     */
    public AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }

    /**
     * <p>setAssetRecordDao</p>
     *
     * @param assetRecordDao a {@link org.opennms.netmgt.dao.api.AssetRecordDao} object.
     */
    public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
        m_assetRecordDao = assetRecordDao;
    }

    /**
     * <p>getTransTemplate</p>
     *
     * @return a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public TransactionTemplate getTransTemplate() {
        return m_transTemplate;
    }

    /**
     * <p>setTransTemplate</p>
     *
     * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTransTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public DeleteOperation createDeleteOperation(Integer nodeId, String foreignSource, String foreignId) {
        return new DeleteOperation(nodeId, foreignSource, foreignId, m_nodeDao);
    }
    
    /**
     * <p>importModelFromResource</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @throws java.io.IOException if any.
     * @throws org.opennms.netmgt.importer.ModelImportException if any.
     */
    protected void importModelFromResource(Resource resource) throws IOException, ModelImportException {
    	importModelFromResource(resource, new DefaultImportStatistics(), null);
    }

    /**
     * <p>importModelFromResource</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @param stats a {@link org.opennms.netmgt.importer.operations.ImportStatistics} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws java.io.IOException if any.
     * @throws org.opennms.netmgt.importer.ModelImportException if any.
     */
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

	/**
	 * <p>createImportOperationsManager</p>
	 *
	 * @param foreignIdsToNodes a {@link java.util.Map} object.
	 * @param stats a {@link org.opennms.netmgt.importer.operations.ImportStatistics} object.
	 * @return a {@link org.opennms.netmgt.importer.operations.ImportOperationsManager} object.
	 */
	protected ImportOperationsManager createImportOperationsManager(Map<String, Integer> foreignIdsToNodes, ImportStatistics stats) {
		ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, this);
        opsMgr.setStats(stats);
		return opsMgr;
	}

    private void auditNodes(final ImportOperationsManager opsMgr, final SpecFile specFile) {
    	m_transTemplate.execute(new TransactionCallbackWithoutResult() {
    
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                ImportAccountant accountant = new ImportAccountant(opsMgr);
                specFile.visitImport(accountant);
            }
            
        });
    }

	class NodeRelator extends AbstractImportVisitor {
		String m_foreignSource;
		
		public NodeRelator(String foreignSource) {
			m_foreignSource = foreignSource;
		}

                @Override
        public void visitNode(final Node node) {
			m_transTemplate.execute(new TransactionCallbackWithoutResult() {
                                @Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					
					OnmsNode dbNode = findNodeByForeignId(m_foreignSource, node.getForeignId());
					if (dbNode == null) {
					    LOG.error("Error setting parent on node: {} node not in database", node.getForeignId());
					    return;
					}
					OnmsNode parent = findParent(node);
					
					OnmsIpInterface critIface = null;
					if (parent != null) {
						critIface = getCriticalInterface(parent);
					}
					
					LOG.info("Setting parent of node: {} to: {}", dbNode, parent);
					dbNode.setParent(parent);
					LOG.info("Setting criticalInterface of node: {} to: {}", dbNode, critIface);
					if (critIface == null) {
						dbNode.setPathElement(null);
					} else {
						final String ipAddress = InetAddressUtils.str(critIface.getIpAddress());
						dbNode.setPathElement(new PathElement(ipAddress, "ICMP"));
					}
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
			
			LOG.error("Unable to locate a unique node using label {}{} nodes found.  Ignoring relationship.", label, nodes.size());
			return null;
		}

		private OnmsNode findNodeByForeignId(String foreignSource, String foreignId) {
            return getNodeDao().findByForeignId(foreignSource, foreignId);
		}

	};

	private void relateNodes(SpecFile specFile) {
		specFile.visitImport(new NodeRelator(specFile.getForeignSource()));
	}


	private Map<String, Integer> getForeignIdToNodeMap(final String foreignSource) {
        return m_transTemplate.execute(new TransactionCallback<Map<String, Integer>>() {
            @Override
            public Map<String,Integer> doInTransaction(TransactionStatus status) {
                return Collections.unmodifiableMap(getNodeDao().getForeignIdToNodeIdMap(foreignSource));
            }
        });
        
    }

    private OnmsDistPoller createDistPollerIfNecessary() {
        return m_transTemplate.execute(new TransactionCallback<OnmsDistPoller>() {
    
            @Override
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

    /**
     * <p>getCategoryDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

	/**
	 * <p>getScanThreads</p>
	 *
	 * @return a int.
	 */
	public int getScanThreads() {
		return m_scanThreads;
	}

	/**
	 * <p>setScanThreads</p>
	 *
	 * @param poolSize a int.
	 */
	public void setScanThreads(int poolSize) {
		m_scanThreads = poolSize;
	}

	/**
	 * <p>getWriteThreads</p>
	 *
	 * @return a int.
	 */
	public int getWriteThreads() {
		return m_writeThreads;
	}

	/**
	 * <p>setWriteThreads</p>
	 *
	 * @param writeThreads a int.
	 */
	public void setWriteThreads(int writeThreads) {
		m_writeThreads = writeThreads;
	}

}
