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
 * An immutable implementation of '{@link IForward}'.
 */
public final class ImmutableForward implements IForward {
    private final String content;
    private final String state;
    private final String mechanism;

    private ImmutableForward(Builder builder) {
        content = builder.content;
        state = builder.state;
        mechanism = builder.mechanism;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IForward forward) {
        return new Builder(forward);
    }

    public static IForward immutableCopy(IForward forward) {
        if (forward == null || forward instanceof ImmutableForward) {
            return forward;
        }
        return newBuilderFrom(forward).build();
    }

    public static final class Builder {
        private String content;
        private String state;
        private String mechanism;

        private Builder() {
        }

        public Builder(IForward forward) {
            content =  forward.getContent();
            state =  forward.getState();
            mechanism =  forward.getMechanism();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setMechanism(String mechanism) {
            this.mechanism = mechanism;
            return this;
        }

        public ImmutableForward build() {
            return new ImmutableForward(this);
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getMechanism() {
        return mechanism;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableForward that = (ImmutableForward) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(state, that.state) &&
                Objects.equals(mechanism, that.mechanism);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, state, mechanism);
    }

    @Override
    public String toString() {
        return "ImmutableForward{" +
                "content='" + content + '\'' +
                ", state='" + state + '\'' +
                ", mechanism='" + mechanism + '\'' +
                '}';
    }
}
