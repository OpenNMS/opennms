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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint64;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElement;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

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
        this.value = Objects.requireNonNull(value);
    }

    public DateTimeValue(final String name, final Instant value) {
        this(name, Optional.empty(), value);
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
            public Value<?> parse(final Session.Resolver resolver, final ByteBuf buffer) {
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
            public Value<?> parse(final Session.Resolver resolver, final ByteBuf buffer) {
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
            public Value<?> parse(final Session.Resolver resolver, final ByteBuf buffer) {
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
            public Value<?> parse(final Session.Resolver resolver, final ByteBuf buffer) {
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
