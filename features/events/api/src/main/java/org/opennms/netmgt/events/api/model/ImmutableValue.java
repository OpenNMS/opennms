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
