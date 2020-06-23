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

package org.opennms.netmgt.alarmd.northbounder.email;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Configuration for the various filters to change the behavior of the forwarder.
 * 
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "email-filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailFilter {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EmailFilter.class);

    /** The enabled flag. */
    @XmlAttribute(name="enabled", required=false)
    private Boolean m_enabled;

    /** The filter name. */
    @XmlAttribute(name="name", required=false)
    private String m_name;

    /** The filter rule. */
    @XmlElement(name="rule", required=true)
    private String m_rule;

    /** The email message field: from. */
    @XmlElement(name="from", required=false)
    private String m_from;

    /** The email message field: to. */
    @XmlElement(name="to", required=false)
    private String m_to;

    /** The template for the email message field: subject. */
    @XmlElement(name="subject", required=false)
    private String m_subject;

    /** The template for the email message field: body. */
    @XmlElement(name="body", required=false)
    private String m_body;

    /**
     * Instantiates a new Email filter.
     */
    public EmailFilter() {
    }

    /**
     * Instantiates a new Email filter.
     *
     * @param name the name
     * @param rule the rule
     */
    public EmailFilter(String name, String rule) {
        super();
        this.m_name = name;
        this.m_rule = rule;
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
     * Gets the email message field: from.
     *
     * @return the from field
     */
    public String getFrom() {
        return m_from;
    }

    /**
     * Gets the email message field: to.
     *
     * @return the to field
     */
    public String getTo() {
        return m_to;
    }

    /**
     * Gets the template of the email message field: subject.
     *
     * @return the subject field template
     */
    public String getSubject() {
        return m_subject;
    }

    /**
     * Gets the template of the email message field: body.
     *
     * @return the body field template
     */
    public String getBody() {
        return m_body;
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
     * Sets the from.
     *
     * @param from the new from
     */
    public void setFrom(String from) {
        this.m_from = from;
    }

    /**
     * Sets the to.
     *
     * @param to the new to
     */
    public void setTo(String to) {
        this.m_to = to;
    }

    /**
     * Sets the subject.
     *
     * @param subject the new subject
     */
    public void setSubject(String subject) {
        this.m_subject = subject;
    }

    /**
     * Sets the body.
     *
     * @param body the new body
     */
    public void setBody(String body) {
        this.m_body = body;
    }

    /**
     * Checks if the alarm is accepted by the filter.
     * 
     * @param alarm the northbound alarm
     * @return true, if successful
     */
    public boolean accepts(NorthboundAlarm alarm) {
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
            LOG.warn("accepts: can't evaluate expression {} for alarm {} because: {}", getRule(), alarm.getUei(), e.getMessage());
        }
        LOG.debug("accepts: checking {} ? {}", getRule(), passed);
        return passed;
    }

    /**
     * Updates the sendmail message object
     *
     * @param message the message object
     */
    public void update(SendmailMessage message) {
        if (m_from != null) {
            message.setFrom(m_from);
        }
        if (m_to != null) {
            message.setTo(m_to);
        }
        if (m_subject != null) {
            message.setSubject(m_subject);
        }
        if (m_body != null) {
            message.setBody(m_body);
        }
    }
}
