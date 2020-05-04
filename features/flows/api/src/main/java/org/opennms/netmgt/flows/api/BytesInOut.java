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

package org.opennms.netmgt.flows.api;


import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

/**
 * Helper class for working with bytes in/out.
 */
public class BytesInOut implements Comparable<BytesInOut> {
    private long bytesIn;
    private long bytesOut;

    public BytesInOut() {
        this(0, 0);
    }

    public BytesInOut(long bytesIn, long bytesOut) {
        this.bytesIn = bytesIn;
        this.bytesOut = bytesOut;
    }

    public static BytesInOut sum(BytesInOut a, BytesInOut b) {
        return new BytesInOut(a.bytesIn + b.bytesIn, a.bytesOut + b.bytesOut);
    }

    public static <T> BytesInOut sum(List<TrafficSummary<T>> summaries) {
        BytesInOut bytes = new BytesInOut();
        for (TrafficSummary<?> summary : summaries) {
            bytes.bytesIn += summary.getBytesIn();
            bytes.bytesOut += summary.getBytesOut();
        }
        return bytes;
    }

    public BytesInOut minus(BytesInOut other) {
        return new BytesInOut(Math.max(bytesIn - other.bytesIn, 0), Math.max(bytesOut - other.bytesOut, 0));
    }

    public static <T> BytesInOut sum(ImmutableSet<Map.Entry<Directional<T>, Double>> entrySet) {
        BytesInOut bytes = new BytesInOut();
        for (Map.Entry<Directional<T>, Double> entry : entrySet) {
            if (entry.getKey().isIngress()) {
                bytes.bytesIn += entry.getValue();
            } else {
                bytes.bytesOut += entry.getValue();
            }
        }
        return bytes;
    }

    public long getBytesIn() {
        return bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    @Override
    public int compareTo(BytesInOut other) {
        return Long.compare(bytesIn + bytesOut, other.bytesIn + other.bytesOut);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BytesInOut)) return false;
        BytesInOut flowBytes = (BytesInOut) o;
        return bytesIn == flowBytes.bytesIn &&
                bytesOut == flowBytes.bytesOut;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bytesIn", this.bytesIn)
                .add("bytesOut", this.bytesOut)
                .toString();
    }

}
