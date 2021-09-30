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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import java.nio.charset.StandardCharsets;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public abstract class Record {
    private final static DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS")
            .withZone(ZoneOffset.UTC);

    private final Type type;

    protected Record(final Type type) {
        this.type = Objects.requireNonNull(type);
    }

    public static String formatTimestamp(final Instant timestamp) {
        if (timestamp == null) {
            return null;
        }
        return TIMESTAMP_FORMATTER.format(timestamp);
    }

    public static String hash(final String... values) {
        final Hasher hasher = Hashing.md5().newHasher();
        for (final String value : values) {
            hasher.putString(value != null ? value : "", StandardCharsets.UTF_8);
        }
        return hasher.hash().toString();
    }

    public static String hash(Transport.IpAddress address, long distinguisher, String routerHashId) {
        return hash(BmpAdapterTools.addressAsStr(address), Long.toString(distinguisher), routerHashId);
    }

    protected abstract String[] fields();

    public final Type getType() {
        return this.type;
    }

    public final void serialize(final StringBuffer buffer) {
        final Iterator<String> fields = Arrays.stream(this.fields())
                                              .map(field -> field != null ? field.replace('\t', ' ')
                                                                 .replace('\n', '\r') : "").iterator();

        if (fields.hasNext()) {
            buffer.append(fields.next());
            while (fields.hasNext()) {
                buffer.append('\t');
                buffer.append(fields.next());
            }
        }

        buffer.append('\n');
    }

    public static String boolAsInt(Boolean truthyFalsy) {
        if (truthyFalsy == null || !truthyFalsy) {
            return "0";
        }
        return "1";
    }

    public static String nullSafeStr(Long val) {
        if (val == null) {
            return "0";
        } else {
            return Long.toString(val);
        }
    }

    public static String nullSafeStr(Integer val) {
        if (val == null) {
            return "0";
        } else {
            return Integer.toString(val);
        }
    }

    public static String nullSafeStr(InetAddress addr) {
        if (addr == null) {
            return "";
        } else {
            return InetAddressUtils.str(addr);
        }
    }

}
