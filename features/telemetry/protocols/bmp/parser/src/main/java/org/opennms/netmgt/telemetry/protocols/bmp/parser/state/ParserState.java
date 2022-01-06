/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.state;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;

import com.google.common.collect.ImmutableList;

public class ParserState {
    public final List<String> connections;

    private ParserState(final Builder builder) {
        this.connections = builder.connections.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmutableList.Builder<String> connections = ImmutableList.builder();

        private Builder() {
        }

        public Builder withConnection(final InetAddress connection) {
            this.connections.add(InetAddressUtils.str(connection));
            return this;
        }

        public Builder withConnections(final Set<InetAddress> connections) {
            connections.forEach(this::withConnection);
            return this;
        }

        public ParserState build() {
            return new ParserState(this);
        }
    }
}
