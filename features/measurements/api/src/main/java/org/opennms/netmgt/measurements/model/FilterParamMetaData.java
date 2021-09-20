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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.base.Preconditions;

/**
 * Used to represent a {@link org.opennms.netmgt.measurements.api.Filter} parameters.
 *
 * @author jwhite
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class FilterParamMetaData {

    @XmlAttribute(name="key", required=true)
    private String key;

    @XmlAttribute(name="type", required=true)
    private String type;

    @XmlAttribute(name="displayName", required=true)
    private String displayName;

    @XmlAttribute(name="description", required=true)
    private String description;

    @XmlAttribute(name="default")
    private String defaultValue;

    @XmlAttribute(name="required", required=true)
    private boolean required = false;

    public FilterParamMetaData() { }

    public FilterParamMetaData(Field field, FilterParam filterParam) {
        Preconditions.checkNotNull(field, "field argument");
        Preconditions.checkNotNull(filterParam, "filterParam argument");

        key = filterParam.key();
        type = field.getType().getSimpleName().toLowerCase();
        displayName = filterParam.displayName();
        description = filterParam.description();
        defaultValue = filterParam.value();
        required = filterParam.required();

        if (required) {
            defaultValue = null;
        }
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final FilterParamMetaData other = (FilterParamMetaData) obj;

       return   com.google.common.base.Objects.equal(this.key, other.key)
             && com.google.common.base.Objects.equal(this.type, other.type)
             && com.google.common.base.Objects.equal(this.displayName, other.displayName)
             && com.google.common.base.Objects.equal(this.description, other.description)
             && com.google.common.base.Objects.equal(this.defaultValue, other.defaultValue)
             && com.google.common.base.Objects.equal(this.required, other.required);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.key, this.type, this.displayName, this.description, this.defaultValue, this.required);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("Key", this.key)
                 .add("Type", this.type)
                 .add("Display Name", this.displayName)
                 .add("Description", this.description)
                 .add("Default", this.defaultValue)
                 .add("Required", this.required)
                 .toString();
    }
}
