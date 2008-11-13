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
package org.opennms.netmgt.provision.service;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.provision.service.operations.DeleteOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationFactory;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.InsertOperation;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.UpdateOperation;
import org.opennms.netmgt.provision.service.specification.AbstractImportVisitor;
import org.opennms.netmgt.provision.service.specification.SpecFile;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class BaseImporter implements ImportOperationFactory, InitializingBean {

    private DefaultProvisionService m_provisionService;
	private int m_scanThreads = 50;
	private int m_writeThreads = 4;
	
	public void setProvisionService(DefaultProvisionService provisionService) {
	    m_provisionService = provisionService;
	}
	
	public DefaultProvisionService getProvisionService() {
	    return m_provisionService;
	}

    public NodeDao getNodeDao() {
        return getProvisionService().getNodeDao();
    }
    
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getProvisionService(), "provisionService property must be set");
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return getProvisionService().getIpInterfaceDao();
    }

    public MonitoredServiceDao getMonitoredServiceDao() {
        return getProvisionService().getMonitoredServiceDao();
    }

    public ServiceTypeDao getServiceTypeDao() {
        return getProvisionService().getServiceTypeDao();
    }

    public AssetRecordDao getAssetRecordDao() {
        return getProvisionService().getAssetRecordDao();
    }

    public InsertOperation createInsertOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        return new InsertOperation(foreignSource, foreignId, nodeLabel, building, city, getProvisionService());
        
    }

    public UpdateOperation createUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        return new UpdateOperation(nodeId, foreignSource, foreignId, nodeLabel, building, city, getProvisionService());
    }

    public ImportOperation createDeleteOperation(Integer nodeId, String foreignSource, String foreignId) {
        return new DeleteOperation(nodeId, foreignSource, foreignId, getProvisionService());
    }
    
    protected void importModelFromResource(Resource resource) throws IOException, ModelImportException {
    	importModelFromResource(null, resource, new NoOpProvisionMonitor());
    }

    protected void importModelFromResource(String foreignSource, Resource resource, ProvisionMonitor monitor)
            throws ModelImportException, IOException {
        doImport(resource, monitor, m_scanThreads, m_writeThreads, new ImportManager(),
                 foreignSource);
    }

    private void doImport(Resource resource, ProvisionMonitor monitor,
            int scanThreads, int writeThreads,
            ImportManager importManager, String foreignSource) throws ModelImportException, IOException {
        
        importManager.getClass();
        monitor.beginImporting();
    	monitor.beginLoadingResource(resource);
    	
        SpecFile specFile = new SpecFile();
        specFile.loadResource(resource);
        
        monitor.finishLoadingResource(resource);
        
        
        if (foreignSource != null) {
            specFile.setForeignSource(foreignSource);
        }
        
        monitor.beginAuditNodes();
        createDistPollerIfNecessary();
        
        Map<String, Integer> foreignIdsToNodes = getForeignIdToNodeMap(specFile.getForeignSource());
        
        ImportOperationsManager opsMgr = createImportOperationsManager(foreignIdsToNodes, monitor);
        opsMgr.setForeignSource(specFile.getForeignSource());
        opsMgr.setScanThreads(scanThreads);
        opsMgr.setWriteThreads(writeThreads);
        
        auditNodes(opsMgr, specFile);
        
        monitor.finishAuditNodes();
        
        opsMgr.persistOperations();
        
        monitor.beginRelateNodes();
        
        relateNodes(specFile);
        
        monitor.finishRelateNodes();
    
        monitor.finishImporting();
    }

    protected ImportOperationsManager createImportOperationsManager(Map<String, Integer> foreignIdsToNodes, ProvisionMonitor stats) {
		ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, this);
        opsMgr.setMonitor(stats);
		return opsMgr;
	}

    private void auditNodes(final ImportOperationsManager opsMgr, final SpecFile specFile) {
        specFile.visitImport(new ImportAccountant(opsMgr));
    }

	class NodeRelator extends AbstractImportVisitor {
		String m_foreignSource;
		
		public NodeRelator(String foreignSource) {
			m_foreignSource = foreignSource;
		}

        public void visitNode(final Node node) {
            getProvisionService().setNodeParentAndDependencies(m_foreignSource, node.getForeignId(), node.getParentForeignId(),
                                                node.getParentNodeLabel());
		}

	};

	private void relateNodes(SpecFile specFile) {
		specFile.visitImport(new NodeRelator(specFile.getForeignSource()));
	}

    public Category log() {
    	return ThreadCategory.getInstance(getClass());
	}


    private Map<String, Integer> getForeignIdToNodeMap(final String foreignSource) {
        return getProvisionService().getForeignIdToNodeIdMap(foreignSource);
    }

    
    private OnmsDistPoller createDistPollerIfNecessary() {
        return getProvisionService().createDistPollerIfNecessary();
    
    }

    public CategoryDao getCategoryDao() {
        return getProvisionService().getCategoryDao();
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
