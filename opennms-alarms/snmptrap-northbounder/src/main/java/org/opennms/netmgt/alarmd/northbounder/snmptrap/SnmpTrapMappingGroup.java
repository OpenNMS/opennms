/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;

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
 * The Class SnmpTrapMappingGroup.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "mapping-group")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpTrapMappingGroup {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(SnmpTrapMappingGroup.class);

    /** The name. */
    @XmlAttribute(name = "name", required = false)
    private String m_name;

    /** The rule. */
    @XmlElement(name = "rule", required = true)
    private String m_rule;

    /** The varbind mappings. */
    @XmlElement(name = "mapping", required = true)
    private List<SnmpTrapMapping> m_mappings = new ArrayList<>();

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the rule.
     *
     * @return the rule
     */
    public String getRule() {
        return m_rule;
    }

    /**
     * Gets the mappings.
     *
     * @return the mappings
     */
    public List<SnmpTrapMapping> getMappings() {
        return m_mappings;
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
     * Sets the rule.
     *
     * @param rule the new rule
     */
    public void setRule(String rule) {
        this.m_rule = rule;
    }

    /**
     * Sets the mappings.
     *
     * @param mappings the new mappings
     */
    public void setMappings(List<SnmpTrapMapping> mappings) {
        this.m_mappings = mappings;
    }

    /**
     * Verifies if the mapping group object accepts a given northbound alarm.
     *
     * @param alarm the northbound alarm
     * @return true, if the alarm is accepted.
     */
    public boolean accepts(NorthboundAlarm alarm) {
        StandardEvaluationContext context = new StandardEvaluationContext(alarm);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(getRule());
        boolean passed = false;
        try {
            passed = (Boolean) exp.getValue(context, Boolean.class);
            if (passed) {
                boolean mappingAccepted = false;
                for (SnmpTrapMapping mapping : getMappings()) {
                    if (mapping.accepts(alarm)) {
                        mappingAccepted = true;
                        break;
                    }
                }
                passed = mappingAccepted;
            }
        } catch (Exception e) {
            LOG.warn("mapping group accepts: can't evaluate expression {} for alarm {} because: {}", getRule(), alarm.getUei(), e.getMessage());
        }
        LOG.debug("mapping group accepts: {} ? {}", getRule(), passed);
        return passed;
    }

}
