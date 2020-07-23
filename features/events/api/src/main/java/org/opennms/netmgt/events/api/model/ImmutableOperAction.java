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
