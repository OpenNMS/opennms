/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.bsf;

import java.util.List;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Process alarms via BSF.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class BSFNorthbounder extends AbstractNorthbounder implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(BSFNorthbounder.class);

    /** The Constant NBI_NAME. */
    protected static final String NBI_NAME = "BSFNBI";

    /** The BSF Configuration DAO. */
    private BSFNorthbounderConfigDao m_configDao;

    /** The BSF Destination. */
    private BSFEngineHandler m_engine;

    /** The BSF manager. */
    private BSFManager m_manager = new BSFManager();

    /** The initialized flag (it will be true when the NBI is properly initialized). */
    private boolean initialized = false;

    /**
     * Instantiates a new BSF northbounder.
     *
     * @param configDao the configuration DAO
     * @param engineName the engine name
     */
    public BSFNorthbounder(BSFNorthbounderConfigDao configDao, String engineName) {
        super(NBI_NAME + '-' + engineName);
        m_configDao = configDao;
        m_engine = configDao.getConfig().getEngine(engineName);

    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        setNaglesDelay(getConfig().getNaglesDelay());
        setMaxBatchSize(getConfig().getBatchSize());
        setMaxPreservedAlarms(getConfig().getQueueSize());
        if (m_engine == null) {
            LOG.error("BSF Northbounder {} is currently disabled because it has not been initialized correctly or there is a problem with the configuration.", getName());
            initialized = false;
            return;
        }
        initializeBSFEngine();
        initialized = true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#onStop()
     */
    @Override
    protected void onStop() {
        if (m_engine.getOnStop() != null) {
            LOG.debug("running stop script for BSF engine {}", getName());
            try {
                m_manager.exec(m_engine.getLanguage(), "", 0, 0, m_engine.getOnStop());
            } catch (BSFException e) {
                throw new NorthbounderException("Cannot execute stop script", e);
            }
        }
    }

    /**
     * Initialize BSF engine.
     *
     * @throws Exception the exception
     */
    private void initializeBSFEngine() throws Exception {
        if (!BSFManager.isLanguageRegistered(m_engine.getLanguage())) {
            LOG.debug("registering BSF language {} using {}", m_engine.getLanguage(), m_engine.getClassName());
            BSFManager.registerScriptingEngine(m_engine.getLanguage(), m_engine.getClassName(), m_engine.getExtensions().split(","));
        }
        m_manager.registerBean("log", LOG);
        if (m_engine.getOnStart() != null) {
            LOG.debug("running start script for BSF engine {}", getName());
            try {
                m_manager.exec(m_engine.getLanguage(), "", 0, 0, m_engine.getOnStart());
            } catch (BSFException e) {
                throw new NorthbounderException("Cannot execute start script", e);
            }
        }
    }

    /**
     * The abstraction makes a call here to determine if the alarm should be placed on the queue of alarms to be sent northerly.
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        if (!initialized) {
            LOG.warn("BSF Northbounder {} has not been properly initialized, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }
        if (!getConfig().isEnabled()) {
            LOG.warn("BSF Northbounder {} is currently disabled, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }

        LOG.debug("Validating UEI of alarm: {}", alarm.getUei());
        if (getConfig().getUeis() == null || getConfig().getUeis().contains(alarm.getUei())) {
            LOG.debug("UEI: {}, accepted.", alarm.getUei());
            boolean passed = m_engine.accepts(alarm);
            LOG.debug("Filters: {}, passed ? {}.", alarm.getUei(), passed);
            return passed;
        }
        LOG.debug("UEI: {}, rejected.", alarm.getUei());
        return false;
    }

    @Override
    public boolean isReady() {
        return initialized && getConfig().isEnabled();
    }

    /**
     * Each implementation of the AbstractNorthbounder has a nice queue (Nagle's algorithmic) and the worker thread that processes the queue
     * calls this method to send alarms to the northern NMS.
     *
     * @param alarms the alarms
     * @throws NorthbounderException the northbounder exception
     */
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        if (alarms == null) {
            String errorMsg = "No alarms in alarms list for BSF forwarding.";
            NorthbounderException e = new NorthbounderException(errorMsg);
            LOG.error(errorMsg, e);
            throw e;
        }
        LOG.info("Forwarding {} alarms to engine {}", alarms.size(), m_engine.getName());
        alarms.forEach(this::process);
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    protected BSFNorthbounderConfig getConfig() {
        return m_configDao.getConfig();
    }

    /**
     * Process.
     *
     * @param alarm the alarm
     * @throws NorthbounderException the northbounder exception
     */
    private void process(NorthboundAlarm alarm) throws NorthbounderException {
        m_manager.registerBean("alarm", alarm);
        try {
            LOG.debug("processing alarm {} with engine {}", alarm, getName());
            m_manager.exec(m_engine.getLanguage(), "", 0, 0, m_engine.getOnAlarm());
        } catch (BSFException e) {
            throw new NorthbounderException("Cannot execute script", e);
        } finally {
            m_manager.unregisterBean("alarm");
        }
    }

}
