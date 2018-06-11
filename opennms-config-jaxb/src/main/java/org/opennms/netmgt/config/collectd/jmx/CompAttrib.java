/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collectd.jmx;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="comp-attrib")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class CompAttrib implements Serializable, Comparable<CompAttrib> {
    private static final Comparator<CompAttrib> COMPARATOR = new Comparator<CompAttrib>() {
        @Override
        public int compare(final CompAttrib o1, final CompAttrib o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    @XmlAttribute(name="name", required=true)
    private String _name;

    @XmlAttribute(name="alias")
    private String _alias;

    @XmlAttribute(name="type", required=true)
    private String _type;

    @XmlElement(name="comp-member")
    private java.util.List<CompMember> _compMemberList = new java.util.ArrayList<>();

    public void addCompMember(final CompMember compMember)
    throws IndexOutOfBoundsException {
        this._compMemberList.add(compMember);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof CompAttrib) {
            CompAttrib temp = (CompAttrib)obj;
            boolean equals = Objects.equals(this._name, temp._name)
                    && Objects.equals(this._alias, temp._alias)
                    && Objects.equals(this._type, temp._type)
                    && Objects.equals(this._compMemberList, temp._compMemberList);
            return equals;
        }
        return false;
    }

    public String getAlias() {
        return this._alias;
    }

    public java.util.List<CompMember> getCompMemberList() {
        return Collections.unmodifiableList(this._compMemberList);
    }

    public int getCompMemberCount() {
        return this._compMemberList.size();
    }

    public String getName() {
        return this._name;
    }

    public String getType() {
        return this._type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _alias, _type, _compMemberList);
    }

    public void setAlias(final String alias) {
        this._alias = alias;
    }

    public void setCompMemberList(final java.util.List<CompMember> vCompMemberList) {
        this._compMemberList.clear();
        this._compMemberList.addAll(vCompMemberList);
    }

    public void setName(final String name) {
        this._name = name;
    }

    public void setType(final String type) {
        this._type = type;
    }

    public void clearCompMembers() {
        this._compMemberList.clear();
    }

    @Override
    public int compareTo(final CompAttrib o) {
        return Objects.compare(this, o, COMPARATOR);
    }
}
