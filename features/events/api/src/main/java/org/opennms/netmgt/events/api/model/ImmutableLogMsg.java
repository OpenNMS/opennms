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
 * An immutable implementation of '{@link ILogMsg}'.
 */
public final class ImmutableLogMsg implements ILogMsg {
    private final String content;
    private final Boolean notify;
    private final String dest;

    private ImmutableLogMsg(Builder builder) {
        content = builder.content;
        notify = builder.notify;
        dest = builder.dest;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(ILogMsg logMsg) {
        return new Builder(logMsg);
    }

    public static ILogMsg immutableCopy(ILogMsg logMsg) {
        if (logMsg == null || logMsg instanceof ImmutableLogMsg) {
            return logMsg;
        }
        return newBuilderFrom(logMsg).build();
    }

    public static final class Builder {
        private String content;
        private Boolean notify;
        private String dest;

        private Builder() {
        }

        public Builder(ILogMsg logMsg) {
            content = logMsg.getContent();
            notify = logMsg.getNotify();
            dest = logMsg.getDest();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setNotify(Boolean notify) {
            this.notify = notify;
            return this;
        }

        public Builder setDest(String dest) {
            this.dest = dest;
            return this;
        }

        public ImmutableLogMsg build() {
            return new ImmutableLogMsg(this);
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Boolean getNotify() {
        return notify == null ? false : notify;
    }

    @Override
    public Boolean copyNotify() {
        return notify;
    }

    @Override
    public String getDest() {
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableLogMsg that = (ImmutableLogMsg) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(notify, that.notify) &&
                Objects.equals(dest, that.dest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, notify, dest);
    }

    @Override
    public String toString() {
        return "ImmutableLogMsg{" +
                "content='" + content + '\'' +
                ", notify=" + notify +
                ", dest='" + dest + '\'' +
                '}';
    }
}
