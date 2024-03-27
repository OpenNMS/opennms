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

public final class ImmutableValue implements IValue {
    private final String content;
    private final String type;
    private final String encoding;
    private final Boolean expand;

    private ImmutableValue(Builder builder) {
        content = builder.content;
        type = builder.type;
        encoding = builder.encoding;
        expand = builder.expand;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(IValue value) {
        return new Builder(value);
    }

    public static IValue immutableCopy(IValue value) {
        if (value == null || value instanceof ImmutableValue) {
            return value;
        }
        return newBuilderFrom(value).build();
    }

    public static final class Builder {
        private String content;
        private String type;
        private String encoding;
        private Boolean expand;

        private Builder() {
        }

        public Builder(IValue value) {
            content = value.getContent();
            type = value.getType();
            encoding = value.getEncoding();
            expand = value.isExpand();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setExpand(Boolean expand) {
            this.expand = expand;
            return this;
        }

        public ImmutableValue build() {
            return new ImmutableValue(this);
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Boolean isExpand() {
        return expand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableValue that = (ImmutableValue) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(type, that.type) &&
                Objects.equals(encoding, that.encoding) &&
                Objects.equals(expand, that.expand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, type, encoding, expand);
    }

    @Override
    public String toString() {
        return "ImmutableValue{" +
                "content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", encoding='" + encoding + '\'' +
                ", expand=" + expand +
                '}';
    }
}
