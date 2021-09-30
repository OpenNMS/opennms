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
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("Name", this.name)
                 .add("Parameters", this.parameters)
                 .toString();
    }
}
