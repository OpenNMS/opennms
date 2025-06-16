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
package org.opennms.netmgt.config.vmware.vijava;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.netmgt.collection.api.AttributeType;

/**
 * An Attribute Object
 */
@XmlRootElement(name = "attrib")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class Attrib implements java.io.Serializable {

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private java.lang.String _name;

    /**
     * Field _alias.
     */
    @XmlAttribute(name = "alias")
    private java.lang.String _alias;

    /**
     * Field _type.
     */
    @XmlAttribute(name = "type")
    private AttributeType _type;

    public Attrib() {
        super();
    }

    /**
     * Overrides the java.lang.Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if (obj instanceof Attrib) {
            Attrib other = (Attrib) obj;
            return new EqualsBuilder()
                    .append(getName(), other.getName())
                    .append(getAlias(), other.getAlias())
                    .append(getType(), other.getType())
                    .isEquals();
        }
        return false;
    }

    /**
     * Returns the value of field 'alias'.
     *
     * @return the value of field 'Alias'.
     */
    public java.lang.String getAlias(
    ) {
        return this._alias == null ? "" : this._alias;
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name == null ? "" : this._name;
    }

    /**
     * Returns the value of field 'type'.
     *
     * @return the value of field 'Type'.
     */
    public AttributeType getType(
    ) {
        return this._type;
    }

    /**
     * Sets the value of field 'alias'.
     *
     * @param alias the value of field 'alias'.
     */
    public void setAlias(
            final java.lang.String alias) {
        this._alias = alias;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'type'.
     *
     * @param type the value of field 'type'.
     */
    public void setType(
            final AttributeType type) {
        this._type = type;
    }
}
