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
package org.opennms.smoketest.telemetry;

import java.util.Objects;

import org.opennms.netmgt.flows.elastic.NetflowVersion;

public class FlowPacket extends Packet {

    private final NetflowVersion netflowVersion;
    private final int flowCount;

    public FlowPacket(final NetflowVersion netflowVersion,
                      final Payload payload,
                      final int flowCount) {
        super(payload);

        this.netflowVersion = Objects.requireNonNull(netflowVersion);
        this.flowCount = flowCount;
        if (flowCount < 1) {
            throw new IllegalArgumentException("Flow count must be strictly positive.");
        }
    }

    public int getFlowCount() {
        return flowCount;
    }

    public NetflowVersion getNetflowVersion() {
        return netflowVersion;
    }
}
