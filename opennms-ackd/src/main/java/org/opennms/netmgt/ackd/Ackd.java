/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ackd;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.ackd.AckReader.AckReaderState;
import org.opennms.netmgt.ackd.readers.ReaderSchedule;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Acknowledgment management Daemon
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
@EventListener(name=Ackd.NAME, logPrefix="ackd")
public class Ackd implements SpringServiceDaemon, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(Ackd.class);
    
	/** Constant <code>NAME="Ackd"</code> */
	public static final String NAME = "Ackd";
	private volatile AckdConfigurationDao m_configDao;

	private volatile AcknowledgmentDao m_ackDao;

	private volatile EventForwarder m_eventForwarder;
	
    private volatile ScheduledThreadPoolExecutor m_executor;


	//FIXME change this to be like provisiond's adapters
	private List<AckReader> m_ackReaders;
    private Object m_lock = new Object();
	
    /**
     * <p>start</p>
     */
        @Override
    public void start() {
        LOG.info("start: Starting {} readers...", m_ackReaders.size());
        startReaders();
        LOG.info("start: readers started.");
    }

    /**
     * <p>destroy</p>
     */
        @Override
    public void destroy() {
        LOG.info("destroy: shutting down readers...");
        try {
            stopReaders();
            m_executor.purge();
            m_executor.shutdown();
    
            //fairly arbitrary time (grin)
            m_executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Throwable e) {
            LOG.error("destroy: error destroying readers.", e);
            m_executor.shutdownNow();
        }
        LOG.info("destroy: readers shutdown.");
    }

    /**
     * Starts the AckReaders without indicating a reload of their configuration is necessary.
     */
    protected void startReaders() {
        this.startReaders(false);
    }
    
    /**
     * Starts the AckReaders indicating a reload of their configuration is necessary.
     *
     * @param reloadConfig a boolean.
     */
    protected void startReaders(boolean reloadConfig) {
        int enabledReaderCount = getConfigDao().getEnabledReaderCount();
        
        if (enabledReaderCount < 1) {
            LOG.info("startReaders: there are not readers enabled in the configuration.");
            return;
        }
        
        m_executor.setCorePoolSize(enabledReaderCount);
        
        LOG.info("startReaders: starting {} enabled readers of {} readers registered.", enabledReaderCount, m_ackReaders.size());
        for (AckReader reader : m_ackReaders) {
            
            LOG.debug("startReaders: starting reader: {}", reader.getName());
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.STOPPED);
            
            try {
                adjustReaderState(reader, AckReaderState.STARTED, allowedStates, reloadConfig);
            } catch (Throwable e) {
                LOG.error("startReaders: Could not start reader: {}", reader.getName(), e);
                continue;
            }
            
            LOG.debug("startReaders: reader: {} started.", reader.getName());
        }
        LOG.info("startReaders: {} readers started.", m_ackReaders.size());
    }
    
    /**
     * <p>stopReaders</p>
     */
    protected void stopReaders() {
        LOG.info("stopReaders: stopping {} readers...", m_ackReaders.size());
        for (AckReader reader : m_ackReaders) {
            LOG.debug("stopReaders: stopping reader: {}", reader.getName());
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
            } catch (Throwable e) {
                LOG.error("startReaders: Could not stop reader: {}", reader.getName(), e);
            }
            
            LOG.debug("stopReaders: reader: {} stopped.", reader.getName());
        }
        LOG.info("stopReaders: {} readers stopped.", m_ackReaders.size());
    }
    
    /**
     * <p>pauseReaders</p>
     */
    protected void pauseReaders() {
        for (AckReader reader : m_ackReaders) {
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.STARTED);
            allowedStates.add(AckReaderState.RESUMED);
            
            try {
                adjustReaderState(reader, AckReaderState.PAUSED, allowedStates, false);
            } catch (Throwable e) {
                LOG.error("startReaders: Could not pause reader: {}", reader.getName(), e);
            }
        }
    }
    
    /**
     * <p>resumeReaders</p>
     */
    protected void resumeReaders() {
        for (AckReader reader : m_ackReaders) {
            List<AckReaderState> allowedStates = new ArrayList<AckReaderState>();
            allowedStates.add(AckReaderState.PAUSED);
            
            try {
                adjustReaderState(reader, AckReaderState.RESUMED, allowedStates, false);
            } catch (Throwable e) {
                LOG.error("startReaders: Could not resume reader: {}", reader.getName(), e);
            }
        }
    }

    
    /**
     * <p>restartReaders</p>
     *
     * @param reloadConfigs a boolean.
     */
    protected void restartReaders(boolean reloadConfigs) {
        LOG.info("restartReaders: restarting readers...");
        stopReaders();
        startReaders(reloadConfigs);
        LOG.info("restartReaders: readers restarted.");
        
    }

    private void adjustReaderState(AckReader reader, 
            AckReaderState requestedState, List<AckReaderState> allowedCurrentStates, boolean reloadConfig) {
    
        synchronized (m_lock) {
    
            if (!getConfigDao().isReaderEnabled(reader.getName())) {
                
                //stop a disabled reader if necessary
                if (!AckReaderState.STOPPED.equals(reader.getState())) {
                    LOG.warn("adjustReaderState: ignoring requested state and stopping the disabled reader: {}...", reader.getName());
                    reader.stop();
                    LOG.warn("adjustReaderState: disabled reader: {} stopped", reader.getName());
                    return;
                }
                
                LOG.warn("adjustReaderState: Not adjustingReaderState, disabled reader: {}", reader.getName());
                return;
            }
    
            if (allowedCurrentStates.contains(reader.getState())) {
    
                LOG.debug("adjustReaderState: adjusting reader state from: {} to: {}...", reader.getState(), requestedState);
    
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
                    LOG.error(e.getLocalizedMessage(), e);
                    throw e;
                }
    
            } else {
                IllegalStateException e = new IllegalStateException("error adjusting reader state; reader cannot be change from: "+reader.getState()+" to: "+requestedState);
                LOG.error(e.getLocalizedMessage(), e);
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
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.ACKNOWLEDGE_EVENT_UEI)
    public void handleAckEvent(Event event) {
        
        LOG.info("handleAckEvent: Received acknowledgment event: {}", event);
        
        OnmsAcknowledgment ack;
        
        try {
            ack = new OnmsAcknowledgment(event);
            m_ackDao.processAck(ack);
        } catch (ParseException e) {
            LOG.error("handleAckEvent: unable to process acknowledgment event: {}", event, e);
        }
    }
    
    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event event) {
        String specifiedDaemon = null;

        LOG.info("handleReloadConfigEvent: processing reload event: {}", event);

        List<Parm> parms = event.getParmCollection();

        for (Parm parm : parms) {
            specifiedDaemon = parm.getValue().getContent();

            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) 
                    && getName().equalsIgnoreCase(specifiedDaemon)) {

                LOG.debug("handleReloadConfigEvent: reload event is for this daemon: {}; reloading configuration...", getName());

                try {
                    m_configDao.reloadConfiguration();
                    EventBuilder bldr = new EventBuilder(
                                                         EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, 
                                                         getName(), 
                                                         Calendar.getInstance().getTime());
                    bldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
                    m_eventForwarder.sendNow(bldr.getEvent());

                    LOG.debug("handleReloadConfigEvent: restarting readers due to reload configuration event...");
                    this.restartReaders(true);
                } catch (Throwable e) {
                    LOG.error("handleReloadConfigEvent: ", e);
                    EventBuilder bldr = new EventBuilder(
                                                         EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, 
                                                         getName(), 
                                                         Calendar.getInstance().getTime());
                    bldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
                    bldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
                    m_eventForwarder.sendNow(bldr.getEvent());
                }

                LOG.debug("handleReloadConfigEvent: configuration reloaded.");

                return;  //return here because we are done.
            }

        }
        LOG.debug("handleReloadConfigEvent: reload event not for this daemon: {}; daemon specified is: {}", getName(), specifiedDaemon);
    }

    /**
     * <p>setExecutor</p>
     *
     * @param executor a {@link java.util.concurrent.ScheduledThreadPoolExecutor} object.
     */
    public void setExecutor(ScheduledThreadPoolExecutor executor) {
        m_executor = executor;
    }

    /**
     * <p>getExecutor</p>
     *
     * @return a {@link java.util.concurrent.ScheduledThreadPoolExecutor} object.
     */
    public ScheduledThreadPoolExecutor getExecutor() {
        return m_executor;
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
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getAckReaders</p>
     *
     * @return a {@link java.util.List} object.
     */
    protected List<AckReader> getAckReaders() {
        return m_ackReaders;
    }

    //FIXME:this is probably bogus
    /**
     * <p>setAckReaders</p>
     *
     * @param ackReaders a {@link java.util.List} object.
     */
    public void setAckReaders(List<AckReader> ackReaders) {
        m_ackReaders = ackReaders;
    }

     /**
     * @return a {@link org.opennms.netmgt.dao.api.AcknowledgmentDao} object.
     */
    public AcknowledgmentDao getAcknowledgmentDao() {
        return m_ackDao;
    }

    /**
     * @param ackDao a {@link org.opennms.netmgt.dao.api.AcknowledgmentDao} object.
     */
    public void setAcknowledgmentDao(AcknowledgmentDao ackDao) {
        m_ackDao = ackDao;
    }

    /**
     * <p>getConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.AckdConfigurationDao} object.
     */
    public AckdConfigurationDao getConfigDao() {
        return m_configDao;
    }

    /**
     * <p>setConfigDao</p>
     *
     * @param config a {@link org.opennms.netmgt.dao.api.AckdConfigurationDao} object.
     */
    public void setConfigDao(AckdConfigurationDao config) {
        m_configDao = config;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return NAME;
    }

}
