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
