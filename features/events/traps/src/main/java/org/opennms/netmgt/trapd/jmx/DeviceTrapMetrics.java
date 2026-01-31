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
package org.opennms.netmgt.trapd.jmx;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-device listener-side trap metrics implementation.
 */
public class DeviceTrapMetrics implements DeviceTrapMetricsMBean {

    private final String location;
    private final String ipAddress;

    private final AtomicLong rawTrapsReceived = new AtomicLong();
    private final AtomicLong trapsErrored = new AtomicLong();

    public DeviceTrapMetrics(String location, String ipAddress) {
        this.location = location;
        this.ipAddress = ipAddress;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String getDeviceId() {
        return location + ":" + ipAddress;
    }

    @Override
    public long getRawTrapsReceived() {
        return rawTrapsReceived.get();
    }

    @Override
    public long getTrapsErrored() {
        return trapsErrored.get();
    }

    public void incRawTrapsReceived() {
        rawTrapsReceived.incrementAndGet();
    }

    public void incTrapsErrored() {
        trapsErrored.incrementAndGet();
    }
}
