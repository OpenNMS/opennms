/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.persistence.api.Protocol;

public class ClassificationRequest {

    private int port;
    private Protocol protocol;
    private String ipAddress;
    private String location;

    public ClassificationRequest(String location, int port, String ipAddress, Protocol protocol) {
        this.location = location;
        this.port = port;
        this.ipAddress = ipAddress;
        this.protocol = protocol;
    }

    public ClassificationRequest() {

    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassificationRequest that = (ClassificationRequest) o;
        boolean equals = Objects.equals(location, that.location)
                && Objects.equals(port, that.port)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(ipAddress, that.ipAddress);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, port, protocol, ipAddress);
    }
}
