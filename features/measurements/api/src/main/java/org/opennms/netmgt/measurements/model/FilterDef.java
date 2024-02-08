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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * Used to reference a filter and set it's parameters.
 *
 * @author jwhite
 */
@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.NONE)
public class FilterDef {

    @XmlAttribute(name="name", required=true)
    private String name;

    @XmlElement(name="parameter")
    private List<FilterParamDef> parameters = Lists.newArrayListWithCapacity(0);

    /**
     * Zero-arg constructor for JAXB.
     */
    public FilterDef() {
    }

    public FilterDef(String name, String... paramNamesAndValues) {
        // Combine the varargs into key-value pairs
        if (paramNamesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must have an even number of parameter names and values");
        }
        List<FilterParamDef> parameters = Lists.newLinkedList();
        for (int i = 0; i < paramNamesAndValues.length; i+=2) {
            parameters.add(new FilterParamDef(
                    paramNamesAndValues[i], paramNamesAndValues[i+1]));
        }

        this.name = name;
        this.parameters = parameters;
    }

    public FilterDef(String name, List<FilterParamDef> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public List<FilterParamDef> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final FilterDef other = (FilterDef) obj;

       return   com.google.common.base.Objects.equal(this.name, other.name)
             && com.google.common.base.Objects.equal(this.parameters, other.parameters);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.name, this.parameters);
    }

    @Override
    public String toString() {
       return com.google.common.base.MoreObjects.toStringHelper(this)
                 .add("Name", this.name)
                 .add("Parameters", this.parameters)
                 .toString();
    }
}
