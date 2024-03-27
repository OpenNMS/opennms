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

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

public interface MessageUtils {
    static Long getLongValue(Value<?> value) {
        if (value instanceof UnsignedValue) {
            UnsignedLong unsignedValue = ((UnsignedValue) value).getValue();
            return unsignedValue.longValue();
        }
        return null;
    }


    static Boolean getBooleanValue(Value<?> value) {
        if (value instanceof BooleanValue) {
            return ((BooleanValue) value).getValue();
        }
        return Boolean.FALSE;
    }

    static InetAddress getInetAddress(Value<?> value) {
        if (value instanceof IPv4AddressValue) {
            return (InetAddress) value.getValue();
        }
        if (value instanceof IPv6AddressValue) {
            return (InetAddress) value.getValue();
        }
        return null;
    }

    static Instant getTime(Value<?> value) {
        if (value instanceof DateTimeValue) {
            return ((DateTimeValue) value).getValue();
        }
        return null;
    }

    @SafeVarargs
    public static <V> Optional<V> first(final V... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .findFirst();
    }


    static Optional<UInt64Value> getUInt64Value(Value<?> value) {
        Long longValue = getLongValue(value);
        if (longValue != null) {
            return Optional.of(UInt64Value.newBuilder().setValue(longValue).build());
        }
        return Optional.empty();
    }

    static Optional<UInt64Value> getUInt64Value(Long value) {
        if (value != null) {
            return Optional.of(UInt64Value.newBuilder().setValue(value).build());
        }
        return Optional.empty();
    }


    static Optional<UInt32Value> getUInt32Value(Value<?> value) {
        Long longValue = getLongValue(value);
        if (longValue != null) {
            return Optional.of(UInt32Value.newBuilder().setValue(longValue.intValue()).build());
        }
        return Optional.empty();
    }


    static Optional<DoubleValue> getDoubleValue(Value<?> value) {
        Long longValue = getLongValue(value);
        if (longValue != null) {
            return Optional.of(DoubleValue.newBuilder().setValue(longValue.doubleValue()).build());
        }
        return Optional.empty();
    }

    static Optional<String> getString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }



    static UInt32Value setIntValue(int value) {
        return UInt32Value.newBuilder().setValue(value).build();
    }

    static UInt64Value setLongValue(long value) {
        return UInt64Value.newBuilder().setValue(value).build();
    }

    static DoubleValue setDoubleValue(double value) {
        return DoubleValue.newBuilder().setValue(value).build();
    }
}
