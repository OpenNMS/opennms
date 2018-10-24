/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.flow;

import java.util.Objects;

public enum FlowPacket {
    Netflow5("/flows/netflow5.dat", 2),
    Netflow9("/flows/netflow9.dat", 7),
    Ipfix("/flows/ipfix.dat", 2),
    Sflow("/flows/sflow.dat", 5);

    private final String resource;
    private final int flows;

    private FlowPacket(String resource, int flowCount) {
        this.resource = Objects.requireNonNull(resource);
        this.flows = flowCount;
    }

    public int getFlowCount() {
        return flows;
    }

    public String getResource() {
        return resource;
    }
}
