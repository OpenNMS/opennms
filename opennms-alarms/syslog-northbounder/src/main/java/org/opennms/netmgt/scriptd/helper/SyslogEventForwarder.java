/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.scriptd.helper;

import java.io.File;
import java.io.IOException;

import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.SyslogRuntimeException;
import org.graylog2.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.graylog2.syslog4j.impl.net.udp.UDPNetSyslogConfig;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination.SyslogFacility;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination.SyslogProtocol;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogFilter;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * The Class SyslogEventForwarder.
 * <p>The implementation of this class is inspired by <code>SyslogNorthbounder</code></p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SyslogEventForwarder {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SyslogEventForwarder.class);

    /** The destination. */
    private SyslogDestination destination;

    /** The initialized. */
    private boolean initialized = false;

    /**
     * Initialize.
     *
     * @param destinationName the destination name
     */
    public void initialize(String destinationName) {
        File configFile = getConfigFile(destinationName);
        if (configFile != null && configFile.exists()) {
            LOG.info("Initializing Syslog instance for destination: '{}'", destinationName);
            destination = JaxbUtils.unmarshal(SyslogDestination.class, configFile);
            destination.setName(destinationName); // To avoid potential conflicts.
            int facility = convertFacility(destination.getFacility());
            SyslogProtocol protocol = destination.getProtocol();
            SyslogConfigIF instanceConfiguration = createConfig(destination, protocol, facility);
            instanceConfiguration.setIdent("OpenNMS");
            instanceConfiguration.setCharSet(destination.getCharSet());
            instanceConfiguration.setMaxMessageLength(destination.getMaxMessageLength());
            instanceConfiguration.setSendLocalName(destination.isSendLocalName());
            instanceConfiguration.setSendLocalTimestamp(destination.isSendLocalTime());
            instanceConfiguration.setTruncateMessage(destination.isTruncateMessage());
            instanceConfiguration.setUseStructuredData(SyslogConstants.USE_STRUCTURED_DATA_DEFAULT);
            try {
                Syslog.createInstance(destination.getName(), instanceConfiguration);
                initialized = true;
            } catch (SyslogRuntimeException e) {
                LOG.error("Could not create event syslog forwarder instance, '{}': {}", destination.getName(), e);
            }
        } else {
            LOG.error("Can't load configuration from {}", configFile);
        }
    }

    public void shutdown() {
        if (initialized) {
            try {
                LOG.info("Shutting down Syslog instance for destination: '{}'", destination.getName());
                initialized = false;
                SyslogIF instance = Syslog.getInstance(destination.getName());
                Syslog.destroyInstance(instance);
            } catch (SyslogRuntimeException e) {
                LOG.error("Could not find Syslog instance for destination: '{}': {}", destination.getName(), e);
                return;
            }
        } else {
            LOG.error("Can't shutdown a SYslog instance for an uninitialized forwarder.");
        }
    }

    /**
     * Reloads the forwarder.
     */
    public void reload() {
        if (initialized) {
            LOG.info("Reloading Syslog instance for destination: '{}'", destination.getName());
            shutdown();
            initialize(destination.getName());
        } else {
            LOG.error("Can't reload configuration for an uninitialized forwarder.");
        }
    }

    /**
     * Forwards an event.
     *
     * @param event the event to be forwarded
     * @param node the node associated with the event if apply
     */
    public void forward(Event event, OnmsNode node) {
        if (initialized) {
            LOG.info("Forwarding event {} to destination:{}", event.getUei(), destination.getName());
            SyslogIF instance;
            try {
                instance = Syslog.getInstance(destination.getName());
            } catch (SyslogRuntimeException e) {
                LOG.error("Could not find Syslog instance for destination: '{}': {}", destination.getName(), e);
                return;
            }
            try {
                LOG.debug("Making substitutions for tokens in message format for event: {}.", event.getDbid());
                String msgFormat = null;
                for (SyslogFilter filter : destination.getFilters()) {
                    if (passFilter(filter, event)) {
                        msgFormat = filter.getMessageFormat();
                    }
                }
                if (msgFormat != null) {
                    String syslogMessage = getTranslatedMessage(event, node, msgFormat);
                    LOG.debug("Determining LOG_LEVEL for event: {}", event.getDbid());
                    int level = determineLogLevel(event.getSeverity());
                    LOG.debug("Forwarding event: {} via syslog to destination: {}", event.getDbid(), destination.getName());
                    instance.log(level, syslogMessage);
                } else {
                    LOG.warn("Can't find message format for the incoming. Check your destination's configuration.");
                }
            } catch (Exception ex) {
                LOG.error("Caught exception sending to destination: '{}': {}", destination.getName(), ex);
            }

        } else {
            LOG.error("Can't forward event {} because the facility as not been initialized.", event.getUei());
        }
    }

    /**
     * Gets the configuration file.
     *
     * @param destinationName the destination name
     * @return the configuration file
     */
    private File getConfigFile(String destinationName) {
        try {
            return ConfigFileConstants.getConfigFileByName("syslog-eventforwarder-" + destinationName + ".xml");
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Creates the syslog configuration.
     *
     * @param dest the syslog destination object
     * @param protocol the syslog protocol
     * @param fac the syslog facility
     * @return the syslog configuration object
     */
    private SyslogConfigIF createConfig(final SyslogDestination dest, final SyslogProtocol protocol, int fac) {
        SyslogConfigIF config;
        switch (protocol) {
        case UDP:
            config = new UDPNetSyslogConfig(fac, dest.getHost(), dest.getPort());
            break;
        case TCP:
            config = new TCPNetSyslogConfig(fac, dest.getHost(), dest.getPort());
            break;
        default:
            config = new UDPNetSyslogConfig(fac, "localhost", 514);
        }
        return config;
    }

    /**
     * Convert facility.
     *
     * @param facility the syslog facility
     * @return the integer version of the facility
     */
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

    /**
     * Determine log level.
     *
     * @param severity the severity
     * @return the integer version of the facility
     */
    private int determineLogLevel(final String severity) {
        int level;
        switch (severity.toUpperCase()) {
        case "CRITICAL":
            level = SyslogConstants.LEVEL_CRITICAL;
            break;
        case "MAJOR":
            level = SyslogConstants.LEVEL_ERROR;
            break;
        case "MINOR":
            level = SyslogConstants.LEVEL_ERROR;
            break;
        case "WARNING":
            level = SyslogConstants.LEVEL_WARN;
            break;
        case "NORMAL":
            level = SyslogConstants.LEVEL_NOTICE;
            break;
        case "CLEARED":
            level = SyslogConstants.LEVEL_INFO;
            break;
        case "INDETERMINATE":
            level = SyslogConstants.LEVEL_DEBUG;
            break;
        default:
            level = SyslogConstants.LEVEL_WARN;
        }
        return level;
    }

    /**
     * Pass filter.
     *
     * @param filter the filter
     * @param event the event
     * @return true, if successful
     */
    private boolean passFilter(SyslogFilter filter, Event event) {
        StandardEvaluationContext context = new StandardEvaluationContext(event);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(filter.getRule());
        boolean passed = false;
        try {
            passed = (Boolean)exp.getValue(context, Boolean.class);
        } catch (Exception e) {
            LOG.warn("passFilter: can't evaluate expression {} for alarm {} because: {}", filter.getRule(), event.getUei(), e.getMessage());
        }
        LOG.debug("passFilter: checking {} ? {}", filter.getRule(), passed);
        return passed;
    }

    /**
     * Gets the translated message.
     *
     * @param event the event
     * @param node the node
     * @param msgFormat the message format
     * @return the translated message
     */
    private String getTranslatedMessage(Event event, OnmsNode node, String msgFormat) {
        StandardEvaluationContext context = new StandardEvaluationContext(event);
        if (node != null) {
            context.setVariable("node", node);
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(msgFormat, new TemplateParserContext());
        try {
            final String msg = (String) exp.getValue(context, String.class);
            LOG.debug("getTranslatedMessage: {} ==> {}", msgFormat, msg);
            return msg;
        } catch (Exception e) {
            LOG.warn("getTranslatedMessage: can't evaluate expression {} for alarm {} because: {}", msgFormat, event.getUei(), e.getMessage());
        }
        return null;
    }

}

