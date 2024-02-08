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
 * An immutable implementation of '{@link IManagedObject}'.
 */
public final class ImmutableManagedObject implements IManagedObject {
    private final String type;

    private ImmutableManagedObject(Builder builder) {
        type = builder.type;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IManagedObject managedObject) {
        return new Builder(managedObject);
    }

    public static IManagedObject immutableCopy(IManagedObject managedObject) {
        if (managedObject == null || managedObject instanceof ImmutableManagedObject) {
            return managedObject;
        }
        return newBuilderFrom(managedObject).build();
    }

    public static final class Builder {
        private String type;

        private Builder() {
        }

        public Builder(IManagedObject managedObject) {
            type = managedObject.getType();
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public ImmutableManagedObject build() {
            return new ImmutableManagedObject(this);
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableManagedObject that = (ImmutableManagedObject) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "ImmutableManagedObject{" +
                "type='" + type + '\'' +
                '}';
    }
}
