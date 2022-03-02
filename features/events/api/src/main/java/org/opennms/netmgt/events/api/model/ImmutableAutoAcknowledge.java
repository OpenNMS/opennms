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
 * An immutable implementation of '{@link IAutoAcknowledge}'.
 */
public final class ImmutableAutoAcknowledge implements IAutoAcknowledge {
    private final String content;
    private final String state;

    private ImmutableAutoAcknowledge(Builder builder) {
        content = builder.content;
        state = builder.state;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IAutoAcknowledge autoacknowledge) {
        return new Builder(autoacknowledge);
    }

    public static IAutoAcknowledge immutableCopy(IAutoAcknowledge autoacknowledge) {
        if (autoacknowledge == null || autoacknowledge instanceof ImmutableAutoAcknowledge) {
            return autoacknowledge;
        }
        return newBuilderFrom(autoacknowledge).build();
    }

    public static final class Builder {
        private String content;
        private String state;

        private Builder() {
        }

        public Builder(IAutoAcknowledge autoacknowledge) {
            content = autoacknowledge.getContent();
            state = autoacknowledge.getState();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public ImmutableAutoAcknowledge build() {
            return new ImmutableAutoAcknowledge(this);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableAutoAcknowledge that = (ImmutableAutoAcknowledge) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, state);
    }

    @Override
    public String toString() {
        return "ImmutableAutoAcknowledge{" +
                "content='" + content + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
