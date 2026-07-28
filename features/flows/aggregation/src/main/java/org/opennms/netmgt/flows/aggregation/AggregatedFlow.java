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
package org.opennms.netmgt.flows.aggregation;

/**
 * One aggregated row emitted when a window closes: the summed ingress/egress bytes for a single
 * grouping key within a fixed time window. This is what {@link FlowAggregator} hands to its sink (for
 * example {@link FlowAggWriter}) and what a reader sums back up per {@code (window, key)}.
 */
public final class AggregatedFlow {

    /** The grouping dimension. TOS and top-K variants are added in a later phase. */
    public enum Dimension {
        /** Per-exporter-interface total (the uncapped parent used to reconstruct "Other"). */
        INTERFACE,
        APPLICATION,
        CONVERSATION,
        HOST
    }

    public final long windowStartMs;
    public final long windowEndMs;
    public final int exporterNodeId;
    public final int ifIndex;
    /** DSCP for a with-TOS aggregation; {@code null} for the without-TOS rollup (over all DSCP). */
    public final Integer dscp;
    public final Dimension dimension;
    /** The dimension value (application name, conversation key, host address); {@code null} for {@link Dimension#INTERFACE} and for "Other". */
    public final String groupedByKey;
    public final long bytesIn;
    public final long bytesOut;
    public final boolean congestionEncountered;
    public final boolean nonEcnCapableTransport;
    /** Resolved host name; only populated for {@link Dimension#HOST}. */
    public final String hostname;

    public AggregatedFlow(final long windowStartMs, final long windowEndMs, final int exporterNodeId, final int ifIndex,
                   final Integer dscp, final Dimension dimension, final String groupedByKey, final long bytesIn,
                   final long bytesOut, final boolean congestionEncountered, final boolean nonEcnCapableTransport,
                   final String hostname) {
        this.windowStartMs = windowStartMs;
        this.windowEndMs = windowEndMs;
        this.exporterNodeId = exporterNodeId;
        this.ifIndex = ifIndex;
        this.dscp = dscp;
        this.dimension = dimension;
        this.groupedByKey = groupedByKey;
        this.bytesIn = bytesIn;
        this.bytesOut = bytesOut;
        this.congestionEncountered = congestionEncountered;
        this.nonEcnCapableTransport = nonEcnCapableTransport;
        this.hostname = hostname;
    }

    public long bytesTotal() {
        return bytesIn + bytesOut;
    }
}
