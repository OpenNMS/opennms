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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.util.List;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;

import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.SyslogRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Forwards alarms via Syslog.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class SyslogNorthbounder extends AbstractNorthbounder implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SyslogNorthbounder.class);

    /** The Constant NBI_NAME. */
    protected static final String NBI_NAME = "SyslogNBI";

    /** The Syslog Configuration DAO. */
    private SyslogNorthbounderConfigDao m_configDao;

    /** The Syslog Destination. */
    private SyslogDestination m_destination;

    /** The initialized flag (it will be true when the NBI is properly initialized). */
    private boolean initialized = false;

    /**
     * Instantiates a new Syslog northbounder.
     *
     * @param configDao the Syslog configuration DAO
     * @param destination the destination
     */
    public SyslogNorthbounder(SyslogNorthbounderConfigDao configDao, String destination) {
        super(NBI_NAME + ":" + destination);
        m_configDao = configDao;
        m_destination = configDao.getConfig().getSyslogDestination(destination);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (m_destination == null) {
            LOG.error("Syslog Northbounder {} is currently disabled because it has not been initialized correctly or there is a problem with the configuration.", getName());
            initialized = false;
            return;
        }
        SyslogUtils.createNorthboundInstance(m_destination);
        setNaglesDelay(getConfig().getNaglesDelay());
        setMaxBatchSize(getConfig().getBatchSize());
        setMaxPreservedAlarms(getConfig().getQueueSize());
        initialized = true;
    }

    /**
     * The abstraction makes a call here to determine if the alarm should be
     * placed on the queue of alarms to be sent northerly.
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        if (!initialized) {
            LOG.warn("Syslog Northbounder {} has not been properly initialized, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }
        if (!getConfig().isEnabled()) {
            LOG.warn("Syslog Northbounder {} is currently disabled, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }

        LOG.debug("Validating UEI of alarm: {}", alarm.getUei());
        if (getConfig().getUeis() == null || getConfig().getUeis().contains(alarm.getUei())) {
            LOG.debug("UEI: {}, accepted.", alarm.getUei());
            if (m_destination.getFilters() == null) {
                return true;
            }
            boolean passed = m_destination.passFilter(alarm);
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
     * Each implementation of the AbstractNorthbounder has a nice queue
     * (Nagle's algorithmic) and the worker thread that processes the queue
     * calls this method to send alarms to the northern NMS.
     *
     * @param alarms the alarms
     * @throws NorthbounderException the northbounder exception
     */
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        if (alarms == null) {
            String errorMsg = "No alarms in alarms list for syslog forwarding.";
            IllegalStateException e = new IllegalStateException(errorMsg);
            LOG.error(errorMsg, e);
            throw e;
        }

        LOG.info("Forwarding {} alarms to destination:{}", alarms.size(), m_destination.getName());
        SyslogIF instance;
        try {
            instance = Syslog.getInstance(m_destination.getName());
        } catch (SyslogRuntimeException e) {
            LOG.error("Could not find Syslog instance for destination: '{}': {}", m_destination.getName(), e);
            throw e;
        }

        /*
         * Iterate over the list of alarms to be forwarded N.
         */
        for (NorthboundAlarm alarm : alarms) {
            Integer count = alarm.getCount();
            if (count > 1 && m_destination.isFirstOccurrenceOnly()) {
                LOG.debug("Destination {} is configured for new alarm instances only.  Alarm has count of {}.", m_destination.getName(), count);
                continue;
            }
            LOG.debug("Creating formatted log message for alarm: {}.", alarm.getId());
            String syslogMessage;
            int level;
            try {
                LOG.debug("Making substitutions for tokens in message format for alarm: {}.", alarm.getId());
                String msgFormat = m_destination.getCustomMessageFormat(alarm);
                if (msgFormat == null) {
                    msgFormat = getConfig().getMessageFormat();
                }
                String dateFormat = getConfig().getDateFormat();
                syslogMessage = PropertiesUtils.substitute(msgFormat, createMapping(alarm, dateFormat));

                LOG.debug("Determining LOG_LEVEL for alarm: {}", alarm.getId());
                level = SyslogUtils.determineLogLevel(alarm.getSeverity());

                LOG.debug("Forwarding alarm: {} via syslog to destination: {}", alarm.getId(), m_destination.getName());
                instance.log(level, syslogMessage);

            } catch (Exception ex) {
                LOG.error("Caught exception sending to destination: '{}': {}", m_destination.getName(), ex);
            }
        }
    }

    /**
     * Gets the Syslog configuration.
     *
     * @return the Syslog configuration
     */
    public SyslogNorthbounderConfig getConfig() {
        return m_configDao.getConfig();
    }

}
