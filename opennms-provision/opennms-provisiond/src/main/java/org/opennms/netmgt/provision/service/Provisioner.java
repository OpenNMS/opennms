/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.tasks.Task;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;
import org.opennms.netmgt.provision.service.operations.RequisitionImportContext;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.google.common.collect.Maps;

/**
 * Massively Parallel Java Provisioning <code>ServiceDaemon</code> for OpenNMS.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@EventListener(name="Provisiond:EventListener", logPrefix="provisiond")
public class Provisioner implements SpringServiceDaemon {
    private static final String SCHEDULE_RESCAN_FOR_UPDATED_NODES = "org.opennms.provisiond.scheduleRescanForUpdatedNodes";
    private static final String SCHEDULE_RESCAN_FOR_EXISTING_NODES = "org.opennms.provisiond.scheduleRescanForExistingNodes";

    private static final Logger LOG = LoggerFactory.getLogger(Provisioner.class);
    

    /** Constant <code>NAME="Provisiond"</code> */
    public static final String NAME = "Provisiond";

    private TaskCoordinator m_taskCoordinator;
    private CoreImportActivities m_importActivities;
    private LifeCycleRepository m_lifeCycleRepository;
    private ProvisionService m_provisionService;
    private ScheduledExecutorService m_scheduledExecutor;
    private final Map<Integer, ScheduledFuture<?>> m_scheduledNodes = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();
    private volatile EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    
    @Autowired
    private ProvisioningAdapterManager m_manager;

    @Autowired
    private MonitoringSystemDao monitoringSystemDao;
    
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
    public void setTaskCoordinator(TaskCoordinator taskCoordinator) {
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
        String enabled = System.getProperty(SCHEDULE_RESCAN_FOR_EXISTING_NODES, "true");
        if (Boolean.valueOf(enabled)) {
            scheduleRescanForExistingNodes();
        } else {
            LOG.warn("The schedule rescan for existing nodes is disabled.");
        }
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
        
        for(final NodeScanSchedule schedule : schedules) {
            if (schedule.getScanInterval().getMillis() <= 0) {
                continue;
            }
            if(!m_scheduledNodes.containsKey(schedule.getNodeId())) {
                addToScheduleQueue(schedule);
            }else {
                updateNodeScheduleInQueue(schedule);
            }            
        }
        
    }
    
    public void doNodeScan(int nodeId) throws InterruptedException, ExecutionException {
    }

    public NodeScan createNodeScan(Integer nodeId, String foreignSource, String foreignId, OnmsMonitoringLocation location) {
        LOG.info("createNodeScan called");
        return new NodeScan(nodeId, foreignSource, foreignId, location, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator);
    }

    public NewSuspectScan createNewSuspectScan(InetAddress ipAddress, String foreignSource, String location) {
        LOG.info("createNewSuspectScan called with IP: "+ipAddress+ "and foreignSource"+foreignSource == null ? "null" : foreignSource);
        return new NewSuspectScan(ipAddress, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator, foreignSource, location);
    }

    public ForceRescanScan createForceRescanScan(Integer nodeId) {
        LOG.info("createForceRescanScan called with nodeId: "+nodeId);
        return new ForceRescanScan(nodeId, m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator);
    }

    //Helper functions for the schedule
    protected void addToScheduleQueue(NodeScanSchedule schedule) {
        ScheduledFuture<?> future = scheduleNodeScan(schedule);
        LOG.warn("addToScheduleQueue future = {}", future);
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
        NodeScan nodeScan = createNodeScan(schedule.getNodeId(), schedule.getForeignSource(), schedule.getForeignId(), schedule.getLocation());
        LOG.warn("nodeScan = {}", nodeScan);
        return nodeScan.schedule(m_scheduledExecutor, schedule);
    }

    public ScheduledFuture<?> getScheduledFutureForNode(int nodeId) {
    	return m_scheduledNodes.get(nodeId);
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

    protected void importModelFromResource(final Resource resource, final String rescanExisting) throws Exception {
        final ImportRequest importRequest = new ImportRequest(NAME)
                .withUrl(resource.getURL().toString())
                .withRescanExisting(rescanExisting);
        importModelFromResource(importRequest);
    }

    protected void importModelFromResource(final ImportRequest importRequest) throws Exception {
        final LifeCycleInstance doImport = m_lifeCycleRepository.createLifeCycleInstance("import", m_importActivities);
        RequisitionImportContext context = new RequisitionImportContext();
        context.setImportRequest(importRequest);
        doImport.setAttribute("context", context);
        doImport.trigger();
        doImport.waitFor();
        final RequisitionImportContext ri = doImport.findAttributeByType(RequisitionImportContext.class);
        if (ri.isAborted()) {
            throw new ModelImportException("Could not perform requested import: " + importRequest, ri.getError());
        }
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
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
        ImportRequest properties = new ImportRequest(event, System.getProperty(SCHEDULE_RESCAN_FOR_UPDATED_NODES, "true"));
        doImport(properties);
    }

    public void doImport(final ImportRequest importRequest) {
        if (importRequest.isValid()) {
            try {
                LOG.info("doImport: importing: {}", importRequest);
                send(importStartedEvent(importRequest));
                importModelFromResource(importRequest);
                LOG.info("Finished Importing: {}");
                send(importSuccessEvent(importRequest));
            } catch (final Throwable t) {
                final String msg = "Exception importing "+importRequest.getUrl();
                LOG.error("Exception importing: {}", importRequest);
                send(importFailedEvent((msg+": "+t.getMessage()), importRequest));
            }
        } else {
            final String msg = "reloadImport event requires 'importResource' or 'importForeignSource' parameter";
            LOG.error("doImport: {}", msg);
            send(importFailedEvent(msg, importRequest));
        }
    }

    @EventHandler(uei = EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAddedEvent(Event e) {
        NodeScanSchedule scheduleForNode = null;
        LOG.warn("node added event ({})", System.currentTimeMillis());
        try {
            /* we don't force a scan on node added so new suspect doesn't cause 2 simultaneous node scans
             * New nodes that are created another way shouldn't have a 'lastCapsPoll' timestamp set 
             */ 
            scheduleForNode = getProvisionService().getScheduleForNode(e.getNodeid().intValue(), false);
        } catch (Throwable t) {
            LOG.error("getScheduleForNode fails", t);
        }
        LOG.warn("scheduleForNode is {}", scheduleForNode);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }

    }
    
    @EventHandler(uei = EventConstants.FORCE_RESCAN_EVENT_UEI)
    public void handleForceRescan(Event e) {
        final Integer nodeId = new Integer(e.getNodeid().intValue());
        removeNodeFromScheduleQueue(nodeId);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    ForceRescanScan scan = createForceRescanScan(nodeId);
                    Task t = scan.createTask();
                    t.schedule();
                    t.waitFor();
                    NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(nodeId, false); // It has 'false' because a node scan was already executed by ForceRescanScan.
                    if (scheduleForNode != null) {
                        addToScheduleQueue(scheduleForNode);
                    }
                } catch (InterruptedException ex) {
                    LOG.error("Task interrupted waiting for rescan of nodeId {} to finish", nodeId, ex);
                } catch (ExecutionException ex) {
                    LOG.error("An expected execution occurred waiting for rescan of nodeId {} to finish", nodeId, ex);
                }
            }
        };
        m_scheduledExecutor.execute(r);
    }
    
    @EventHandler(uei = EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
    public void handleNewSuspectEvent(Event e) {
        final Event event = e;
        final String uei = e.getUei();
        final String ip = e.getInterface();
        final Map<String, String> paramMap = Maps.newHashMap();
        e.getParmCollection().forEach(eachParam -> paramMap.put(eachParam.getParmName(), eachParam.getValue().getContent()));

        if (ip == null) {
            LOG.error("Received a {} event with a null ipAddress", uei);
            return;
        }

        if (!getProvisionService().isDiscoveryEnabled()) {
            LOG.info("Ignoring {} event for ip {} since discovery handling is disabled in provisiond", uei, ip);
            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress addr = addr(ip);
                    if (addr == null) {
                    	LOG.error("Unable to convert {} to an InetAddress.", ip);
                    	return;
                    }

                    final String location;
                    if (paramMap.containsKey("location")) {
                        location = paramMap.get("location");
                    } else {
                        location = monitoringSystemDao.get(event.getDistPoller()).getLocation();
                    }

                    NewSuspectScan scan = createNewSuspectScan(addr, paramMap.get("foreignSource"), location);
                    Task t = scan.createTask();
                    t.schedule();
                    t.waitFor();
                } catch (InterruptedException ex) {
                    LOG.error("Task interrupted waiting for new suspect scan of {} to finish", ip, ex);
                } catch (ExecutionException ex) {
                    LOG.error("An expected execution occurred waiting for new suspect scan of {} to finish", ip, ex);
                }
                
            }
        };

        m_scheduledExecutor.execute(r);
        
    }
    
    /**
     * <p>handleNodeUpdated</p>
     * A re-import has occurred, attempt a rescan now.
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.NODE_UPDATED_EVENT_UEI)
    public void handleNodeUpdated(Event e) {
    	LOG.debug("Node updated event received: {}", e);
    	
        if (!Boolean.valueOf(System.getProperty(SCHEDULE_RESCAN_FOR_UPDATED_NODES, "true"))) {
        	LOG.debug("Rescanning updated nodes is disabled via property: {}", SCHEDULE_RESCAN_FOR_UPDATED_NODES);
        	return;
        }
        String rescanExisting = Boolean.TRUE.toString(); // Default
        for (Parm parm : e.getParmCollection()) {
            if (EventConstants.PARM_RESCAN_EXISTING.equals(parm.getParmName()) && ("false".equalsIgnoreCase(parm.getValue().getContent()) || "dbonly".equalsIgnoreCase(parm.getValue().getContent()))) {
                rescanExisting = Boolean.FALSE.toString();
            }
        }
        if (!Boolean.valueOf(rescanExisting)) {
            LOG.debug("Rescanning updated nodes is disabled via event parameter: {}", EventConstants.PARM_RESCAN_EXISTING);
            return;
        }
        
        removeNodeFromScheduleQueue(new Long(e.getNodeid()).intValue());
        NodeScanSchedule scheduleForNode = getProvisionService().getScheduleForNode(e.getNodeid().intValue(), true);
        if (scheduleForNode != null) {
            addToScheduleQueue(scheduleForNode);
        }
        
    }

    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(Event e) {
        removeNodeFromScheduleQueue(e.getNodeid().intValue());
        
    }
    
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {
        
        if (isReloadConfigEventTarget(e)) {
            LOG.info("handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            try {
                LOG.debug("handleReloadConfigEvent: lock acquired, unscheduling current reports...");
                
                m_importSchedule.rebuildImportSchedule();
                
                LOG.debug("handleRelodConfigEvent: reports rescheduled.");
                
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Provisiond");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond");
                
            } catch (Throwable exception) {
                
                LOG.error("handleReloadConfigurationEvent: Error reloading configuration", exception);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Provisiond");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond");
                ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
                
            }
            
            if (ebldr != null) {
                m_eventForwarder.sendNow(ebldr.getEvent());
            }
            LOG.info("handleReloadConfigEvent: configuration reloaded.");
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
        
        LOG.debug("isReloadConfigEventTarget: Provisiond was target of reload event: {}", isTarget);
        return isTarget;
    }

    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleAddInterface(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doAddInterface(event.getNodeid(), event.getInterface());
            } catch (Throwable e) {
                LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
            }
        }
    }
    
    private void doAddInterface(long nodeId, String ipAddr) {
        // FIXME: Handle Rackspace ADD_INTERFACE event
        throw new UnsupportedOperationException("Provisioner.doAddInterface is not yet implemented");
    }

    @EventHandler(uei=EventConstants.ADD_NODE_EVENT_UEI)
    public void handleAddNode(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doAddNode(event.getInterface(), EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Throwable e) {
                LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
            }
        }
    }

    private void doAddNode(String ipAddr, String nodeLabel) {

        OnmsNode node = new OnmsNode();
        node.setLabel(nodeLabel);

        OnmsIpInterface iface = new OnmsIpInterface(addr(ipAddr), node);
        iface.setIsManaged("M");
        iface.setPrimaryString("N");

        m_provisionService.insertNode(node);

    }

    @EventHandler(uei=EventConstants.CHANGE_SERVICE_EVENT_UEI)
    public void handleChangeService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doChangeService(event.getInterface(), event.getService(), EventUtils.getParm(event, EventConstants.PARM_ACTION));
            } catch (Throwable e) {
                LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
            }
        }
    }
    
    private void doChangeService(String ipAddr, String service, String action) {
        // FIXME: Handle Rackspace CHANGE_SERVICE event
        throw new UnsupportedOperationException("Provisioner.doChangeService is not yet implemented");
    }

    @EventHandler(uei=EventConstants.DELETE_INTERFACE_EVENT_UEI)
    public void handleDeleteInterface(Event event) {
        try {
            doDeleteInterface(event.getNodeid(), event.getInterface());
        } catch (Throwable e) {
            LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
        }
    }
    
    private void doDeleteInterface(long nodeId, String ipAddr) {
        m_provisionService.deleteInterface((int)nodeId, ipAddr);
    }

    @EventHandler(uei=EventConstants.DELETE_NODE_EVENT_UEI)
    public void handleDeleteNode(Event event) {
        try {
            doDeleteNode(event.getNodeid());
        } catch (Throwable e) {
            LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
        }
    }
    
    private void doDeleteNode(long nodeId) {
        m_provisionService.deleteNode((int)nodeId);
    }

    @EventHandler(uei=EventConstants.DELETE_SERVICE_EVENT_UEI)
    public void handleDeleteService(Event event) {
        try {
	    doDeleteService(event.getNodeid(), event.getInterfaceAddress() == null ? null : event.getInterfaceAddress(), event.getService());
        } catch (Throwable e) {
            LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
        }
    }
    
    private void doDeleteService(long nodeId, InetAddress addr, String service) {
        m_provisionService.deleteService((int)nodeId, addr, service);
    }

    @EventHandler(uei=EventConstants.UPDATE_SERVER_EVENT_UEI)
    public void handleUpdateServer(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doUpdateServer(event.getInterface(), event.getHost(), 
                        EventUtils.getParm(event, EventConstants.PARM_ACTION),
                        EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Throwable e) {
                LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
            }
        }
    }
    
    private void doUpdateServer(String ipAddr, String host, String action, String nodeLabel) {
        // FIXME: Handle Rackspace UPDATE_SERVER event
        throw new UnsupportedOperationException("Provisioner.doUpdateServer is not yet implemented");
    }

    @EventHandler(uei=EventConstants.UPDATE_SERVICE_EVENT_UEI)
    public void handleUpdateService(Event event) {
        if (m_provisionService.isDiscoveryEnabled()) {
            try {
                doUpdateService(event.getInterface(), event.getService(), 
                        EventUtils.getParm(event, EventConstants.PARM_ACTION),
                        EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL));
            } catch (Throwable e) {
                LOG.error("Unexpected exception processing event: {}", event.getUei(), e);
            }
        }
    }
    
    
    private void doUpdateService(String ipAddr, String service, String action, String nodeLabel) {
        // FIXME: Handle Rackspace UPDATE_SERVICE event
        throw new UnsupportedOperationException("Provisioner.doUpdateService is not yet implemented");
    }
    
    private Event importSuccessEvent(final ImportRequest importRequest) {
        return importRequest.addEventParameters(new EventBuilder( EventConstants.IMPORT_SUCCESSFUL_UEI, NAME )).getEvent();
    }

    private void send(final Event event) {
        m_eventForwarder.sendNow(event);
    }

    private Event importFailedEvent(final String msg, final ImportRequest importRequest) {
        return importRequest.addEventParameters(
                new EventBuilder( EventConstants.IMPORT_FAILED_UEI, NAME )
                    .addParam( EventConstants.PARM_FAILURE_MESSAGE, msg )
            ).getEvent();
    }

    private Event importStartedEvent(final ImportRequest importRequest) {
        return importRequest.addEventParameters(new EventBuilder( EventConstants.IMPORT_STARTED_UEI, NAME ))
            .getEvent();
    }

    public void waitFor() {
        final ScheduledFuture<?> future = m_scheduledExecutor.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        }, 0, TimeUnit.SECONDS);
        try {
            future.get();
        } catch (final Exception e) {
            // ignore, we're just waiting for a reasonable chance things were finished
        }
    }

}
