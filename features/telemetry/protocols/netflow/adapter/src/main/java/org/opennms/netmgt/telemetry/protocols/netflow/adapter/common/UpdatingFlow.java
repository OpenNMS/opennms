/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.common;

import java.util.Optional;

import org.opennms.netmgt.flows.api.Flow;

public abstract class UpdatingFlow implements Flow {
    public static class Timeout {
        private final long active;
        private final long inactive;

        public Timeout(final long active, final long inactive) {
            this.active = active;
            this.inactive = inactive;
        }

        public long getActive() {
            return this.active;
        }

        public long getInactive() {
            return this.inactive;
        }
    }

    public abstract Optional<Timeout> getTimeout();

    @Override
    public Long getDeltaSwitched() {
        // The timespan for this flow export depends on the timeout, if present: use the active or inactive timeout,
        // whether the flow was active or not, and subtract it from last switched timestamp. Short living flows are
        // capped to the real first switched timestamp. If there is no timeout information available the first switched
        // timestamp is used as an fallback.

        return this.getTimeout()
                .map(timeout -> (this.getBytes() > 0  || this.getPackets() > 0) ? timeout.active : timeout.inactive)
                .map(timeout -> this.getLastSwitched() - timeout)
                .map(t -> Math.max(this.getFirstSwitched(), t))
                .orElseGet(this::getFirstSwitched);
    }
}
