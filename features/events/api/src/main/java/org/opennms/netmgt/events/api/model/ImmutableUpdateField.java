/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api.model;

import java.util.Objects;

/**
 * An immutable implementation of '{@link IUpdateField}'.
 */
public final class ImmutableUpdateField implements IUpdateField {
    private final String fieldName;
    private final Boolean updateOnReduction;
    private final String valueExpression;

    private ImmutableUpdateField(Builder builder) {
        fieldName = builder.fieldName;
        updateOnReduction = builder.updateOnReduction;
        valueExpression = builder.valueExpression;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IUpdateField updateField) {
        return new Builder(updateField);
    }

    public static IUpdateField immutableCopyFrom(IUpdateField updateField) {
        if (updateField == null || updateField instanceof ImmutableUpdateField) {
            return updateField;
        }
        return newBuilderFrom(updateField).build();
    }

    public static final class Builder {
        private String fieldName;
        private Boolean updateOnReduction;
        private String valueExpression;

        private Builder() {
        }

        public Builder(IUpdateField updateField) {
            fieldName = updateField.getFieldName();
            updateOnReduction = updateField.isUpdateOnReduction();
            valueExpression = updateField.getValueExpression();
        }

        public Builder setFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder setUpdateOnReduction(Boolean updateOnReduction) {
            this.updateOnReduction = updateOnReduction;
            return this;
        }

        public Builder setValueExpression(String valueExpression) {
            this.valueExpression = valueExpression;
            return this;
        }

        public ImmutableUpdateField build() {
            return new ImmutableUpdateField(this);
        }
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Boolean isUpdateOnReduction() {
        return updateOnReduction;
    }

    @Override
    public String getValueExpression() {
        return valueExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableUpdateField that = (ImmutableUpdateField) o;
        return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(updateOnReduction, that.updateOnReduction) &&
                Objects.equals(valueExpression, that.valueExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, updateOnReduction, valueExpression);
    }

    @Override
    public String toString() {
        return "ImmutableUpdateField{" +
                "fieldName='" + fieldName + '\'' +
                ", updateOnReduction=" + updateOnReduction +
                ", valueExpression='" + valueExpression + '\'' +
                '}';
    }
}
