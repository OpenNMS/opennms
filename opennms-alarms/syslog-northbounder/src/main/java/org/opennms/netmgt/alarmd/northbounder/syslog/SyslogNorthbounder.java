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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination.SyslogFacility;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination.SyslogProtocol;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Forwards alarms, N, via Syslog.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
public class SyslogNorthbounder extends AbstractNorthbounder implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogNorthbounder.class);

    private static final String NBI_NAME = "SyslogNBI" ;

    private SyslogNorthbounderConfig m_config;

    private SyslogDestination m_destination;

    public SyslogNorthbounder(SyslogNorthbounderConfig config, SyslogDestination destination) {
        super(NBI_NAME+":"+destination);
        m_config = config;
        m_destination = destination;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (m_config == null) {

            LOG.info("Syslog Northbounder is currently disabled, rejecting alarm.");

            String msg = "Syslog forwarding configuration is not initialized.";
            IllegalStateException e = new IllegalStateException(msg);
            LOG.error(msg, e);
            throw e;
        }

        createNorthboundInstance();
        setNaglesDelay(m_config.getNaglesDelay());
        setMaxBatchSize(m_config.getBatchSize());
        setMaxPreservedAlarms(m_config.getQueueSize());
    }

    /**
     * The abstraction makes a call here to determine if the alarm should be placed
     * on the queue of alarms to be sent northerly.
     * 
     */
    @Override
    public boolean accepts(NorthboundAlarm alarm) {

        if (!m_config.isEnabled()) {
            return false;
        }

        LOG.debug("Validating UEI of alarm: {}", alarm.getUei());

        if (getConfig().getUeis() == null
                || getConfig().getUeis().contains(alarm.getUei())) {
            LOG.debug("UEI: {}, accepted.", alarm.getUei());
            return true;
        }

        LOG.debug("UEI: {}, rejected.", alarm.getUei());
        return false;
    }
    
    /**
     * Each implementation of the AbstractNorthbounder has a nice queue (Nagle's algorithmic) and the worker
     * thread that processes the queue calls this method to send alarms to the northern NMS.
     * 
     */
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms)
            throws NorthbounderException {

        if (alarms == null) {
            String errorMsg = "No alarms in alarms list for syslog forwarding.";
            IllegalStateException e = new IllegalStateException(errorMsg);
            LOG.error(errorMsg, e);
            throw e;
        }

        LOG.info("Forwarding {} alarms to destination:{}", alarms.size(),
                 m_destination.getName());

        Map<Integer, Map<String, Object>> alarmMappings = new HashMap<Integer, Map<String, Object>>();

        SyslogIF instance;
        try {
            instance = Syslog.getInstance(m_destination.getName());
        } catch (SyslogRuntimeException e) {
            LOG.error("Could not find Syslog instance for destination: '{}': {}",
                      m_destination.getName(), e);
            throw e;
        }

        /*
         * Iterate over the list of alarms to be forwarded N.
         */
        for (NorthboundAlarm alarm : alarms) {

            Integer count = alarm.getCount();
            if (count > 1 && m_destination.isFirstOccurrenceOnly()) {
                LOG.debug("Destination {} is configured for new alarm instances only.  Alarm has count of {}.",
                          m_destination.getName(), count);
                continue;
            }

            LOG.debug("Creating formatted log message for alarm: {}.",
                      alarm.getId());

            Map<String, Object> mapping = null;

            String syslogMessage;
            int level;
            try {
                if (alarmMappings != null) {
                    mapping = alarmMappings.get(alarm.getId());
                }

                if (mapping == null) {
                    mapping = createMapping(alarmMappings, alarm);
                }

                LOG.debug("Making substitutions for tokens in message format for alarm: {}.",
                          alarm.getId());
                syslogMessage = PropertiesUtils.substitute(m_config.getMessageFormat(),
                                                           mapping);

                LOG.debug("Determining LOG_LEVEL for alarm: {}",
                          alarm.getId());
                level = determineLogLevel(alarm.getSeverity());

                LOG.debug("Forwarding alarm: {} via syslog to destination: {}",
                          alarm.getId(), m_destination.getName());
                instance.log(level, syslogMessage);

            } catch (Exception e1) {
                LOG.error("Caught exception sending to destination: '{}': {}",
                          m_destination.getName(), e1);
            }
        }
    }
	
    /**
     * This is here, for now, until it can be properly wired and proper configuration can be created.
     * This allows generic 127.0.0.1:UDP/514 to work with OpenNMS having no configuration.  This is
     * trickery in its finest hour.
     */
    private void createNorthboundInstance() throws SyslogRuntimeException {

        LOG.info("Creating Syslog Northbound Instance:{}",
                 m_destination.getName());

        String instName = m_destination.getName();
        int facility = convertFacility(m_destination.getFacility());
        SyslogProtocol protocol = m_destination.getProtocol();
        SyslogConfigIF instanceConfiguration = createConfig(m_destination,
                                                            protocol,
                                                            facility);
        instanceConfiguration.setIdent("OpenNMS");
        instanceConfiguration.setCharSet(m_destination.getCharSet());
        instanceConfiguration.setMaxMessageLength(m_destination.getMaxMessageLength());
        instanceConfiguration.setSendLocalName(m_destination.isSendLocalName());
        instanceConfiguration.setSendLocalTimestamp(m_destination.isSendLocalTime());
        instanceConfiguration.setTruncateMessage(m_destination.isTruncateMessage());
        instanceConfiguration.setUseStructuredData(SyslogConstants.USE_STRUCTURED_DATA_DEFAULT);

        try {
            Syslog.createInstance(instName, instanceConfiguration);
        } catch (SyslogRuntimeException e) {
            LOG.error("Could not create northbound instance, '{}': {}",
                      instName, e);
            throw e;
        }

    }

    private SyslogConfigIF createConfig(final SyslogDestination dest,
            final SyslogProtocol protocol, int fac) {
        SyslogConfigIF config;
        switch (protocol) {
        case UDP:
            config = new UDPNetSyslogConfig(fac, dest.getHost(),
                                            dest.getPort());
            break;
        case TCP:
            config = new TCPNetSyslogConfig(fac, dest.getHost(),
                                            dest.getPort());
            break;
        default:
            config = new UDPNetSyslogConfig(fac, "localhost", 514);
        }
        return config;
    }

    private int convertFacility(final SyslogFacility facility) {
        int fac;
        switch (facility) {
        case KERN:
            fac = SyslogConstants.FACILITY_KERN;
            break;
        case USER:
            fac = SyslogConstants.FACILITY_USER;
            break;
        case MAIL:
            fac = SyslogConstants.FACILITY_MAIL;
            break;
        case DAEMON:
            fac = SyslogConstants.FACILITY_DAEMON;
            break;
        case AUTH:
            fac = SyslogConstants.FACILITY_AUTH;
            break;
        case SYSLOG:
            fac = SyslogConstants.FACILITY_SYSLOG;
            break;
        case LPR:
            fac = SyslogConstants.FACILITY_LPR;
            break;
        case NEWS:
            fac = SyslogConstants.FACILITY_NEWS;
            break;
        case UUCP:
            fac = SyslogConstants.FACILITY_UUCP;
            break;
        case CRON:
            fac = SyslogConstants.FACILITY_CRON;
            break;
        case AUTHPRIV:
            fac = SyslogConstants.FACILITY_AUTHPRIV;
            break;
        case FTP:
            fac = SyslogConstants.FACILITY_FTP;
            break;
        case LOCAL0:
            fac = SyslogConstants.FACILITY_LOCAL0;
            break;
        case LOCAL1:
            fac = SyslogConstants.FACILITY_LOCAL1;
            break;
        case LOCAL2:
            fac = SyslogConstants.FACILITY_LOCAL2;
            break;
        case LOCAL3:
            fac = SyslogConstants.FACILITY_LOCAL3;
            break;
        case LOCAL4:
            fac = SyslogConstants.FACILITY_LOCAL4;
            break;
        case LOCAL5:
            fac = SyslogConstants.FACILITY_LOCAL5;
            break;
        case LOCAL6:
            fac = SyslogConstants.FACILITY_LOCAL6;
            break;
        case LOCAL7:
            fac = SyslogConstants.FACILITY_LOCAL7;
            break;
        default:
            fac = SyslogConstants.FACILITY_USER;
        }
        return fac;
    }

    private int determineLogLevel(final OnmsSeverity severity) {
        int level;
        switch (severity) {
        case CRITICAL:
            level = SyslogConstants.LEVEL_CRITICAL;
            break;
        case MAJOR:
            level = SyslogConstants.LEVEL_ERROR;
            break;
        case MINOR:
            level = SyslogConstants.LEVEL_ERROR;
            break;
        case WARNING:
            level = SyslogConstants.LEVEL_WARN;
            break;
        case NORMAL:
            level = SyslogConstants.LEVEL_NOTICE;
            break;
        case CLEARED:
            level = SyslogConstants.LEVEL_INFO;
            break;
        case INDETERMINATE:
            level = SyslogConstants.LEVEL_DEBUG;
            break;
        default:
            level = SyslogConstants.LEVEL_WARN;
        }
        return level;
    }

    public SyslogNorthbounderConfig getConfig() {

        if (m_config == null) {
            String errMsg = "Syslog Northbounder configuration is not set.";
            LOG.error(errMsg);
            throw new IllegalStateException(errMsg);
        }
        return m_config;
    }

    public void setConfig(final SyslogNorthbounderConfig config) {

        if (config == null) {
            String string = "Syslog Northbounder configuration cannot be set null";
            LOG.error(string);
            throw new IllegalStateException(string);
        }

    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

}
