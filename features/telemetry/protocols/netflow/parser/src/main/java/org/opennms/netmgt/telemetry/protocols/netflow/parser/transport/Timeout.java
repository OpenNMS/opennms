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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.transport;

public class Timeout {

    private final Long flowActiveTimeout;
    private final Long flowInActiveTimeout;

    private Long numBytes;
    private Long numPackets;
    private Long firstSwitched;
    private Long lastSwitched;

    public Timeout(final Long active, final Long inactive) {
        this.flowActiveTimeout = active;
        this.flowInActiveTimeout = inactive;
    }

    public void setNumBytes(Long numBytes) {
        this.numBytes = numBytes;
    }

    public void setNumPackets(Long numPackets) {
        this.numPackets = numPackets;
    }

    public void setFirstSwitched(Long firstSwitched) {
        this.firstSwitched = firstSwitched;
    }

    public void setLastSwitched(Long lastSwitched) {
        this.lastSwitched = lastSwitched;
    }

    public Long getDeltaSwitched() {
        if (flowActiveTimeout != null && flowInActiveTimeout != null) {
            long active = flowActiveTimeout * 1000;
            long inActive = flowInActiveTimeout * 1000;
            long numBytes = this.numBytes != null ? this.numBytes : 0;
            long numPackets = this.numPackets != null ? this.numPackets : 0;
            long firstSwitched = this.firstSwitched != null ? this.firstSwitched: 0;
            long lastSwitched = this.lastSwitched != null ? this.lastSwitched : 0;

            long timeout = (numBytes > 0 || numPackets > 0) ? active : inActive;
            long delta = lastSwitched - timeout;

            return Math.max(firstSwitched, delta);
        }
        return firstSwitched;
    }
}
