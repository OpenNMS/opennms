/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow.ie.values;

import static org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils.uint64;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElement;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Semantics;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.session.Session;

import com.google.common.base.MoreObjects;

public class DateTimeValue extends Value<Instant> {

    /**
     * Number of seconds between 1900-01-01 and 1970-01-01 according to RFC 868.
     */
    public static final long SECONDS_TO_EPOCH = 2208988800L;

    private final Instant value;

    public DateTimeValue(final String name,
                         final Optional<Semantics> semantics,
                         final Instant value) {
        super(name, semantics);
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("value", value)
                .toString();
    }

    public static InformationElement parserWithSeconds(final String name, final Optional<Semantics> semantics) {
        return new InformationElement() {
            @Override
            public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) {
                return new DateTimeValue(name, semantics, Instant.ofEpochSecond(uint32(buffer)));
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getMinimumFieldLength() {
                return 4;
            }

            @Override
            public int getMaximumFieldLength() {
                return 4;
            }
        };
    }

    public static InformationElement parserWithMilliseconds(final String name, final Optional<Semantics> semantics) {
        return new InformationElement() {
            @Override
            public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) {
                return new DateTimeValue(name, semantics, Instant.ofEpochMilli(uint64(buffer).longValue()));
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getMinimumFieldLength() {
                return 8;
            }

            @Override
            public int getMaximumFieldLength() {
                return 8;
            }
        };
    }

    public static InformationElement parserWithMicroseconds(final String name, final Optional<Semantics> semantics) {
        return new InformationElement() {
            @Override
            public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) {
                final long seconds = uint32(buffer);
                final long fraction = uint32(buffer) & (0xFFFFFFFF << 11);

                final Instant value = Instant.ofEpochSecond(seconds - SECONDS_TO_EPOCH, fraction * 1_000_000_000L / (1L << 32));

                return new DateTimeValue(name, semantics, value);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getMinimumFieldLength() {
                return 8;
            }

            @Override
            public int getMaximumFieldLength() {
                return 8;
            }
        };
    }

    public static InformationElement parserWithNanoseconds(final String name, final Optional<Semantics> semantics) {
        return new InformationElement() {
            @Override
            public Value<?> parse(final Session.Resolver resolver, final ByteBuffer buffer) {
                final long seconds = uint32(buffer);
                final long fraction = uint32(buffer);

                final Instant value = Instant.ofEpochSecond(seconds - SECONDS_TO_EPOCH, fraction * 1_000_000_000L / (1L << 32));

                return new DateTimeValue(name, semantics, value);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getMinimumFieldLength() {
                return 8;
            }

            @Override
            public int getMaximumFieldLength() {
                return 8;
            }
        };
    }

    @Override
    public Instant getValue() {
        return this.value;
    }

    @Override
    public void visit(final Visitor visitor) {
        visitor.accept(this);
    }
}
