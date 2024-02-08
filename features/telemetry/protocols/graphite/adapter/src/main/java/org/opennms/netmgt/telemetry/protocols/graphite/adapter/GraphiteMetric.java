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
