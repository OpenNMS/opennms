/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
 * Configuration for the various filters to change the behavior of the forwarder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "syslog-filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogFilter {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SyslogFilter.class);

    /** The enabled flag. */
    @XmlAttribute(name="enabled", required=false)
    private Boolean m_enabled;

    /** The filter name. */
    @XmlAttribute(name="name", required=false)
    private String m_name;

    /** The filter rule. */
    @XmlElement(name="rule", required=true)
    private String m_rule;

    /** The m_message format. */
    @XmlElement(name="message-format", required=false)
    private String m_messageFormat;

    /**
     * Instantiates a new Syslog filter.
     */
    public SyslogFilter() {
    }

    /**
     * Instantiates a new Syslog filter.
     *
     * @param name the name
     * @param rule the rule
     * @param messageFormat the message format
     */
    public SyslogFilter(String name, String rule, String messageFormat) {
        super();
        this.m_name = name;
        this.m_rule = rule;
        this.m_messageFormat = messageFormat;
    }

    /**
     * Checks if the rule is enabled.
     *
     * @return true, if is enabled
     */
    public Boolean isEnabled() {
        return m_enabled == null ? Boolean.TRUE : m_enabled;
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the filter rule.
     *
     * @return the filter rule
     */
    public String getRule() {
        return m_rule;
    }

    /**
     * Gets the message format.
     *
     * @return the message format
     */
    public String getMessageFormat() {
        return m_messageFormat;
    }

    /**
     * Sets the enabled flag.
     *
     * @param enabled the new enabled
     */
    public void setEnabled(Boolean enabled) {
        this.m_enabled = enabled;
    }

    /**
     * Sets the filter name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.m_name = name;
    }

    /**
     * Sets the filter rule.
     *
     * @param rule the rule
     */
    public void setRule(String rule) {
        this.m_rule = rule;
    }

    /**
     * Sets the message format.
     *
     * @param messageFormat the new message format
     */
    public void setMessageFormat(String messageFormat) {
        this.m_messageFormat = messageFormat;
    }

    /**
     * Pass filter.
     *
     * @param alarm the alarm
     * @return true, if successful
     */
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
