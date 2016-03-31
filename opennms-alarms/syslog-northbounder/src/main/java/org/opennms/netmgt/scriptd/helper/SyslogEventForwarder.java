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
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.SyslogRuntimeException;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogFilter;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
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
            SyslogUtils.createNorthboundInstance(destination);
            initialized = true;
        } else {
            LOG.error("Can't load configuration from {}", configFile);
        }
    }

    /**
     * Shutdown.
     */
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
                    int level = SyslogUtils.determineLogLevel(OnmsSeverity.get(event.getSeverity()));
                    LOG.debug("Forwarding event: {} via syslog to destination: {}", event.getDbid(), destination.getName());
                    instance.log(level, syslogMessage);
                } else {
                    LOG.warn("Can't find message format for the incoming. Check your destination's configuration.");
                }
            } catch (Exception ex) {
                LOG.error("Caught exception sending to destination: '{}': {}", destination.getName(), ex);
            }

        } else {
            LOG.error("Can't forward event {} because the facility has not been initialized.", event.getUei());
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
        Expression exp = parser.parseExpression(msgFormat, new TemplateParserContext("${", "}"));
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

