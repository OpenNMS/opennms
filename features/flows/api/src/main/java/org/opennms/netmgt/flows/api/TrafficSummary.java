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

import java.util.Objects;

import com.google.common.base.MoreObjects;

import io.searchbox.core.search.aggregation.MaxAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;

/**
 * Total bytes in/out related to some entity.
 */
public class TrafficSummary<T> {

    private final T entity;
    private final long bytesIn;
    private final long bytesOut;
    private final boolean congestionEncountered;
    private final boolean nonEcnCapableTransport;

    public TrafficSummary(final TrafficSummary.Builder<T> builder) {
        this.entity = Objects.requireNonNull(builder.entity);
        this.bytesIn = builder.bytesIn;
        this.bytesOut = builder.bytesOut;
        this.congestionEncountered = builder.congestionEncountered;
        this.nonEcnCapableTransport = builder.nonEcnCapableTransport;
    }

    public T getEntity() {
        return entity;
    }

    public long getBytesIn() {
        return bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    public BytesInOut getBytesInOut() {
        // BytesInOut objects are mutable so we create a new one every get
        return new BytesInOut(bytesIn, bytesOut);
    }

    public boolean isCongestionEncountered() {
        return congestionEncountered;
    }

    public boolean isNonEcnCapableTransport() {
        return nonEcnCapableTransport;
    }

    /**
     * Combines the two booleans {@link #isCongestionEncountered()} and {@link #isNonEcnCapableTransport()} that
     * capture information about encountered ecn values into a single integer.
     *
     * The resulting integers are:
     *
     * <ul>
     *     <li>0: !nonEcnCapableTransport && !congestionEncountered</li>
     *     <li>1:  nonEcnCapableTransport && !congestionEncountered</li>
     *     <li>2: !nonEcnCapableTransport &&  congestionEncountered</li>
     *     <li>3:  nonEcnCapableTransport &&  congestionEncountered</li>
     * </ul>
     */
    public int ecnInfo() {
        return (nonEcnCapableTransport ? 1 : 0) + 2 * (congestionEncountered ? 1 : 0);
    }

    public static class Builder<T> {
        private T entity;
        private long bytesIn;
        private long bytesOut;
        private boolean congestionEncountered;
        private boolean nonEcnCapableTransport;

        private Builder() {
        }

        public Builder<T> withEntity(final T entity) {
            this.entity = Objects.requireNonNull(entity);
            return this;
        }

        public Builder<T> withBytesIn(final long bytesIn) {
            this.bytesIn = bytesIn;
            return this;
        }

        public Builder<T> withBytesOut(final long bytesOut) {
            this.bytesOut = bytesOut;
            return this;
        }

        public Builder<T> withCongestionEncountered(boolean congestionEncountered) {
            this.congestionEncountered = congestionEncountered;
            return this;
        }

        public Builder<T> withNonEcnCapableTransport(boolean nonEcnCapableTransport) {
            this.nonEcnCapableTransport = nonEcnCapableTransport;
            return this;
        }

        public Builder<T> withEcnInfo(TrafficSummary ts) {
            this.congestionEncountered = ts.isCongestionEncountered();
            this.nonEcnCapableTransport = ts.isNonEcnCapableTransport();
            return this;
        }

        public Builder<T> withEcnInfo(MetricAggregation outerAgg) {
            // sets the ecn info from corresponding elastic aggregations results
            MaxAggregation ceAgg = outerAgg.getMaxAggregation("congestion_encountered");
            if (ceAgg != null) {
                this.congestionEncountered = ceAgg.getMax() != null && ceAgg.getMax() > 0;
                MaxAggregation nonEctAgg = outerAgg.getMaxAggregation("non_ect");
                this.nonEcnCapableTransport = nonEctAgg.getMax() != null && nonEctAgg.getMax() > 0;
            }
            return this;
        }

        public Builder<T> withBytes(final long bytesIn, final long bytesOut) {
            return this
                    .withBytesIn(bytesIn)
                    .withBytesOut(bytesOut);
        }

        public Builder<T> withBytesAndEcnInfo(final TrafficSummary<?> source) {
            Objects.requireNonNull(source);
            this.bytesIn = source.getBytesIn();
            this.bytesOut = source.getBytesOut();
            return withEcnInfo(source);
        }

        public TrafficSummary<T> build() {
            return new TrafficSummary<>(this);
        }
    }

    public static <T> TrafficSummary.Builder<T> builder() {
        return new TrafficSummary.Builder<>();
    }

    public static <T> TrafficSummary.Builder<T> from(final T entity) {
        return new TrafficSummary.Builder<T>()
                .withEntity(entity);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrafficSummary)) {
            return false;
        }

        final TrafficSummary<?> that = (TrafficSummary<?>) o;
        return this.bytesIn == that.bytesIn &&
               this.bytesOut == that.bytesOut &&
               congestionEncountered == that.congestionEncountered &&
               nonEcnCapableTransport == that.nonEcnCapableTransport &&
               Objects.equals(this.entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, bytesIn, bytesOut, congestionEncountered, nonEcnCapableTransport);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("entity", this.entity)
                .add("bytesIn", this.bytesIn)
                .add("bytesOut", this.bytesOut)
                .add("nonEcnCapableTransport", this.nonEcnCapableTransport)
                .add("congestionEncountered", this.congestionEncountered)
                .toString();
    }
}
