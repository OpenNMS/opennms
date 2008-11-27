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

import java.util.concurrent.Executors;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.lifecycle.DefaultLifeCycleRepository;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycle;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class BaseProvisioner implements InitializingBean {

    private CoreImportActivities m_provider;
    private LifeCycleRepository m_lifeCycleRepository;
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
        
        DefaultLifeCycleRepository lifeCycleRepository = new DefaultLifeCycleRepository(Executors.newFixedThreadPool(m_scanThreads+m_writeThreads));
        
        LifeCycle importLifeCycle = new LifeCycle("import")
            .addPhase("validate")
            .addPhase("audit")
            .addPhase("scan")
            .addPhase("delete")
            .addPhase("update")
            .addPhase("insert")
            .addPhase("relate");
            
            
        lifeCycleRepository.addLifeCycle(importLifeCycle);
        
        LifeCycle nodeScanLifeCycle = new LifeCycle("nodeScan")
            .addPhase("scan")
            .addPhase("persist");
        

        lifeCycleRepository.addLifeCycle(nodeScanLifeCycle);
        
        m_lifeCycleRepository = lifeCycleRepository;
        
        
        m_provider = new CoreImportActivities(getProvisionService());
        
        
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
        
        LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_provider);
        doImport.setAttribute("resource", resource);
        doImport.setAttribute("foreignSoruce", foreignSource);
        
        doImport.trigger();
        
        doImport.waitFor();

//        SpecFile specFile = m_provider.loadSpecFile(resource, foreignSource);
//        ImportOperationsManager opsMgr = m_provider.auditNodes(specFile);
//        opsMgr.persistOperations(doImport);
//        relateNodes(specFile);

            

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
