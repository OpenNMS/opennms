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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class BaseProvisioner implements InitializingBean {

    private List<Object> m_providers;
    private LifeCycleRepository m_lifeCycleRepository;
    private ProvisionService m_provisionService;
    private ScheduledExecutorService m_scheduledExecutor;
    private final Map<Integer, ScheduledFuture<?>> m_scheduledNodes = new HashMap<Integer, ScheduledFuture<?>>();
	
	public void setProvisionService(ProvisionService provisionService) {
	    m_provisionService = provisionService;
	}
	
	public ProvisionService getProvisionService() {
	    return m_provisionService;
	}
	
	public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
	    m_scheduledExecutor = scheduledExecutor;
	}

	public void setLifeCycleRepository(LifeCycleRepository lifeCycleRepository) {
	    m_lifeCycleRepository = lifeCycleRepository;
	}

	public void setProviders(List<Object> providers) {
	    m_providers = providers;
	}
	
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getProvisionService(), "provisionService property must be set");
        Assert.notNull(m_scheduledExecutor, "scheduledExecutor property must be set");
        Assert.notNull(m_lifeCycleRepository, "lifeCycleRepository property must be set");
    }
    
    protected void scheduleRescanForExistingNodes() {        
        List<NodeScanSchedule> schedules = m_provisionService.getScheduleForNodes();
        
        checkNodeListForRemovals(schedules);
        
        for(NodeScanSchedule schedule : schedules) {
            if(!m_scheduledNodes.containsKey(schedule.getNodeId())) {
                addToScheduleQueue(schedule);
            }else {
                updateNodeScheduleInQueue(schedule);
            }            
        }
        
    }

    protected Runnable nodeScanner(final NodeScanSchedule schedule) {
        return new Runnable() {
            public void run() {
                System.out.println(String.format("Gotta write the node scan code for node %s", schedule.getNodeId()));
            }
        };
    }
    
    //Helper functions for the schedule
    protected void addToScheduleQueue(NodeScanSchedule schedule) {
        m_scheduledNodes.put(schedule.getNodeId(), m_scheduledExecutor.scheduleWithFixedDelay(nodeScanner(schedule), schedule.getInitialDelay(), schedule.getScanInterval(), TimeUnit.MILLISECONDS));
    }
    
    protected void updateNodeScheduleInQueue(NodeScanSchedule schedule) {
        ScheduledFuture<?> scheduledFuture = m_scheduledNodes.get(schedule.getNodeId());
        
        if(!scheduledFuture.isDone() && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            scheduledFuture = m_scheduledExecutor.scheduleWithFixedDelay(nodeScanner(schedule), schedule.getInitialDelay(), schedule.getScanInterval(), TimeUnit.MILLISECONDS);
        }
    }
    
    protected void removeNodeFromScheduleQueue(Integer nodeId) {
        ScheduledFuture<?> scheduledFuture = m_scheduledNodes.remove(nodeId);
        
        if(scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
        
    }
    
    protected void removeFromScheduleQueue(List<Integer> nodeIds) {
        for(Integer nodeId : nodeIds) {
            removeNodeFromScheduleQueue(nodeId);
        }
    }
    
    /**
     * @param schedules
     */
    protected void checkNodeListForRemovals(List<NodeScanSchedule> schedules) {
        Set<Integer> keySet = m_scheduledNodes.keySet();
        List<Integer> markedForDelete = new ArrayList<Integer>(); 
        
        for(int nodeId : keySet) {
            boolean isDirty = false;
            
            for(NodeScanSchedule schedule : schedules) {
                if(schedule.getNodeId() == nodeId) {
                    isDirty = true;
                }
            }
            
            if(!isDirty) {
                markedForDelete.add(nodeId);
            }
        }
        
        removeFromScheduleQueue(markedForDelete);
    }
   

    public int getScheduleLength() {
        return m_scheduledNodes.size();
    }
    //^ Helper functions for the schedule
    
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
        
        LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_providers.toArray());
        doImport.setAttribute("resource", resource);
        doImport.setAttribute("foreignSource", foreignSource);
        
        doImport.trigger();
        
        doImport.waitFor();
    }

    public Category log() {
    	return ThreadCategory.getInstance(getClass());
	}

}
