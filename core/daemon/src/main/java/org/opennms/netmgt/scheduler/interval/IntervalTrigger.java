/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scheduler.interval;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class IntervalTrigger implements Trigger {
    private final Duration interval;

    private Instant next;

    public IntervalTrigger(final Duration interval, final Instant next) {
        this.interval = Objects.requireNonNull(interval);
        this.next = Objects.requireNonNull(next);
    }

    @Override
    public void fired(final Instant now) {
        // TODO fooker: Log missed intervals?
        while (!now.isBefore(this.next)) {
            this.next = this.next.plus(this.interval);
        }
    }

    @Override
    public Instant next() {
        return this.next;
    }

    @Override
    public Object key() {
        return this.interval;
    }
}
