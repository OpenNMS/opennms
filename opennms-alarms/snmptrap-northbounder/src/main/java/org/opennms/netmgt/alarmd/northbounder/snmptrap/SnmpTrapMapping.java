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
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * The Class SnmpTrapMapping.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "mapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpTrapMapping {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(SnmpTrapMappingGroup.class);

    /** The name. */
    @XmlAttribute(name = "name", required = false)
    private String m_name;

    /** The rule. */
    @XmlElement(name = "rule", required = true)
    private String m_rule;

    /** The enterprise OID. */
    @XmlElement(name = "enterprise-oid", required = true)
    private String m_enterpriseOid;

    /** The m_generic. */
    @XmlElement(name = "generic", required = false, defaultValue = "6")
    private Integer m_generic;

    /** The m_specific. */
    @XmlElement(name = "specific", required = false)
    private Integer m_specific;

    /** The varbind mappings. */
    @XmlElement(name = "varbind", required = true)
    private List<VarbindMapping> m_varbindMappings = new ArrayList<>();

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
     * Gets the enterprise OID.
     *
     * @return the enterprise OID
     */
    public String getEnterpriseOid() {
        return m_enterpriseOid;
    }

    /**
     * Gets the generic.
     *
     * @return the generic
     */
    public Integer getGeneric() {
        return m_generic == null ? 6 : m_generic;
    }

    /**
     * Gets the specific.
     *
     * @return the specific
     */
    public Integer getSpecific() {
        return m_specific;
    }

    /**
     * Gets the varbind mappings.
     *
     * @return the varbind mappings
     */
    public List<VarbindMapping> getVarbindMappings() {
        return m_varbindMappings;
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
     * Sets the enterprise OID.
     *
     * @param enterpriseOid the new enterprise OID
     */
    public void setEnterpriseOid(String enterpriseOid) {
        this.m_enterpriseOid = enterpriseOid;
    }

    /**
     * Sets the generic.
     *
     * @param generic the new generic
     */
    public void setGeneric(Integer generic) {
        this.m_generic = generic;
    }

    /**
     * Sets the specific.
     *
     * @param specific the new specific
     */
    public void setSpecific(Integer specific) {
        this.m_specific = specific;
    }

    /**
     * Sets the varbind mappings.
     *
     * @param varbindMappings the new varbind mappings
     */
    public void setVarbindMappings(List<VarbindMapping> varbindMappings) {
        this.m_varbindMappings = varbindMappings;
    }

    /**
     * Verifies if the mapping object accepts a given northbound alarm.
     *
     * @param alarm the northbound alarm
     * @return true, if the alarm is accepted
     */
    public boolean accepts(NorthboundAlarm alarm) {
        StandardEvaluationContext context = new StandardEvaluationContext(alarm);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(getRule());
        boolean passed = false;
        try {
            passed = (Boolean)exp.getValue(context, Boolean.class);
        } catch (Exception e) {
            LOG.warn("mapping accepts: can't evaluate expression {} for alarm {} because: {}", getRule(), alarm.getUei(), e.getMessage());
        }
        LOG.debug("mapping accepts: checking {} ? {}", getRule(), passed);
        return passed;
    }

    /**
     * Gets the parameters list.
     *
     * @param alarm the northbound alarm
     * @return the parameters
     */
    public List<Parm> getParams(NorthboundAlarm alarm) {
        List<Parm> parms = new ArrayList<>();
        for (VarbindMapping vb : getVarbindMappings()) {
            Parm p = vb.getParameter(alarm);
            parms.add(p);
        }
        return parms;
    }

}
