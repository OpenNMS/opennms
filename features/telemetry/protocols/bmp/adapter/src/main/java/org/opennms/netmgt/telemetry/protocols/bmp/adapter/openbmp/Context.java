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
