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
package org.opennms.netmgt.discovery;

import java.net.InetAddress;

/**
 * Encapsulate detection result and IP Address with ping duration.
 */
public class DiscoveryResult {

    private final Boolean detectResult;

    private final InetAddress address;

    private final Double pingDuration;

    public DiscoveryResult(Boolean detectResult, InetAddress address, Double pingDuration) {
        this.detectResult = detectResult;
        this.address = address;
        this.pingDuration = pingDuration;
    }

    public Boolean getDetectResult() {
        return detectResult;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Double getPingDuration() {
        return pingDuration;
    }
}
