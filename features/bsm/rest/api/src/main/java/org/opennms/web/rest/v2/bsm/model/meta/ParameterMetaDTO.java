/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm.model.meta;

import java.lang.reflect.Field;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class ParameterMetaDTO {

    @XmlAttribute(name="key", required=true)
    private String key;

    @XmlAttribute(name="type", required=true)
    private String type;

    @XmlAttribute(name="description", required=true)
    private String description;

    // At the moment we do not have an optional parameter, but we may get it in the future
    @XmlAttribute(name="required", required=true)
    private boolean required = true;

    // JAXB requires a no-arg constructor
    public ParameterMetaDTO() {

    }

    public ParameterMetaDTO(Field field, Parameter parameter) {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(parameter, "parameter must not be null");

        key = parameter.key();
        type = field.getType().getSimpleName().toLowerCase();
        description = parameter.description();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParameterMetaDTO other = (ParameterMetaDTO) obj;
        return   Objects.equals(this.key, other.key)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.required, other.required);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.description, this.type, this.required);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("Key", this.key)
                .add("Description", this.description)
                .add("Type", this.type)
                .add("Required", this.required)
                .toString();
    }
}
