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
