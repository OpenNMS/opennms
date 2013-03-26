/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.vmware.vijava;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
    private java.lang.String _type;

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
    public java.lang.String getType(
    ) {
        return this._type == null ? "" : this._type;
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
            final java.lang.String type) {
        this._type = type;
    }
}
