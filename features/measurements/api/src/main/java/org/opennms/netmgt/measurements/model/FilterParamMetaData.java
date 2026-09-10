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

import java.lang.reflect.Field;

import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(name = "key", required = true,
            description = "Parameter name, the value to put in a filter's parameter[].key.",
            example = "cutoffDate")
    public String getKey() {
        return key;
    }

    // Derived by reflection from the filter field, so the set is open rather than an enumeration.
    @Schema(name = "type", required = true,
            description = "Declared type the value is coerced to, the lower-cased simple name of the filter field's "
                    + "type. The shipped filters use boolean, double, int, long and string.",
            example = "double")
    public String getType() {
        return type;
    }

    @Schema(name = "displayName", required = true,
            description = "Short label suitable for a form field.",
            example = "Cutoff")
    public String getDisplayName() {
        return displayName;
    }

    @Schema(name = "description", required = true,
            description = "Human readable description of the parameter.",
            example = "Timestamp in milliseconds. Any rows before this time will be removed.")
    public String getDescription() {
        return description;
    }

    // Wire name is "default", not the bean name. Null for a required parameter.
    @Schema(name = "default",
            description = "Value used when the parameter is left out of the request. Null when the parameter is "
                    + "required.",
            example = "0")
    public String getDefaultValue() {
        return defaultValue;
    }

    @Schema(name = "required", required = true,
            description = "Whether the filter fails when this parameter is absent.",
            example = "false")
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
       return com.google.common.base.MoreObjects.toStringHelper(this)
                 .add("Key", this.key)
                 .add("Type", this.type)
                 .add("Display Name", this.displayName)
                 .add("Description", this.description)
                 .add("Default", this.defaultValue)
                 .add("Required", this.required)
                 .toString();
    }
}
