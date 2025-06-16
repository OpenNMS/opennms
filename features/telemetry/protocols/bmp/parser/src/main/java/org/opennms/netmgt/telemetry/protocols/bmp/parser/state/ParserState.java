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
