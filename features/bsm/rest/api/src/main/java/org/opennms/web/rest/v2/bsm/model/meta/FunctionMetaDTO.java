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
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("Name", this.name)
                .add("Description", this.description)
                .add("Type", this.type)
                .add("Parameters", this.parameters)
                .toString();
    }
}
