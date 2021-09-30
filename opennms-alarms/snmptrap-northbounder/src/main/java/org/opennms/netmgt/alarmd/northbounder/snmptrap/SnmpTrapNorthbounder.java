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

package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.util.List;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Forwards alarms via SNMP Trap.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapNorthbounder extends AbstractNorthbounder implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SnmpTrapNorthbounder.class);

    /** The Constant NBI_NAME. */
    protected static final String NBI_NAME = "SnmpTrapNBI";

    /** The SNMP Trap Configuration DAO. */
    private SnmpTrapNorthbounderConfigDao m_configDao;

    /** The SNMP Trap Sink. */
    private SnmpTrapSink m_trapSink;

    /** The SNMP Trap helper. */
    private SnmpTrapHelper m_trapHelper;

    /** The initialized flag (it will be true when the NBI is properly initialized). */
    private boolean initialized = false;

    /**
     * Instantiates a new SNMP Trap northbounder.
     *
     * @param configDao the SNMP Trap configuration DAO
     * @param trapSink the trap sink
     */
    public SnmpTrapNorthbounder(SnmpTrapNorthbounderConfigDao configDao, String trapSink) {
        super(NBI_NAME + ":" + trapSink);
        m_configDao = configDao;
        m_trapSink = configDao.getConfig().getSnmpTrapSink(trapSink);
        m_trapHelper = new SnmpTrapHelper();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (m_configDao == null || m_trapSink == null) {
            LOG.error("SNMP Trap Northbounder {} is currently disabled because it has not been initialized correctly or there is a problem with the configuration.", getName());
            initialized = false;
            return;
        }
        setNaglesDelay(getConfig().getNaglesDelay());
        setMaxBatchSize(getConfig().getBatchSize());
        setMaxPreservedAlarms(getConfig().getQueueSize());
        initialized = true;
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
            LOG.warn("SNMP Trap Northbounder {} has not been properly initialized, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }
        if (!getConfig().isEnabled()) {
            LOG.warn("SNMP Trap Northbounder {} is currently disabled, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }

        LOG.debug("Validating UEI of alarm: {}", alarm.getUei());
        if (getConfig().getUeis() == null || getConfig().getUeis().contains(alarm.getUei())) {
            LOG.debug("UEI: {}, accepted.", alarm.getUei());
            boolean passed = m_trapSink.accepts(alarm);
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
            String errorMsg = "No alarms in alarms list for SNMP Trap forwarding.";
            NorthbounderException e = new NorthbounderException(errorMsg);
            LOG.error(errorMsg, e);
            throw e;
        }
        LOG.info("Forwarding {} alarms to destination {}", alarms.size(), m_trapSink.getName());
        for (NorthboundAlarm alarm : alarms) {
            try {
                SnmpTrapConfig config = m_trapSink.createTrapConfig(alarm);
                if (config != null) {
                    m_trapHelper.forwardTrap(config);
                }
            } catch (SnmpTrapException e) {
                LOG.error("Can't send trap for {}", alarm, e);
            }
        }
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    public SnmpTrapNorthbounderConfig getConfig() {
        return m_configDao.getConfig();
    }

}
