/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.syslogd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * String against which to match the message body; interpreted
 *  as a substring or a regular expression according to the
 *  value of the "type" attribute
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "match")
@XmlAccessorType(XmlAccessType.FIELD)
public class Match implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Whether to interpret this match string as a simple
     *  substring or as a regular expression
     *  
     */
    @XmlAttribute(name = "type", required = true)
    private String type;

    /**
     * The match expression
     */
    @XmlAttribute(name = "expression", required = true)
    private String expression;

    /**
     * Whether to do the default mappings of matching-groups to
     *  event parameters (group 1 -> group1, etc.) if this is a
     *  regex match.
     *  
     */
    @XmlAttribute(name = "default-parameter-mapping")
    private Boolean defaultParameterMapping;

    public Match() {
    }

    /**
     */
    public void deleteDefaultParameterMapping() {
        this.defaultParameterMapping= null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Match) {
            Match temp = (Match)obj;
            boolean equals = Objects.equals(temp.type, type)
                && Objects.equals(temp.expression, expression)
                && Objects.equals(temp.defaultParameterMapping, defaultParameterMapping);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultParameterMapping'. The field
     * 'defaultParameterMapping' has the following description: Whether to do the
     * default mappings of matching-groups to
     *  event parameters (group 1 -> group1, etc.) if this is a
     *  regex match.
     *  
     * 
     * @return the value of field 'DefaultParameterMapping'.
     */
    public Boolean getDefaultParameterMapping() {
        return this.defaultParameterMapping != null ? this.defaultParameterMapping : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'expression'. The field 'expression' has the
     * following description: The match expression
     * 
     * @return the value of field 'Expression'.
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the following
     * description: Whether to interpret this match string as a simple
     *  substring or as a regular expression
     *  
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Method hasDefaultParameterMapping.
     * 
     * @return true if at least one DefaultParameterMapping has been added
     */
    public boolean hasDefaultParameterMapping() {
        return this.defaultParameterMapping != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            type, 
            expression, 
            defaultParameterMapping);
        return hash;
    }

    /**
     * Returns the value of field 'defaultParameterMapping'. The field
     * 'defaultParameterMapping' has the following description: Whether to do the
     * default mappings of matching-groups to
     *  event parameters (group 1 -> group1, etc.) if this is a
     *  regex match.
     *  
     * 
     * @return the value of field 'DefaultParameterMapping'.
     */
    public Boolean isDefaultParameterMapping() {
        return this.defaultParameterMapping != null ? this.defaultParameterMapping : Boolean.valueOf("true");
    }

    /**
     * Sets the value of field 'defaultParameterMapping'. The field
     * 'defaultParameterMapping' has the following description: Whether to do the
     * default mappings of matching-groups to
     *  event parameters (group 1 -> group1, etc.) if this is a
     *  regex match.
     *  
     * 
     * @param defaultParameterMapping the value of field 'defaultParameterMapping'.
     */
    public void setDefaultParameterMapping(final Boolean defaultParameterMapping) {
        this.defaultParameterMapping = defaultParameterMapping;
    }

    /**
     * Sets the value of field 'expression'. The field 'expression' has the
     * following description: The match expression
     * 
     * @param expression the value of field 'expression'.
     */
    public void setExpression(final String expression) {
        this.expression = expression;
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the following
     * description: Whether to interpret this match string as a simple
     *  substring or as a regular expression
     *  
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

}
