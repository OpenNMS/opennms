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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

@EventListener(name="Provisiond:EventListener")
public class Provisioner implements SpringServiceDaemon {
    
    public static final String NAME = "Provisioner";

    private List<Object> m_providers;
    private LifeCycleRepository m_lifeCycleRepository;
    private ProvisionService m_provisionService;
    private ScheduledExecutorService m_scheduledExecutor;
    private final Map<Integer, ScheduledFuture<?>> m_scheduledNodes = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();
    private volatile Resource m_importResource;
    private volatile EventSubscriptionService m_eventSubscriptionService;
    private volatile EventForwarder m_eventForwarder;
    
    private volatile TimeTrackingMonitor m_stats;

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
	
    public void start() throws Exception {
        scheduleRescanForExistingNodes();
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
    
    public void doNodeScan(int nodeId) throws InterruptedException, ExecutionException {
    }
    
    public NodeScan createNodeScan(String foreignSource, String foreignId) {
        return new NodeScan(foreignSource, foreignId, m_provisionService, m_lifeCycleRepository, m_providers);
    }

    //Helper functions for the schedule
    protected void addToScheduleQueue(NodeScanSchedule schedule) {
        ScheduledFuture<?> future = scheduleNodeScan(schedule);
        m_scheduledNodes.put(schedule.getNodeId(), future);
    }
    

    protected void updateNodeScheduleInQueue(NodeScanSchedule schedule) {
        ScheduledFuture<?> scheduledFuture = getScheduledFutureForNode(schedule.getNodeId());
        
        if(!scheduledFuture.isDone() && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            scheduledFuture = scheduleNodeScan(schedule);
            m_scheduledNodes.put(schedule.getNodeId(), scheduledFuture);
        }
    }

    private ScheduledFuture<?> scheduleNodeScan(NodeScanSchedule schedule) {
        NodeScan nodeScan = createNodeScan(schedule.getForeignSource(), schedule.getForeignId());
        return nodeScan.schedule(m_scheduledExecutor, schedule);
    }

    public ScheduledFuture<?> getScheduledFutureForNode(int nodeId) {
        ScheduledFuture<?> scheduledFuture = m_scheduledNodes.get(nodeId);
        return scheduledFuture;
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
    	importModelFromResource(resource, new NoOpProvisionMonitor());
    }

    protected void importModelFromResource(Resource resource, ProvisionMonitor monitor)
            throws Exception {
        doImport(resource, monitor, new ImportManager());
    }

    private void doImport(Resource resource, final ProvisionMonitor monitor,
            ImportManager importManager) throws Exception {
        
        LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_providers.toArray());
        doImport.setAttribute("resource", resource);
        
        doImport.trigger();
        
        doImport.waitFor();
    }

    public Category log() {
    	return ThreadCategory.getInstance(getClass());
	}

    public void setImportResource(Resource resource) {
        m_importResource = resource;
    }

    public Resource getImportResource() {
        return m_importResource;
    }

    public EventSubscriptionService getEventSubscriptionService() {
        return m_eventSubscriptionService;
    }

    public void setEventSubscriptionService(EventSubscriptionService eventManager) {
    	m_eventSubscriptionService = eventManager;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void doImport() {
        doImport(null);
    }

    /**
     * Begins importing from resource specified in model-importer.properties file or
     * in event parameter: url.  Import Resources are managed with a "key" called 
     * "foreignSource" specified in the XML retrieved by the resource and can be overridden 
     * as a parameter of an event.
     * @param event
     */
    @EventHandler(uei = EventConstants.RELOAD_IMPORT_UEI)
    public void doImport(Event event) {
        Resource resource = null;
        try {
            m_stats = new TimeTrackingMonitor();
            
            resource = ((event != null && getEventUrl(event) != null) ? new UrlResource(getEventUrl(event)) : getImportResource()); 
    
            send(importStartedEvent(resource));
    
    		importModelFromResource(resource, m_stats);
    
    		log().info("Finished Importing: "+m_stats);
    
    		send(importSuccessEvent(m_stats, resource));
    
        } catch (Exception e) {
            String msg = "Exception importing "+resource;
    		log().error(msg, e);
            send(importFailedEvent((msg+": "+e.getMessage()), resource));
        }
    }

    /**
     * @param e
     */
    @EventHandler(uei = EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAddedEvent(Event e) {
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue());
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }

    }
    
    @EventHandler(uei = EventConstants.NODE_UPDATED_EVENT_UEI)
    public void handleNodeUpdated(Event e) {
        
        //TODO Handle scheduling
    }

    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(Event e) {
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        
    }

    private String getEventUrl(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_URL);
    }

    public String getStats() { return (m_stats == null ? "No Stats Availabile" : m_stats.toString()); }

    private Event importSuccessEvent(TimeTrackingMonitor stats, Resource resource) {
    
        return new EventBuilder( EventConstants.IMPORT_SUCCESSFUL_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .addParam( EventConstants.PARM_IMPORT_STATS, stats.toString() )
            .getEvent();
    }

    private void send(Event event) {
        getEventForwarder().sendNow(event);
    }

    private Event importFailedEvent(String msg, Resource resource) {
    
        return new EventBuilder( EventConstants.IMPORT_FAILED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .addParam( EventConstants.PARM_FAILURE_MESSAGE, msg )
            .getEvent();
    }

    private Event importStartedEvent(Resource resource) {
    
        return new EventBuilder( EventConstants.IMPORT_STARTED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .getEvent();
    }

    protected String getEventForeignSource(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_FOREIGN_SOURCE);
    }

}
