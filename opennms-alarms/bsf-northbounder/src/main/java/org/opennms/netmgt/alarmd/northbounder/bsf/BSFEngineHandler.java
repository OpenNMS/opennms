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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.Destination;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Configuration for the BSF engine.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "engine")
@XmlAccessorType(XmlAccessType.FIELD)
public class BSFEngineHandler implements Destination {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6351398964945801319L;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(BSFEngineHandler.class);

    /** The engine name. */
    @XmlElement(name = "name", required = true)
    private String m_name;

    /** The filter. */
    @XmlElement(name = "filter", required = false)
    private String m_filter;

    /** The engine language. */
    @XmlElement(name = "language", required = false, defaultValue = "beanshell")
    private String m_language;

    /** The engine class name. */
    @XmlElement(name = "className", required = false, defaultValue = "bsh.util.BeanShellBSFEngine")
    private String m_className;

    /** The engine file extensions. */
    @XmlElement(name = "extensions", required = false, defaultValue = "bsh")
    private String m_extensions;

    /** The on-start content. */
    @XmlElement(name = "onStart", required = false)
    private String m_onStart;

    /** The on-stop content. */
    @XmlElement(name = "onStop", required = false)
    private String m_onStop;

    /** The on-alarm content. */
    @XmlElement(name = "onAlarm", required = true)
    private String m_onAlarm;

    /**
     * Instantiates a new BSF engine handler.
     */
    public BSFEngineHandler() {}

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#isFirstOccurrenceOnly()
     */
    @Override
    public boolean isFirstOccurrenceOnly() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.m_name = name;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return m_filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter the new filter
     */
    public void setFilter(String filter) {
        this.m_filter = filter;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage() {
        return m_language == null ? "beanshell" : m_language;
    }

    /**
     * Sets the language.
     *
     * @param language the new language
     */
    public void setLanguage(String language) {
        this.m_language = language;
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return m_className == null ? "bsh.util.BeanShellBSFEngine" : m_className;
    }

    /**
     * Sets the class name.
     *
     * @param className the new class name
     */
    public void setClassName(String className) {
        this.m_className = className;
    }

    /**
     * Gets the extensions.
     *
     * @return the extensions
     */
    public String getExtensions() {
        return m_extensions == null ? "bsh" : m_extensions;
    }

    /**
     * Sets the extensions.
     *
     * @param extensions the new extensions
     */
    public void setExtensions(String extensions) {
        this.m_extensions = extensions;
    }

    /**
     * Gets the on start.
     *
     * @return the on start
     */
    public String getOnStart() {
        return m_onStart;
    }

    /**
     * Sets the on start.
     *
     * @param onStart the new on start
     */
    public void setOnStart(String onStart) {
        this.m_onStart = onStart;
    }

    /**
     * Gets the on stop.
     *
     * @return the on stop
     */
    public String getOnStop() {
        return m_onStop;
    }

    /**
     * Sets the on stop.
     *
     * @param onStop the new on stop
     */
    public void setOnStop(String onStop) {
        this.m_onStop = onStop;
    }

    /**
     * Gets the on alarm.
     *
     * @return the on alarm
     */
    public String getOnAlarm() {
        return m_onAlarm;
    }

    /**
     * Sets the on alarm.
     *
     * @param onAlarm the new on alarm
     */
    public void setOnAlarm(String onAlarm) {
        this.m_onAlarm = onAlarm;
    }

    /**
     * Accepts.
     * <p>If the engine doesn't have filter, the method will return true.</p>
     * <p>If the method has a filter, it will be evaluated.</p>
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    public boolean accepts(NorthboundAlarm alarm) {
        if (getFilter() != null) {
            StandardEvaluationContext context = new StandardEvaluationContext(alarm);
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(m_filter);
            boolean passed = false;
            try {
                passed = (Boolean)exp.getValue(context, Boolean.class);
            } catch (Exception e) {
                LOG.warn("accepts: can't evaluate expression {} for alarm {} because: {}", getFilter(), alarm.getUei(), e.getMessage());
            }
            LOG.debug("accepts: checking {} ? {}", m_filter, passed);
            return passed;
        }
        return true;
    }

}
