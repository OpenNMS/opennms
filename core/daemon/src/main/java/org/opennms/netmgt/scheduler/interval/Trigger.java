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

import org.opennms.netmgt.scheduler.Timer;

public interface Trigger {
    Instant next();

    void fired(final Instant now);

    default boolean isReady(final Instant now) {
        return now.isAfter(this.next());
    }

    Object key();

    static Trigger parse(final Timer timer, final String s) {
        Objects.requireNonNull(s);

        if (s.startsWith("~")) {
            // TODO fooker: Implement
            return new CalendarTrigger();

        } else {
            // TODO fooker: allow formats like "5s"
            final var duration = Duration.ofMillis(Integer.parseUnsignedInt(s));
            return new IntervalTrigger(duration, Instant.ofEpochMilli(timer.getCurrentTime()).plus(duration));
        }
    }

    // A trigger that never triggers
    Trigger NEVER = new Trigger() {
        @Override
        public Instant next() {
            return null;
        }

        @Override
        public void fired(final Instant time) {
        }

        @Override
        public boolean isReady(final Instant time) {
            return false;
        }

        @Override
        public Object key() {
            return null;
        }
    };

    // A trigger that always triggers
    Trigger ASAP = new Trigger() {
        @Override
        public Instant next() {
            return null;
        }

        @Override
        public void fired(final Instant time) {
        }

        @Override
        public boolean isReady(final Instant time) {
            return true;
        }

        @Override
        public Object key() {
            return Duration.ZERO;
        }
    };

    static Trigger interval(final Timer timer, final long ms) {
        // TODO fooker: This method must be gone or use a sane type

        if (ms == -1) {
            return Trigger.NEVER;
        }
        if (ms == 0) {
            return Trigger.ASAP;
        }

        final var interval = Duration.ofMillis(ms);
        return new IntervalTrigger(interval, Instant.ofEpochMilli(timer.getCurrentTime()).plus(interval));
    }
}
