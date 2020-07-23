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

package org.opennms.netmgt.telemetry.stream.listeners;

import java.util.Map;
import java.util.Objects;

public class Config {

    private final Integer nodeId;

    private final String ipAddress;

    private Map<String, String> params;

    public Config(final Integer nodeId, final String ipAddress, Map<String, String> params) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.ipAddress =  Objects.requireNonNull(ipAddress);
        this.params = Objects.requireNonNull(params);
    }

    public Config(Integer nodeId, String ipAddress) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
