/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents a JEXL expression.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
@XmlRootElement(name="expression")
@XmlAccessorType(XmlAccessType.NONE)
public class Expression {
    /**
     * Used to identify the results of this expression.
     * Should be unique amongst all sources and other expressions.
     */
    private String label;

    /**
     * JEXL expression.
     * Should return a Double value.
     */
    private String expression;

    /**
     * Enable to exclude the results of this expression from the
     * response, but allow other expressions to derive values from it.
     */
    private boolean isTransient = false;

    public Expression() {
    }

    public Expression(final String label,
                      final String expression,
                      final boolean isTransient) {
        this.label = label;
        this.expression = expression;
        this.isTransient = isTransient;
    }

    @XmlAttribute(name = "label")
    @JsonProperty("label")
    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    @XmlValue
    @JsonProperty("value")
    public String getExpression() {
        return this.expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    @XmlAttribute(name = "transient")
    @JsonProperty("transient")
    public boolean getTransient() {
        return isTransient;
    };

    public void setTransient(final boolean isTransient) {
        this.isTransient = isTransient;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final Expression other = (Expression) obj;

       return   com.google.common.base.Objects.equal(this.label, other.label)
             && com.google.common.base.Objects.equal(this.expression, other.expression)
             && com.google.common.base.Objects.equal(this.isTransient, other.isTransient);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.label, this.expression, this.isTransient);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("Label", this.label)
                 .add("Expression", this.expression)
                 .add("Transient", this.isTransient)
                 .toString();
    }
}
