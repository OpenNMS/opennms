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
    public boolean hasNotify() {
        return notify != null;
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
