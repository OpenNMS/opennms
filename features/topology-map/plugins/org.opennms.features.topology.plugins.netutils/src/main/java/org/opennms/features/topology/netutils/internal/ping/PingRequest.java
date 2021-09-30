/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.ping;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class PingRequest {
    private long timeout;
    private int packetSize;
    private int retries;
    private String ipAddress;
    private int numberRequests;
    private String location;

    public PingRequest withTimeout(long timeout, TimeUnit unit) {
        Preconditions.checkArgument(timeout > 0, "timeout must be > 0");
        Objects.requireNonNull(unit);
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        return this;
    }

    public PingRequest withPackageSize(int packageSize) {
        Preconditions.checkArgument(packageSize > 0, "packetSize must be > 0");
        this.packetSize = packageSize;
        return this;
    }

    public PingRequest withRetries(int retries) {
        Preconditions.checkArgument(retries >= 0, "retries must be >= 0");
        this.retries = retries;
        return this;
    }

    public PingRequest withIpAddress(String ipAddress) {
        this.ipAddress = Objects.requireNonNull(ipAddress);
        return this;
    }

    public PingRequest withNumberRequests(int numberRequests) {
        Preconditions.checkArgument(numberRequests > 0, "number of requests must be > 0");
        this.numberRequests = numberRequests;
        return this;
    }

    public PingRequest withLocation(String location) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(location), "Location must not be empty or null");
        this.location = location;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getRetries() {
        return retries;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getNumberRequests() {
        return numberRequests;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setNumberRequests(int numberRequests) {
        this.numberRequests = numberRequests;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
