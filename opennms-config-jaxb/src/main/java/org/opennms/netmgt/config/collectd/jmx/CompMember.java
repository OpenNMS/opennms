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
import java.util.Comparator;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.api.AttributeType;

@XmlRootElement(name = "comp-member")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class CompMember implements Serializable, Comparable<CompMember> {

    private static final Comparator COMPARATOR = new Comparator<CompMember>() {
        @Override
        public int compare(final CompMember o1, final CompMember o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    @XmlAttribute(name = "name", required = true)
    private String _name;

    @XmlAttribute(name = "alias")
    private String _alias;

    @XmlAttribute(name = "type", required = true)
    private AttributeType _type;

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

    public AttributeType getType() {
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

    public void setType(final AttributeType type) {
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

    @Override
    public int compareTo(final CompMember o) {
        return Objects.compare(this, o, COMPARATOR);
    }

}
