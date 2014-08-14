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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd.jmx;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "comp-member")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class CompMember implements java.io.Serializable {

    @XmlAttribute(name = "name", required = true)
    private String _name;

    @XmlAttribute(name = "alias")
    private String _alias;

    @XmlAttribute(name = "type", required = true)
    private String _type;

    @XmlAttribute(name = "maxval")
    private String _maxval;

    @XmlAttribute(name = "minval")
    private String _minval;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof CompMember) {
            CompMember temp = (CompMember) obj;
            boolean equals = Objects.equals(this._name, temp._name)
                    && Objects.equals(this._alias, temp._alias)
                    && Objects.equals(this._type, temp._type)
                    && Objects.equals(this._maxval, temp._maxval)
                    && Objects.equals(this._minval, temp._minval);
            return equals;
        }
        return false;
    }

    public String getAlias() {
        return this._alias;
    }

    public String getMaxval() {
        return this._maxval;
    }

    public String getMinval() {
        return this._minval;
    }

    public String getName() {
        return this._name;
    }

    public String getType() {
        return this._type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _alias, _type, _minval, _maxval);
    }

    public void setAlias(final String alias) {
        this._alias = alias;
    }

    public void setMaxval(final String maxval) {
        this._maxval = maxval;
    }

    public void setMinval(final String minval) {
        this._minval = minval;
    }

    public void setName(final String name) {
        this._name = name;
    }

    public void setType(final String type) {
        this._type = type;
    }

    /**
     * Converts this {@link org.opennms.netmgt.config.collectd.jmx.CompMember} to an {@link org.opennms.netmgt.config.collectd.jmx.Attrib}.
     * This is basically to use most of the APIs which have already been written for Attrib.
     *
     * @return The converted CompMember as an Attrib.
     */
    public Attrib toAttrib() {
        Attrib attrib = new Attrib();
        attrib.setAlias(_alias);
        attrib.setMaxval(_maxval);
        attrib.setMinval(_minval);
        attrib.setType(_type);
        attrib.setName(_name);
        return attrib;
    }

}
