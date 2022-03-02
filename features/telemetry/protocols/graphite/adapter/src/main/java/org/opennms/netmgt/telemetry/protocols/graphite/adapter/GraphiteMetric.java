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

package org.opennms.netmgt.telemetry.protocols.graphite.adapter;

import java.time.Instant;
import java.util.Objects;

public class GraphiteMetric {
    /**
     * The graphite path, an arbitrary key.
     */
    private String path;

    /**
     * The value to be stored.
     */
    private String value;

    /**
     * The timestamp as a UNIX epoch.
     */
    private long timestamp;

    public GraphiteMetric(final String path, final String value, final long timestamp) {
        this.path = Objects.requireNonNull(path);
        this.value = Objects.requireNonNull(value);
        this.timestamp = timestamp;
    }

    /**
     * The graphite path, an arbitrary key.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * The raw value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * The value, as an Integer number.
     */
    public Integer intValue() {
        return Integer.valueOf(this.value, 10);
    }

    /**
     * The value, as a Long number.
     */
    public Long longValue() {
        return Long.valueOf(this.value, 10);
    }

    /**
     * The value, as a floating-point number.
     */
    public Float floatValue() {
        return Float.valueOf(this.value);
    }

    /**
     * The value, as a double-precision number.
     */
    public Double doubleValue() {
        return Double.valueOf(this.value);
    }

    /**
     * The timestamp as a UNIX epoch.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * The timestamp as an Instant.
     */
    public Instant getInstant() {
        return Instant.ofEpochMilli(this.timestamp);
    }

    @Override
    public String toString() {
        return "GraphiteMetric [path=" + path + ", value=" + value + ", timestamp=" + timestamp + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, timestamp, value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GraphiteMetric)) return false;
        final GraphiteMetric other = (GraphiteMetric) obj;
        return Objects.equals(path, other.path)
                && Objects.equals(value, other.value)
                && timestamp == other.timestamp;
    }
}
