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

public class Conversation {
    private final String location;
    private final Integer protocol;
    private final Host lowerHost;
    private final Host upperHost;
    private final String application;

    private Conversation(final Builder builder) {
        this.location = Objects.requireNonNull(builder.location);
        this.protocol = Objects.requireNonNull(builder.protocol);
        this.lowerHost = builder.lowerHost.build();
        this.upperHost = builder.upperHost.build();
        this.application = builder.application;
    }

    public String getLocation() {
        return this.location;
    }

    public Integer getProtocol() {
        return this.protocol;
    }

    public Host getLowerHost() {
        return this.lowerHost;
    }

    public String getLowerIp() {
        return this.lowerHost.getIp();
    }

    public Optional<String> getLowerHostname() {
        return this.lowerHost.getHostname();
    }

    public Host getUpperHost() {
        return this.upperHost;
    }

    public String getUpperIp() {
        return this.upperHost.getIp();
    }

    public Optional<String> getUpperHostname() {
        return this.upperHost.getHostname();
    }

    public String getApplication() {
        return this.application;
    }

    public static class Builder {
        private String location;
        private Integer protocol;
        private Host.Builder lowerHost = Host.builder();
        private Host.Builder upperHost = Host.builder();
        private String application;

        private Builder() {
        }

        public Builder withLocation(final String location) {
            this.location = Objects.requireNonNull(location);
            return this;
        }

        public Builder withProtocol(final Integer protocol) {
            this.protocol = Objects.requireNonNull(protocol);
            return this;
        }

        public Builder withLowerHost(final Host.Builder lowerHost) {
            this.lowerHost = Objects.requireNonNull(lowerHost);
            return this;
        }

        public Builder withUpperHost(final Host.Builder upperHost) {
            this.upperHost = Objects.requireNonNull(upperHost);
            return this;
        }

        public Builder withLowerIp(final String lowerIp) {
            this.lowerHost.withIp(lowerIp);
            return this;
        }

        public Builder withUpperIp(final String upperIp) {
            this.upperHost.withIp(upperIp);
            return this;
        }

        public Builder withLowerHostname(final String hostname) {
            this.lowerHost.withHostname(hostname);
            return this;
        }

        public Builder withUpperHostname(final String hostname) {
            this.upperHost.withHostname(hostname);
            return this;
        }

        public Builder withApplication(final String application) {
            this.application = application;
            return this;
        }

        public Conversation build() {
            return new Conversation(this);
        }
    }

    public static Conversation.Builder builder() {
        return new Conversation.Builder();
    }

    public static Conversation.Builder from(final ConversationKey key) {
        return new Conversation.Builder()
                .withLocation(key.getLocation())
                .withProtocol(key.getProtocol())
                .withLowerIp(key.getLowerIp())
                .withUpperIp(key.getUpperIp())
                .withApplication(key.getApplication());
    }

    public static Conversation.Builder from(final Conversation convo) {
        return new Conversation.Builder()
                .withLocation(convo.location)
                .withProtocol(convo.protocol)
                .withLowerHost(Host.from(convo.lowerHost))
                .withUpperHost(Host.from(convo.upperHost))
                .withApplication(convo.application);
    }

    public static Conversation.Builder forOther() {
        return Conversation.builder()
                .withLocation("Other")
                .withProtocol(-1)
                .withLowerHost(Host.forOther())
                .withUpperHost(Host.forOther())
                .withApplication("Other");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Conversation)) {
            return false;
        }

        final Conversation that = (Conversation) o;
        return Objects.equals(this.location, that.location) &&
                Objects.equals(this.protocol, that.protocol) &&
                Objects.equals(this.lowerHost, that.lowerHost) &&
                Objects.equals(this.upperHost, that.upperHost) &&
                Objects.equals(this.application, that.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.location, this.protocol, this.lowerHost, this.upperHost, this.application);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("location", this.location)
                .add("protocol", this.protocol)
                .add("lowerHost", this.lowerHost)
                .add("upperHost", this.upperHost)
                .add("application", this.application)
                .toString();
    }
}
