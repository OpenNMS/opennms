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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;

public class Context {

    public final String adminId;

    public final String collectorHashId;
    public final String routerHashId;

    public final Instant timestamp;

    public final InetAddress sourceAddress;
    public final int sourcePort;
    public final String location;

    public Context(final String adminId,
                    final String collectorHashId,
                    final String routerHashId,
                    final Instant timestamp,
                    final InetAddress sourceAddress,
                    final int sourcePort, String location) {
        this.adminId = Objects.requireNonNull(adminId);
        this.collectorHashId = Objects.requireNonNull(collectorHashId);
        this.routerHashId = Objects.requireNonNull(routerHashId);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.sourceAddress = Objects.requireNonNull(sourceAddress);
        this.sourcePort = sourcePort;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getRouterHash() {
        return routerHashId;
    }
}
