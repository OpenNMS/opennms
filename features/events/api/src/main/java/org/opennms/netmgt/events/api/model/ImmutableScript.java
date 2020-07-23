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
 * An immutable implementation of '{@link IScript}'.
 */
public final class ImmutableScript implements IScript {
    private final String content;
    private final String language;

    private ImmutableScript(Builder builder) {
        content = builder.content;
        language = builder.language;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IScript script) {
        return new Builder(script);
    }

    public static IScript immutableCopy(IScript script) {
        if (script == null || script instanceof ImmutableScript) {
            return script;
        }
        return newBuilderFrom(script).build();
    }

    public static final class Builder {
        private String content;
        private String language;

        private Builder() {
        }

        public Builder(IScript script) {
            content = script.getContent();
            language = script.getLanguage();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public ImmutableScript build() {
            return new ImmutableScript(this);
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableScript that = (ImmutableScript) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(language, that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, language);
    }

    @Override
    public String toString() {
        return "ImmutableScript{" +
                "content='" + content + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
