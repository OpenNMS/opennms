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
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("Key", this.key)
                .add("Description", this.description)
                .add("Type", this.type)
                .add("Required", this.required)
                .toString();
    }
}
