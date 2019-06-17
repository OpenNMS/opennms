/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.api;

import java.util.Objects;
import java.util.Optional;

public class Conversation {
    private final String location;
    private final Integer protocol;
    private final String lowerIp;
    private final String upperIp;
    private final Optional<String> lowerHostname;
    private final Optional<String> upperHostname;
    private final String application;

    private Conversation(final String location,
                        final Integer protocol,
                        final String lowerIp,
                        final String upperIp,
                        final Optional<String> lowerHostname,
                        final Optional<String> upperHostname,
                        final String application) {
        this.location = Objects.requireNonNull(location);
        this.protocol = Objects.requireNonNull(protocol);
        this.lowerIp = Objects.requireNonNull(lowerIp);
        this.upperIp = Objects.requireNonNull(upperIp);
        this.lowerHostname = Objects.requireNonNull(lowerHostname);
        this.upperHostname = Objects.requireNonNull(upperHostname);
        this.application = Objects.requireNonNull(application);
    }

    public String getLocation() {
        return this.location;
    }

    public Integer getProtocol() {
        return this.protocol;
    }

    public String getLowerIp() {
        return this.lowerIp;
    }

    public String getUpperIp() {
        return this.upperIp;
    }

    public Optional<String> getLowerHostname() {
        return this.lowerHostname;
    }

    public Optional<String> getUpperHostname() {
        return this.upperHostname;
    }

    public String getApplication() {
        return this.application;
    }

    public static class Builder {
        private String location;
        private Integer protocol;
        private String lowerIp;
        private String upperIp;
        private Optional<String> lowerHostname;
        private Optional<String> upperHostname;
        private String application;

        private Builder() {}

        public Builder withLocation(final String location) {
            this.location = Objects.requireNonNull(location);
            return this;
        }

        public Builder withProtocol(final Integer protocol) {
            this.protocol = Objects.requireNonNull(protocol);
            return this;
        }

        public Builder withLowerIp(final String lowerIp) {
            this.lowerIp = Objects.requireNonNull(lowerIp);
            return this;
        }

        public Builder withUpperIp(final String upperIp) {
            this.upperIp = Objects.requireNonNull(upperIp);
            return this;
        }

        public Builder withLowerHostname(final String hostname) {
            this.lowerHostname = Optional.of(hostname);
            return this;
        }

        public Builder withUpperHostname(final String hostname) {
            this.upperHostname = Optional.of(hostname);
            return this;
        }

        public Builder withApplication(final String application) {
            this.application = Objects.requireNonNull(application);
            return this;
        }

        public Conversation build() {
            return new Conversation(this.location,
                    this.protocol,
                    this.lowerIp,
                    this.upperIp,
                    this.lowerHostname,
                    this.upperHostname,
                    this.application);
        }
    }

    public static Conversation.Builder from(final ConversationKey key) {
        return new Conversation.Builder()
                .withLocation(key.getLocation())
                .withProtocol(key.getProtocol())
                .withLowerIp(key.getLowerIp())
                .withUpperIp(key.getUpperIp())
                .withApplication(key.getApplication());
    }
}
