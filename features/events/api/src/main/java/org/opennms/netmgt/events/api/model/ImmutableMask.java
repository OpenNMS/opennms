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
 * An immutable implementation of '{@link IMask}'.
 */
public final class ImmutableMask implements IMask {
    private final List<IMaskElement> maskElements;

    private ImmutableMask(Builder builder) {
        maskElements = ImmutableCollections.with(ImmutableMaskElement::immutableCopy)
                .newList(builder.maskElements);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IMask fromMask) {
        return new Builder(fromMask);
    }

    public static IMask immutableCopy(IMask mask) {
        if (mask == null || mask instanceof ImmutableMask) {
            return mask;
        }
        return newBuilderFrom(mask).build();
    }

    public static final class Builder {
        private List<IMaskElement> maskElements;

        private Builder() {
        }

        public Builder(IMask mask) {
            maskElements = MutableCollections.copyListFromNullable(mask.getMaskelementCollection());
        }

        public Builder setMaskElements(List<IMaskElement> maskElements) {
            this.maskElements = maskElements;
            return this;
        }

        public ImmutableMask build() {
            return new ImmutableMask(this);
        }
    }

    @Override
    public List<IMaskElement> getMaskelementCollection() {
        return maskElements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMask that = (ImmutableMask) o;
        return Objects.equals(maskElements, that.maskElements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maskElements);
    }

    @Override
    public String toString() {
        return "ImmutableMask{" +
                "maskElements=" + maskElements +
                '}';
    }
}
