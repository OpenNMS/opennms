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
