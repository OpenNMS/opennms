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
