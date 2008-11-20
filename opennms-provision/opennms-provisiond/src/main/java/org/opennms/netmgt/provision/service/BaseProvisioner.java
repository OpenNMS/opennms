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
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.provision.service.operations.DeleteOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ImportOperationFactory;
import org.opennms.netmgt.provision.service.operations.ImportOperationsManager;
import org.opennms.netmgt.provision.service.operations.InsertOperation;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation;
import org.opennms.netmgt.provision.service.operations.UpdateOperation;
import org.opennms.netmgt.provision.service.specification.AbstractImportVisitor;
import org.opennms.netmgt.provision.service.specification.SpecFile;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class BaseProvisioner implements ImportOperationFactory, InitializingBean {

    private ProvisionService m_provisionService;
	private int m_scanThreads = 50;
	private int m_writeThreads = 4;
	
	public void setProvisionService(ProvisionService provisionService) {
	    m_provisionService = provisionService;
	}
	
	public ProvisionService getProvisionService() {
	    return m_provisionService;
	}

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getProvisionService(), "provisionService property must be set");
    }

    public SaveOrUpdateOperation createInsertOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        return new InsertOperation(foreignSource, foreignId, nodeLabel, building, city, getProvisionService());
        
    }

    public UpdateOperation createUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        return new UpdateOperation(nodeId, foreignSource, foreignId, nodeLabel, building, city, getProvisionService());
    }

    public ImportOperation createDeleteOperation(Integer nodeId, String foreignSource, String foreignId) {
        return new DeleteOperation(nodeId, foreignSource, foreignId, getProvisionService());
    }
    
    protected void importModelFromResource(Resource resource) throws Exception {
    	importModelFromResource(null, resource, new NoOpProvisionMonitor());
    }

    protected void importModelFromResource(String foreignSource, Resource resource, ProvisionMonitor monitor)
            throws Exception {
        doImport(resource, monitor, m_scanThreads, m_writeThreads, new ImportManager(),
                 foreignSource);
    }

    private void doImport(Resource resource, final ProvisionMonitor monitor,
            final int scanThreads, final int writeThreads,
            ImportManager importManager, final String foreignSource) throws Exception {
        
        importManager.getClass();
        
        ExecutorService executor = null;
        
        final Task<Resource, SpecFile> loader = new Task<Resource, SpecFile>(executor) {

            @Override
            public SpecFile execute(Resource resource) throws Exception {
                return loadSpecFile(resource, foreignSource, monitor);
            }
            
        };
        
        

        
        final Task<SpecFile, ImportOperationsManager> auditor = new Task<SpecFile, ImportOperationsManager>(executor) {

            @Override
            public ImportOperationsManager execute(SpecFile specFile) throws Exception {
                monitor.beginAuditNodes();
                ImportOperationsManager opsMgr = auditNodes(specFile, monitor);
                monitor.finishAuditNodes();
                return opsMgr;
            }
        };
        
        
        
        final Task<ImportOperationsManager, Void> persistor = new Task<ImportOperationsManager, Void>(executor) {

            @Override
            public Void execute(ImportOperationsManager opsMgr) throws Exception {
                opsMgr.persistOperations(writeThreads, scanThreads, monitor);
                return null;
            }
            
        };
        
        
        
        final Task<SpecFile, Void> relator = new Task<SpecFile, Void>(executor) {

            @Override
            public Void execute(SpecFile specFile) throws Exception {
                monitor.beginRelateNodes();
                
                relateNodes(specFile);
                
                monitor.finishRelateNodes();
                return null;
            }
            
        };
        
        
        final Task<Resource, Void> importer = new Task<Resource, Void>(executor) {

            @Override
            public Void execute(Resource resource) throws Exception {
                monitor.beginImporting();

                SpecFile specFile = loader.execute(resource);
                ImportOperationsManager opsMgr = auditor.execute(specFile);
                persistor.execute(opsMgr);
                relator.execute(specFile);
                
            
                monitor.finishImporting();
                return null;
            }
            
        };
        
        importer.execute(resource);

    }

    private ImportOperationsManager auditNodes(SpecFile specFile, ProvisionMonitor monitor) {
        
        getProvisionService().createDistPollerIfNecessary("localhost", "127.0.0.1");
        String foreignSource = specFile.getForeignSource();
        Map<String, Integer> foreignIdsToNodes = getProvisionService().getForeignIdToNodeIdMap(foreignSource);
        
        ImportOperationsManager opsMgr = new ImportOperationsManager(foreignIdsToNodes, this, getProvisionService());

        opsMgr.setForeignSource(foreignSource);
        
        opsMgr.auditNodes(specFile);

        return opsMgr;
    }

    private SpecFile loadSpecFile(Resource resource, String foreignSource,
            ProvisionMonitor monitor) throws ModelImportException,
            IOException {
        monitor.beginLoadingResource(resource);
    	
        SpecFile specFile = new SpecFile();
        specFile.loadResource(resource);
        
        if (foreignSource != null) {
            specFile.setForeignSource(foreignSource);
        }
        
        monitor.finishLoadingResource(resource);
        return specFile;
    }

    class NodeRelator extends AbstractImportVisitor {
		String m_foreignSource;
		
		public NodeRelator(String foreignSource) {
			m_foreignSource = foreignSource;
		}

        public void visitNode(final Node node) {
            getProvisionService().setNodeParentAndDependencies(m_foreignSource, node.getForeignId(), node.getParentForeignId(),
                                                node.getParentNodeLabel());

            getProvisionService().clearCache();
		}

	};

	private void relateNodes(SpecFile specFile) {
		specFile.visitImport(new NodeRelator(specFile.getForeignSource()));
	}

    public Category log() {
    	return ThreadCategory.getInstance(getClass());
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
