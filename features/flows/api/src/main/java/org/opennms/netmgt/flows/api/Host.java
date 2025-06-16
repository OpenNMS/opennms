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
package org.opennms.netmgt.flows.api;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;

public class Host {
    private final String ip;
    private final String hostname;

    public Host(final String ip) {
        this.ip = Objects.requireNonNull(ip);
        this.hostname = null;
    }

    public Host(final String ip, final String hostname) {
        this.ip = Objects.requireNonNull(ip);
        this.hostname = hostname;
    }

    public Host(final Builder builder) {
        this.ip = Objects.requireNonNull(builder.ip);
        this.hostname = builder.hostname;
    }

    public String getIp() {
        return this.ip;
    }

    public Optional<String> getHostname() {
        return Optional.ofNullable(this.hostname);
    }

    public static class Builder {
        private String ip;
        private String hostname;

        private Builder() {
        }

        public Builder withIp(final String ip) {
            this.ip = Objects.requireNonNull(ip);
            return this;
        }

        public Builder withHostname(final String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Host build() {
            return new Host(this);
        }
    }

    public static Host.Builder builder() {
        return new Host.Builder();
    }

    public static Host.Builder from(final String ip) {
        return new Host.Builder()
                .withIp(ip);
    }

    public static Host.Builder from(final Host host) {
        return new Host.Builder()
                .withIp(host.ip)
                .withHostname(host.hostname);
    }

    public static Host.Builder forOther() {
        return builder()
                .withIp("Other");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Host)) {
            return false;
        }

        final Host host = (Host) o;
        return Objects.equals(this.ip, host.ip) &&
                Objects.equals(this.hostname, host.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ip, this.hostname);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ip", this.ip)
                .add("hostname", this.hostname)
                .toString();
    }
}
