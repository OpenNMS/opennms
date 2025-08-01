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

import org.opennms.core.utils.ImmutableCollections;
import org.opennms.core.utils.MutableCollections;

import java.util.List;
import java.util.Objects;

/**
 * An immutable implementation of '{@link IAlarmData}'.
 */
public final class ImmutableAlarmData implements IAlarmData {
    private final String reductionKey;
    private final Integer alarmType;
    private final String clearKey;
    private final Boolean autoClean;
    private final String x733AlarmType;
    private final Integer x733ProbableCause;
    private final List<IUpdateField> updateFieldList;
    private final IManagedObject managedObject;

    private ImmutableAlarmData(Builder builder) {
        reductionKey = builder.reductionKey;
        alarmType = builder.alarmType;
        clearKey = builder.clearKey;
        autoClean = builder.autoClean;
        x733AlarmType = builder.x733AlarmType;
        x733ProbableCause = builder.x733ProbableCause;
        updateFieldList = ImmutableCollections.with(ImmutableUpdateField::immutableCopyFrom)
                .newList(builder.updateFieldList);
        managedObject = ImmutableManagedObject.immutableCopy(builder.managedObject);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IAlarmData alarmData) {
        return new Builder(alarmData);
    }

    public static IAlarmData immutableCopy(IAlarmData alarmData) {
        if (alarmData == null || alarmData instanceof ImmutableAlarmData) {
            return alarmData;
        }
        return newBuilderFrom(alarmData).build();
    }

    public static final class Builder {
        private String reductionKey;
        private Integer alarmType;
        private String clearKey;
        private Boolean autoClean;
        private String x733AlarmType;
        private Integer x733ProbableCause;
        private List<IUpdateField> updateFieldList;
        private IManagedObject managedObject;

        private Builder() {
        }

        public Builder(IAlarmData alarmData) {
            reductionKey = alarmData.getReductionKey();
            alarmType = alarmData.getAlarmType();
            clearKey = alarmData.getClearKey();
            autoClean = alarmData.getAutoClean();
            x733AlarmType = alarmData.getX733AlarmType();
            x733ProbableCause = alarmData.getX733ProbableCause();
            updateFieldList = MutableCollections.copyListFromNullable(alarmData.getUpdateFieldList());
            managedObject = alarmData.getManagedObject();
        }

        public Builder setReductionKey(String reductionKey) {
            this.reductionKey = reductionKey;
            return this;
        }

        public Builder setAlarmType(Integer alarmType) {
            this.alarmType = alarmType;
            return this;
        }

        public Builder setClearKey(String clearKey) {
            this.clearKey = clearKey;
            return this;
        }

        public Builder setAutoClean(Boolean autoClean) {
            this.autoClean = autoClean;
            return this;
        }

        public Builder setX733AlarmType(String x733AlarmType) {
            this.x733AlarmType = x733AlarmType;
            return this;
        }

        public Builder setX733ProbableCause(Integer x733ProbableCause) {
            this.x733ProbableCause = x733ProbableCause;
            return this;
        }

        public Builder setUpdateFieldList(List<IUpdateField> updateFieldList) {
            this.updateFieldList = updateFieldList;
            return this;
        }

        public Builder setManagedObject(IManagedObject managedObject) {
            this.managedObject = managedObject;
            return this;
        }

        public ImmutableAlarmData build() {
            return new ImmutableAlarmData(this);
        }
    }

    @Override
    public Integer getAlarmType() {
        return alarmType == null ? 0 : alarmType;
    }

    @Override
    public boolean hasAlarmType() {
        return alarmType != null;
    }

    @Override
    public Boolean getAutoClean() {
        return autoClean == null ? false : autoClean;
    }

    @Override
    public boolean hasAutoClean() {
        return autoClean != null;
    }

    @Override
    public String getClearKey() {
        return clearKey;
    }

    @Override
    public String getReductionKey() {
        return reductionKey;
    }

    @Override
    public String getX733AlarmType() {
        return x733AlarmType;
    }

    @Override
    public Integer getX733ProbableCause() {
        return x733ProbableCause == null ? 0 : x733ProbableCause;
    }

    @Override
    public boolean hasX733ProbableCause() {
        return x733ProbableCause != null;
    }

    @Override
    public Boolean isAutoClean() {
        return getAutoClean();
    }

    @Override
    public List<IUpdateField> getUpdateFieldList() {
        return updateFieldList;
    }

    @Override
    public Boolean hasUpdateFields() {
        return updateFieldList != null && !updateFieldList.isEmpty();
    }

    @Override
    public IManagedObject getManagedObject() {
        return managedObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableAlarmData that = (ImmutableAlarmData) o;
        return Objects.equals(reductionKey, that.reductionKey) &&
                Objects.equals(alarmType, that.alarmType) &&
                Objects.equals(clearKey, that.clearKey) &&
                Objects.equals(autoClean, that.autoClean) &&
                Objects.equals(x733AlarmType, that.x733AlarmType) &&
                Objects.equals(x733ProbableCause, that.x733ProbableCause) &&
                Objects.equals(updateFieldList, that.updateFieldList) &&
                Objects.equals(managedObject, that.managedObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reductionKey, alarmType, clearKey, autoClean, x733AlarmType, x733ProbableCause,
                updateFieldList, managedObject);
    }

    @Override
    public String toString() {
        return "ImmutableAlarmData{" +
                "reductionKey='" + reductionKey + '\'' +
                ", alarmType=" + alarmType +
                ", clearKey='" + clearKey + '\'' +
                ", autoClean=" + autoClean +
                ", x733AlarmType='" + x733AlarmType + '\'' +
                ", x733ProbableCause=" + x733ProbableCause +
                ", updateFieldList=" + updateFieldList +
                ", managedObject=" + managedObject +
                '}';
    }
}
