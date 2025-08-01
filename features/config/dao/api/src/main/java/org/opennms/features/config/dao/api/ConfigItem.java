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
package org.opennms.features.config.dao.api;

import org.opennms.features.config.exception.SchemaConversionException;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConfigItem {
    private String name;

    /**
     * A reference to the related element in the source schema.
     */
    private String schemaRef;
    private Type type;
    private List<ConfigItem> children = new LinkedList<>();
    private Long min = null;
    private Long max = null;
    private boolean maxExclusive = false;
    private boolean minExclusive = false;
    private Long multipleOf = null;
    private String pattern;
    private Object defaultValue;
    private String documentation;
    private List<String> enumValues;

    private boolean required = false;

    public enum Type {
        OBJECT,
        ARRAY,
        STRING,
        NUMBER,
        INTEGER,
        LONG,
        BOOLEAN,
        DATE,  //YYYY-MM-DD
        DATE_TIME, //YYYY-MM-DDThh:mm:ss
        POSITIVE_INTEGER, // 1,2,3....
        NON_NEGATIVE_INTEGER, // 0,1,2,....
        NEGATIVE_INTEGER,
        ANY_TYPE,
        SIMPLE_TYPE;

        public boolean isSimple() {
            return !(this.equals(OBJECT) || this.equals(ARRAY));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemaRef() {
        return schemaRef;
    }

    public void setSchemaRef(String schemaRef) {
        this.schemaRef = schemaRef;
    }

    public Type getType() {
        return type;
    }

    public boolean isPrimitiveType() {
        switch (this.getType()) {
            case STRING:
            case NUMBER:
            case INTEGER:
            case LONG:
            case BOOLEAN:
            case DATE:
            case DATE_TIME:
            case POSITIVE_INTEGER:
            case NON_NEGATIVE_INTEGER:
            case NEGATIVE_INTEGER:
                return true;
        }
        return false;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<ConfigItem> getChildren() {
        return children;
    }

    public Optional<ConfigItem> getChild(final String name) {
        Objects.requireNonNull(name);
        if (this.children == null) {
            return Optional.empty();
        }
        return children
                .stream()
                .filter(item -> name.equals(item.getName()))
                .findAny();
    }

    public void setChildren(List<ConfigItem> children) {
        this.children = children;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public boolean isMaxExclusive() {
        return maxExclusive;
    }

    public void setMaxExclusive(boolean maxExclusive) {
        this.maxExclusive = maxExclusive;
    }

    public boolean isMinExclusive() {
        return minExclusive;
    }

    public void setMinExclusive(boolean minExclusive) {
        this.minExclusive = minExclusive;
    }

    public Long getMultipleOf() {
        return multipleOf;
    }

    public void setMultipleOf(Long multipleOf) {
        if (multipleOf < 0) {
            throw new SchemaConversionException("multipleOf must > 0");
        }
        this.multipleOf = multipleOf;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigItem that = (ConfigItem) o;
        return required == that.required && Objects.equals(name, that.name) && Objects.equals(schemaRef, that.schemaRef)
                && type == that.type && Objects.equals(children, that.children) && Objects.equals(max, that.max)
                && Objects.equals(min, that.min) && Objects.equals(pattern, that.pattern)
                && Objects.equals(multipleOf, that.multipleOf) && Objects.equals(enumValues, that.enumValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schemaRef, type, children, required);
    }

    @Override
    public String toString() {
        return "ConfigItem{" +
                "name='" + name + '\'' +
                ", schemaRef='" + schemaRef + '\'' +
                ", type=" + type +
                ", children=" + children +
                ", required=" + required +
                ", min=" + min +
                ", max=" + max +
                ", pattern=" + pattern +
                ", multipleOf=" + multipleOf +
                ", enumValues=" + enumValues +
                '}';
    }
}
