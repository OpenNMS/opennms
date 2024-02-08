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
       return com.google.common.base.MoreObjects.toStringHelper(this)
                 .add("Label", this.label)
                 .add("Expression", this.expression)
                 .add("Transient", this.isTransient)
                 .toString();
    }
}
