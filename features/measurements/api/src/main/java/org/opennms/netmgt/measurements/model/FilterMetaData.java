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

import java.lang.reflect.Field;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Used to represent a {@link org.opennms.netmgt.measurements.api.Filter} and it's parameters.
 *
 * @author jwhite
 */
@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.NONE)
public class FilterMetaData {

    @XmlAttribute(name="canonicalName", required=true)
    private String canonicalName;

    @XmlAttribute(name="name", required=true)
    private String name;

    @XmlAttribute(name="description")
    private String description;

    @XmlAttribute(name="backend")
    private String backend;

    @XmlElement(name="parameter")
    private List<FilterParamMetaData> parameters;

    public FilterMetaData() { }

    /**
     * Reads the class and field annotations to populate the meta-data.
     */
    public FilterMetaData(Class<? extends Filter> type) {
        Preconditions.checkNotNull(type, "type argument");
        
        FilterInfo info = type.getAnnotation(FilterInfo.class);
        Preconditions.checkState(info != null, "Filters must be annotated with FilterInfo.");

        canonicalName = type.getCanonicalName();
        name = info.name();
        description = info.description();
        backend = info.backend();

        parameters = Lists.newArrayList();
        for(Field field : type.getDeclaredFields()) {
            FilterParam filterParam = field.getAnnotation(FilterParam.class);
            if (filterParam == null) {
                // Skip fields that are not annotated
                continue;
            }

            parameters.add(new FilterParamMetaData(field, filterParam));
        }
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBackend() {
        return backend;
    }

    public List<FilterParamMetaData> getParameters() {
        return parameters;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public void setParameters(List<FilterParamMetaData> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final FilterMetaData other = (FilterMetaData) obj;

       return   com.google.common.base.Objects.equal(this.canonicalName, other.canonicalName)
             && com.google.common.base.Objects.equal(this.name, other.name)
             && com.google.common.base.Objects.equal(this.description, other.description)
             && com.google.common.base.Objects.equal(this.backend, other.backend)
             && com.google.common.base.Objects.equal(this.parameters, other.parameters);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.canonicalName, this.name, this.description, this.backend, this.parameters);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("CanonicalName", this.canonicalName)
                 .add("Name", this.name)
                 .add("Description", this.description)
                 .add("Backend", this.backend)
                 .add("Parameters", this.parameters)
                 .toString();
    }
}
