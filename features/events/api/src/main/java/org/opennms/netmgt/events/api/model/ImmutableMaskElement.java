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

import org.opennms.core.utils.ImmutableCollections;
import org.opennms.core.utils.MutableCollections;

import java.util.List;
import java.util.Objects;

/**
 * An immutable implementation of '{@link IMaskElement}'.
 */
public final class ImmutableMaskElement implements IMaskElement {
    private final String meName;
    private final List<String> meValues;

    private ImmutableMaskElement(Builder builder) {
      meName = builder.meName;
      meValues = ImmutableCollections.newListOfImmutableType(builder.meValues);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IMaskElement fromMaskElement) {
        return new Builder(fromMaskElement);
    }

    public static IMaskElement immutableCopy(IMaskElement maskElement) {
        if (maskElement == null || maskElement instanceof ImmutableMaskElement) {
            return maskElement;
        }
        return newBuilderFrom(maskElement).build();
    }

    public static final class Builder {
        private String meName;
        private List<String> meValues;

        private Builder() {
        }

        public Builder(IMaskElement maskElement) {
            meName = maskElement.getMename();
            meValues = MutableCollections.copyListFromNullable(maskElement.getMevalueCollection());
        }

        public Builder setMeName(String meName) {
            this.meName = meName;
            return this;
        }

        public Builder setMeValues(List<String> meValues) {
            this.meValues = meValues;
            return this;
        }

        public ImmutableMaskElement build() {
            return new ImmutableMaskElement(this);
        }
    }

    @Override
    public String getMename() {
        return meName;
    }

    @Override
    public List<String> getMevalueCollection() {
        return meValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMaskElement that = (ImmutableMaskElement) o;
        return Objects.equals(meName, that.meName) &&
                Objects.equals(meValues, that.meValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meName, meValues);
    }

    @Override
    public String toString() {
        return "ImmutableMaskElement{" +
                "meName='" + meName + '\'' +
                ", meValues=" + meValues +
                '}';
    }
}
