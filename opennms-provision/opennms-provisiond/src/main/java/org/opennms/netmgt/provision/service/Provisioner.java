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
// 2008 Mar 20: Remove println. - dj@opennms.org
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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.service.dns.DnsUrlFactory;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

/**
 * Massively Parallel Java Provisioning <code>ServiceDaemon</code> for OpenNMS.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
@EventListener(name="Provisiond:EventListener")
public class Provisioner implements SpringServiceDaemon {
    
    public static final String NAME = "Provisiond";

    private DefaultTaskCoordinator m_taskCoordinator;
    private CoreImportActivities m_importActivities;
    private LifeCycleRepository m_lifeCycleRepository;
    private ProvisionService m_provisionService;
    private ScheduledExecutorService m_scheduledExecutor;
    private final Map<Integer, ScheduledFuture<?>> m_scheduledNodes = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();
    private volatile EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    
    private volatile TimeTrackingMonitor m_stats;
    
    @Autowired
    private ProvisioningAdapterManager m_manager;
    
    private ImportScheduler m_importSchedule;

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

	public void setImportSchedule(ImportScheduler schedule) {
        m_importSchedule = schedule;
    }

    /**
     * @param importActivities the importActivities to set
     */
    public void setImportActivities(CoreImportActivities importActivities) {
        m_importActivities = importActivities;
    }

    /**
     * @param taskCoordinator the taskCoordinator to set
     */
    public void setTaskCoordinator(DefaultTaskCoordinator taskCoordinator) {
        m_taskCoordinator = taskCoordinator;
    }
    


    /**
     * @param agentConfigFactory the agentConfigFactory to set
     */
    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }

    public ImportScheduler getImportSchedule() {
        return m_importSchedule;
    }


	
    public void start() throws Exception {
        m_manager.initializeAdapters();
        scheduleRescanForExistingNodes();
        m_importSchedule.start();
    }
	
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getProvisionService(), "provisionService property must be set");
        Assert.notNull(m_scheduledExecutor, "scheduledExecutor property must be set");
        Assert.notNull(m_lifeCycleRepository, "lifeCycleRepository property must be set");
        Assert.notNull(m_importActivities, "importActivities property must be set");
        Assert.notNull(m_taskCoordinator, "taskCoordinator property must be set");
        Assert.notNull(m_agentConfigFactory, "agentConfigFactory property must be set");
        

        //since this class depends on the Import Schedule, the UrlFactory should already
        //be registered in the URL class.. but, just in-case...
        try {
            new URL("dns://localhost/localhost");
        } catch (MalformedURLException e) {
            URL.setURLStreamHandlerFactory(new DnsUrlFactory());
        }
            
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
    
    public NodeScan createNodeScan(Integer nodeId, String foreignSource, String foreignId) {
        log().info("createNodeScan called");
        return new NodeScan(nodeId, foreignSource, foreignId, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator);
    }

    public NewSuspectScan createNewSuspectScan(InetAddress ipAddress) {
        log().info("createNewSuspectScan called");
        return new NewSuspectScan(ipAddress, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator);
    }

    //Helper functions for the schedule
    protected void addToScheduleQueue(NodeScanSchedule schedule) {
        ScheduledFuture<?> future = scheduleNodeScan(schedule);
        log().warn("addToScheduleQueue future = " + future);
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
        NodeScan nodeScan = createNodeScan(schedule.getNodeId(), schedule.getForeignSource(), schedule.getForeignId());
        log().warn("nodeScan = " + nodeScan);
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

    //FIXME? ImportManager is not used.
    private void doImport(Resource resource, final ProvisionMonitor monitor,
            ImportManager importManager) throws Exception {
        
        LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_importActivities);
        doImport.setAttribute("resource", resource);
        
        doImport.trigger();
        
        doImport.waitFor();
    }

    public ThreadCategory log() {
    	return ThreadCategory.getInstance(getClass());
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void doImport() {
        Event e = null;
        doImport(e);
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
        String url = getEventUrl(event);
        
        if (url != null) {
            doImport(url);
        } else {
            String msg = "reloadImport event requires 'url' paramter";
            log().error("doImport: "+msg);
            send(importFailedEvent(msg, url));
        }
        
    }

    public void doImport(String url) {
        
        try {
            
            log().info("doImport: importing from url: "+url+"...");
            
            Resource resource = new UrlResource(url);
            
            m_stats = new TimeTrackingMonitor();
            
            send(importStartedEvent(resource));
    
            importModelFromResource(resource, m_stats);
    
            log().info("Finished Importing: "+m_stats);
    
            send(importSuccessEvent(m_stats, url));
    
        } catch (Exception e) {
            String msg = "Exception importing "+url;
            log().error(msg, e);
            send(importFailedEvent((msg+": "+e.getMessage()), url));
        }
    }


    /**
     * @param e
     */
    @EventHandler(uei = EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAddedEvent(Event e) {
        NodeScanSchedule scheduleForNode = null;
        log().warn("node added event (" + System.currentTimeMillis() + ")");
        try {
            /* we don't force a scan on node added so new suspect doesn't cause 2 simultaneous node scans
             * New nodes that are created another way shouldn't have a 'lastCapsPoll' timestamp set 
             */ 
            scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue(), false);
        } catch (Throwable t) {
            log().error("getScheduleForNode fails", t);
        }
        log().warn("scheduleForNode is " + scheduleForNode);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }

    }
    
    @EventHandler(uei = EventConstants.FORCE_RESCAN_EVENT_UEI)
    public void handleForceRescan(Event e) {
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue(), true);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }

    }
    
    @EventHandler(uei = EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
    public void handleNewSuspectEvent(Event e) {
        
        final String uei = e.getUei();
        final String ip = e.getInterface();

        if (ip == null) {
            log().error("Received a "+uei+" event with a null ipAddress");
        }

        if (!getProvisionService().isDiscoveryEnabled()) {
            log().info("Ignoring "+uei+" event for ip "+ip+" since discovery handling is disabled in provisiond");
        }
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    InetAddress addr = InetAddress.getByName(ip);
                    NewSuspectScan scan = createNewSuspectScan(addr);
                    Task t = scan.createTask();
                    t.schedule();
                    t.waitFor();
                } catch (UnknownHostException ex) {
                    log().error("Unable to convert address "+ip+" from "+uei+" event to InetAddress", ex);
                } catch (InterruptedException ex) {
                    log().error("Task interrupted waiting for new suspect scan of "+ip+" to finish", ex);
                } catch (ExecutionException ex) {
                    log().error("An expected execution occurred waiting for new suspect scan of "+ip+" to finish", ex);
                }
                
            }
        };

        m_scheduledExecutor.execute(r);
        
    }
    
    @EventHandler(uei = EventConstants.NODE_UPDATED_EVENT_UEI)
    public void handleNodeUpdated(Event e) {
        // scan now since a reimport has occurred
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue(), true);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }
        
    }

    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(Event e) {
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        
    }
    
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {
        
        if (isReloadConfigEventTarget(e)) {
            log().info("handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            try {
                log().debug("handleReloadConfigEvent: lock acquired, unscheduling current reports...");
                
                m_importSchedule.rebuildImportSchedule();
                
                log().debug("handleRelodConfigEvent: reports rescheduled.");
                
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Provisiond");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Statsd");
                
            } catch (Exception exception) {
                
                log().error("handleReloadConfigurationEvent: Error reloading configuration:"+exception, exception);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Provisiond");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond");
                ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
                
            }
            
            if (ebldr != null) {
                getEventForwarder().sendNow(ebldr.getEvent());
            }
            log().info("handleReloadConfigEvent: configuration reloaded.");
        }
        
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParms().getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Provisiond".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: Provisiond was target of reload event: "+isTarget);
        return isTarget;
    }
    
    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleAddInterface(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doAddInterface(event.getNodeid(), event.getInterface());
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doAddInterface(long nodeId, String ipAddr) {
        throw new UnsupportedOperationException("Provisioner.doAddInterface is not yet implemented");
    }

    @EventHandler(uei=EventConstants.ADD_NODE_EVENT_UEI)
    public void handleAddNode(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doAddNode(event.getNodeid());
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doAddNode(long nodeId) {
        throw new UnsupportedOperationException("Provisioner.doAddNode is not yet implemented");
    }

    @EventHandler(uei=EventConstants.CHANGE_SERVICE_EVENT_UEI)
    public void handleChangeService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doChangeService(event.getInterface(), event.getService(), EventUtils.getParm(event, EventConstants.PARM_ACTION));
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doChangeService(String ipAddr, String service, String action) {
        throw new UnsupportedOperationException("Provisioner.doChangeService is not yet implemented");
    }

    @EventHandler(uei=EventConstants.DELETE_INTERFACE_EVENT_UEI)
    public void handleDeleteInterface(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doDeleteInterface(event.getNodeid(), event.getInterface());
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doDeleteInterface(long nodeId, String ipAddr) {
        m_provisionService.deleteInterface((int)nodeId, ipAddr);
    }

    @EventHandler(uei=EventConstants.DELETE_NODE_EVENT_UEI)
    public void handleDeleteNode(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doDeleteNode(event.getNodeid());
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doDeleteNode(long nodeId) {
        m_provisionService.deleteNode((int)nodeId);
    }

    @EventHandler(uei=EventConstants.DELETE_SERVICE_EVENT_UEI)
    public void handleDeleteService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doDeleteService(event.getNodeid(), event.getInterface(), event.getService());
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doDeleteService(long nodeId, String ipAddr, String service) {
        m_provisionService.deleteService((int)nodeId, ipAddr, service);
    }

    @EventHandler(uei=EventConstants.UPDATE_SERVER_EVENT_UEI)
    public void handleUpdateServer(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doUpdateServer(event.getInterface(), event.getHost(), 
                        EventUtils.getParm(event, EventConstants.PARM_ACTION),
                        EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doUpdateServer(String ipAddr, String host, String action, String nodeLabel) {
        throw new UnsupportedOperationException("Provisioner.doUpdateServer is not yet implemented");
    }

    @EventHandler(uei=EventConstants.UPDATE_SERVICE_EVENT_UEI)
    public void handleUpdateService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doUpdateService(event.getInterface(), event.getService(), 
                        EventUtils.getParm(event, EventConstants.PARM_ACTION),
                        EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Exception e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    
    private void doUpdateService(String ipAddr, String service, String action, String nodeLabel) {
        throw new UnsupportedOperationException("Provisioner.doUpdateService is not yet implemented");
    }

    private String getEventUrl(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_URL);
    }

    public String getStats() { return (m_stats == null ? "No Stats Availabile" : m_stats.toString()); }

    private Event importSuccessEvent(TimeTrackingMonitor stats, String url) {
    
        return new EventBuilder( EventConstants.IMPORT_SUCCESSFUL_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, url)
            .addParam( EventConstants.PARM_IMPORT_STATS, stats.toString() )
            .getEvent();
    }

    private void send(Event event) {
        getEventForwarder().sendNow(event);
    }

    private Event importFailedEvent(String msg, String url) {
    
        return new EventBuilder( EventConstants.IMPORT_FAILED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, url)
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
