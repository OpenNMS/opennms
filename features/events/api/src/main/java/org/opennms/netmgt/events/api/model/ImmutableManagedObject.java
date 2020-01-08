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
