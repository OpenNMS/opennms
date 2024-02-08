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
