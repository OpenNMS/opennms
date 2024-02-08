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
 * An immutable implementation of '{@link IOperAction}'.
 */
public final class ImmutableOperAction implements IOperAction {
    private String content;
    private String state;
    private String menutext;

    private ImmutableOperAction(Builder builder) {
        content = builder.content;
        state = builder.state;
        menutext = builder.menutext;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IOperAction operAction) {
        return new Builder(operAction);
    }

    public static IOperAction immutableCopy(IOperAction operAction) {
        if (operAction == null || operAction instanceof ImmutableOperAction) {
            return operAction;
        }
        return newBuilderFrom(operAction).build();
    }

    public static final class Builder {
        private String content;
        private String state;
        private String menutext;

        private Builder() {
        }

        public Builder(IOperAction operAction) {
            content = operAction.getContent();
            state = operAction.getState();
            menutext = operAction.getMenutext();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setMenutext(String menutext) {
            this.menutext = menutext;
            return this;
        }

        public ImmutableOperAction build() {
            return new ImmutableOperAction(this);
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
    public String getMenutext() {
        return menutext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableOperAction that = (ImmutableOperAction) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(state, that.state) &&
                Objects.equals(menutext, that.menutext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, state, menutext);
    }

    @Override
    public String toString() {
        return "ImmutableOperAction{" +
                "content='" + content + '\'' +
                ", state='" + state + '\'' +
                ", menutext='" + menutext + '\'' +
                '}';
    }
}
