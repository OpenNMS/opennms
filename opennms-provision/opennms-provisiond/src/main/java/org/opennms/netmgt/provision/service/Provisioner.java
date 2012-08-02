/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpAgentConfigFactory;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.NoOpProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.RequisitionImport;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Massively Parallel Java Provisioning <code>ServiceDaemon</code> for OpenNMS.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@EventListener(name="Provisiond:EventListener")
public class Provisioner implements SpringServiceDaemon {
    
    /** Constant <code>NAME="Provisiond"</code> */
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

    /**
     * <p>setProvisionService</p>
     *
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public void setProvisionService(ProvisionService provisionService) {
	    m_provisionService = provisionService;
	}
	
	/**
	 * <p>getProvisionService</p>
	 *
	 * @return a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
	 */
	public ProvisionService getProvisionService() {
	    return m_provisionService;
	}
	
	/**
	 * <p>setScheduledExecutor</p>
	 *
	 * @param scheduledExecutor a {@link java.util.concurrent.ScheduledExecutorService} object.
	 */
	public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
	    m_scheduledExecutor = scheduledExecutor;
	}

    /**
     * <p>setLifeCycleRepository</p>
     *
     * @param lifeCycleRepository a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository} object.
     */
    public void setLifeCycleRepository(LifeCycleRepository lifeCycleRepository) {
	    m_lifeCycleRepository = lifeCycleRepository;
	}

	/**
	 * <p>setImportSchedule</p>
	 *
	 * @param schedule a {@link org.opennms.netmgt.provision.service.ImportScheduler} object.
	 */
	public void setImportSchedule(ImportScheduler schedule) {
        m_importSchedule = schedule;
    }

    /**
     * <p>setImportActivities</p>
     *
     * @param importActivities the importActivities to set
     */
    public void setImportActivities(CoreImportActivities importActivities) {
        m_importActivities = importActivities;
    }

    /**
     * <p>setTaskCoordinator</p>
     *
     * @param taskCoordinator the taskCoordinator to set
     */
    public void setTaskCoordinator(DefaultTaskCoordinator taskCoordinator) {
        m_taskCoordinator = taskCoordinator;
    }
    


    /**
     * <p>setAgentConfigFactory</p>
     *
     * @param agentConfigFactory the agentConfigFactory to set
     */
    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }

    /**
     * <p>getImportSchedule</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.ImportScheduler} object.
     */
    public ImportScheduler getImportSchedule() {
        return m_importSchedule;
    }


	
    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void start() throws Exception {
        m_manager.initializeAdapters();
        scheduleRescanForExistingNodes();
        m_importSchedule.start();
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() throws Exception {
        m_importSchedule.stop();
        m_scheduledExecutor.shutdown();
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        GenericURLFactory.initialize();
    }
    
    /**
     * <p>scheduleRescanForExistingNodes</p>
     */
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
    
    /**
     * <p>doNodeScan</p>
     *
     * @param nodeId a int.
     * @throws java.lang.InterruptedException if any.
     * @throws java.util.concurrent.ExecutionException if any.
     */
    public void doNodeScan(int nodeId) throws InterruptedException, ExecutionException {
    }
    
    /**
     * <p>createNodeScan</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.NodeScan} object.
     */
    public NodeScan createNodeScan(Integer nodeId, String foreignSource, String foreignId) {
        log().info("createNodeScan called");
        return new NodeScan(nodeId, foreignSource, foreignId, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator);
    }

    /**
     * <p>createNewSuspectScan</p>
     *
     * @param ipAddress a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.provision.service.NewSuspectScan} object.
     */
    public NewSuspectScan createNewSuspectScan(InetAddress ipAddress) {
        log().info("createNewSuspectScan called");
        return new NewSuspectScan(ipAddress, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator);
    }

    //Helper functions for the schedule
    /**
     * <p>addToScheduleQueue</p>
     *
     * @param schedule a {@link org.opennms.netmgt.provision.service.NodeScanSchedule} object.
     */
    protected void addToScheduleQueue(NodeScanSchedule schedule) {
        ScheduledFuture<?> future = scheduleNodeScan(schedule);
        log().warn("addToScheduleQueue future = " + future);
        m_scheduledNodes.put(schedule.getNodeId(), future);
    }
    

    /**
     * <p>updateNodeScheduleInQueue</p>
     *
     * @param schedule a {@link org.opennms.netmgt.provision.service.NodeScanSchedule} object.
     */
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

    /**
     * <p>getScheduledFutureForNode</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.concurrent.ScheduledFuture} object.
     */
    public ScheduledFuture<?> getScheduledFutureForNode(int nodeId) {
        ScheduledFuture<?> scheduledFuture = m_scheduledNodes.get(nodeId);
        return scheduledFuture;
    }
    
    /**
     * <p>removeNodeFromScheduleQueue</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     */
    protected void removeNodeFromScheduleQueue(Integer nodeId) {
        ScheduledFuture<?> scheduledFuture = m_scheduledNodes.remove(nodeId);
        
        if(scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
        
    }
    
    /**
     * <p>removeFromScheduleQueue</p>
     *
     * @param nodeIds a {@link java.util.List} object.
     */
    protected void removeFromScheduleQueue(List<Integer> nodeIds) {
        for(Integer nodeId : nodeIds) {
            removeNodeFromScheduleQueue(nodeId);
        }
    }
    
    /**
     * <p>checkNodeListForRemovals</p>
     *
     * @param schedules a {@link java.util.List} object.
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
   

    /**
     * <p>getScheduleLength</p>
     *
     * @return a int.
     */
    public int getScheduleLength() {
        return m_scheduledNodes.size();
    }
    //^ Helper functions for the schedule
    
    /**
     * <p>importModelFromResource</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @param rescanExisting TODO
     * @throws java.lang.Exception if any.
     */
    protected void importModelFromResource(final Resource resource, final Boolean rescanExisting) throws Exception {
    	importModelFromResource(resource, rescanExisting, new NoOpProvisionMonitor());
    }

    /**
     * <p>importModelFromResource</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @param rescanExisting TODO
     * @param monitor a {@link org.opennms.netmgt.provision.service.operations.ProvisionMonitor} object.
     * @throws java.lang.Exception if any.
     */
    protected void importModelFromResource(final Resource resource, final Boolean rescanExisting, final ProvisionMonitor monitor) throws Exception {
        final LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_importActivities);
        doImport.setAttribute("resource", resource);
        doImport.setAttribute("rescanExisting", Boolean.valueOf(rescanExisting));
        doImport.trigger();
        doImport.waitFor();
        final RequisitionImport ri = doImport.findAttributeByType(RequisitionImport.class);
        if (ri.isAborted()) {
            throw new ModelImportException("Import failed for resource " + resource.toString(), ri.getError());
        }
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
    	return ThreadCategory.getInstance(getClass());
	}

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>doImport</p>
     */
    public void doImport() {
        Event e = null;
        doImport(e);
    }
    
    /**
     * Begins importing from resource specified in model-importer.properties file or
     * in event parameter: url.  Import Resources are managed with a "key" called
     * "foreignSource" specified in the XML retrieved by the resource and can be overridden
     * as a parameter of an event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_IMPORT_UEI)
    public void doImport(final Event event) {
        final String url = getEventUrl(event);
        final boolean rescanExistingOnImport = getEventRescanExistingOnImport(event);

        if (url != null) {
            doImport(url, rescanExistingOnImport);
        } else {
            final String msg = "reloadImport event requires 'url' parameter";
            log().error("doImport: " + msg);
            send(importFailedEvent(msg, url));
        }
        
    }

    /**
     * <p>doImport</p>
     *
     * @param url a {@link java.lang.String} object.
     * @param rescanExisting TODO
     */
    public void doImport(final String url, final boolean rescanExisting) {
        
        try {
            
            log().info("doImport: importing from url: "+url+"...");
            
            Resource resource = new UrlResource(url);
            
            m_stats = new TimeTrackingMonitor();
            
            send(importStartedEvent(resource));
    
            importModelFromResource(resource, rescanExisting, m_stats);
    
            log().info("Finished Importing: "+m_stats);
    
            send(importSuccessEvent(m_stats, url));
    
        } catch (final Throwable t) {
            final String msg = "Exception importing "+url;
            log().error(msg, t);
            send(importFailedEvent((msg+": "+t.getMessage()), url));
        }
    }


    /**
     * <p>handleNodeAddedEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
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
    
    /**
     * <p>handleForceRescan</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.FORCE_RESCAN_EVENT_UEI)
    public void handleForceRescan(Event e) {
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue(), true);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }

    }
    
    /**
     * <p>handleNewSuspectEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
    public void handleNewSuspectEvent(Event e) {
        
        final String uei = e.getUei();
        final String ip = e.getInterface();

        if (ip == null) {
            log().error("Received a "+uei+" event with a null ipAddress");
            return;
        }

        if (!getProvisionService().isDiscoveryEnabled()) {
            log().info("Ignoring "+uei+" event for ip "+ip+" since discovery handling is disabled in provisiond");
            return;
        }
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    InetAddress addr = InetAddressUtils.addr(ip);
                    if (addr == null) {
                    	log().error("Unable to convert " + ip + " to an InetAddress.");
                    	return;
                    }
                    NewSuspectScan scan = createNewSuspectScan(addr);
                    Task t = scan.createTask();
                    t.schedule();
                    t.waitFor();
                } catch (InterruptedException ex) {
                    log().error("Task interrupted waiting for new suspect scan of "+ip+" to finish", ex);
                } catch (ExecutionException ex) {
                    log().error("An expected execution occurred waiting for new suspect scan of "+ip+" to finish", ex);
                }
                
            }
        };

        m_scheduledExecutor.execute(r);
        
    }
    
    /**
     * <p>handleNodeUpdated</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.NODE_UPDATED_EVENT_UEI)
    public void handleNodeUpdated(Event e) {
        // scan now since a reimport has occurred
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(new Long(e.getNodeid()).intValue(), true);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }
        
    }

    /**
     * <p>handleNodeDeletedEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(Event e) {
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        
    }
    
    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
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
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond");
                
            } catch (Throwable exception) {
                
                log().error("handleReloadConfigurationEvent: Error reloading configuration:"+exception, exception);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Provisiond");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond");
                ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
                
            }
            
            if (ebldr != null) {
                m_eventForwarder.sendNow(ebldr.getEvent());
            }
            log().info("handleReloadConfigEvent: configuration reloaded.");
        }
        
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParmCollection();
        

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Provisiond".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: Provisiond was target of reload event: "+isTarget);
        return isTarget;
    }

    /**
     * <p>handleAddInterface</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleAddInterface(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doAddInterface(event.getNodeid(), event.getInterface());
            } catch (Throwable e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doAddInterface(long nodeId, String ipAddr) {
        // FIXME: Handle Rackspace ADD_INTERFACE event
        throw new UnsupportedOperationException("Provisioner.doAddInterface is not yet implemented");
    }

    /**
     * <p>handleAddNode</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.ADD_NODE_EVENT_UEI)
    public void handleAddNode(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doAddNode(event.getInterface(), EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Throwable e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doAddNode(String ipAddr, String nodeLabel) {

        OnmsNode node = new OnmsNode();
        node.setLabel(nodeLabel);
        
        OnmsIpInterface iface = new OnmsIpInterface(ipAddr, node);
        iface.setIsManaged("M");
        iface.setPrimaryString("N");
        
        m_provisionService.insertNode(node);
        
    }

    /**
     * <p>handleChangeService</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.CHANGE_SERVICE_EVENT_UEI)
    public void handleChangeService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doChangeService(event.getInterface(), event.getService(), EventUtils.getParm(event, EventConstants.PARM_ACTION));
            } catch (Throwable e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doChangeService(String ipAddr, String service, String action) {
        // FIXME: Handle Rackspace CHANGE_SERVICE event
        throw new UnsupportedOperationException("Provisioner.doChangeService is not yet implemented");
    }

    /**
     * <p>handleDeleteInterface</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DELETE_INTERFACE_EVENT_UEI)
    public void handleDeleteInterface(Event event) {
        try {
            doDeleteInterface(event.getNodeid(), event.getInterface());
        } catch (Throwable e) {
            log().error("Unexpected exception processing event: " + event.getUei(), e);
        }
    }
    
    private void doDeleteInterface(long nodeId, String ipAddr) {
        m_provisionService.deleteInterface((int)nodeId, ipAddr);
    }

    /**
     * <p>handleDeleteNode</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DELETE_NODE_EVENT_UEI)
    public void handleDeleteNode(Event event) {
        try {
            doDeleteNode(event.getNodeid());
        } catch (Throwable e) {
            log().error("Unexpected exception processing event: " + event.getUei(), e);
        }
    }
    
    private void doDeleteNode(long nodeId) {
        m_provisionService.deleteNode((int)nodeId);
    }

    /**
     * <p>handleDeleteService</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DELETE_SERVICE_EVENT_UEI)
    public void handleDeleteService(Event event) {
        try {
	    doDeleteService(event.getNodeid(), event.getInterfaceAddress() == null ? null : event.getInterfaceAddress(), event.getService());
        } catch (Throwable e) {
            log().error("Unexpected exception processing event: " + event.getUei(), e);
        }
    }
    
    private void doDeleteService(long nodeId, InetAddress addr, String service) {
        m_provisionService.deleteService((int)nodeId, addr, service);
    }

    /**
     * <p>handleUpdateServer</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.UPDATE_SERVER_EVENT_UEI)
    public void handleUpdateServer(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doUpdateServer(event.getInterface(), event.getHost(), 
                        EventUtils.getParm(event, EventConstants.PARM_ACTION),
                        EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Throwable e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    private void doUpdateServer(String ipAddr, String host, String action, String nodeLabel) {
        // FIXME: Handle Rackspace UPDATE_SERVER event
        throw new UnsupportedOperationException("Provisioner.doUpdateServer is not yet implemented");
    }

    /**
     * <p>handleUpdateService</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.UPDATE_SERVICE_EVENT_UEI)
    public void handleUpdateService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doUpdateService(event.getInterface(), event.getService(), 
                        EventUtils.getParm(event, EventConstants.PARM_ACTION),
                        EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Throwable e) {
                log().error("Unexpected exception processing event: " + event.getUei(), e);
            }
        }
    }
    
    
    private void doUpdateService(String ipAddr, String service, String action, String nodeLabel) {
        // FIXME: Handle Rackspace UPDATE_SERVICE event
        throw new UnsupportedOperationException("Provisioner.doUpdateService is not yet implemented");
    }

    private String getEventUrl(Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_URL);
    }

    private boolean getEventRescanExistingOnImport(final Event event) {
        final String rescanExisting = EventUtils.getParm(event, EventConstants.PARM_IMPORT_RESCAN_EXISTING);
        if (rescanExisting == null) return true;
        return Boolean.parseBoolean(rescanExisting);
    }
    
    /**
     * <p>getStats</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStats() { return (m_stats == null ? "No Stats Availabile" : m_stats.toString()); }

    private Event importSuccessEvent(final TimeTrackingMonitor stats, final String url) {
    
        return new EventBuilder( EventConstants.IMPORT_SUCCESSFUL_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, url)
            .addParam( EventConstants.PARM_IMPORT_STATS, stats.toString() )
            .getEvent();
    }

    private void send(final Event event) {
        m_eventForwarder.sendNow(event);
    }

    private Event importFailedEvent(final String msg, final String url) {
    
        return new EventBuilder( EventConstants.IMPORT_FAILED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, url)
            .addParam( EventConstants.PARM_FAILURE_MESSAGE, msg )
            .getEvent();
    }

    private Event importStartedEvent(final Resource resource) {
    
        return new EventBuilder( EventConstants.IMPORT_STARTED_UEI, NAME )
            .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
            .getEvent();
    }

    /**
     * <p>getEventForeignSource</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getEventForeignSource(final Event event) {
        return EventUtils.getParm(event, EventConstants.PARM_FOREIGN_SOURCE);
    }

}
