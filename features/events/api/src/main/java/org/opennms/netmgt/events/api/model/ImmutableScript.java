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
