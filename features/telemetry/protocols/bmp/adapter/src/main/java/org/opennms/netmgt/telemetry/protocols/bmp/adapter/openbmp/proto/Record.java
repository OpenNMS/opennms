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

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public abstract class Record {
    private final Type type;

    protected Record(final Type type) {
        this.type = Objects.requireNonNull(type);
    }

    public static String formatTimestamp(final Instant timestamp) {
        return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL", timestamp);
    }

    protected abstract String[] fields();

    public final Type getType() {
        return this.type;
    }

    public final void serialize(final StringBuffer buffer) {
        final Iterator<String> fields = Arrays.stream(this.fields())
                                              .map(field -> field.replace('\t', ' ')
                                                                 .replace('\n', '\r')).iterator();

        if (fields.hasNext()) {
            buffer.append(fields.next());
            while (fields.hasNext()) {
                buffer.append('\t');
                buffer.append(fields.next());
            }
        }

        buffer.append('\n');
    }
}
