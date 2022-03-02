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
 * An immutable implementation of '{@link IParm}'.
 */
public final class ImmutableParm implements IParm {

    private final String parmName;
    private final IValue value;

    private ImmutableParm(Builder builder) {
        parmName = builder.parmName;
        value = builder.value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IParm parm) {
        return new Builder(parm);
    }

    public static IParm immutableCopy(IParm parm) {
        if (parm == null || parm instanceof ImmutableParm) {
            return parm;
        }
        return newBuilderFrom(parm).build();
    }

    public static final class Builder {
        private String parmName;
        private IValue value;

        private Builder() {
        }

        public Builder(IParm parm) {
            parmName = parm.getParmName();
            value = parm.getValue();
        }

        public Builder setParmName(String parmName) {
            this.parmName = parmName;
            return this;
        }

        public Builder setValue(IValue value) {
            this.value = value;
            return this;
        }

        public ImmutableParm build() {
            return new ImmutableParm(this);
        }
    }

    @Override
    public String getParmName() {
        return parmName;
    }

    @Override
    public IValue getValue() {
        return value;
    }

    @Override
    public boolean isValid() {
        return getParmName() != null && getValue() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableParm that = (ImmutableParm) o;
        return Objects.equals(parmName, that.parmName) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parmName, value);
    }

    @Override
    public String toString() {
        return "ImmutableParm{" +
                "parmName='" + parmName + '\'' +
                ", value=" + value +
                '}';
    }
}
