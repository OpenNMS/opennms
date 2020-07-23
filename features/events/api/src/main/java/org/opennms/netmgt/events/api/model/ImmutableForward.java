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
