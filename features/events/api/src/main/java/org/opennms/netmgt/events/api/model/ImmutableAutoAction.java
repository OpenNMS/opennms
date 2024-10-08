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
 * An immutable implementation of '{@link IAutoAction}'.
 */
public final class ImmutableAutoAction implements IAutoAction {
    private final String content;
    private final String state;

    private ImmutableAutoAction(Builder builder) {
        content = builder.content;
        state = builder.state;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IAutoAction autoAction) {
        return new Builder(autoAction);
    }

    public static IAutoAction immutableCopy(IAutoAction autoAction) {
        if (autoAction == null || autoAction instanceof ImmutableAutoAction) {
            return autoAction;
        }
        return newBuilderFrom(autoAction).build();
    }

    public static final class Builder {
        private String content;
        private String state;

        private Builder() {
        }

        public Builder(IAutoAction autoAction) {
            content = autoAction.getContent();
            state = autoAction.getState();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public ImmutableAutoAction build() {
            return new ImmutableAutoAction(this);
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
        ImmutableAutoAction that = (ImmutableAutoAction) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, state);
    }

    @Override
    public String toString() {
        return "ImmutableAutoAction{" +
                "content='" + content + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
