/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ackd;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.ackd.AckReader.AckReaderState;
import org.opennms.netmgt.ackd.readers.ReaderSchedule;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.acknowledgments.AckService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.DisposableBean;

/**
 * Acknowledgment management Daemon
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
@EventListener(name=Ackd.NAME)
public class Ackd implements SpringServiceDaemon, DisposableBean {
    
	public static final String NAME = "Ackd";
	private volatile AckdConfigurationDao m_configDao;

	private volatile EventForwarder m_eventForwarder;
	
    private volatile ScheduledThreadPoolExecutor m_executor;


	//FIXME change this to be like provisiond's adapters
	private List<AckReader> m_ackReaders;
    private AckService m_ackService;
    private Object m_lock = new Object();
	
    public void start() {
        log().info("start: Starting "+m_ackReaders.size()+" readers...");
        startReaders();
        log().info("start: readers started.");
    }

    public void destroy() {
        log().info("destroy: shutting down readers...");
        try {
            stopReaders();
            m_executor.purge();
            m_executor.shutdown();
    
            //fairly arbitrary time (grin)
            m_executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log().error("destroy: error destorying readers."+e, e);
            m_executor.shutdownNow();
        }
        log().info("destroy: readers shutdown.");
    }

    /**
     * Starts the AckReaders without indicating a reload of their configuration is necessary.
     */
    protected void startReaders() {
        this.startReaders(false);
    }
    
    /**
     * Starts the AckReaders indicating a reload of their configuration is necessary.
     */
    protected void startReaders(boolean reloadConfig) {
        int enabledReaderCount = getConfigDao().getEnabledReaderCount();
        
        if (enabledReaderCount < 1) {
            log().info("startReaders: there are not readers enabled in the configuration.");
            return;
        }
        
        m_executor.setCorePoolSize(enabledReaderCount);
        
        log().info("startReaders: starting "+enabledReaderCount+" enabled readers of "+m_ackReaders.size()+" readers registered.");
        for (AckReader reader : m_ackReaders) {
            
            log().debug("startReaders: starting reader: "+reader.getName());
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.STOPPED);
            
            try {
                adjustReaderState(reader, AckReaderState.STARTED, allowedStates, reloadConfig);
            } catch (Exception e) {
                log().error("startReaders: Could not start reader: "+reader.getName(), e);
                continue;
            }
            
            log().debug("startReaders: reader: "+reader.getName()+" started.");
        }
        log().info("startReaders: "+m_ackReaders.size()+" readers started.");
    }
    
    protected void stopReaders() {
        log().info("stopReaders: stopping "+m_ackReaders.size()+" readers...");
        for (AckReader reader : m_ackReaders) {
            log().debug("stopReaders: stopping reader: "+reader.getName());
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.PAUSE_PENDING);
            allowedStates.add(AckReaderState.PAUSED);
            allowedStates.add(AckReaderState.RESUME_PENDING);
            allowedStates.add(AckReaderState.RESUMED);
            allowedStates.add(AckReaderState.STARTED);
            allowedStates.add(AckReaderState.START_PENDING);
            allowedStates.add(AckReaderState.STOP_PENDING);
            
            try {
                adjustReaderState(reader, AckReaderState.STOPPED, allowedStates, false);
            } catch (Exception e) {
                log().error("startReaders: Could not stop reader: "+reader.getName(), e);
            }
            
            log().debug("stopReaders: reader: "+reader.getName()+" stopped.");
        }
        log().info("stopReaders: "+m_ackReaders.size()+" readers stopped.");
    }
    
    protected void pauseReaders() {
        for (AckReader reader : m_ackReaders) {
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.STARTED);
            allowedStates.add(AckReaderState.RESUMED);
            
            try {
                adjustReaderState(reader, AckReaderState.PAUSED, allowedStates, false);
            } catch (Exception e) {
                log().error("startReaders: Could not pause reader: "+reader.getName(), e);
            }
        }
    }
    
    protected void resumeReaders() {
        for (AckReader reader : m_ackReaders) {
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.PAUSED);
            
            try {
                adjustReaderState(reader, AckReaderState.RESUMED, allowedStates, false);
            } catch (Exception e) {
                log().error("startReaders: Could not resume reader: "+reader.getName(), e);
            }
        }
    }

    
    protected void restartReaders(boolean reloadConfigs) {
        log().info("restartReaders: restarting readers...");
        stopReaders();
        startReaders(reloadConfigs);
        log().info("restartReaders: readers restarted.");
        
    }

    private void adjustReaderState(AckReader reader, 
            AckReaderState requestedState, List<AckReaderState> allowedCurrentStates, boolean reloadConfig) {
    
        synchronized (m_lock) {
    
            if (!getConfigDao().isReaderEnabled(reader.getName())) {
                
                //stop a disabled reader if necessary
                if (!AckReaderState.STOPPED.equals(reader.getState())) {
                    log().warn("adjustReaderState: ignoring requested state and stopping the disabled reader: "+reader.getName()+"...");
                    reader.stop();
                    log().warn("adjustReaderState: disabled reader: "+reader.getName()+" stopped");
                    return;
                }
                
                log().warn("adjustReaderState: Not adjustingReaderState, disabled reader: "+reader.getName());
                return;
            }
    
            if (allowedCurrentStates.contains(reader.getState())) {
    
                log().debug("adjustReaderState: adjusting reader state from: "+reader.getState()+" to: "+requestedState+"...");
    
                org.opennms.netmgt.config.ackd.ReaderSchedule configSchedule = getConfigDao().getReaderSchedule(reader.getName());
    
                long interval = configSchedule.getInterval();
                String unit = configSchedule.getUnit();
    
                /**
                 * TODO: Make this so that a reference to the executor doesn't have to be passed in and
                 * the start method simply returns the task to be scheduled.  The schedule can be adjusted
                 * by the AckReader.  We just need to make sure that the future gets set in the AckReaer.
                 */
                if (AckReaderState.STARTED.equals(requestedState)) {
                    reader.start(m_executor, ReaderSchedule.createSchedule(interval, unit), reloadConfig);
                    
                } else if (AckReaderState.STOPPED.equals(requestedState)) {
                    reader.stop();
                    
                } else if (AckReaderState.PAUSED.equals(requestedState)) {
                    reader.pause();
                    
                } else if (AckReaderState.RESUMED.equals(requestedState)) {
                    reader.resume(m_executor);
                    
                } else {
                    IllegalStateException e = new IllegalStateException("adjustReaderState: cannot request state: "+requestedState);
                    log().error(e.getLocalizedMessage(), e);
                    throw e;
                }
    
            } else {
                IllegalStateException e = new IllegalStateException("error adjusting reader state; reader cannot be change from: "+reader.getState()+" to: "+requestedState);
                log().error(e.getLocalizedMessage(), e);
                throw e; 
            }
        }
    }

    /**
     * Handles the event driven access to acknowledging <code>OnmsAcknowledgable</code>s.  The acknowledgment event
     * contains 4 parameters: 
     *     ackUser: The user acknowledging the <code>OnmsAcknowledgable</code>
     *     ackAction: ack, unack, esc, clear
     *     ackType: <code>AckType</code. representing either an <code>OnmsAlarm</code>, <code>OnmsNotification</code>, etc.
     *     refId: The ID of the <code>OnmsAcknowledgable</code>
     * @param event
     */
    @EventHandler(uei=EventConstants.ACKNOWLEDGE_EVENT_UEI)
    public void handleAckEvent(Event event) {
        
        log().info("handleAckEvent: Received acknowledgment event: "+event);
        
        OnmsAcknowledgment ack;
        
        try {
            ack = new OnmsAcknowledgment(event);
            m_ackService.processAck(ack);
        } catch (ParseException e) {
            log().error("handleAckEvent: unable to process acknowledgment event: "+event+"\t"+e);
        }
    }
    
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event event) {
        String specifiedDaemon = null;
        
        log().info("handleReloadConfigEvent: processing reload event: "+event+"...");
        
        if (event.getParms() != null && event.getParms().getParmCount() >=1 ) {
            List<Parm> parms = event.getParms().getParmCollection();
            
            for (Parm parm : parms) {
                specifiedDaemon = parm.getValue().getContent();
                
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) 
                        && getName().equalsIgnoreCase(specifiedDaemon)) {
                    
                    log().debug("handleReloadConfigEvent: reload event is for this daemon: "+getName()+"; reloading configuration...");
                    
                    try {
                        m_configDao.reloadConfiguration();
                        EventBuilder bldr = new EventBuilder(
                                                             EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, 
                                                             getName(), 
                                                             Calendar.getInstance().getTime());
                        bldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
                        m_eventForwarder.sendNow(bldr.getEvent());
                        
                        log().debug("handleReloadConfigEvent: restarting readers due to reload configuration event...");
                        this.restartReaders(true);
                    } catch (Exception e) {
                        log().error("handleReloadConfigEvent: "+e, e);
                        EventBuilder bldr = new EventBuilder(
                                                             EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, 
                                                             getName(), 
                                                             Calendar.getInstance().getTime());
                        bldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
                        bldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
                        m_eventForwarder.sendNow(bldr.getEvent());
                    }
                    
                    log().debug("handleReloadConfigEvent: configuration reloaded.");
                    
                    return;  //return here because we are done.
                }
                
            }
            log().debug("handleReloadConfigEvent: reload event not for this daemon: "+getName()+"; daemon specified is: "+specifiedDaemon);
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getName());
    }

    public void setExecutor(ScheduledThreadPoolExecutor executor) {
        m_executor = executor;
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return m_executor;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    protected List<AckReader> getAckReaders() {
        return m_ackReaders;
    }

    //FIXME:this is probably bogus
    public void setAckReaders(List<AckReader> ackReaders) {
        m_ackReaders = ackReaders;
    }

    public AckService getAckService() {
        return m_ackService;
    }

    public void setAckService(AckService ackService) {
        m_ackService = ackService;
    }

    public AckdConfigurationDao getConfigDao() {
        return m_configDao;
    }

    public void setConfigDao(AckdConfigurationDao config) {
        m_configDao = config;
    }

    public void afterPropertiesSet() throws Exception {
    }

    public String getName() {
        return NAME;
    }

}