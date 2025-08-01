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
