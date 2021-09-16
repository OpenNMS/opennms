/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.dao.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ConfigItem {
    private String name;

    /**
     * A reference to the related element in the source schema.
     */
    private String schemaRef;
    private Type type;
    private List<ConfigItem> children = new LinkedList<>();
    private boolean minSet = false;
    private long min = 0;
    private boolean maxSet = false;
    private long max = 0;
    private Object defaultValue;
    private String documentation;

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

    static public boolean isPrimitiveType(Type type) {
        switch (type) {
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

    public void setChildren(List<ConfigItem> children) {
        this.children = children;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isMaxSet() {
        return maxSet;
    }

    public void setMax(long max) {
        maxSet = true;
        this.max = max;
    }

    public long getMax() {
        return max;
    }

    public boolean isMinSet() {
        return minSet;
    }

    public void setMin(long min) {
        minSet = true;
        this.min = min;
    }

    public long getMin() {
        return min;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigItem that = (ConfigItem) o;
        return required == that.required && Objects.equals(name, that.name) && Objects.equals(schemaRef, that.schemaRef) && type == that.type && Objects.equals(children, that.children)
                && Objects.equals(maxSet, that.maxSet) && Objects.equals(max, that.max) && Objects.equals(minSet, that.minSet) && Objects.equals(min, that.min);
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
                '}';
    }
}