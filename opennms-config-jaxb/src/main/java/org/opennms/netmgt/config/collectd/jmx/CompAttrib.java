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
