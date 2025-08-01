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

import com.google.common.base.Preconditions;

/**
 * Used to set the value of a particular filter parameter.
 *
 * @author jwhite
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class FilterParamDef {

    @XmlAttribute(name="key", required=true)
    private String key;

    @XmlValue
    private String value;

    /**
     * Zero-arg constructor for JAXB.
     */
    public FilterParamDef() {
    }

    public FilterParamDef(String key, String value) {
        this.key = Preconditions.checkNotNull(key, "key argument");
        this.value = Preconditions.checkNotNull(value, "value argument");
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final FilterParamDef other = (FilterParamDef) obj;

       return   com.google.common.base.Objects.equal(this.key, other.key)
             && com.google.common.base.Objects.equal(this.value, other.value);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.key, this.value);
    }

    @Override
    public String toString() {
       return com.google.common.base.MoreObjects.toStringHelper(this)
                 .add("Key", this.key)
                 .add("Value", this.value)
                 .toString();
    }
}
