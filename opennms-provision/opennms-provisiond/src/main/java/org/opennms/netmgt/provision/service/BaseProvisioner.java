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

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.lifecycle.DefaultLifeCycleRepository;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycle;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.tasks.DefaultTaskCoordinator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class BaseProvisioner implements InitializingBean {

    private CoreImportActivities m_provider;
    private LifeCycleRepository m_lifeCycleRepository;
    private ProvisionService m_provisionService;
    private Executor m_scanExecutor;
    private Executor m_writeExecutor;
    private ScheduledExecutorService m_scheduledExecutor;
	
	public void setProvisionService(ProvisionService provisionService) {
	    m_provisionService = provisionService;
	}
	
	public ProvisionService getProvisionService() {
	    return m_provisionService;
	}
	
	public void setScanExecutor(Executor scanExecutor) {
	    m_scanExecutor = scanExecutor;
	}
	
	public void setWriteExecutor(Executor writeExecutor) {
	    m_writeExecutor = writeExecutor;
	}
	
	public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
	    m_scheduledExecutor = scheduledExecutor;
	}

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getProvisionService(), "provisionService property must be set");
        Assert.notNull(m_scanExecutor, "scanExecutor property must be set");
        Assert.notNull(m_writeExecutor, "writeExecutor property must be set");
        Assert.notNull(m_scheduledExecutor, "scheduledExecutor property must be set");
        
        DefaultTaskCoordinator coordinator = new DefaultTaskCoordinator();
        coordinator.setDefaultExecutor("scan");
        coordinator.addExecutor("scan", m_scanExecutor);
        coordinator.addExecutor("write", m_writeExecutor);
        coordinator.afterPropertiesSet();
        
        DefaultLifeCycleRepository lifeCycleRepository = new DefaultLifeCycleRepository(coordinator);
        
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
    
    protected void scheduleRescanForExistingNodes() {
        List<NodeScanSchedule> schedules = m_provisionService.getScheduleForNodes();
        
        System.err.println("Schedules has size "+schedules.size());
        
        for(NodeScanSchedule schedule : schedules) {
            m_scheduledExecutor.scheduleWithFixedDelay(nodeScanner(schedule), schedule.getInitialDelay(), schedule.getScanInterval(), TimeUnit.MILLISECONDS);
        }
        
        
    }
    
    protected Runnable nodeScanner(final NodeScanSchedule schedule) {
        return new Runnable() {
            public void run() {
                System.out.println(String.format("Gotta write the node scan code for node %s", schedule.getNodeId()));
            }
        };
    }

    protected void importModelFromResource(Resource resource) throws Exception {
    	importModelFromResource(null, resource, new NoOpProvisionMonitor());
    }

    protected void importModelFromResource(String foreignSource, Resource resource, ProvisionMonitor monitor)
            throws Exception {
        doImport(resource, monitor, new ImportManager(), foreignSource);
    }

    private void doImport(Resource resource, final ProvisionMonitor monitor,
            ImportManager importManager, final String foreignSource) throws Exception {
        
        importManager.getClass();
        
        LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_provider);
        doImport.setAttribute("resource", resource);
        doImport.setAttribute("foreignSource", foreignSource);
        
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

}
