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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Configuration for the various filters to change the behavior of the forrwarder.
 * 
 * @author <a href="agalue@opennms.org>Alejandro Galue</a>
 */
@XmlRootElement(name = "syslog-filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogFilter.class);

    @XmlAttribute(name="enabled", required=false)
    private boolean m_enabled = true;

    @XmlElement(name="name", required=false)
    private String m_name = "Undefined";

    @XmlElement(name="rule", required=true)
    private String m_rule = "localhost";

    @XmlElement(name="destination", required=true)
    private String m_destination;

    @XmlElement(name="message-format", required=false)
    private String m_messageFormat;

    public SyslogFilter() {
    }

    public SyslogFilter(String name, String rule, String destination) {
        super();
        this.m_name = name;
        this.m_rule = rule;
        this.m_destination = destination;
    }

    public boolean isEnabled() {
        return m_enabled;
    }

    public String getName() {
        return m_name;
    }

    public String getRule() {
        return m_rule;
    }

    public String getDestination() {
        return m_destination;
    }

    public void setEnabled(boolean m_enabled) {
        this.m_enabled = m_enabled;
    }

    public void setName(String m_name) {
        this.m_name = m_name;
    }

    public void seRule(String m_rule) {
        this.m_rule = m_rule;
    }

    public void setDestination(String m_destination) {
        this.m_destination = m_destination;
    }

    public String getMessageFormat() {
        return m_messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.m_messageFormat = messageFormat;
    }

    public boolean passFilter(NorthboundAlarm alarm) {
        if (!isEnabled()) {
            return false;
        }
        StandardEvaluationContext context = new StandardEvaluationContext(alarm);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(getRule());
        boolean passed = false;
        try {
            passed = (Boolean)exp.getValue(context, Boolean.class);
        } catch (Exception e) {
            LOG.warn("passFilter: can't evaluate expression {} for alarm {} because: {}", getRule(), alarm.getUei(), e.getMessage());
        }
        LOG.debug("passFilter: checking {} ? {}", getRule(), passed);
        return passed;
    }
}
