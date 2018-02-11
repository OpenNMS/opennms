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
 * The Class VarbindMapping.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "varbind")
@XmlAccessorType(XmlAccessType.FIELD)
public class VarbindMapping {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(VarbindMapping.class);

    /** The name. */
    @XmlAttribute(name = "name", required = false)
    private String m_name;

    /** The OID. */
    @XmlElement(name = "oid", required = true)
    private String m_oid;

    /** The type. */
    @XmlElement(name = "type", required = true)
    private VarbindType m_type;

    /** The value expression. */
    @XmlElement(name = "value", required = true)
    private String m_value;

    /** The max number of characters for the value when the type is DISPLAY_STRING. */
    @XmlElement(name = "max", required = false)
    private Integer m_max;

    /** The instance (could be a numeric value or a string). */
    @XmlElement(name = "instance", required = false)
    private String m_instance;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the OID.
     *
     * @return the OID
     */
    public String getOid() {
        return m_oid;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public VarbindType getType() {
        return m_type;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Gets the varbind instance
     *
     * @return varbind instance
     */
    public String getInstance() {
        return m_instance;
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public Integer getMax() {
        return m_max;
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
     * Sets the OID.
     *
     * @param oid the new OID
     */
    public void setOid(String oid) {
        this.m_oid = oid;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(VarbindType type) {
        this.m_type = type;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.m_value = value;
    }

    /**
     * Sets the varbind instance.
     *
     * @param instance the new varbind instance
     */
    public void setInstance(String instance) {
        this.m_instance = instance;
    }

    /**
     * Sets the max.
     *
     * @param max the new max
     */
    public void setMax(Integer max) {
        this.m_max = max;
    }

    /**
     * Gets the evaluated parameter object based on a given northbound alarm
     *
     * @param alarm the alarm
     * @return the parameter
     */
    public Parm getParameter(NorthboundAlarm alarm) {
        String oid = getOid();
        String instance = evaluate(getInstance(), alarm);
        if (instance != null) {
            oid += isNumber(instance) ? "." + instance : encode(instance);
        }
        String value = evaluate(getValue(), alarm);
        if (getType().equals(VarbindType.TYPE_SNMP_OCTET_STRING) && getMax() > 0 && getType().value().length() > getMax()) {
            value = value.substring(0, getMax());
        }
        final Parm parm = new Parm(oid, value);
        parm.getValue().setType(getType().value());
        return parm;
    }

    /**
     * Encodes a string into a SNMP OID
     *
     * @param parameterInstance the parameter instance
     * @return the string
     */
    private String encode(String parameterInstance) {
        int snLength = parameterInstance.length();
        final StringBuilder oidBuf = new StringBuilder();
        oidBuf.append(".").append(Integer.toString(snLength));
        for (byte thisByte : parameterInstance.getBytes()) {
            oidBuf.append(".").append(Byte.toString(thisByte));
        }
        return oidBuf.toString();
    }

    /**
     * Checks if is number.
     *
     * @param text the string to check
     * @return true, if is number
     */
    private boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Evaluates a SPEL expression based on a given northbound alarm
     *
     * @param expression the expression
     * @param alarm the northbound alarm
     * @return the string
     */
    private String evaluate(String expression, NorthboundAlarm alarm) {
        if (expression == null) {
            return null;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext(alarm);
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(expression);
            return (String) exp.getValue(context, String.class);
        } catch (Exception e) {
            LOG.warn("Can't evaluate expression {} for alarm {} because: {}", getValue(), alarm.getUei(), e.getMessage());
        }
        return null;
    }
}
