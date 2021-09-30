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
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@XmlRootElement(name="function")
@XmlAccessorType(XmlAccessType.NONE)
public class FunctionMetaDTO {

    @XmlAttribute
    private FunctionType type;

    @XmlAttribute(name="name", required=true)
    private String name;

    @XmlAttribute(name="description")
    private String description;

    @XmlElement(name="parameter")
    private List<ParameterMetaDTO> parameters;

    // JAXB requires a no-arg constructor
    public FunctionMetaDTO() {

    }

    /**
     * Reads the class and field annotations to populate the meta-data.
     */
    public FunctionMetaDTO(Class<?> function, FunctionType type) {
        Objects.requireNonNull(function, "function must not be null");
        Objects.requireNonNull(type, "type must not be null");

        Function functionAnnotation = function.getAnnotation(Function.class);
        Preconditions.checkState(functionAnnotation != null, "Functions must be annotated with @Function.");
        this.type = type;
        name = functionAnnotation.name();
        description = functionAnnotation.description();

        parameters = Lists.newArrayList();
        for(Field field : function.getDeclaredFields()) {
            Parameter filterParam = field.getAnnotation(Parameter.class);
            if (filterParam != null) { // only consider annotated fields
                parameters.add(new ParameterMetaDTO(field, filterParam));
            }
        }
    }

    public FunctionType getType() {
        return type;
    }

    public void setType(FunctionType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterMetaDTO> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetaDTO> parameters) {
        this.parameters = parameters;
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
        final FunctionMetaDTO other = (FunctionMetaDTO) obj;

        final boolean equals = Objects.equals(this.name, other.name)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.parameters, other.parameters);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.description, this.type, this.parameters);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("Name", this.name)
                .add("Description", this.description)
                .add("Type", this.type)
                .add("Parameters", this.parameters)
                .toString();
    }
}
